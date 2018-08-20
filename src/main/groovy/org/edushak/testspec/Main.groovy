package org.edushak.testspec

import cucumber.runtime.ClassFinder
import cucumber.runtime.Runtime
import cucumber.runtime.RuntimeOptions
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.io.ResourceLoader
import cucumber.runtime.io.ResourceLoaderClassFinder
import cucumber.runtime.model.CucumberExamples
import cucumber.runtime.model.CucumberScenarioOutline
import cucumber.runtime.model.CucumberTagStatement
import gherkin.formatter.model.Examples
import gherkin.formatter.model.ExamplesTableRow
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.edushak.testspec.util.Helper

@Slf4j
class Main {
    static final List SUPPORTER_DATA_PROVIDERS = ['csv', 'excel', 'delimited'].asImmutable()

    Main() {
    }

    static void main(String[] argv) throws Throwable {
        byte exitstatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitstatus);
    }

    static byte run(String[] argv, ClassLoader classLoader) throws IOException {
        RuntimeOptions runtimeOptions = new RuntimeOptions(new ArrayList(Arrays.asList(argv)));
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
        runtime.run();
        return runtime.exitStatus();
    }

    static loadExamplesFromExternalSource(CucumberTagStatement tagStatement) {
        if (tagStatement instanceof CucumberScenarioOutline) {
            for (CucumberExamples cucumberExamples : tagStatement.getCucumberExamplesList()) {
                try {
                    loadExamplesFromExternalSource(cucumberExamples.examples)
                } catch (Throwable th) {
                    throw new Exception("Failed to load test cases (Examples:) from external source", th)
                }
            }
        }
    }

    static loadExamplesFromExternalSource(Examples examples) {
        String examplesDescription = examples.description
        if (examplesDescription && examplesDescription.contains('"dataprovider"')) {
            def examplesDescriptionJson = new JsonSlurper().parseText(examplesDescription)
            List<List> dataRows = loadTestCases(examplesDescriptionJson)
            if (dataRows) {
                List<ExamplesTableRow> cucumberRows = buildCucumberRows(dataRows, examples)
                // TODO:
                // cucumberRows = Helper.applyRange(cucumberRows, examplesDescriptionJson.range)
                examples.setRows cucumberRows
            } else {
                throw new Exception("No test cases found in external data provider: ${examplesDescriptionJson.dataprovider}")
            }
        }
    }

    static List<ExamplesTableRow> buildCucumberRows(List<List> dataRows, Examples examples) {
        List<ExamplesTableRow> cucumberRows = []
        dataRows.eachWithIndex { List<String> row, int index ->
            if (!row.findAll { !it.trim().empty }.empty) {
                cucumberRows.add(new ExamplesTableRow(examples.comments, row, examples.line, examples.id))
            }
        }
        cucumberRows
    }

    static List<List> loadTestCases(examplesDescJson) {
        String dataprovider = examplesDescJson.dataprovider.toLowerCase()

        if (!(dataprovider in SUPPORTER_DATA_PROVIDERS)) {
            throw new Exception("Unsupported dataprovider: $dataprovider")
        }
        if (!examplesDescJson.filepath) {
            throw new Exception("Parameter filepath is mandatory for external dataprovider")
        }
        File resolvedFile = Helper.resolveFile(examplesDescJson.filepath, false)
        if (resolvedFile == null) {
            throw new Exception("Parameter filepath is not a valid file path; passed: ${examplesDescJson.filepath}, resolved path: $resolvedFile")
        }

        List<List> dataRows = null
        switch (dataprovider) {
            case 'excel':
                dataRows = Helper.readExcel(resolvedFile, examplesDescJson.sheet)
                break

            case 'csv':
                dataRows = Helper.readCsvAsList(resolvedFile)
                break

            case 'delimited':
                char delimiter = (char) examplesDescJson.delimiter ?: ','
                dataRows = Helper.readDelimited(resolvedFile, delimiter, false)
                break
        }
        Helper.replaceAll(dataRows, null, '')
        dataRows
    }
}
