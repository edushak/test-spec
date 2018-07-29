import org.edushak.testspec.util.Helper
import org.edushak.testspec.util.RestHelper

import static cucumber.api.groovy.EN.*

final List builtInResponceAttributes = ['status','statusLine','contentType','reasonPhrase']

When(~/^I send (GET|POST|PUT|DELETE|HEAD) request with parameters to (.*)$/) { String method, String endpoint, String tokens ->
    // endpoint = Common.detokenize(endpoint, binding.variables)

    ConfigObject params = new ConfigSlurper().parse(tokens)
    Map headersMap = null
    if (params.headers) {
        headersMap = params.headers
    }

    Map<String, Object> parameters = [:]
    if (params.requestContentType) {
        parameters << [requestContentType: params.requestContentType]
    }

    _restResponse = invokeAndSaveResponse(endpoint, method, parameters, headersMap)
}

Then(~/^rest response (status|statusLine|contentType|reasonPhrase|.*) (==|contains) (.*)$/) { String what, String operator, String expectedValue ->
    expectedValue = normalizeValue(expectedValue)
    if (expectedValue.startsWith('$') || Helper.isClosure(expectedValue)) {
        // expectedValue = normalizeParameter(expectedValue) // TODO
        expectedValue = Helper.evaluate(expectedValue, binding)
    }
    if (operator == '==') {
        operator = 'equals'
    }

    if (what in builtInResponceAttributes) {
        if (what == 'reasonPhrase') {
            assert _restResponse.data.error.reason."$operator"(expectedValue)
        } else {
            assert (_restResponse."$what")?.toString()."$operator"(expectedValue)
        }
    } else if(what.contains('data.')) { // TODO: what if it's bla.mydata.... ?
        String expression = what.replaceFirst('data.', 'restResponse.data.')
        assert Helper.evaluate(expression, binding)?.toString()."$operator"(expectedValue)
    }
}

Then(~/^response text should (be|contain|match) (.*)$/) { String operator, String expectedValue ->
    expectedValue = normalizeValue(expectedValue)
    if (expectedValue.startsWith('$') || Helper.isClosure(expectedValue)) {
        // expectedValue = normalizeParameter(expectedValue) // TODO
        expectedValue = Helper.evaluate(expectedValue, binding)
    }
    String plainText = _restResponse.plainText
    if (operator == 'be') {
        assert plainText == expectedValue
    } else if(operator == 'contain'){
        assert plainText?.contains(expectedValue)
    } else {
        assert plainText ==~ expectedValue
    }
}

def invokeAndSaveResponse(String endpoint, String method, Map parameters, Map headersMap) {
    if (endpoint.startsWith('/')) {
        endpoint = binding['restBaseUrl'] + endpoint
    } else {
        // must be absolute
    }
    def restResponse = RestHelper.call(endpoint, method, parameters, headersMap)
    binding.setVariable('_restResponse', restResponse)
    restResponse
}