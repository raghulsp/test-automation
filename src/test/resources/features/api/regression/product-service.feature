@api-regression @product-api
Feature: Product Service — Catalog Operations
  Covers product listing, retrieval by ID, category filtering, creation and deletion

  Scenario: Retrieve all products
    When I request all products
    Then the product response status should be 200
    And the product list should not be empty

  Scenario: Retrieve product by valid ID
    When I request product with id 1
    Then the product response status should be 200
    And the product response field "title" should be present
    And the product response field "price" should be present
    And the product response field "category" should be present

  Scenario: Retrieve products in electronics category
    When I request products in category "electronics"
    Then the product response status should be 200
    And the product list should not be empty

  Scenario: Retrieve products in jewelery category
    When I request products in category "jewelery"
    Then the product response status should be 200
    And the product list should not be empty

  Scenario: Retrieve all product categories
    When I request all product categories
    Then the product response status should be 200
    And the product list should not be empty

  Scenario: Create a new product
    When I create a product with title "Automation Test Device" and price 499.99
    Then the product response status should be 200
    And the created product id should be present

  Scenario: Delete a product
    When I delete product with id 5
    Then the product response status should be 200
