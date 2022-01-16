package org.edushak.testspec

//import cucumber.runtime.ClassFinder
//import cucumber.runtime.Runtime
//import cucumber.runtime.RuntimeOptions
//import cucumber.runtime.io.MultiLoader
//import cucumber.runtime.io.ResourceLoader
//import cucumber.runtime.io.ResourceLoaderClassFinder
import groovy.util.logging.Slf4j
import io.cucumber.core.options.CommandlineOptionsParser
import io.cucumber.core.options.CucumberProperties
import io.cucumber.core.options.CucumberPropertiesParser
import io.cucumber.core.options.RuntimeOptions
import io.cucumber.core.runtime.Runtime

@Slf4j
class Main {
    static final List SUPPORTED_DATA_PROVIDERS = ['csv', 'excel', 'delimited'].asImmutable()

    Main() {
        log.info("inside Main constructor")
    }

    static void main(String[] argv) {
        log.info('inside Main.main({})', argv.inspect())
        byte exitStatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitStatus);
    }

    /**
     * Launches the Cucumber-JVM command line.
     *
     * @param  argv runtime options. See details in the
     *              {@code cucumber.api.cli.Usage.txt} resource.
     * @return      0 if execution was successful, 1 if it was not (test
     *              failures)
     */
    public static byte run(String... argv) {
        return run(argv, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Launches the Cucumber-JVM command line.
     *
     * @param  argv        runtime options. See details in the
     *                     {@code cucumber.api.cli.Usage.txt} resource.
     * @param  classLoader classloader used to load the runtime
     * @return             0 if execution was successful, 1 if it was not (test
     *                     failures)
     */
    public static byte run(String[] argv, ClassLoader classLoader) {
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromPropertiesFile())
                .build();

        RuntimeOptions environmentOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromEnvironment())
                .build(propertiesFileOptions);

        RuntimeOptions systemOptions = new CucumberPropertiesParser()
                .parse(CucumberProperties.fromSystemProperties())
                .build(environmentOptions);

        CommandlineOptionsParser commandlineOptionsParser = new CommandlineOptionsParser(System.out);
        RuntimeOptions runtimeOptions = commandlineOptionsParser
                .parse(argv)
                .addDefaultGlueIfAbsent()
                .addDefaultFeaturePathIfAbsent()
                .addDefaultFormatterIfAbsent()
                .addDefaultSummaryPrinterIfAbsent()
                .enablePublishPlugin()
                .build(systemOptions);

        Optional<Byte> exitStatus = commandlineOptionsParser.exitStatus();
        if (exitStatus.isPresent()) {
            return exitStatus.get();
        }

        final Runtime runtime = Runtime.builder()
                .withRuntimeOptions(runtimeOptions)
                .withClassLoader(() -> classLoader)
                .build();

        runtime.run();
        return runtime.exitStatus();
    }

/*
    static void main(String[] argv) throws Throwable {
        // new io.cucumber.core.cli.Main()
        byte exitStatus = run(argv, Thread.currentThread().getContextClassLoader());
        System.exit(exitStatus);
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

    static List<List> loadTestCases(examplesDescJson) {
        String dataprovider = examplesDescJson.dataprovider.toLowerCase()

        if (!(dataprovider in SUPPORTED_DATA_PROVIDERS)) {
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

    static List<ExamplesTableRow> buildCucumberRows(List<List> dataRows, Examples examples) {
        List<ExamplesTableRow> cucumberRows = []
        dataRows.eachWithIndex { List<String> row, int index ->
            if (!row.findAll { !it.trim().empty }.empty) {
                cucumberRows.add(new ExamplesTableRow(examples.comments, row, examples.line, examples.id))
            }
        }
        cucumberRows
    }
*/

}
