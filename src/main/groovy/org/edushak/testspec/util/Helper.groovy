package org.edushak.testspec.util

import groovy.text.GStringTemplateEngine
import groovy.text.StreamingTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.Memoized
import org.codehaus.groovy.control.CompilationFailedException

class Helper {

    static class Engines {
        public static final TemplateEngine GString = new GStringTemplateEngine()
        public static final TemplateEngine Streaming = new StreamingTemplateEngine()
    }

    static closureMatcher = /^(?s)\{.*\}$/,
            closureCallMatcher = /^(?s)\{.*\}\s*.\s*call\s\(.*\).*$/

    static boolean isClosure(String expression) {
        if (expression instanceof CharSequence) {
            String str = expression.toString().trim()
            str && str.contains('->') && (str ==~ closureMatcher || str ==~ closureCallMatcher)
        } else {
            false
        }
    }

    static boolean isParametrizedClosure(String expression) {
        if (isClosure(expression)) {
            String str = expression?.toString()?.trim()
            int arrowIndex = str.indexOf('->')
            arrowIndex > -1 && !str.substring(1, arrowIndex).trim().isEmpty()
        } else {
            false
        }
    }

    static boolean isSelector(String input) {
        isCssSelector(input) || isXPathSelector(input)
    }
    static boolean isCssSelector(String input) {
        String str = input?.toString()?.trim()
        str && str.startsWith('$(') && !str.startsWith('$(By.xpath') && str.endsWith(')') || (str.endsWith(']') && str.contains('['))
    }
    static boolean isXPathSelector(String input) {
        String str = input?.toString()?.trim()
        str && (str.startsWith('$x(') || str.startsWith('$(By.xpath')) && str.endsWith(')') || (str.endsWith(']') && str.contains('['))
    }

    static Object evaluate(String expression, Binding binding, boolean wrapIntoClosure = false) {
        if (isClosure(expression) || wrapIntoClosure) {
            String expressionToEval
            if (isClosure(expression)) {
                expressionToEval = expression
            } else {
                expressionToEval = '{->' + expression + '}'
            }

            if (!expressionToEval.contains('.call(') && !expressionToEval.contains('}()')) {
                expressionToEval = expressionToEval.trim() + '.call();'
            }

            if (binding == null) {
                binding = new Binding()
            }

            try {
                new GroovyShell(getClass().getClassLoader(), binding).evaluate(expressionToEval)
            } catch(CompilationFailedException cfe) {
                throw new Exception("Failed to compile expression: $expression", cfe)
            }
        } else {
            return expression
        }
    }

    static ConfigObject loadConfig(String filePath, boolean throwIfNotFound = true) {
        File file = new File(filePath)
        URL resource
        if(file.exists()) {
            resource = file.toURI().toURL()
        } else {
            resource = fromClassPath(filePath)
        }
        if (resource == null) {
            if (throwIfNotFound) {
                throw new Exception("config file cannot be found neither by absolute path nor on classpath")
            } else {
                return null
            }
        } else {
            new ConfigSlurper().parse(resource)
        }
    }

    static URL fromClassPath(String filePath, boolean throwIfNotFound = false) {
        URL resource = Thread.currentThread().contextClassLoader.getResource(filePath)
        if (throwIfNotFound && resource == null) {
            throw new FileNotFoundException("Resource cannot be loaded from classpath: $filePath")
        }
        return resource
    }

    static def noQuotes(String str) {
        if (str == null) { return  null }
        if (str == '""' || str == "''") {return ''}
        if (str.startsWith("'''") && str.endsWith("'''") ||
            str.startsWith('"""') && str.endsWith('"""')) {
            return str[3..-4]
        }
        if (str.startsWith('\'') && str.endsWith('\'') ||
            str.startsWith('\"') && str.endsWith('\"')) {
            return str[1..-2]
        }
        return str
    }

    static String detokenize(String templateSource, Map tokens, TemplateEngine engine = Engines.Streaming) {
        getTemplate(templateSource as String, engine)?.make(tokens)
    }

    @Memoized
    static Template getTemplate(String templateStr, TemplateEngine engine) {
        if (templateStr == null) {
            return null
        }
        engine.createTemplate(templateStr)
    }

    @Memoized
    static Template getTemplate(File templateFile, TemplateEngine engine) {
        if (templateFile == null) {
            return null
        }
        engine.createTemplate(templateFile.text)
    }
}
