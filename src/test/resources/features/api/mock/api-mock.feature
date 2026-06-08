@mock
Feature: API Mock Tests — stub external services for deterministic scenarios
  Runs against embedded WireMock (localhost:9999) via -Dapi.env=mock.
  No network dependency — fully offline and deterministic.

  # ---------------------------------------------------------------------------
  # User service — happy paths
  # ---------------------------------------------------------------------------

  @mock @user-api
  Scenario: Fetch user by ID returns mocked data
    Given the GET "/users/1" endpoint is stubbed to return 200 with body:
      """
      {"data": {"id": 1, "email": "mock@test.com", "first_name": "Mock", "last_name": "User"}}
      """
    When I request user with id 1
    Then the user response status should be 200
    And the user response field "data.email" should equal "mock@test.com"

  @mock @user-api
  Scenario: Create user returns mocked 201
    Given the POST "/users" endpoint is stubbed to return 201 with body:
      """
      {"id": "999", "name": "Mock User", "job": "QA Engineer", "createdAt": "2024-01-01T00:00:00.000Z"}
      """
    When I create a user with name "Mock User" and job "QA Engineer"
    Then the user response status should be 201
    And the user response field "id" should be present
    And the user response field "name" should equal "Mock User"

  @mock @user-api
  Scenario: Update user returns mocked response
    Given the PUT "/users/2" endpoint is stubbed to return 200 with body:
      """
      {"name": "Updated User", "job": "Senior Engineer", "updatedAt": "2024-01-01T00:00:00.000Z"}
      """
    When I update user 2 with name "Updated User" and job "Senior Engineer"
    Then the user response status should be 200
    And the user response field "name" should equal "Updated User"

  @mock @user-api
  Scenario: Delete user returns mocked 204
    Given the DELETE "/users/2" endpoint is stubbed to return 204
    When I delete user 2
    Then the user response status should be 204

  # ---------------------------------------------------------------------------
  # User service — error paths (hard to reproduce on real APIs)
  # ---------------------------------------------------------------------------

  @mock @user-api
  Scenario: User not found returns 404
    Given the GET "/users/999" endpoint is stubbed to return 404 with body:
      """
      {"error": "Not Found"}
      """
    When I request user with id 999
    Then the user response status should be 404

  @mock @user-api
  Scenario: User service returns 500 server error
    Given the "/users" endpoint is stubbed to return a 500 server error
    When I request all users on page 1
    Then the user response status should be 500

  # ---------------------------------------------------------------------------
  # Auth service — error paths
  # ---------------------------------------------------------------------------

  @mock @auth-api
  Scenario: Login with wrong credentials returns mocked 400
    Given the POST "/login" endpoint is stubbed to return 400 with body:
      """
      {"error": "Missing password"}
      """
    When I login with email "bad@test.com" and password "wrongpass"
    Then the auth response status should be 400

  @mock @auth-api
  Scenario: Unauthorized access returns 401
    Given the "/login" endpoint is stubbed to return a 401 unauthorized
    When I login with email "hack@evil.com" and password "password"
    Then the auth response status should be 401
