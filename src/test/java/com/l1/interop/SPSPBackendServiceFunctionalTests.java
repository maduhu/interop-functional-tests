package com.l1.interop;

import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.isEmptyOrNullString;
//import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.output.WriterOutputStream;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class SPSPBackendServiceFunctionalTests {

	private static String host;
	private static String port;
	private static String url;
	private static String dfsp_username;
	private static String dfsp_password;
	
	private Properties prop = new Properties();

	FileWriter writer;
    PrintStream captor;
    
	
	@BeforeClass(alwaysRun=true)
    private void beforeClass() throws Exception {

//      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1-test.properties");
      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp2-test.properties");
//      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1-qa.properties");
//      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp2-qa.properties");
        
		prop.load(is);
        
        String environment = System.getProperty("env");
        if(environment != null){
            is = ClassLoader.getSystemResourceAsStream("dfsp1-"+environment.toLowerCase()+".properties");
        }
        
        prop.load(is);
        host = prop.getProperty("host");
        port = prop.getProperty("port");
        url = "http://"+host+":"+port;
        
        /*
         * 
         * Override url for local testing
         * 
         */
//        url = "http://localhost:8081";
        
        System.out.println("**************************************************************************************************************");
        System.out.println("*                                                                                                            *");
        System.out.println("*                         Tests running using the URL of :: " + url + "   *******************");
        System.out.println("*                                                                                                            *");
        System.out.println("**************************************************************************************************************");
        
        dfsp_username = prop.getProperty("dfsp.username");
        dfsp_password = prop.getProperty("dfsp.password");
        
        if(!(new File("target/failure-reports")).exists())
            new File("target/failure-reports").mkdirs();
        
        writer = new FileWriter("target/failure-reports/SPSP Backend Services.html");
        captor = new PrintStream(new WriterOutputStream(writer), true);
        captor.println( "<html lang='en'>\n" );
        
        captor.println( "<head>\n" );
        captor.println( "<meta charset='utf-8'>\n" );
        captor.println( "<title>Failure Report</title>\n" );
        captor.println( "</head>\n\n" );
        
        captor.println( "<body>\n" );
        captor.println( "<h1><center>Functional Test Failure Report</center></h1>\n" );
    }
    
	
    @AfterClass(alwaysRun=true)
    private void afterClass() throws Exception {
        captor.println( "</body>\n" );
        captor.println( "</html>\n" );
    }
    
    
    @BeforeTest(alwaysRun=true)
    private void setup() throws Exception {
        RestAssured.config = RestAssuredConfig.config().logConfig(LogConfig.logConfig().enablePrettyPrinting(true));
    }
    
    
    @DataProvider(name = "invoice_create_positive")
    private Iterator<Object []> dpSPSPClientProxy_invoice_Post_positive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/backend_services/invoice_post_positive.csv");
        return testCases.iterator();
    }
    
    
    @DataProvider(name = "invoice_positive")
    private Iterator<Object []> dpSPSPClientProxy_invoice_Get_negative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/backend_services/backend_invoice_positive.csv");
        return testCases.iterator();
    }
    
    
    /**
	 * 
	 * The goal of this test is to ensure that we get back the key attributes back and a http status of 200
	 * 
	 */
	@Test(description="get_backend_services_receiver_positive", enabled=true, groups={"spsp_backend_service_all"})
	public void get_backend_services_receiver_positive() {

		/*
		 * This is the sample data that is returned
			{
			  "type": "payee",
			  "name": "Bob Dylan",
			  "account": "ilpdemo.red.bob",
			  "currencyCode": "USD",
			  "currencySymbol": "$",
			  "imageUrl": "https://red.ilpdemo.org/api/receivers/bob/profile_pic.jpg",
			  "paymentsUrl": "http://backend.example/receivers/bob/:id"
			}
		 * 
		 */
		
		
		/*
    	 * 
    	 * 3/20/2017 Error:
    	 *   Test passed     dfsp1-qa   
    	 *   
    	 *   
    	 *  
    	 *  Test failed on dfsp2-test  :: Looks like the data is the only problem.  Might need to now have the receiver in the file, but created during the test so it can adapt to each environment.
    	 *  Consider creating a receiver on the fly by leveraging 
    	 *  	setUp_ForValidReceiver_ShouldReturn201_ShouldReturnValidResponse from the SPSP Client Proxy Functional Test to create one on the fly.
    	 *  
    	 *  
    	    [03-20 20:25:48,860] INFO  [[interop-domain].api-httpListenerConfig.worker.431] spsp-backend-api-main: Received request with traceID=92c0f2b4-d0e6-4c5b-854b-ef7c8c0c40ea at path=/spsp/backend/v1/receivers/54200545
			[03-20 20:25:48,860] INFO  [[interop-domain].api-httpListenerConfig.worker.431] spsp-backend-fetch-payee: Processing request for GET on http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:8010/receivers/54200545, traceID=92c0f2b4-d0e6-4c5b-854b-ef7c8c0c40ea, payee=54200545
			[03-20 20:25:48,926] ERROR [[interop-domain].api-httpListenerConfig.worker.431] spsp-backend-fetch-payee: Failed to get account {"id":"Directory.UserNotFound","message":"User not found"}
			[03-20 20:25:48,926] INFO  [[interop-domain].api-httpListenerConfig.worker.431] spsp-backend-fetch-payee: Processing complete for get:/receivers/{payee}, traceID=92c0f2b4-d0e6-4c5b-854b-ef7c8c0c40ea, payee=54200545, payload={"id":"Directory.UserNotFound","message":"User not found"}
			[03-20 20:25:49,251] INFO  [metrics-logger-reporter-22-thread-1] metrics: type=COUNTER, name=l1p.spsp-backend.fetch.api.FetchPayeeGetRequest, timestamp=1490041549, total_count=118, interval=5, delta=1
			[03-20 20:25:49,251] INFO  [metrics-logger-reporter-22-thread-1] metrics: type=COUNTER, name=l1p.spsp-backend.payments.api.PaymentsInvoicePostRequest, timestamp=1490041549, total_count=7, interval=5, delta=0
			[03-20 20:25:49,251] INFO  [metrics-logger-reporter-22-thread-1] metrics: type=COUNTER, name=l1p.spsp-backend.payments.api.PaymentsPayeePutRequest, timestamp=1490041549, total_count=557, interval=5, delta=0
			[03-20 20:25:49,251] INFO  [metrics-logger-reporter-22-thread-1] metrics: type=TIMER, category=l1p.spsp-backend.fetch.api.FetchPayeeGetTime, timestamp=1490041549, total_count=118, interval=5, delta=1, interval_rate=0.2, m1=6.515301826113625E-59, min=60.195271, max=195.07154699999998, mean=65.93319, p75=65.93319, p95=65.93319, p98=65.93319, p99=65.93319, p999=65.93319, rate_unit=events/second, duration_unit=milliseconds
			[03-20 20:25:49,251] INFO  [metrics-logger-reporter-22-thread-1] metrics: type=TIMER, category=l1p.spsp-backend.payments.api.PaymentsInvoicePostTime, timestamp=1490041549, total_count=7, interval=5, delta=0, interval_rate=0.0, m1=1.4404152909158775E-84, min=31.007709, max=83.825063, mean=31.387717596304395, p75=31.383271999999998, p95=31.383271999999998, p98=31.383271999999998, p99=31.383271999999998, p999=31.383271999999998, rate_unit=events/second, duration_unit=milliseconds
			[03-20 20:25:49,251] INFO  [metrics-logger-reporter-22-thread-1] metrics: type=TIMER, category=l1p.spsp-backend.payments.api.PaymentsPayeePutTime, timestamp=1490041549, total_count=557, interval=5, delta=0, interval_rate=0.0, m1=5.097512724826319E-58, min=14.052519, max=444.74994699999996, mean=302.9909600938894, p75=362.517496, p95=420.546196, p98=431.297525, p99=435.21588299999996, p999=444.74994699999996, rate_unit=events/second, duration_unit=milliseconds


    	 *  
    	 *  
    	 */
		
		// TODO  Need to add the receiver ID from a file not hard coded.  
		
        String urlPath = "/spsp/backend/v1/receivers/54200545";  // Need to add the receiver ID from a file not hard coded 
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
        	
            Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            when().
            	get(url+urlPath);
            
            
            JsonPath jsonPath = response.jsonPath();
            String responseJson = response.asString();
            System.out.println("JSON 3.9 response for get_backend_services_receiver_positive: " + responseJson);
            
            assertThat(response.getStatusCode(), equalTo(200));
            
            if (response.asString().contains("User not found")) {
            	throw new IllegalStateException("Something did not go correctly. Service response = " + response.asString());
            }
            
            assertThat(jsonPath.getString("type"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("name"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("account"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("currencyCode"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("currencySymbol"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("imageUrl"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("imageUrl"), not(isEmptyOrNullString()));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>get_backend_services_receiver_positive</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "No parameters");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
		
	}
	
	
	
	/**
	 * 
	 * The goal of this test is to complete a full end to end test of receiver invoices.
	 * Generate an invoice ID.  Try to query it.  If it does not exist, then POST it to 
	 * create a new invoice. 
	 * 
	 * Then query back the newly created invoice.
	 * Then Update the invoice with the PUT.
	 * 
	 * Then query it back to ensure that the update worked.
	 * 
	 * 2/27/2017 - Noticed this test is not complete or functional so commenting it out for now.
	 * 
	 */
//	@Test(groups={"spsp_backend_service_all"})  // Noticed this test is not complete or functional so commenting it out for now.
	public void test_full_end_to_end_invoice_positive() {
		
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
        	
        	// create 
        	String baseReceiverInvoicePath = "/receivers/invoices/";
            Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            when().
	            get(url+baseReceiverInvoicePath);
            
            JsonPath jsonPath = response.jsonPath();
            String jsonResponse = response.asString();
            System.out.println("test_full_end_to_end_invoice_positive :: " + jsonResponse);
            
            assertThat(response.getStatusCode(), equalTo(200));
            
            assertThat(jsonPath.getString("type"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("name"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("account"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("currencyCode"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("currencySymbol"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("imageUrl"), not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("paymentsUrl"), not(isEmptyOrNullString()));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>get_backend_services_receiver_positive</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "No parameters");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }

	}
	

	
	@Test(dataProvider="invoice_positive", groups={"spsp_backend_service_all"})
	public void test_get_an_existing_invoice_positive(String personName, String invoiceUrl, String account, String name, String currencyCode, String currencySymbol, String amount, String status, String invoiceInfo) {
		
		/*
		 * Sample JSON
		 * 
		 * {
			  "account": "ilpdemo.red.bob",
			  "name": "Bob Dylan",
			  "currencyCode": "USD",
			  "currencySymbol": "$",
			  "amount": "10.40",
			  "status": "unpaid",
			  "invoiceInfo": "https://merchant-website.example/gp/your-account/order-details?ie=UTF8&orderID=111-7777777-1111111"
			}
		 * 
		 */
		
		
		
		
	/*	
	 * 2/20/2017 Test failed on dfps1 QA for the following reasons.  No errors were logged to server log file, only the console in the test.
	 * 
	 * 
	 * 
	 * *                                                                                                            *
		**************************************************************************************************************
		Get invoice existing URL: http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:8088/spsp/client/v1/invoices/{invoiceId}
		Response JSON from call: "Not Found"
		Get invoice existing URL: http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:8088/spsp/client/v1/invoices/{invoiceId}
		Response JSON from call: "Not Found"
		FAILED: test_get_an_existing_invoice_positive("Bob", "123", "dfsp2.bob.dylan.account", "Bob Dylan", "USD", "$", "10.4", "unpaid", "https://www.example.com/gp/your-account/order-details?ie=UTF8&orderID=111-7777777-1111111")
		io.restassured.path.json.exception.JsonPathException: Failed to parse the JSON document
			at io.restassured.path.json.JsonPath$ExceptionCatcher.invoke(JsonPath.java:930)
			
			
			
		--------	2/20/2017 Test also failed on dfsp2 test.    ----------
			
			Get invoice existing URL: http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:8088/spsp/client/v1/invoices/{invoiceId}
			Response JSON from call: "Not Found"
			Get invoice existing URL: http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:8088/spsp/client/v1/invoices/{invoiceId}
			Response JSON from call: "Not Found"
			FAILED: test_get_an_existing_invoice_positive("Bob", "123", "dfsp2.bob.dylan.account", "Bob Dylan", "USD", "$", "10.4", "unpaid", "https://www.example.com/gp/your-account/order-details?ie=UTF8&orderID=111-7777777-1111111")
			io.restassured.path.json.exception.JsonPathException: Failed to parse the JSON document
				at io.restassured.path.json.JsonPath$ExceptionCatcher.invoke(JsonPath.java:930)
				at io.restassured.path.json.JsonPath$4.doParseWith(JsonPath.java:895)
				at io.restassured.path.json.JsonPath$JsonParser.parseWith(JsonPath.java:975)
				at io.restassured.path.json.JsonPath.get(JsonPath.java:201)
				at io.restassured.path.json.JsonPath.getString(JsonPath.java:351)
				at com.l1.interop.SPSPBackendServiceFunctionalTests.test_get_an_existing_invoice_positive(SPSPBackendServiceFunctionalTests.java:345)
				at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
				at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
				at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
				at java.lang.reflect.Method.invoke(Method.java:498)
				at org.testng.internal.MethodInvocationHelper.invokeMethod(MethodInvocationHelper.java:84)
				at org.testng.internal.Invoker.invokeMethod(Invoker.java:714)
				at org.testng.internal.Invoker.invokeTestMethod(Invoker.java:901)
				at org.testng.internal.Invoker.invokeTestMethods(Invoker.java:1231)
				at org.testng.internal.TestMethodWorker.invokeTestMethods(TestMethodWorker.java:127)
				at org.testng.internal.TestMethodWorker.run(TestMethodWorker.java:111)
				at org.testng.TestRunner.privateRun(TestRunner.java:767)
				at org.testng.TestRunner.run(TestRunner.java:617)
				at org.testng.SuiteRunner.runTest(SuiteRunner.java:348)
				at org.testng.SuiteRunner.runSequentially(SuiteRunner.java:343)
				at org.testng.SuiteRunner.privateRun(SuiteRunner.java:305)
				at org.testng.SuiteRunner.run(SuiteRunner.java:254)
				at org.testng.SuiteRunnerWorker.runSuite(SuiteRunnerWorker.java:52)
				at org.testng.SuiteRunnerWorker.run(SuiteRunnerWorker.java:86)
				at org.testng.TestNG.runSuitesSequentially(TestNG.java:1224)
				at org.testng.TestNG.runSuitesLocally(TestNG.java:1149)
				at org.testng.TestNG.run(TestNG.java:1057)
				at org.testng.remote.AbstractRemoteTestNG.run(AbstractRemoteTestNG.java:132)
				at org.testng.remote.RemoteTestNG.initAndRun(RemoteTestNG.java:230)
				at org.testng.remote.RemoteTestNG.main(RemoteTestNG.java:76)
			Caused by: groovy.json.JsonException: A JSON payload should start with an openning curly brace '{' or an openning square bracket '['.
			Instead, '"Not Found"' was found on line: 1, column: 1
				at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
			
			
		*/
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
        	
//        	String fullPath = url+"/receivers/invoices/{invoiceId}";
        	String fullPath = url+"/spsp/client/v1/invoices/{invoiceId}";
        	System.out.println("Get invoice existing URL: " + fullPath);
            Response response = 
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	pathParam("invoiceId", invoiceUrl).
            when().
            	get(fullPath);  // the { } are bindings for invoiceUrl defined up in the Given section.

            
            System.out.println("Response JSON from call: " + response.asString());
            JsonPath jsonPath = response.jsonPath();
            
            assertThat(response.getStatusCode(), equalTo(200));
            
            assertThat("json response", response.asString(), not(equalTo("Not Found")));
            assertThat(jsonPath.getString("account"), 		not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("name"), 			not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("currencyCode"), 	not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("currencySymbol"),not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("amount"),		not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("status"), 		not(isEmptyOrNullString()));
            assertThat(jsonPath.getString("invoiceInfo"), 	not(isEmptyOrNullString()));
            
            
            // TODO:  Add data validation to the data level after the fetch.  Compare the returned data from the 
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>get_backend_services_receiver_positive</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "No parameters");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
	}
	
	
//	@Test(dataProvider="invoice_create_positive", groups={"spsp_backend_service_all"})
//	public void test_paying_an_invoice_positive(String invoiceUrl, String invoiceId, String submissionUrl, String senderIdentifier, String memo) {
//		
//	}
    
}
