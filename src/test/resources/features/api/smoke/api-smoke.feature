@api-smoke
Feature: API Smoke Tests
  Quick sanity check — one happy-path scenario per service

  @api-smoke @user-api
  Scenario: Fetch user list returns data
    When I request all users on page 1
    Then the user response status should be 200
    And the user response field "data" should be present

  @api-smoke @auth-api
  Scenario: Login with valid credentials returns token
    When I login with email "eve.holt@reqres.in" and password "cityslicka"
    Then the auth response status should be 200
    And a valid auth token should be returned

  @api-smoke @product-api
  Scenario: Product catalog returns non-empty list
    When I request all products
    Then the product response status should be 200
    And the product list should not be empty

  @api-smoke @order-api
  Scenario: Order list returns non-empty list
    When I request all orders
    Then the order response status should be 200
    And the order list should not be empty
