Feature: Command steps usage examples

  Scenario: echo
    When I execute command: echo Test-spec can call external processes
    Then last command exit code should be 0
     And last command STDOUT should be 'Test-spec can call external processes'
     And last command STDOUT should contain 'can call external'
     And last command STDOUT should match '(.*)can call external(.*)'
     And last command STDERR should be ''
