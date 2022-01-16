package org.edushak.testspec.util

import groovy.text.GStringTemplateEngine
import groovy.text.StreamingTemplateEngine
import groovy.text.Template
import groovy.text.TemplateEngine
import groovy.transform.Memoized
import groovy.util.logging.Slf4j
import org.apache.poi.poifs.filesystem.NotOLE2FileException
import org.apache.poi.ss.usermodel.*
import org.codehaus.groovy.control.CompilationFailedException
import org.supercsv.io.CsvListReader
import org.supercsv.io.ICsvListReader
import org.supercsv.prefs.CsvPreference

@Slf4j
class  Helper {
    static final String PROJECT_ROOT_DIR, MAIN_RESOURCES_DIR, TEST_RESOURCES_DIR
    static final Properties SYSTEM_PROPERTIES
    static {
        SYSTEM_PROPERTIES = System.getProperties()
        PROJECT_ROOT_DIR = SYSTEM_PROPERTIES['projectDir'] ?: SYSTEM_PROPERTIES['user.dir']
        MAIN_RESOURCES_DIR = PROJECT_ROOT_DIR + '/src/main/resources'
        TEST_RESOURCES_DIR = PROJECT_ROOT_DIR + '/src/test/resources'
    }

    static class Engines {
        public static final TemplateEngine GString = new GStringTemplateEngine()
        public static final TemplateEngine Streaming = new StreamingTemplateEngine()
    }

    static closureMatcher = /^(?s)\{.*\}$/,
           closureCallMatcher = /^(?s)\{.*\}\s*.\s*call\s\(.*\).*$/

    static Properties loadProperties(File file) {
        if (file == null) {
            return null
        }
        Properties props = new Properties()
        props.load(file.newDataInputStream())
        props
    }

    static List<List> readCsvAsList(File file) { // , boolean skipFirstRow
        CsvPreference pref = CsvPreference.STANDARD_PREFERENCE
        return readDelimited(file, pref)
    }

    static List<List> readDelimited(File file, int delimiter) { // , boolean skipFirstRow
        CsvPreference pref = new CsvPreference.Builder((char)'"', delimiter, System.lineSeparator()).build()
        return readDelimited(file, pref)
    }

    static ArrayList<List> readDelimited(File file, CsvPreference pref) {
        List<List> dataRows = []
        ICsvListReader listReader = null;
        try {
            listReader = new CsvListReader(new FileReader(file), pref)
            listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
            List<Object> oneRow;
            while ((oneRow = listReader.read()) != null) {
                log.debug("lineNo={}, rowNo={}, oneRow={}", listReader.getLineNumber(), listReader.getRowNumber(), oneRow)
                dataRows << oneRow
            }
        } finally {
            listReader?.close()
        }
        return dataRows
    }

    static List<List> readExcel(File file, String sheetName) {
        if (file == null) {
            throw new IllegalArgumentException("Excel file parameter may not be null")
        }
        if (sheetName == null) {
            throw new IllegalArgumentException("Excel sheetName parameter may not be null")
        }
        List<List> dataSheet = []
        Workbook workbook = null
        try {
            workbook = WorkbookFactory.create(file)
            int index = workbook.getSheetIndex(sheetName)
            if (index < 0) {
                throw new IllegalArgumentException("Non-existent Excel sheetName name: $sheetName")
            }
            Sheet sheet2 = workbook.getSheet(sheetName)
            println "sheet2: $sheet2"

            for (Row row : sheet2) {
                List<String> rowVal = []
                for (Cell cell : row) {
                    String cellValue = cell.getStringCellValue()
                    rowVal << cellValue
                }
                dataSheet << rowVal
            }

        } catch (NotOLE2FileException e) {
            throw new IllegalArgumentException("It looks like you are attempting to read a non-Excel file; cause: " + e.message)
        } catch (FileNotFoundException e2) {
            throw new FileNotFoundException("Must be valid file: " + e2.message)
        } finally {
            workbook?.close()
        }
        return dataSheet
    }

    static int replaceAll(List<List> dataRows, String searchFor, String replaceWith) {
        int replacedValues = 0
        dataRows.each { List cellsInRow ->
            cellsInRow.eachWithIndex { Object value, int i ->
                if (value == searchFor) {
                    cellsInRow[i] = replaceWith
                    replacedValues++
                }
            }
        }
        return replacedValues
    }

    static File resolveFile(String filePath, boolean throwWhenNotFound = true) {
        File result = new File(filePath)
        if (result.exists()) {
            return result
        }

        result = new File("$PROJECT_ROOT_DIR/$filePath")
        if (result.exists()) {
            return result
        }

        result = new File("$TEST_RESOURCES_DIR/$filePath")
        if (result.exists()) {
            return result
        }
        if (throwWhenNotFound) {
            throw new FileNotFoundException("Cannot resolve file path: $filePath")
        } else {
            return null
        }
    }

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

    static boolean isWindows() {
        SYSTEM_PROPERTIES.getProperty("os.name")?.toLowerCase()?.contains('win')
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
