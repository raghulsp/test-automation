@api-regression @order-api
Feature: Order Service — Order Management
  Covers order retrieval, filtering by user, creation and cancellation

  Scenario: Retrieve all orders
    When I request all orders
    Then the order response status should be 200
    And the order list should not be empty

  Scenario: Retrieve order by valid ID
    When I request order with id 1
    Then the order response status should be 200
    And the order response field "id" should be present
    And the order response field "title" should be present
    And the order response field "userId" should be present

  Scenario: Retrieve orders belonging to a specific user
    When I request orders for user 1
    Then the order response status should be 200
    And the order list should not be empty
    And the order user id should be 1

  Scenario: Create a new order for a user
    When I create an order for user 3 with title "Priority Shipment - New Order"
    Then the order response status should be 201
    And the created order id should be present
    And the order response field "title" should be present

  Scenario: Cancel an existing order
    When I cancel order 10
    Then the order response status should be 200
