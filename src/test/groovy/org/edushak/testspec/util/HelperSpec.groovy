package org.edushak.testspec.util

import org.edushak.testspec.BaseSpec
import spock.lang.Shared
import spock.lang.Unroll

class HelperSpec extends BaseSpec {

    //@Shared File excelFile = new File(Helper.TEST_RESOURCES_DIR + "/unit/Book1.xlsx")
    @Shared File csvFile = new File(Helper.TEST_RESOURCES_DIR + "/unit/test.csv")

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


//    @Unroll
//    def "readExcel(#file, #sheetName)"(File file, String sheetName, List<List> expectedContent) {
//        when:
//        List<List> content = Helper.readExcel(file, sheetName)
//
//        then:
//        content == expectedContent
//
//        where:
//        file                      | sheetName  | expectedContent
//        excelFile                 | "Sheet2"   | [['a1', 'b1'],
//                                                  ['a2', 'b2']]
//    }

//    @Unroll
//    def "readExcel negative cases(#file, #sheetName)"(File file, String sheetName, exType, exMessage) {
//        when:
//        Helper.readExcel(file, sheetName)
//
//        then:
//        Throwable th = thrown()
//        th.message.startsWith(exMessage)
//        th.getClass() == exType
//
//        where:
//        file                      | sheetName  | exType                   | exMessage
//        excelFile                 | null       | IllegalArgumentException | "Excel sheetName parameter may not be null"
//        excelFile                 | "non-exist"| IllegalArgumentException | "Non-existent Excel sheetName name: non-exist"
//        null                      | "Sheet2"   | IllegalArgumentException | "Excel file parameter may not be null"
//        csvFile                   | "Sheet2"   | IllegalArgumentException | "It looks like you are attempting to read a non-Excel file"
//        new File("bla")  | "Sh1"      | FileNotFoundException    | "Must be valid file"
//    }
}
