@integration
Feature: User Login Flow — API setup + UI verification
  Demonstrates end-to-end flow where a user is provisioned via the API
  and the same credentials are then validated through the web UI.

  For this scenario to pass end-to-end in your environment:
    - api.base.url must point to your application's API (the one that creates real accounts)
    - base.url must point to the same application's login page
    - The API response must echo back the email / username so UserService can parse it

  The default demo uses reqres.in (mock API) + the-internet.herokuapp.com (fixed credentials),
  so these two systems do not share a user store — update the URLs for your real application.

  Scenario: Login to UI with credentials created through the API
    Given a new user is created via the API
    And the user is on the login page
    When the user logs in with the API-created credentials
    And the user clicks on the login button
    Then the user should be able to see the "login success"
