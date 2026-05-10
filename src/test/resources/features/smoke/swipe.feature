Feature: Mobile swipe gestures

  @smoke @mobile-only
  Scenario: User can swipe to the bottom of a scrollable screen and back to the top
    Given the user is on a scrollable mobile screen
    When the user swipes to the bottom of the screen
    And the user swipes back to the top of the screen
    Then the screen should be back at the top
