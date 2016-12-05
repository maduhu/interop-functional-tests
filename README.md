# interop-functional-tests
Repo that holds the functional tests


- Steps to run tests:
1. After checkout, at the project root folder issue "mvn clean test" command.
2. In target/surefire-reports folder, open index.html file to view the TestNG report. This report lists out the total test cases executed, number of passed and failed test cases
3. In targe/failed-reports folder, there will be html file for each corresponding api against which the functional test cases has been executed. This report has additional details around request and response payloads, end-point information and the query parameters, if any.
4. To generate javadoc, run mvn javadoc:test-javadoc at the project root folder

-
Current Interop projects covered:
  - Interop spsp clientproxy (12/2/2016)
  - Interop User Registrations (11/28/2016)
  - Interop DFSP Directory (11/28/2016)
  - Interop ILP Ledger Adapter
  
Coming Soon:
  - Interop spsp backend services
