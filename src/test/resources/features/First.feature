Feature: first

  Scenario: Git

    When I send GET request with parameters to https://github.com/edushak
    """
    """

    Then rest response status == 200
     And rest response statusLine == HTTP/1.1 200 OK
     And rest response statusLine == 'HTTP/1.1 200 OK'

     And rest response contentType == text/html
     And rest response contentType == 'text/html'
#     And rest response data.toString() contains Popular repositories

     And response text should contain 'Popular repositories'
     And response text should contain Popular repositories
