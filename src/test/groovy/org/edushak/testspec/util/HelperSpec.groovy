package org.edushak.testspec.util

import org.edushak.testspec.BaseSpec

class HelperSpec extends BaseSpec {

    def "loadProperties"() {
        given:
        File file = new File(TEST_RESOURCES_DIR + "/unit/test.properties")

        when:
        Map actual = Helper.loadProperties(file)

        then:
        actual == ['a':'1']

        when:
        actual = Helper.loadProperties(null)

        then:
        actual == null
    }

    def "readDelimited"() {
        given:
        File file = new File("$TEST_RESOURCES_DIR/unit/test.pipe")

        when:
        List<List> dataRows = Helper.readDelimited(file, (int)'|')

        then:
        dataRows == [
                ['1', 'Ed', 'New York'],
                ['2', 'Leo', 'New Jersey']
        ]
    }

    def "readCsv"() {
        given:
        File csvFile = new File("$TEST_RESOURCES_DIR/unit/test.csv")

        when:
        List<List> dataRows = Helper.readCsvAsList(csvFile)

        then:
        dataRows == [
            ['1', 'Ed', 'New York'],
            ['2', 'Leo', 'New Jersey']
        ]
    }

    def "resolveFile"() {
        when:
        File resolvedFile = Helper.resolveFile('unit/test.csv', false)

        then:
        resolvedFile != null && resolvedFile.exists()
    }

    def "replaceAll"() {
        when:
        List<List> dataRows = [ [] ]
        int replacements = Helper.replaceAll(dataRows, "one", "two")
        then:
        replacements == 0

        when:
        dataRows = [ ["he","is","one"] ]
        replacements = Helper.replaceAll(dataRows, "one", "two")
        then:
        replacements == 1
        dataRows == [ ["he","is","two"] ]
    }
}
