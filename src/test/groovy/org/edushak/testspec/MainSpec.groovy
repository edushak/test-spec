package org.edushak.testspec

import spock.lang.Ignore

class MainSpec extends BaseSpec {

/*
    def "loadTestCases"() {
        given:
        String examplesDescription = '''
        { "dataprovider" : "csv", 
          "filepath" : "unit/test.csv" }
        '''
        def examplesDescriptionJson = new JsonSlurper().parseText(examplesDescription)

        when:
        List<List> dataRows = Main.loadTestCases(examplesDescriptionJson)

        then:
        dataRows == [
            ['1', 'Ed', 'New York'],
            ['2', 'Leo', 'New Jersey']
        ]
    }
*/

    @Ignore
    def "buildCucumberRows"() {
        given:
        List dataRows = [
            ['1', 'Ed', 'New York'],
            ['2', 'Leo', 'New Jersey']
        ]
        Main.buildCucumberRows(dataRows, null)
    }
}
