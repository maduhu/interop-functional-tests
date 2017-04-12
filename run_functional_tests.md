## Dependencies 

	The ModusBox’s Interop Functional tests are based on several Java frameworks and therefore requires Java 1.8 to run.  
	Additionally, all of the Interop projects are Apache Maven based for build automation, so Maven is required as well.
	Git is needed to check out the source code from our GitHub software repository.


## Functional Tests

	Currently there are two basic types of functional tests in the interop-functional-tests project:

	1.  API functional tests specific to testing the functionality of the Interop- projects
		DirectoryFunctionalTest.java
		ILPLedgerAdapterFunctionalTest.java
		SPSPBackendServiceFunctionalTests.java
		SPSPClientProxyFunctionalTest.java
		UserRegistrationFunctionalTests.java
	2) End-to-end USSD Functional Test that exercises every layer of L1P
		USSDFunctionalTest.java


## Setup / Configuration

	Currently, the functional test are hosted on the DFSP1 test server.  This reduces the complexity environment setup and execution of the tests.
	In order to run the tests, you must log on to the dfsp1 test server.  This requires a security file, called "interop-dev1.pem”.  This can be downloaded from GitHub from the following URL: https://github.com/LevelOneProject/Docs/tree/master/AWS/Infrastructure/PI4-Test-Env

		Then click on the interop-dev1.pem file to down it.  Save it to a location on your local hard drive.


## How to run functional test

	Perform the following two steps to run the functional test and to see the functional test output.

	Step 1.  Make sure the code base is up today
        * Log in to dfsp1-test
            * ssh -i "interop-dev1.pem" ec2-user@ec2-35-166-189-14.us-west-2.compute.amazonaws.com
        * Change directory to where the functional tests are located
			cd /home/ec2-user/scripts/modusbox/FunctionalTest/interop-functional-tests
        * Issue the command “updateFunctionalTests.sh”. (This name could change but this is what is should be at the time of this writing)
            * This will ensure the functional test code is up to date with the Master branch from GitHub.

	Step 2.  Execute the functional test
        * To execute a functional test, issue the following command:
        * mvn test -Dgroups=ussd_createuser -Denv=test

		The above command uses two java command line switches.  -Dgroups=   tells the functional test what groups (or collections of tests) that you want to run.  See the readme.md in the “interop-functional-test” GitHub project for a list of groups that are available for use.

		The second switch, “-Devn=“ tells the functional test which environment you want it to test against.   There are two values to choose from “test” or “qa”.  Both will execute using DFSP1.


## Analyze the results 

	There are two type of output.  
	The first is the output directly from the test run.  Below is an example of the output directly from Maven test command line

	Results :
	Tests run: 22, Failures: 0, Errors: 0, Skipped: 0

	The above output tells you that 22 tests were executed, zero failed, zero errors, and zero were skipped. 


	The second type of output are the HTML reports the tests produce when Errors are encountered. 
	Since these reports are HTML, they are best viewed if you copy the files to your local PC/Mac and opened with your favorite browser or html editor.

	Report output folder:
	/home/ec2-user/scripts/modusbox/FunctionalTest/interop-functional-tests/target/failure-reports

	Copy a report to your local hard drive
	
	To copy a report file to your local hard drive, from your local PC/Mac, issue the following command where you saved the “inter-dev1.pem” file.
	scp -i "interop-dev1.pem" ec2-user@ec2-35-166-189-14.us-west-2.compute.amazonaws.com:/home/ec2-user/scripts/modusbox/FunctionalTest/interop-functional-tests/target/failure-reports/USSD-Functional-Tests.html   local-USSD-Functional-Tests.html
	
	The above command, copied the USSD-Functional-Tests.html from the AWS server to you local hard drive and called it “local-USSD-Functional-Tests.html”.  You can call this file what ever you want.  This example, we added the “local-“ prefix so it is clear that this is the local file name.  
	
## Triage

	Under construction

	Due to the complex nature of the LevelOneProject , errors or failures could occur one or more layers, so the best approach to discovering the nature of the error is to use Kabana to search logs.
	L1P_TRACE_ID is the key/value that will uniquely all log entries that relate to the same transaction.

