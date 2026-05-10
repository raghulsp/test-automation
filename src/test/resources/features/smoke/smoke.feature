Feature: Smoke Tests

  @smoke
  Scenario Outline: Smoke - verify critical flow passes
    Given a "<suite>" precondition is met
    When a "<action>" action is performed
    Then a "<assertion>" assertion passes

    Examples:
      | suite | action        | assertion      |
      | smoke | page load     | title visible  |
      | smoke | nav link click| content loaded |
