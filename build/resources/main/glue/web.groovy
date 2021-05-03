
import geb.navigator.Navigator
import geb.waiting.WaitTimeoutException
import static cucumber.api.groovy.EN.*

When(~/^I navigate to (.*)$/) { String url ->
    url = normalizeParameter(url)
    // Page page = new WebPage()
    // page.init(theBrowser)
    // page.setUrl(url)
    theBrowser.go(url)
}

Then(~/^page title should be (.*)$/) { String pageTitle ->
    pageTitle = normalizeParameter(pageTitle)
    try {
        theBrowser.waitFor {
            theBrowser.driver.title == pageTitle
        }
    } catch (WaitTimeoutException wte) {
        System.err.print("WaitTimeoutException: ")
        System.err.print(wte.message)
        assert theBrowser.driver.title == pageTitle
    }
}

When(~/^I enter (.*) into (.*)$/) { String value, String selector ->
    selector = normalizeParameter(selector)
    value = normalizeParameter(value)
    Navigator element = findElement(selector)
    element.value(value)
}

When(~/^I click on (.*)$/) { String selector ->
    selector = normalizeParameter(selector)
    Navigator element = findElement(selector)
    element.click()
}

Then(~/^web element (.*) should (have|contain) text (.*)$/) { String selector, String operator, String expectedText ->
    selector = normalizeParameter(selector)
    expectedText = normalizeParameter(expectedText)
    Navigator element = findElement(selector)
    if (operator == 'have') {
        assert element.text() == expectedText
    } else {
        assert element.text().contains(expectedText)
    }
}
Then(~/^web elements (.*) should (have|contain) texts (.*)$/) { String selector, String operator, List<String> expectedText ->
    selector = normalizeParameter(selector)
    expectedText = expectedText.collect { normalizeParameter(it) }
    Navigator element = findElement(selector)
    if (operator == 'have') {
        assert element*.text() == expectedText
    } else {
        assert element*.text().containsAll(expectedText)
    }
}
