Feature: Application login


Scenario: Assign Permission set to User 
Given User login to Salesforce via API
When User assign the permission set to a another user
Then Permission set should be assinged successfully
