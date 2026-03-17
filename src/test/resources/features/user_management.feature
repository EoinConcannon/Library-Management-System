Feature: User Account Management

  Scenario: Creating a new user account as a librarian
    Given I am logged in as a librarian
    When I navigate to the Manage Users page
    And I fill in the user registration form with name "John Test", email "johntest@test.com" and role "STUDENT"
    And I submit the user registration form
    Then a new user account is created
    And the login credentials are displayed on screen