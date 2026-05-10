package com.framework.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DummySteps {

    @Given("a {string} precondition is met")
    public void preconditionIsMet(String label) {
    }

    @When("a {string} action is performed")
    public void actionIsPerformed(String label) {
    }

    @Then("a {string} assertion passes")
    public void assertionPasses(String label) {
    }
}
