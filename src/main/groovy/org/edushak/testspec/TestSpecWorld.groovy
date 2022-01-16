package org.edushak.testspec

import geb.Browser
import geb.Configuration
import geb.binding.BindingUpdater
import geb.navigator.DefaultNavigator // replaced in v3 import geb.navigator.NonEmptyNavigator
import geb.navigator.Navigator
import groovy.util.logging.Slf4j
import groovyx.gpars.GParsPool
import groovyx.gpars.dataflow.Promise
import jsr166y.ForkJoinPool
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory
import org.apache.groovy.groovysh.util.NoExitSecurityManager
import org.edushak.testspec.util.Helper
import org.edushak.testspec.util.RestHelper

import javax.script.ScriptEngine
import java.util.concurrent.ConcurrentLinkedDeque

@Slf4j
class TestSpecWorld {
    static TestSpecWorld currentWorld
    static Queue<Promise> _promises = new ConcurrentLinkedDeque<Promise>()

    // for web
    static Browser theBrowser
    static Configuration configuration

    Binding binding
    BindingUpdater bindingUpdater

    SecurityManager defaultManager = System.securityManager,
                    noExitManager = new NoExitSecurityManager()
    Closure executeCodeAsync
    ForkJoinPool pool = new ForkJoinPool()

    TestSpecWorld() {
        GParsPool.withExistingPool(pool) {
            executeCodeAsync = { code, binding ->
                try {
                    executeCode(code, binding)
                } catch(Throwable th) {
                    log.error("Failed to run executeCode() asynchronously", th)
                }
            }.asyncFun()
        }
    }

    void setBinding(Binding binding) {
        this.binding = binding
        if (!binding.hasVariable('_restCall')) {
            binding.setVariable('_restCall', RestHelper.&call)
        }
    }

    /**
     * With waiting
     * @param selector
     * @return Navigator if found
     */
    Navigator findElement(String selector) {
        Navigator element
        theBrowser.waitFor {
            (element = Helper.evaluate("browser.${selector}", binding, true)).with {
                it != null && (it instanceof DefaultNavigator ) // DefaultNavigator replaced NonEmptyNavigator
            }
        }
        return element
    }

    boolean isVariable(String str) {
        isVariable(str, binding)
    }
    static boolean isVariable(String str, Binding binding) {
        str && str != '$' && binding.hasVariable(str)
    }

    static noQuotes(Object str) {
        if (str instanceof CharSequence) {
            Helper.noQuotes(str)
        } else {
            str
        }
    }
    String normalizeValue(String value) {
        normalizeValue(value,binding)
    }

    def normalizeParameter(String input, boolean preserveQuotes = false) {
        normalizeParameter(input, preserveQuotes, binding)
    }
    static normalizeParameter(String input, boolean preserveQuotes, Binding binding) {
        if (!preserveQuotes) {
            input = noQuotes(input)
        }
        def inputValue
        String actualParameters = ''
        boolean isParametrizedIdentifier = isParametrizedIdentifier(input)
        if (isParametrizedIdentifier) {
            List tokens = input.tokenize('(')
            if (isVariable(tokens.first(), binding)) {
                inputValue = variable(tokens.first(), binding)
            } else {
                inputValue = input
                isParametrizedIdentifier = false
            }
            if (tokens.size() == 2) {
                actualParameters = tokens.last()[0..-2]
            } else {
                throw new Exception("Bad formatted identifier call: $input;" +
                        "It should look like: identifier('parameter')")
            }
        } else {
            if (isVariable(input, binding)) {
                inputValue = variable(input, binding)
            } else {
                inputValue = input
            }
        }

        inputValue = resolveSelector(inputValue, isParametrizedIdentifier)

        if (isParametrizedIdentifier || Helper.isClosure(inputValue)) {
            inputValue = inputValue.trim()
            inputValue += ".call($actualParameters);"
        }

        return inputValue
    }

    private static Object resolveSelector(inputValue, boolean isParametrizedIdentifier) {
        if (inputValue instanceof CharSequence) {
            boolean isParametrizedValue = inputValue instanceof CharSequence ? isParametrizedValue(inputValue) : false
            if (isParametrizedIdentifier && !isParametrizedValue) {
                throw new Exception("identifier IS parametrized, while value IS NOT")
            }
            if (!isParametrizedIdentifier && isParametrizedValue) {
                throw new Exception("identifier is NOT parametrized, while value IS")
            }

            if (inputValue.contains('$(By.')) {
                inputValue = inputValue.replace('$(By.', '$(org.openqa.selenium.By.')
            } else if (inputValue.contains('$x(')) {
                inputValue = StringUtils.replace(inputValue.toString(), '$x(', '$(org.openqa.selenium.By.xpath(')
                int lastClosingParentesis = inputValue.lastIndexOf(')')
                inputValue = inputValue.substring(0, lastClosingParentesis) + ')' + inputValue.substring(lastClosingParentesis)
            }
        }
        inputValue
    }

    static boolean isParametrizedIdentifier(String input) {
        if (!input) {return null}
        !Helper.isSelector(input) && input.indexOf(')') > input.indexOf('(') &&
                input.substring(input.length()-1) == ')'

    }
    static boolean isParametrizedValue(String input) {
        Helper.isParametrizedClosure(input)
    }

    static String normalizeValue(String value, Binding binding) {
        variable(Helper.noQuotes(value) as String, binding)
    }

    Object variable(String varName) {
        variable(varName, binding)
    }
    static Object variable(String str, Binding binding) {
        binding.hasVariable(str) ? binding.getVariable(str) : str
    }


    Closure getEngine = { String lang, Binding ->
        if (!binding.hasVariable('__engines')) {
            binding.setVariable('__engines', [:])
        }
        if (binding.__engines[lang] == null) {
            if (lang == 'groovy') {
                binding.__engines[lang] = new GroovyScriptEngineFactory().getScriptEngine()
            }
            // TODO: support more languages ?
        }
        binding.__engines[lang]
    }

    Closure executeCodeFacade = { def async, String code ->
        Binding _binding = new Binding(binding.variables)
        if (async) {
            GParsPool.withExistingPool(pool) {
                Promise p = executeCodeAsync(code, _binding)
                _promises.offer(p)
            }
        } else {
            executeCode(code, _binding)
        }
    }

    Closure executeCode = { String code, Binding binding ->
        if (code.contains('exit')) {
            System.setSecurityManager(noExitManager)
        }
        ScriptEngine engine = getEngine('groovy', binding)
        if (!engine) {
            throw new Exception("No support for chosen language")
        }
        Object retValue = null
        try {
            code = Helper.noQuotes(code)
            if (Helper.isClosure(code) && !code.contains('.call()') && !code.trim().endsWith('()')){
                code = code + '.call()'
            }
            retValue = engine.eval(code, binding)
        } catch (javax.script.ScriptException se) {
            if (se.cause?.cause instanceof MissingPropertyException) {
                throw new Exception("You might be accessing a variable that has not been defined yet", se.cause.cause)
            } else {
                throw se
            }
        } finally {
            if (code.contains('exit')) {
                System.setSecurityManager(defaultManager)
            }
        }
        retValue
    }

    /**
     * Remove elements that begin with "cucumber.runtime.groovy"
     * @param ste
     * @return
     */
    /*
    StackTraceElement sanitize(StackTraceElement ste) {

    }
    */
}
