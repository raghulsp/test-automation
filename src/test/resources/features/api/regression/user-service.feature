@api-regression @user-api
Feature: User Service — Full CRUD
  Covers all user service endpoints including edge cases

  Scenario: Retrieve paginated user list — page 1
    When I request all users on page 1
    Then the user response status should be 200
    And the user response field "data" should be present
    And the user response field "total" should be present

  Scenario: Retrieve paginated user list — page 2
    When I request all users on page 2
    Then the user response status should be 200
    And the user response field "data" should be present

  Scenario: Retrieve existing user by ID
    When I request user with id 2
    Then the user response status should be 200
    And the user response field "data.id" should be present
    And the user response field "data.email" should be present

  Scenario: Retrieve non-existent user returns 404
    When I request user with id 99999
    Then the user response status should be 404

  Scenario: Create a new user
    When I create a user with name "Jane Doe" and job "Test Engineer"
    Then the user response status should be 201
    And the created user id should be present
    And the user response field "name" should be present
    And the user response field "job" should be present

  Scenario: Update an existing user
    When I update user 2 with name "Updated User" and job "Senior Engineer"
    Then the user response status should be 200
    And the user response field "name" should equal "Updated User"
    And the user response field "job" should equal "Senior Engineer"

  Scenario: Delete a user
    When I delete user 2
    Then the user response status should be 204
