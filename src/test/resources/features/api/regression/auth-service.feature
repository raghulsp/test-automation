@api-regression @auth-api
Feature: Auth Service — Authentication Flows
  Covers login and registration including negative scenarios

  Scenario: Successful login returns token
    When I login with email "eve.holt@reqres.in" and password "cityslicka"
    Then the auth response status should be 200
    And a valid auth token should be returned

  Scenario: Login with unrecognised email returns 400
    When I login with email "notauser@example.com" and password "wrongpassword"
    Then the auth response status should be 400
    And an auth error message should be returned

  Scenario: Login with missing password returns 400
    When I login with email "eve.holt@reqres.in" and password ""
    Then the auth response status should be 400
    And an auth error message should be returned

  Scenario: Register a new user successfully
    When I register with email "newuser@example.com" and password "secure123"
    Then the auth response status should be 200

  Scenario: Register with missing password returns 400
    When I register with email "newuser@example.com" and password ""
    Then the auth response status should be 400
    And an auth error message should be returned
