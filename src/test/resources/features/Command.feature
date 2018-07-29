Feature: Command

  Scenario: echo
    When I execute command: echo One
    Then last command exit code should be 0
