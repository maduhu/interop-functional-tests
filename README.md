# interop-functional-tests
Repo that holds the functional tests


- Steps to run tests:
1. After checkout, at the project root folder issue "mvn clean test" command.
2. In target/surefire-reports folder, open index.html file to view the TestNG report. This report lists out the total test cases executed, number of passed and failed test cases
3. In targe/failed-reports folder, there will be html file for each corresponding api against which the functional test cases has been executed. This report has additional details around request and response payloads, end-point information and the query parameters, if any.
4. To generate javadoc, run "mvn javadoc:test-javadoc" at the project root folder

New Testing feature added 12/9/2016 to run test on a finer granular scale.  
This allows test to be run a set of targeted tests, not the whole test suite.
Since this is the initial release with the ability to run specific groups of tests,
our groupings may not be perfect or ideal.  Please let the ModusBox developers know
if you have suggestions for better groupings.



Example:
  The test suite that we are using for running functional tests, TestNG, allows various ways of running 1 or more tests.
  After checking out the project, cd to the project root of this project.
  
  Examples of how to run tests:
  
  - "mvn test" -- Runs all functional tests part of this project
  - "mvn test -Dgroups=directory_all" -- Runs only the tests for dfsp_directory
  - "mvn test -Dgroups=spsp_backend_service_all,spsp_client_proxy_quote" -- Runs all the tests targeted for spsp_backend_services as well as just the quoting test in the spsp_client_proxy
  
  
* Below are the lists of the various test groups *
  
### DirectoryFunctional test groups
* directory_all
* directory_identifier_types
* register_dfsp"
* directory_resources
* directory_metadata"}

### ILP Ledger Adapter test groups
* ilp_ledger_adapater_all
* ilp_ledger_adapater_prepare_fulfill_transfer
* ilp_ledger_adapater_prepare_fulfilled_transfer
* ilp_ledger_adapater_prepare_transfer

### SPSP Backend Services test groups
* spsp_backend_service_all

### SPSP Client Proxy test groups
* spsp_client_proxy_all
* spsp_client_proxy_quote
* spsp_client_proxy_payment_setup
* spsp_client_proxy_payment
* spsp_client_proxy_invoice

### User Registration test groups
* user_registration_all
* user_registration_full_end_to_end

### FullPaymentWithNotifications test groups
* payment_setup_and_execute_with_notification


### Current Interop projects covered by functional tests:
  - Interop spsp clientproxy   (12/2/2016)
  - Interop User Registrations (11/28/2016)
  - Interop DFSP Directory     (11/28/2016)
  - Interop ILP Ledger Adapter (?)
  
### Coming Soon:
  - Interop spsp backend services
  - ilp ledger full payment with notifications
