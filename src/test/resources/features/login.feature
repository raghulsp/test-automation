Feature: Login Functionality

    Scenario Outline: Successful login with valid credentials
        Given the user is on the login page
        When the user enters "<username>" and "<password>"
        # And the user clicks on the login button
        # Then the user should be able to see the "<message>"

        Examples:
        |username|password|message|
        |validUser|validPassword|login success|
        |invalidUser|invalidPassword|error message|