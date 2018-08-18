package org.edushak.testspec

class Scenario {
    String featureFile, featureName, scenarioSource, scenarioName, status, errorMessage = ''
    int passed, failed // skipped?
    List tags
    long execTimeMs
}
