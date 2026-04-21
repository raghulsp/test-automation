@api
Feature: User API
  Validates the user resource endpoints.
  These scenarios run without a browser — no WebDriver is initialised.
  Change api.base.url in config.properties (or pass -Dapi.base.url=...) to target your environment.

  @smoke
  Scenario: Create a new user returns 201 with an ID
    When I send POST to "/api/users" with new user details
    Then the response status should be 201
    And the response body should contain "id"
    And the response body should contain "createdAt"

  Scenario: Get an existing user returns 200
    When I send GET to "/api/users/2"
    Then the response status should be 200
    And the response body should contain "data"

  Scenario: Delete a user returns 204
    When I send DELETE to "/api/users/2"
    Then the response status should be 204

  Scenario: Authenticate with valid credentials returns a token
    When I authenticate with email "eve.holt@reqres.in" and password "cityslicka"
    Then the response status should be 200
    And the response should contain an auth token
