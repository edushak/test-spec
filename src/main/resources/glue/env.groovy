package glue

import geb.Browser
import geb.ConfigurationLoader
import geb.binding.BindingUpdater
import geb.driver.DriverCreationException
import io.cucumber.groovy.Scenario
import org.edushak.testspec.TestSpecWorld
import org.edushak.testspec.util.Helper
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot

import static io.cucumber.groovy.Hooks.Before
import static io.cucumber.groovy.Hooks.After

//import org.edushak.testspec.Scenario
//import org.edushak.testspec.util.ElasticClient

long scenarioEndTime, scenarioStartTime

Before() { Scenario scenario ->
    TestSpecWorld.log.debug("Inside Before() hook")
    scenarioStartTime = System.currentTimeMillis()

    String environment = Helper.SYSTEM_PROPERTIES['browser']
    TestSpecWorld.log.debug("environment = '{}'", environment)

    // boolean isWebEnabled = (environment != null)
    if (environment != null) {
        if (theBrowser == null) {
            // org.edushak.testspec.TestSpecWorld.currentWorld.binding
            // if (user asks for it, load browser)

            // !!! v6 scans everything under src/test/resources, including 'BrowserConfig.conf' and fails
            // Because it's not a normal groovy script, but rather a Config script!
            String configFilePath = 'BrowserConfig.conf'
            configuration = new ConfigurationLoader(environment).getConf(configFilePath) // frameworkConfigFile.toURI().toURL(), Thread.currentThread().getContextClassLoader()
            theBrowser = new Browser(configuration)
            try {
                binding.variables.putAll(configuration.rawConfig)
                bindingUpdater = new BindingUpdater(binding, theBrowser)
                bindingUpdater.initialize()
            } catch (DriverCreationException dce) {
                dce.printStackTrace()
                // incompatibility between driver and browser
            } catch (IllegalStateException ise) {
                ise.printStackTrace()
                // driver cannot be found ?
            }
            // TODO: grab browser name & version and send to ElasticClient
        }
        if (!binding.hasVariable('browser')) {
            binding.variables.putAll(theBrowser.getOwner().properties)
        }
    }
}

After() { Scenario scenario ->
    scenarioEndTime = System.currentTimeMillis()
    long scenarioDuration = scenarioEndTime - scenarioStartTime
    bindingUpdater?.remove()

    // List tags = scenario.sourceTagNames.collect { it.toLowerCase() }
    // Result failedResult = scenario.stepResults.find { it.status == 'failed' }

    if (scenario.failed) {
        if (theBrowser != null) {
            // todo: capture screenshot and attach
            if (theBrowser.getDriver() instanceof TakesScreenshot) {
                def screenShot = ((TakesScreenshot) theBrowser.driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenShot, "image/png", "screenshot")
                // String featureLine = failedResult.error.stackTrace.last()
            }
        }
    }

    // TODO: turn it into a plugin
    // publishToElastic(scenario, scenarioDuration)
}

/*
def publishToElastic(ScenarioImpl scenario, long scenarioDuration) {
    if (ElasticClient.instance.isActive()) {
        // save currently executed feature in Main
        CucumberFeature currentFeature = null
        CucumberTagStatement currentScenario = null

        String scenarioSource = getScenarioSource(currentScenario)
        String errorMessage = getErrorMessage(scenario)

        List tags = fetchTags(scenario)

        ElasticClient.instance << new Scenario(
            featureFile: (currentFeature.path as File)?.name ?: '',
            featureName: currentFeature.gherkinFeature.name ?: '',
            scenarioSource: scenarioSource,
            scenarioName: scenario.name,
            status: scenario.status,
            errorMessage: errorMessage,
            passed: scenario.status == 'passed' ? 1 : 0,
            failed: scenario.status == 'failed' ? 1 : 0,
            execTimeMs: scenarioDuration,
            tags: tags
        )
    }
}

String getErrorMessage(ScenarioImpl scenario) {
    String errorMessage = ''
    if (scenario.failed) {
        for (Result stepResult : scenario.@stepResults) {
            if (stepResult.status == 'failed') {
                errorMessage = stepResult.errorMessage
                break
            }
        }
    }
    return errorMessage
}

String getScenarioSource(CucumberTagStatement scenario) {
    def result = new StringBuilder()
    def formatter = new PrettyFormatter(result, true, false)
    TagStatement model = scenario?.gherkinModel
    if (model instanceof Messages.GherkinDocument.Feature.Background) {
        formatter.background(model)
    }
    // ...
    scenario.steps.each {
        formatter.step(it)
    }
    formatter.eof()
    return result.toString()
}

List fetchTags(ScenarioImpl scenario) {
    scenario.sourceTagNames
}
*/
