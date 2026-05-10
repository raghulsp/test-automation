Feature: Regression Tests

  @regression
  Scenario Outline: Regression - verify edge case handling
    Given a "<suite>" precondition is met
    When a "<action>" action is performed
    Then a "<assertion>" assertion passes

    Examples:
      | suite      | action             | assertion              |
      | regression | empty form submit  | validation error shown |
      | regression | session expiry     | redirect to login      |
