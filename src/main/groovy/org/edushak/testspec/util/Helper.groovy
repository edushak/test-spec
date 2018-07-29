package org.edushak.testspec.util

import org.codehaus.groovy.control.CompilationFailedException

class Helper {

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
}
