# Running Interop Functional Tests

We have a new and improved means to run the Interop Functional Tests.  This new approach requires no local changes or no installation of any software on the host computer.


## Dependencies 

Now that we can run the functional tests directly on the server, these dependencies are already resolved and applied/downloaded.


## Functional Tests

There are two basic types of functional tests in the interop-functional-tests project:

###	1.  API functional tests specific to testing the functionality of the Interop- projects
		DirectoryFunctionalTest.java
		ILPLedgerAdapterFunctionalTest.java
		SPSPBackendServiceFunctionalTests.java
		SPSPClientProxyFunctionalTest.java
		UserRegistrationFunctionalTests.java
###	2. End-to-end USSD Functional Test that exercises every layer of L1P
		USSDFunctionalTest.java


## Setup / Configuration

Currently, the functional tests are hosted on the DFSP1 test server.  This reduces the complexity of environment setup and execution of the tests.
	In order to run the tests, you must log on to the DFSP1 test server.  This requires a file key, called "interop-dev1.pem” which can be downloaded from GitHub by following the URL: https://github.com/LevelOneProject/Docs/tree/master/AWS/Infrastructure/PI4-Test-Env

Then click on the interop-dev1.pem file to download it.  Save it to a location on your local hard drive.


## How to run the functional tests

Perform the two steps below to run the functional tests and see the results.

###	Step 1.  Make sure the code base is up-to-date
* Log in to dfsp1-test
  * _ssh -i "interop-dev1.pem" ec2-user@ec2-35-166-189-14.us-west-2.compute.amazonaws.com_
* Change directory to where the functional tests are located
  * _cd /home/ec2-user/scripts/modusbox/FunctionalTest/interop-functional-tests_
* Issue the command “_updateFunctionalTests.sh_”. (This name could change but this is what is should be at the time of this writing)
  * This will ensure the functional test code is up to date with the Master branch from GitHub.

###	Step 2.  Execute the functional tests
        * To execute a functional test, issue the following command:
        * _mvn test -Dgroups=ussd_createuser -Denv=test_

		The above command uses two java command line switches.  -Dgroups=   specifies what groups (or collections of tests) that need to be run.  The readme.md in the “interop-functional-test” GitHub project has a list of groups that are available for use.

		The second switch, “-Denv=“ specifies the environment against which the test should run. There are two values to choose from “test” or “qa”.  Both will execute using DFSP1.


## Analyze the results 

There are two types of output from the functional tests:
	* The first is the output directly from the test run.  Below is an example of the output directly from Maven test command line

Results :  
    Tests run: 22, Failures: 0, Errors: 0, Skipped: 0

The above output tells you that 22 tests were executed, zero failed, zero errors, and zero were skipped. 


* The second type of output contains HTML reports the tests produce when Errors are encountered. 
	Since these reports are HTML, they are best viewed if you copy the files to your local PC/Mac and open with your favorite browser or html editor.

Report output folder:
/home/ec2-user/scripts/modusbox/FunctionalTest/interop-functional-tests/target/failure-reports

* How to copy a report to your local hard drive for viewing
	
To copy a report file to your local hard drive from your local PC/Mac, issue the following command where you saved the “inter-dev1.pem” file.
_scp -i "interop-dev1.pem" ec2-user@ec2-35-166-189-14.us-west-2.compute.amazonaws.com:/home/ec2-user/scripts/modusbox/FunctionalTest/interop-functional-tests/target/failure-reports/USSD-Functional-Tests.html   local-USSD-Functional-Tests.html_
	
The above command, copied the USSD-Functional-Tests.html from the AWS server to you local hard drive and called it “local-USSD-Functional-Tests.html”.  You can call this file what ever you want.  This example, we added the “local-“ prefix so it is clear that this is the local file name.  
	
## Triage

Under construction

Due to the complex nature of the LevelOneProject, errors or failures could occur in different layers, so a good approach to discovering the nature of the error is to use Kibana to search logs for the specific run of the test.
L1P_TRACE_ID is the key/value that will uniquely all log entries that relate to the same transaction.
