Feature: REST steps usage examples

  Scenario: setup
    Given I execute code "to set user dynamically":
    """
    user = 'edushak'
    """


  Scenario: simple GET with fixed endpoint
    When I send GET request to https://github.com/edushak
    Then rest response status == 200
    And rest response statusLine == HTTP/1.1 200 OK


  Scenario: simple GET with parametrized endpoint
    When I send GET request to https://github.com/$user
    Then rest response status == 200
    And rest response statusLine == HTTP/1.1 200 OK


  Scenario: GET with headers
    HTTP headers values passed are ignored by server in this case

    When I send GET request with parameters to https://github.com/$user as:
    """
    headers = ['key' : 'value']
    // body = ''
    """

    Then rest response status == 200
     And rest response statusLine == HTTP/1.1 200 OK
     And rest response statusLine == 'HTTP/1.1 200 OK'

     And rest response contentType == text/html
     And rest response contentType == 'text/html'

     And response text should contain 'Popular repositories'
     And response text should contain Popular repositories

     And response text should contain 'test-spec'
     And response text should not contain 'some other project name'
