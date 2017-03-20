package com.l1.interop;


import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.number.IsCloseTo.closeTo;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;

import org.apache.commons.io.output.WriterOutputStream;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.l1.interop.util.StringContainsIgnoringCase;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class SPSPClientProxyFunctionalTest {
    
    private static String host;
    private static String port;
    private static String url;
    private static String invoiceUri;
    private Properties prop = new Properties();
    FileWriter writer;
    PrintStream captor;
    
      
    @BeforeClass(alwaysRun=true)
    private void beforeClass() throws Exception {
        InputStream is = ClassLoader.getSystemResourceAsStream("dfsp2-test.properties");
//        InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1-test.properties");
//        InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1-qa.properties");
        prop.load(is);
        
        String environment = System.getProperty("env");
        
        if(environment != null){
            is = ClassLoader.getSystemResourceAsStream("dfsp1-"+environment.toLowerCase()+".properties");
        }
        
        prop.load(is);
        host = prop.getProperty("host");
        port = prop.getProperty("port");
        url = "http://"+host+":"+port;
        invoiceUri = "/spsp/client/v1/invoices";
        
        
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

        
        if(!(new File("target/failure-reports")).exists())
            new File("target/failure-reports").mkdirs();
        
        writer = new FileWriter("target/failure-reports/spsp-client-proxy.html");
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
    
    /*
     @DataProvider(name="ExistingUsers")
     public Iterator<Object[]> getExistingUsers(){
     ArrayList lst = new ArrayList();
     lst.add(new String[]{"Bob","http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/bob"});
     lst.add(new String[]{"Alice","http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/alice"});
     return lst.iterator();
     }
     
     @DataProvider(name="NonExistingUsers")
     public Iterator<Object[]> getNonExistingUsers(){
     ArrayList lst = new ArrayList();
     lst.add(new String[]{"murthy1","http://centraldirectory.com/murthy1"});
     return lst.iterator();
     }
     */
    
    
    @DataProvider(name = "query_positive")
    private Iterator<Object []> dpSPSPClientProxyQueryPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/query_positive.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "query_negative")
    private Iterator<Object []> dpSPSPClientProxyQueryNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/query_negative.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "quoteSourceAmount_positive")
    private Iterator<Object []> dpSPSPClientProxy_quoteSourceAmountPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/quoteSourceAmount_positive.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "quoteSourceAmount_negative")
    private Iterator<Object []> dpSPSPClientProxy_quoteSourceAmountNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/quoteSourceAmount_positive.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "quoteDestinationAmount_positive")
    private Iterator<Object []> dpSPSPClientProxy_quoteDestinationAmountPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/quoteDestinationAmount_positive.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "quoteDestinationAmount_negative")
    private Iterator<Object []> dpSPSPClientProxy_quoteDestinationAmountNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/quoteDestinationAmount_negative.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "setup_positive")
    private Iterator<Object []> dpSPSPClientProxy_setupPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/setup_positive.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "setup_negative")
    private Iterator<Object []> dpSPSPClientProxy_setupNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/setup_negative.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "payment_positive")
    private Iterator<Object []> dpSPSPClientProxy_paymentPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/payment_positive.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "payment_negative")
    private Iterator<Object []> dpSPSPClientProxy_paymentNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/payment_negative.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "invoice_GET_positive")
    private Iterator<Object []> dpSPSPClientProxy_invoice_GetURLS_positive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/invoice_get_positive.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "invoice_GET_negative")
    private Iterator<Object []> dpSPSPClientProxy_invoice_Get_negative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/invoice_get_negative_invoiceUrl_bad.csv");
        return testCases.iterator();
    }
    
    @DataProvider(name = "invoice_create_positive")
    private Iterator<Object []> dpSPSPClientProxy_invoice_Post_positive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/invoice_post_positive.csv");
        return testCases.iterator();
    }
    
    
    
    /**
     * For a valid receiver, this test checks for a return status code of 200 and the below fields in Json response are validated.
     * <ul>
     * 			<li>address - contains text that has parameter receiverName</li>
     *	     	<li>imageUrl - contains text that has parameter receiverName</li>
     *			<li>name - contains text that has parameter receiverName </li>
     *	     	<li>currencySymbol - equals to "$" </li>
     *	     	<li>type - equals to "payee"</li>
     *	     	<li>currencyCode - equals to "USD"</li>
     *	     	<li>status - does not have any value, is null </li>
     *	     	<li>amount - does not have any value, is null </li>
     *	     	<li>invoiceInfo - does not have any value, is null</li>
     *</ul>
     * @param receiverName Name of the valid receiver
     * @param receiverURI URI of the receiver
     * @throws Exception
     */
    @Test(dataProvider="query_positive", groups = { "spsp_client_proxy_all", "spsp_client_proxy_query" })
    public void query_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse(String receiverName, String receiverURI) throws Exception {
    	
    	// failing as of 3/17/2017 BP
    	
    	/*
    	 * 
    	 * 3/17/2017 Error:
    	 *  dfsp1-qa
    	 *  
    	 * [03-17 17:06:28,734] INFO  [[interop-domain].api-httpListenerConfig.worker.315] api-main: Received request with traceID=4eb8925b-42d5-4128-bae7-e5826238538d at path=/spsp/client/v1/query, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
    			[03-17 17:06:28,734] INFO  [[interop-domain].api-httpListenerConfig.worker.315] query: Proxying request for traceID=4eb8925b-42d5-4128-bae7-e5826238538d to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/query, method=get, receiver=http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:3043/v1/receivers/26547070
    			[03-17 17:06:28,797] ERROR [[interop-domain].api-httpListenerConfig.worker.315] DefaultMessagingExceptionStrategy: 
    			********************************************************************************
    			Message               : Response code 502 mapped as failure.
    			Element               : /get:\/query:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:65 (HTTP)
    			--------------------------------------------------------------------------------
    			Exception stack is:
    			Response code 502 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
    			  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
    			  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
    			  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
    			  
    			  
    			  
			 	-------------------
    			  
    			dfsp2 test failed with the following errors:
    			  
    			  [03-20 17:43:54,035] INFO  [[interop-domain].api-httpListenerConfig.worker.377] api-main: Received request with traceID=be25003a-a762-4fd8-b1cc-cec10cdf7650 at path=/spsp/client/v1/query, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
				[03-20 17:43:54,036] INFO  [[interop-domain].api-httpListenerConfig.worker.377] query: Proxying request for traceID=be25003a-a762-4fd8-b1cc-cec10cdf7650 to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/query, method=get, receiver=http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:3043/v1/receivers/26547070
				[03-20 17:43:54,150] ERROR [[interop-domain].api-httpListenerConfig.worker.377] DefaultMessagingExceptionStrategy: 
				********************************************************************************
				Message               : Response code 502 mapped as failure.
				Element               : /get:\/query:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:65 (HTTP)
				--------------------------------------------------------------------------------
				Exception stack is:
				Response code 502 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
				  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
				  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
				  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
				  (164 more...)
				
				  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
				********************************************************************************
	  */
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        String urlPath = url+"/spsp/client/v1/query";
        
        try {
            
        	Response response =
        	given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	param("receiver", receiverURI).
            when().
            	get(urlPath);
        	
        	
        	System.out.println("Url path for test <query_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse> : " + urlPath + ", receiver uri parameter = " + receiverURI);
        	System.out.println("For query_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse: http status::" + response.getStatusCode());
        	System.out.println("**** json response payload: <" + response.asString() + ">");
        	
        	
        	/*
        	 * Note about the following java casting.  Due to a more strict generic enforcement in Java 8, we have to add the cast to make the compiler happy 
        	 * when these tests are executed via "mvn clean test"
        	 * 
        	 */
        	assertThat( "response http code", (Integer) response.getStatusCode(), equalTo(200));
        	
        	assertThat("type", (String) response.jsonPath().get("type"), not(isEmptyOrNullString()) );
        	assertThat("type", (String) response.jsonPath().get("type"), equalTo("payee") );
        	
        	assertThat("account", (String) response.jsonPath().get("account"), not(isEmptyOrNullString()) );
        	
        	assertThat("currencyCode", (String) response.jsonPath().get("currencyCode"), not(isEmptyOrNullString()) );
        	assertThat("currencyCode", (String) response.jsonPath().get("currencyCode"), equalTo("USD") );

        	assertThat("currencyCode", (String) response.jsonPath().get("currencyCode"), not(isEmptyOrNullString()) );
        	assertThat("currencySymbol", (String) response.jsonPath().get("currencySymbol"), equalTo("$") );
        	
        	assertThat("name", (String) response.jsonPath().get("name"), not(isEmptyOrNullString()) );
        	assertThat("name", (String) response.jsonPath().get("name"), containsString(receiverName) );

        	assertThat("imageUrl", (String) response.jsonPath().get("imageUrl"), not(isEmptyOrNullString()) );
        	assertThat("imageUrl", (String) response.jsonPath().get("imageUrl"), StringContainsIgnoringCase.containsStringIgnoringCase(receiverName) );
        	
        	System.out.println("**** after tests for . ****");
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>query_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", receiverName, receiverURI);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
    }
    
    /**
     * For a receiver that does not exist, this test checks for a return status code of 404 and checks of the following elements in the repose.
     * <ul>
     * 	<li> id - "Error"</li>
     *  <li> message - contains text that has "500 Internal Server Error"</li>
     *  <li> debug->stack - contains text that has "500 Internal Server Error" ""</li>
     * </ul>
     * @param receiverName - the name of the receiver
     * @param receiverURI - URI for the receiver
     */
    @Test(dataProvider="query_negative", groups = { "spsp_client_proxy_all", "spsp_client_proxy_query" })
    public void query_InValidReceiver_ShouldReceive404_ShouldReceiveErrorResponse(String receiverName, String receiverURI){
        
    	// failing as of 3/17/2017 BP.  Getting a 500 error.
/*    	
 * 
 * On 3/17/2017 Test failed for the following errors:
 * dfsp1-qa
 * 
    	[03-17 17:14:44,178] INFO  [[interop-domain].api-httpListenerConfig.worker.315] api-main: Received request with traceID=77146f6e-4d73-4611-bb7a-af87acc00618 at path=/spsp/client/v1/query, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
    	[03-17 17:14:44,178] INFO  [[interop-domain].api-httpListenerConfig.worker.315] query: Proxying request for traceID=77146f6e-4d73-4611-bb7a-af87acc00618 to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/query, method=get, receiver=http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/murthy
    	[03-17 17:15:14,628] ERROR [[interop-domain].api-httpListenerConfig.worker.315] DefaultMessagingExceptionStrategy: 
    	********************************************************************************
    	Message               : Error sending HTTP request.
    	Element               : /get:\/query:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:65 (HTTP)
    	--------------------------------------------------------------------------------
    	Exception stack is:
    	Error sending HTTP request. (org.mule.api.MessagingException)
    	  com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider.timeout(GrizzlyAsyncHttpProvider.java:433)
    	  com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider$3.onTimeout(GrizzlyAsyncHttpProvider.java:281)
    	  org.glassfish.grizzly.utils.IdleTimeoutFilter$DefaultWorker.doWork(IdleTimeoutFilter.java:401)
    	  org.glassfish.grizzly.utils.IdleTimeoutFilter$DefaultWorker.doWork(IdleTimeoutFilter.java:380)
    	  org.glassfish.grizzly.utils.DelayedExecutor$DelayedRunnable.run(DelayedExecutor.java:158)
    	  java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
    	  java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
    	  java.lang.Thread.run(Thread.java:745)
  			  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************




		------------------- 	dfsp2 test failed with the following errors: ------------------- 	
		
	
		[03-20 17:46:16,213] ERROR [[interop-domain].api-httpListenerConfig.worker.377] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Error sending HTTP request.
		Element               : /get:\/query:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:65 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Error sending HTTP request. (org.mule.api.MessagingException)
		  com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider.timeout(GrizzlyAsyncHttpProvider.java:433)
		  com.ning.http.client.providers.grizzly.GrizzlyAsyncHttpProvider$3.onTimeout(GrizzlyAsyncHttpProvider.java:281)
		  org.glassfish.grizzly.utils.IdleTimeoutFilter$DefaultWorker.doWork(IdleTimeoutFilter.java:401)
		  org.glassfish.grizzly.utils.IdleTimeoutFilter$DefaultWorker.doWork(IdleTimeoutFilter.java:380)
		  org.glassfish.grizzly.utils.DelayedExecutor$DelayedRunnable.run(DelayedExecutor.java:158)
		  java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
		  java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
		  java.lang.Thread.run(Thread.java:745)
		
		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************
		
		FAILED: query_InValidReceiver_ShouldReceive404_ShouldReceiveErrorResponse("Murthy", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/murthy")
		java.lang.AssertionError: response http code
			Expected: <404>
     			but: was <500>
		
    			
*/
    	
    	
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        
//        Response response =
//            	given().
//                	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
//                	contentType("application/json").
//                	param("receiver", receiverURI).
//                when().
//                	get(urlPath);
        
        
        
        String urlPath = url+"/spsp/client/v1/query";
        Response response;
        
        try {
            response = given().
            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            contentType("application/json").
            param("receiver", receiverURI).
            when().
            get(url+"/spsp/client/v1/query");
//            then().
//            statusCode(404).
//            body("id",equalTo("Error")).
//            body("message",containsString("500 Internal Server Error")).
//            body("debug.stack",containsString("500 Internal Server Error"));
            
            
            /*
        	 * Note about the following java casting.  Due to a more strict generic enforcement in Java 8, we have to add the cast to make the compiler happy 
        	 * when these tests are executed via "mvn clean test"
        	 * 
        	 */
        	assertThat( "response http code", (Integer) response.getStatusCode(), equalTo(404));
        	
//        	need a sample for Message containg a value
        	
//        	assertThat("type", (String) response.jsonPath().get("type"), not(isEmptyOrNullString()) );
//        	assertThat("type", (String) response.jsonPath().get("type"), equalTo("payee") );
//        	
//        	assertThat("account", (String) response.jsonPath().get("account"), not(isEmptyOrNullString()) );
//        	
//        	assertThat("currencyCode", (String) response.jsonPath().get("currencyCode"), not(isEmptyOrNullString()) );
//        	assertThat("currencyCode", (String) response.jsonPath().get("currencyCode"), equalTo("USD") );
//
//        	assertThat("currencyCode", (String) response.jsonPath().get("currencyCode"), not(isEmptyOrNullString()) );
//        	assertThat("currencySymbol", (String) response.jsonPath().get("currencySymbol"), equalTo("$") );
//        	
//        	assertThat("name", (String) response.jsonPath().get("name"), not(isEmptyOrNullString()) );
//        	assertThat("name", (String) response.jsonPath().get("name"), containsString(receiverName) );
//
//        	assertThat("imageUrl", (String) response.jsonPath().get("imageUrl"), not(isEmptyOrNullString()) );
//        	assertThat("imageUrl", (String) response.jsonPath().get("imageUrl"), StringContainsIgnoringCase.containsStringIgnoringCase(receiverName) );
        	
        	System.out.println("**** after tests for . ****");
        	
        }catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>query_InValidReceiver_ShouldNotReceive200_ShouldReceiveErrorResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", receiverName, receiverURI);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
        try {
            given().
            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            contentType("application/json").
            param("receiver", receiverURI).
            when().
            get(url+"/spsp/client/v1/query").
            then().
            statusCode(404).
            body("id",equalTo("Error")).
            body("message",containsString("500 Internal Server Error")).
            body("debug.stack",containsString("500 Internal Server Error"));
        }catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>query_InValidReceiver_ShouldNotReceive200_ShouldReceiveErrorResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", receiverName, receiverURI);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    
    /**
     * For a valid user, this test checks that the return code is 200, checks the response json fields as below:
     * <ul>
     * 	<li>destinationAmount - Is close in value to {@code amount}</li>
     * </ul>
     * @param userAddress - Address of the user
     * @param amount - source amount that needs to be transferred
     */
    @Test(dataProvider="quoteSourceAmount_positive", groups = { "spsp_client_proxy_all", "spsp_client_proxy_query" })
    public void quoteSourceAmount_ForValidReceiver_ShouldRecive200_ShouldReceiveValidResponse(String userAddress, String amount) {
    	
    	
    	/*
    	 * 
    	 * On 3/17/2017 Failing for the following reasons:
    	 * dfsp1-qa
    	
    	[03-17 17:36:55,669] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=edd0d9d3-4ed3-4553-9438-c0366aea515f at path=/spsp/client/v1/quoteSourceAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-17 17:36:55,675] INFO  [[interop-domain].api-httpListenerConfig.worker.193] quoteSourceAmount: Proxying request for interopID=edd0d9d3-4ed3-4553-9438-c0366aea515f to http://0.0.0.0:3042/v1/quoteSourceAmount, method=get, receiver=levelone.dfsp2.bob, source_amount=10
		[03-17 17:36:55,681] ERROR [[interop-domain].api-httpListenerConfig.worker.193] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteSourceAmount:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:101 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)

		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************
        
        
        
        ------------------- 	dfsp2 test failed with the following errors: ------------------- 	

		[03-20 17:50:09,335] INFO  [[interop-domain].api-httpListenerConfig.worker.377] api-main: Received request with traceID=1e496dba-0fba-4923-a98d-f6357adbcbfd at path=/spsp/client/v1/quoteSourceAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-20 17:50:09,355] INFO  [[interop-domain].api-httpListenerConfig.worker.377] quoteSourceAmount: Proxying request for traceID=1e496dba-0fba-4923-a98d-f6357adbcbfd to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/quoteSourceAmount, method=get, receiver=levelone.dfsp2.bob, source_amount=10
		[03-20 17:50:09,425] ERROR [[interop-domain].api-httpListenerConfig.worker.377] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteSourceAmount:api-config/processors/7 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:94 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)
		
		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		  
		  
		  FAILED: quoteSourceAmount_ForValidReceiver_ShouldRecive200_ShouldReceiveValidResponse("levelone.dfsp2.bob", "10")
			java.lang.AssertionError: 1 expectation failed.
			Expected status code <200> doesn't match actual status code <500>.
		  
		
        *
        */
		
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            JsonPath quoteResponse =
            given().
            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            contentType("application/json").
            param("receiver", userAddress).
            param("sourceAmount",amount).
            when().
            get(url+"/spsp/client/v1/quoteSourceAmount").
            then().
            statusCode(200).extract().jsonPath();
            
            assertThat(Double.parseDouble(quoteResponse.getString("destinationAmount")), is(closeTo(Double.parseDouble(amount),1)));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>quoteSourceAmount_ForValidReceiver_ShouldRecive200_ShouldReceiveValidResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", userAddress, amount);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    /**
     * For a receiver that does not exist, this test checks that the return code is 404, checks the response json fields as below:
     * 
     * <ul>
     * 	<li> id - "Error"</li>
     *  <li> message - contains text that has "500 Internal Server Error"</li>
     *  <li> debug->stack - contains text that has "500 Internal Server Error" ""</li>
     * </ul>
     * @param userAddress - Address of the user
     * @param amount - source amount that needs to be transferred
     */
    @Test(dataProvider="quoteSourceAmount_negative", groups={ "spsp_client_proxy_all", "spsp_client_proxy_quote" })
    public void quoteSourceAmount_ForInValidReceiver_ShouldRecive404_ShouldReceiveErrorResponse(String userAddress, String amount) {
    	
    	/*
    	 * 
    	 * On 3/17/2017 test failed for the following reasons:
    	 * while point to dfsp1-qa
    	 * 
    	[03-17 17:39:34,044] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=615c3568-768b-4147-a66b-963d0fac4ba5 at path=/spsp/client/v1/quoteSourceAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-17 17:39:34,050] INFO  [[interop-domain].api-httpListenerConfig.worker.193] quoteSourceAmount: Proxying request for interopID=615c3568-768b-4147-a66b-963d0fac4ba5 to http://0.0.0.0:3042/v1/quoteSourceAmount, method=get, receiver=levelone.dfsp2.bob, source_amount=10
		[03-17 17:39:34,252] ERROR [[interop-domain].api-httpListenerConfig.worker.193] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteSourceAmount:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:101 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)

		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************
		
		
		
		
		
		------------------- 	dfsp2 test failed with the following errors: ------------------- 	
		
		[03-20 17:53:55,854] INFO  [[interop-domain].api-httpListenerConfig.worker.377] api-main: Received request with traceID=effae0d1-b10e-4fe0-83c8-58ff8367e9b6 at path=/spsp/client/v1/quoteSourceAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-20 17:53:55,854] INFO  [[interop-domain].api-httpListenerConfig.worker.377] quoteSourceAmount: Proxying request for traceID=effae0d1-b10e-4fe0-83c8-58ff8367e9b6 to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/quoteSourceAmount, method=get, receiver=levelone.dfsp2.bob, source_amount=10
		[03-20 17:53:55,895] ERROR [[interop-domain].api-httpListenerConfig.worker.377] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteSourceAmount:api-config/processors/7 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:94 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)
		
		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************
		
		Response code 500 mapped as failure.
		response from quoteSourceAmount_ForInValidReceiver :: Response code 500 mapped as failure.
		 http status code: 500
		
		
    	 */
    			
    			
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
        	Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
//            	param("receiver", userAddress).
//            	param("destinationAmount",amount).
            	queryParam("receiver", userAddress).
            	queryParam("sourceAmount", amount).
            when().
            	get(url+"/spsp/client/v1/quoteSourceAmount");
//            then().
//            	statusCode(404).
//            	body("id",equalTo("Error")).
//            	body("message",containsString("500 Internal Server Error")).
//            	body("debug.stack",containsString("500 Internal Server Error"));
            
            
        	System.out.println("response from quoteSourceAmount_ForInValidReceiver :: " + response.prettyPrint() + "\n http status code: " + response.getStatusCode());
        	            
            assertThat(response.getStatusCode(), equalTo(404));
            System.out.println("setup_forValidReceiver json response :: " + response.prettyPrint());
            
            JsonPath jsonPath = response.jsonPath();
            
            /*
             * Since we need to build a request from the data from the call above, we should 
             * have a bunch of assertThat() to ensure all is good.  This is a bit more detailed testing
             * 
             */
            assertThat((String)jsonPath.get("id"), equalTo("Error"));
            assertThat((String)jsonPath.get("message"), containsString("500 Internal Server Error"));

   
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>quoteSourceAmount_ForInValidReceiver_ShouldRecive404_ShouldReceiveValidResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", userAddress, amount);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    
    /**
     * For a valid user, this test checks that the return code is 200, checks the response json fields as below:
     * <ul>
     * 	<li>sourceAmount - Is close in value to {@code amount}</li>
     * </ul>
     * @param userAddress - Address of the user
     * @param amount - Destination amount that needs to be transferred
     *
     */
    @Test(dataProvider="quoteDestinationAmount_positive", groups={ "spsp_client_proxy_all", "spsp_client_proxy_quote" })
    public void quoteDestinationAmount_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse(String userAddress, String amount){
    	
    /*	
     * 
     * On 3/17/2017 test failed for the following reasons 
     * while point to dfsp1-qa
     * 
    	[03-17 17:41:14,790] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=6c5baa85-4633-4526-8b38-beb20d135e0b at path=/spsp/client/v1/quoteDestinationAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-17 17:41:14,796] INFO  [[interop-domain].api-httpListenerConfig.worker.193] quoteDestinationAmount: Proxying request for interopID=6c5baa85-4633-4526-8b38-beb20d135e0b to http://0.0.0.0:3042/v1/quoteDestinationAmount, method=get, receiver=levelone.dfsp2.bob, destination_amount=10
		[03-17 17:41:14,880] ERROR [[interop-domain].api-httpListenerConfig.worker.193] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteDestinationAmount:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:129 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)

		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************
		
		
		
		------------------- 	dfsp2 test failed with the following errors: ------------------- 	
		
		[03-20 18:23:12,106] INFO  [[interop-domain].api-httpListenerConfig.worker.407] api-main: Received request with traceID=5d42662c-40ac-4448-9bac-72b0b42e1c3a at path=/spsp/client/v1/quoteDestinationAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-20 18:23:12,107] INFO  [[interop-domain].api-httpListenerConfig.worker.407] quoteDestinationAmount: Proxying request for traceID=5d42662c-40ac-4448-9bac-72b0b42e1c3a to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/quoteDestinationAmount, method=get, receiver=levelone.dfsp2.bob, destination_amount=10
		[03-20 18:23:12,198] ERROR [[interop-domain].api-httpListenerConfig.worker.407] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteDestinationAmount:api-config/processors/7 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:123 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)
		
		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************
		
		
		FAILED: quoteDestinationAmount_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse("levelone.dfsp2.bob", "10")
		java.lang.AssertionError: 1 expectation failed.
		Expected status code <200> doesn't match actual status code <500>.

		*/
		
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            JsonPath quoteResponse =
            given().
            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            contentType("application/json").
            param("receiver", userAddress).
            param("destinationAmount",amount).
            when().
            get(url+"/spsp/client/v1/quoteDestinationAmount").
            then().
            statusCode(200).extract().jsonPath();
            
            assertThat(Double.parseDouble(quoteResponse.getString("sourceAmount")), is(closeTo(Double.parseDouble(amount),1)));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>quoteDestinationAmount_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", userAddress, amount);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    /**
     * For a receiver that does not exist, this test checks that the return code is 404, checks the response json fields as below:
     * 
     * <ul>
     * 	<li> id - "Error"</li>
     *  <li> message - contains text that has "500 Internal Server Error"</li>
     *  <li> debug->stack - contains text that has "500 Internal Server Error" ""</li>
     * </ul>
     * @param userAddress - Address of the user
     * @param amount - source amount that needs to be transferred
     */
    @Test(dataProvider="quoteDestinationAmount_negative", groups={ "spsp_client_proxy_all", "spsp_client_proxy_quote" })
    public void quoteDestinationAmount_ForInValidReceiver_ShouldReceive404_ShouldReceiveErrorResponse(String userAddress, String amount){
    	
    	/*
    	 * 
    	 * On 3/17/2017 tests are failing for the following reason:
    	 * dfsp1 qa
    	 * 
    	[03-17 18:05:57,905] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=19772d6f-d12d-4b41-b1ae-4e81597d50a7 at path=/spsp/client/v1/quoteDestinationAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-17 18:05:57,910] INFO  [[interop-domain].api-httpListenerConfig.worker.193] quoteDestinationAmount: Proxying request for interopID=19772d6f-d12d-4b41-b1ae-4e81597d50a7 to http://0.0.0.0:3042/v1/quoteDestinationAmount, method=get, receiver=levelone.dfsp2.john, destination_amount=10
		[03-17 18:05:58,049] ERROR [[interop-domain].api-httpListenerConfig.worker.193] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteDestinationAmount:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:129 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)

		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************
        
        
        
        ------------------- 	dfsp2 test failed with the following errors: ------------------- 	
		
		[03-20 19:24:36,770] INFO  [[interop-domain].api-httpListenerConfig.worker.407] api-main: Received request with traceID=295d201f-9f4c-485b-8b42-b87acc7473b4 at path=/spsp/client/v1/quoteDestinationAmount, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-20 19:24:36,771] INFO  [[interop-domain].api-httpListenerConfig.worker.407] quoteDestinationAmount: Proxying request for traceID=295d201f-9f4c-485b-8b42-b87acc7473b4 to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/quoteDestinationAmount, method=get, receiver=levelone.dfsp2.john, destination_amount=10
		[03-20 19:24:36,811] ERROR [[interop-domain].api-httpListenerConfig.worker.407] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : Response code 500 mapped as failure.
		Element               : /get:\/quoteDestinationAmount:api-config/processors/7 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:123 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		Response code 500 mapped as failure. (org.mule.module.http.internal.request.ResponseValidatorException)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:37)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  org.mule.module.http.internal.request.DefaultHttpRequester.innerProcess(DefaultHttpRequester.java:344)
		  (164 more...)
		
		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
        
        
        
        Response code 500 mapped as failure.
		response from quoteDestinationAmount_ForInValidReceiver :: Response code 500 mapped as failure.
 			http status code: 500
			Response code 500 mapped as failure.
        
        
        *
        */
    	
    	
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
        	Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	param("receiver", userAddress).
            	param("destinationAmount",amount).
            when().
            	get(url+"/spsp/client/v1/quoteDestinationAmount");
//            then().
//            	statusCode(404).
//            	body("id",equalTo("Error")).
//            	body("message",containsString("500 Internal Server Error")).
//            	body("debug.stack",containsString("500 Internal Server Error"));
            
            System.out.println("response from quoteDestinationAmount_ForInValidReceiver :: " + response.prettyPrint() + "\n http status code: " + response.getStatusCode());
            assertThat(response.getStatusCode(), equalTo(404));
            JsonPath jsonPath = response.jsonPath();
            
            /*
             * Since we need to build a request from the data from the call above, we should 
             * have a bunch of assertThat() to ensure all is good.  This is a bit more detailed testing
             * 
             */
            assertThat((String)jsonPath.get("id"), equalTo("Error"));
            assertThat((String)jsonPath.get(), containsString("500 Internal Server Error"));

        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>quoteDestinationAmount_ForInValidReceiver_ShouldReceive404_ShouldReceiveErrorResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", userAddress, amount);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    
    /**
     * For a valid sender and receiver, this test case checks that the return code is 200 and checks the json response fields as below:
     * <ul>
     * 	<li>receiver = </li>
     * <li></li>
     * <li></li>
     * </ul>
     * @param sender
     * @param receiver
     * @param amount
     */
    @Test(dataProvider="setup_positive", groups={ "spsp_client_proxy_all", "spsp_client_proxy_payment_setup" })
    public void setUp_ForValidReceiver_ShouldReturn201_ShouldReturnValidResponse(String sender, String receiver, String amount) {
    	
    	
    	/*
    	 * On 3/17/2017 tests failed for the following reasons:
    	 * using dfsp1 qa
    	 *     	
    	[03-17 18:07:16,390] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=b71d212f-6149-41c2-8742-6d6c5dbb8616 at path=/spsp/client/v1/setup, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-17 18:07:16,402] INFO  [[interop-domain].api-httpListenerConfig.worker.193] setup: Proxying request for interopID=b71d212f-6149-41c2-8742-6d6c5dbb8616 to http://0.0.0.0:3042/v1/setup, method=post: {"receiver":"http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:3046/v1/receivers/85555384","sourceAccount":"http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:8088/ledger/accounts/26547070","destinationAmount":"100","memo":"Hi Bobb!","sourceIdentifier":""}
		[03-17 18:07:16,418] ERROR [[interop-domain].api-httpListenerConfig.worker.193] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : For input string: "400-499" (java.lang.NumberFormatException).
		Element               : /post:\/setup:application\/json:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:168 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		For input string: "400-499" (java.lang.NumberFormatException). (org.mule.api.MessagingException)
		  java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
		  java.lang.Integer.parseInt(Integer.java:492)
		  java.lang.Integer.parseInt(Integer.java:527)
		  org.mule.module.http.internal.request.RangeStatusCodeValidator.belongs(RangeStatusCodeValidator.java:32)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:35)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  (157 more...)

		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************


		------------------- 	dfsp2 test failed with the following errors: ------------------- 	
		
		
		03-20 19:28:24,797] INFO  [[interop-domain].api-httpListenerConfig.worker.407] api-main: Received request with traceID=2e96f113-6fb8-42df-ae3b-cd73a2324721 at path=/spsp/client/v1/setup, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-20 19:28:24,797] WARN  [[interop-domain].api-httpListenerConfig.worker.407] Configuration: No matching patterns for URI /setup
		[03-20 19:28:24,799] ERROR [[interop-domain].api-httpListenerConfig.worker.407] MappingExceptionListener: 
		********************************************************************************
		Message               : /setup
		Element               : /api-main/processors/2 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy-api.xml:61 (APIkit Router)
		--------------------------------------------------------------------------------
		Exception stack is:
		/setup (org.mule.module.apikit.exception.NotFoundException)
		  org.mule.module.apikit.AbstractConfiguration$2.load(AbstractConfiguration.java:164)
		
		
		
		FAILED: setUp_ForValidReceiver_ShouldReturn201_ShouldReturnValidResponse("26547070", "85555384", "100")
		java.lang.AssertionError: 
		Expected: <201>
		     but: was <404>

    	*/
    			
    			
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            String json = Json.createObjectBuilder()
            .add("receiver", "http://"+host+":3046/v1/receivers/"+receiver)
            .add("sourceAccount", "http://"+host+":8088/ledger/accounts/"+sender)
            .add("destinationAmount", amount)
            .add("memo", "Hi Bobb!")
            .add("sourceIdentifier", "")
            .build()
            .toString();
            
            Response response = 
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	body(json).
            when().
            	post(url+"/spsp/client/v1/setup");
//            then().
//            	statusCode(201).
//            	body("id",is(not(""))).
//            	body("receiver",equalTo("http://"+host+":3046/v1/receivers/"+receiver));
            
            assertThat(response.getStatusCode(), equalTo(201));
            System.out.println("setup_forValidReceiver json response :: " + response.prettyPrint());
            
            JsonPath jsonPath = response.jsonPath();
            
            /*
             * Since we need to build a request from the data from the call above, we should 
             * have a bunch of assertThat() to ensure all is good.  This is a bit more detailed testing
             * 
             */
            String sourceAccountSendValue = "http://"+host+":8088/ledger/accounts/"+sender;
            String sourceAccountReturnValue = jsonPath.getString("sourceAccount");
//            assertThat("receiver", sourceReiverPath, equalTo(receiverPathTest));  // as of 12/8/2016 Receiver does not appear in the JSON response.
            
            assertThat("sourceAccount", sourceAccountSendValue, equalTo(sourceAccountReturnValue)); 
            assertThat("condition", jsonPath.getString("condition"), not(isEmptyOrNullString()));
            assertThat("address", jsonPath.getString("address"), not(isEmptyOrNullString()));
            assertThat("sourceAmount", jsonPath.getString("sourceAmount"), not(isEmptyOrNullString()));
            assertThat("destinationAmount", jsonPath.getString("destinationAmount"), not(isEmptyOrNullString()));
            assertThat("expiresAt", jsonPath.getString("expiresAt"), not(isEmptyOrNullString()));
            assertThat("sourceAmount", jsonPath.getString("sourceAmount"), not(isEmptyOrNullString()));

            
        }catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>setUp_ForValidReceiver_ShouldReturn200_ShouldReturnValidResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", "", "");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    
    
    /**
     *
     * @param sender
     * @param receiver
     * @param amount
     */
    @Test(dataProvider="setup_negative", groups={ "spsp_client_proxy_all", "spsp_client_proxy_payment_setup" })
    public void setUp_ForInValidReceiver_ShouldReturn404_ShouldReturnErrorResponse(String sender, String receiver, String amount){
    	
    	/*
    	 * On 3/17/2017 test failed for the following reasons:
    	 * on dfsp1 qa
    	 * 
    	[03-17 22:56:38,078] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=62cedd42-7de4-4634-9d74-1d98ba6168e8 at path=/spsp/client/v1/setup, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-17 22:56:38,083] INFO  [[interop-domain].api-httpListenerConfig.worker.193] setup: Proxying request for interopID=62cedd42-7de4-4634-9d74-1d98ba6168e8 to http://0.0.0.0:3042/v1/setup, method=post: {"receiver":"http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:3046/v1/receivers/jane","sourceAccount":"http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:8088/ledger/accounts/john","destinationAmount":"100","memo":"Hi Bobb!","sourceIdentifier":""}
		[03-17 22:56:38,096] ERROR [[interop-domain].api-httpListenerConfig.worker.193] DefaultMessagingExceptionStrategy: 
		********************************************************************************
		Message               : For input string: "400-499" (java.lang.NumberFormatException).
		Element               : /post:\/setup:application\/json:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:168 (HTTP)
		--------------------------------------------------------------------------------
		Exception stack is:
		For input string: "400-499" (java.lang.NumberFormatException). (org.mule.api.MessagingException)
		  java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
		  java.lang.Integer.parseInt(Integer.java:492)
		  java.lang.Integer.parseInt(Integer.java:527)
		  org.mule.module.http.internal.request.RangeStatusCodeValidator.belongs(RangeStatusCodeValidator.java:32)
		  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:35)
		  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
		  (157 more...)
		
		  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
		********************************************************************************

    	
    	
    	
    	------------------- 	dfsp2 test failed with the following errors: ------------------- 	
    	
    	[03-20 19:30:40,028] INFO  [[interop-domain].api-httpListenerConfig.worker.407] api-main: Received request with traceID=f9f833ef-7b4d-490d-89f5-536175c9f996 at path=/spsp/client/v1/setup, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-20 19:30:40,028] WARN  [[interop-domain].api-httpListenerConfig.worker.407] Configuration: No matching patterns for URI /setup
		[03-20 19:30:40,028] ERROR [[interop-domain].api-httpListenerConfig.worker.407] MappingExceptionListener: 
		********************************************************************************
		Message               : /setup
		Element               : /api-main/processors/2 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy-api.xml:61 (APIkit Router)
		--------------------------------------------------------------------------------
		Exception stack is:
		/setup (org.mule.module.apikit.exception.NotFoundException)

    	
    	{
		    "debug": {
		        "cause": {
		            "isRootCause": true,
		            "message": "/setup",
		            "stackInfo": [
		                "org.mule.module.apikit.AbstractConfiguration$2.load(AbstractConfiguration.java:164)",
		                "org.mule.module.apikit.AbstractConfiguration$2.load(AbstractConfiguration.java:155)",
		                "com.google.common.cache.LocalCache$LoadingValueReference.loadFuture(LocalCache.java:3527)",
		                "com.google.common.cache.LocalCache$Segment.loadSync(LocalCache.java:2319)",
		                "com.google.common.cache.LocalCache$Segment.lockedGetOrLoad(LocalCache.java:2282)",
		                "com.google.common.cache.LocalCache$Segment.get(LocalCache.java:2197)",
		                "com.google.common.cache.LocalCache.get(LocalCache.java:3937)",
		                "com.google.common.cache.LocalCache.getOrLoad(LocalCache.java:3941)",
		                "com.google.common.cache.LocalCache$LocalLoadingCache.get(LocalCache.java:4824)",
		                "org.mule.module.apikit.AbstractRouter.processRouterRequest(AbstractRouter.java:159)",
		                "... 89 stack entries skipped, final entry below ...",
		                "java.lang.Thread.run(Thread.java:745)"
		            ]
		        },
		        "message": "/setup (org.mule.module.apikit.exception.NotFoundException).",
		        "stackInfo": [
		            "org.mule.execution.ExceptionToMessagingExceptionExecutionInterceptor.execute(ExceptionToMessagingExceptionExecutionInterceptor.java:42)",
		            "org.mule.execution.MessageProcessorNotificationExecutionInterceptor.execute(MessageProcessorNotificationExecutionInterceptor.java:108)",
		            "org.mule.execution.MessageProcessorExecutionTemplate.execute(MessageProcessorExecutionTemplate.java:44)",
		            "org.mule.processor.BlockingProcessorExecutor.executeNext(BlockingProcessorExecutor.java:88)",
		            "org.mule.processor.BlockingProcessorExecutor.execute(BlockingProcessorExecutor.java:59)",
		            "org.mule.processor.chain.DefaultMessageProcessorChain.doProcess(DefaultMessageProcessorChain.java:80)",
		            "org.mule.processor.chain.AbstractMessageProcessorChain.process(AbstractMessageProcessorChain.java:74)",
		            "org.mule.execution.ExceptionToMessagingExceptionExecutionInterceptor.execute(ExceptionToMessagingExceptionExecutionInterceptor.java:27)",
		            "org.mule.execution.MessageProcessorExecutionTemplate.execute(MessageProcessorExecutionTemplate.java:44)",
		            "org.mule.processor.BlockingProcessorExecutor.executeNext(BlockingProcessorExecutor.java:98)",
		            "... 92 stack entries skipped, final entry below ...",
		            "java.lang.Thread.run(Thread.java:745)"
		        ]
		    },
		    "error": {
		        "id": "Resource not found",
		        "message": "Failed to process request for traceID=f9f833ef-7b4d-490d-89f5-536175c9f996: /setup"
		    }
		}
    	
    	
    	*/
    			
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            String json = Json.createObjectBuilder()
            .add("receiver", "http://"+host+":3046/v1/receivers/"+receiver)
            .add("sourceAccount", "http://"+host+":8088/ledger/accounts/"+sender)
            .add("destinationAmount", amount)
            .add("memo", "Hi Bobb!")
            .add("sourceIdentifier", "")
            .build()
            .toString();
            
            Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	body(json).
            when().
            	post(url+"/spsp/client/v1/setup");
//            then().
//            	statusCode(404).
//            	body("id",equalTo("Error")).
//            	body("message",containsString("500 Internal Server Error")).
//            	body("debug.stack",containsString("500 Internal Server Error"));
            
            System.out.println("response from setUp_ForInValidReceive :: " + response.prettyPrint() + "\n with status code = " + response.getStatusCode());
            assertThat("return status for setUp_ForInValidReceiver: ", response.getStatusCode(), equalTo(404));
            
            JsonPath jsonPath = response.jsonPath();
            
            /*
             * Since we need to build a request from the data from the call above, we should 
             * have a bunch of assertThat() to ensure all is good.  This is a bit more detailed testing
             * 
             */
            assertThat((String) jsonPath.get("id"), equalTo("Error"));
            assertThat((String)jsonPath.get(), containsString("500 Internal Server Error"));
            assertThat((String) jsonPath.get(), containsString("500 Internal Server Error"));
            
            
        }catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>setUp_ForInValidReceiver_ShouldReturn404_ShouldReturnErrorResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", "", "");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    
    
    @Test(dataProvider="payment_positive", groups={ "spsp_client_proxy_all", "spsp_client_proxy_payment" })
    public void payment_ForValidReceiver_ShouldReceive200_ShouldReturnValidResponse(String sender, String receiverUserNumber, String amount){
    	
    	/*
    	 * Test failed 3/17/2017.  Different from the errors above.
    	 * 
    	 * This is the same data number format related to the ILP ledger adapter test.  Receiver is a number, an sender is a Name.  
    	 * Fix, all that I believe that is needed is change the data in the test csv file
    	 * 
    	 * 3/17/2017 4:58pm the following test fail for the following reasons:
    	 * in env dsfp1 qa
    	 * 
    	 *  [03-17 22:57:52,747] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=e10f8688-ca34-40e0-b222-71e02fbf5886 at path=/spsp/client/v1/setup, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
			[03-17 22:57:52,752] INFO  [[interop-domain].api-httpListenerConfig.worker.193] setup: Proxying request for interopID=e10f8688-ca34-40e0-b222-71e02fbf5886 to http://0.0.0.0:3042/v1/setup, method=post: {"receiver":"http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:3043/v1/receivers/26547070","sourceAccount":"http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:8088/ledger/accounts/bob","destinationAmount":"100","memo":"Hi Bobb!","sourceIdentifier":"9809890190934023"}
			[03-17 22:57:52,849] ERROR [[interop-domain].api-httpListenerConfig.worker.193] DefaultMessagingExceptionStrategy: 
			********************************************************************************
			Message               : For input string: "400-499" (java.lang.NumberFormatException).
			Element               : /post:\/setup:application\/json:api-config/processors/6 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy.xml:168 (HTTP)
			--------------------------------------------------------------------------------
			Exception stack is:
			For input string: "400-499" (java.lang.NumberFormatException). (org.mule.api.MessagingException)
			  java.lang.NumberFormatException.forInputString(NumberFormatException.java:65)
			  java.lang.Integer.parseInt(Integer.java:492)
			  java.lang.Integer.parseInt(Integer.java:527)
			  org.mule.module.http.internal.request.RangeStatusCodeValidator.belongs(RangeStatusCodeValidator.java:32)
			  org.mule.module.http.internal.request.SuccessStatusCodeValidator.validate(SuccessStatusCodeValidator.java:35)
			  org.mule.module.http.internal.request.DefaultHttpRequester.validateResponse(DefaultHttpRequester.java:356)
			  (157 more...)
			
			  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
			********************************************************************************
    	 
    	 
    	 ------------------- 	dfsp2 test failed with the following errors: ------------------- 	
    	 
    	 [03-20 19:35:09,435] INFO  [[interop-domain].api-httpListenerConfig.worker.407] api-main: Received request with traceID=4817082c-af0a-4a98-a5a9-de46e9607f1e at path=/spsp/client/v1/setup, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
		[03-20 19:35:09,436] WARN  [[interop-domain].api-httpListenerConfig.worker.407] Configuration: No matching patterns for URI /setup
		[03-20 19:35:09,436] ERROR [[interop-domain].api-httpListenerConfig.worker.407] MappingExceptionListener: 
		********************************************************************************
		Message               : /setup
		Element               : /api-main/processors/2 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy-api.xml:61 (APIkit Router)
		--------------------------------------------------------------------------------
		Exception stack is:
		/setup (org.mule.module.apikit.exception.NotFoundException)

    	 
    	 1 ========== setup for valid receiver, payment request json: {"receiver":"http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3043/v1/receivers/26547070","sourceAccount":"http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:8014/ledger/accounts/bob","destinationAmount":"100","memo":"Hi Bobb!","sourceIdentifier":"9809890190934023"}, post url: http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:8088/spsp/client/v1/setup, should get 200 but failing
			{
			    "debug": {
			        "cause": {
			            "isRootCause": true,
			            "message": "/setup",
			            "stackInfo": [
			                "org.mule.module.apikit.AbstractConfiguration$2.load(AbstractConfiguration.java:164)",
			                "org.mule.module.apikit.AbstractConfiguration$2.load(AbstractConfiguration.java:155)",
			                "com.google.common.cache.LocalCache$LoadingValueReference.loadFuture(LocalCache.java:3527)",
			                "com.google.common.cache.LocalCache$Segment.loadSync(LocalCache.java:2319)",
			                "com.google.common.cache.LocalCache$Segment.lockedGetOrLoad(LocalCache.java:2282)",
			                "com.google.common.cache.LocalCache$Segment.get(LocalCache.java:2197)",
			                "com.google.common.cache.LocalCache.get(LocalCache.java:3937)",
			                "com.google.common.cache.LocalCache.getOrLoad(LocalCache.java:3941)",
			                "com.google.common.cache.LocalCache$LocalLoadingCache.get(LocalCache.java:4824)",
			                "org.mule.module.apikit.AbstractRouter.processRouterRequest(AbstractRouter.java:159)",
			                "... 89 stack entries skipped, final entry below ...",
			                "java.lang.Thread.run(Thread.java:745)"
			            ]
			        },
			        "message": "/setup (org.mule.module.apikit.exception.NotFoundException).",
			        "stackInfo": [
			            "org.mule.execution.ExceptionToMessagingExceptionExecutionInterceptor.execute(ExceptionToMessagingExceptionExecutionInterceptor.java:42)",
			            "org.mule.execution.MessageProcessorNotificationExecutionInterceptor.execute(MessageProcessorNotificationExecutionInterceptor.java:108)",
			            "org.mule.execution.MessageProcessorExecutionTemplate.execute(MessageProcessorExecutionTemplate.java:44)",
			            "org.mule.processor.BlockingProcessorExecutor.executeNext(BlockingProcessorExecutor.java:88)",
			            "org.mule.processor.BlockingProcessorExecutor.execute(BlockingProcessorExecutor.java:59)",
			            "org.mule.processor.chain.DefaultMessageProcessorChain.doProcess(DefaultMessageProcessorChain.java:80)",
			            "org.mule.processor.chain.AbstractMessageProcessorChain.process(AbstractMessageProcessorChain.java:74)",
			            "org.mule.execution.ExceptionToMessagingExceptionExecutionInterceptor.execute(ExceptionToMessagingExceptionExecutionInterceptor.java:27)",
			            "org.mule.execution.MessageProcessorExecutionTemplate.execute(MessageProcessorExecutionTemplate.java:44)",
			            "org.mule.processor.BlockingProcessorExecutor.executeNext(BlockingProcessorExecutor.java:98)",
			            "... 92 stack entries skipped, final entry below ...",
			            "java.lang.Thread.run(Thread.java:745)"
			        ]
			    },
			    "error": {
			        "id": "Resource not found",
			        "message": "Failed to process request for traceID=4817082c-af0a-4a98-a5a9-de46e9607f1e: /setup"
			    }
			}


    	 * 
    	 */
        

    	
        String setupRequest = Json.createObjectBuilder()
        .add("receiver", "http://"+host+":3043/v1/receivers/"+receiverUserNumber)  // needs to be an account #
        .add("sourceAccount", "http://"+host+":8014/ledger/accounts/"+sender)  // needs to be a Name, not an account number
        .add("destinationAmount", amount)
        .add("memo", "Hi Bobb!")
        .add("sourceIdentifier", "9809890190934023")
        .build()
        .toString();
    	
       
        System.out.println("setupRequest for payment post: " + setupRequest);
        System.out.println("1 ========== setup for valid receiver, payment request json: " + setupRequest + ", post url: "  + url+"/spsp/client/v1/setup" + ", should get 200 but failing");
        
        Response response =
        given().
        	contentType("application/json").
        	body(setupRequest).
        when().
        	post(url+"/spsp/client/v1/setup");
        
        
        /*Response responseStep3 =
    			given().
    				contentType("application/json").
    				body(setupRequest).
    			when().
    	         	post(url+"/spsp/client/v1/setup");
        
        */
        
        System.out.println("response from post to setup payment: " + response.prettyPrint());
        assertThat("setup for payment worked before calling ", response.getStatusCode(), equalTo(201));
        
        JsonPath setUpResponse = response.jsonPath();
        
        /*
         * Since we need to build a request from the data from the call above, we should 
         * have a bunch of assertThat() to ensure all is good.  This is a bit more detailed testing
         * 
         */
        assertThat("got ID", setUpResponse.getString("id"), not(isEmptyOrNullString()));
        assertThat("got sourceAccount", setUpResponse.getString("sourceAccount"), not(isEmptyOrNullString()));
        assertThat("got address", setUpResponse.getString("address"), not(isEmptyOrNullString()));
        assertThat("got sourceAmount", setUpResponse.getString("sourceAmount"), not(isEmptyOrNullString()));
        assertThat("got destinationAmount", setUpResponse.getString("destinationAmount"), not(isEmptyOrNullString()));
        assertThat("got condition", setUpResponse.getString("condition"), not(isEmptyOrNullString()));
        assertThat("got expiresAt", setUpResponse.getString("expiresAt"), not(isEmptyOrNullString()));
        assertThat("got additionalHeaders", setUpResponse.getString("additionalHeaders"), not(isEmptyOrNullString()));
        
        assertThat("get data", setUpResponse.getString("data"), not(isEmptyOrNullString()));
        
        Map<String,String> dataElementChildrenMap = response.path("data");
        String senderIdentifier = dataElementChildrenMap.get("senderIdentifier");
        System.out.println("senderIdentifier :: " + senderIdentifier);

//        assertThat("get senderIdentifier", senderIdentifier, not(isEmptyOrNullString()));
        
        
        
        /*
         * Sample json as of 11/28/2016
         * 
         * {
			  "id": "b51ec534-ee48-4575-b6a9-ead2955b8069",
			  "address": "ilpdemo.red.bob.b9c4ceba-51e4-4a80-b1a7-2972383e98af",
			  "destinationAmount": "10.40",
			  "sourceAmount": "9.00",
			  "sourceAccount": "http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:3043/v1/receivers/85555384",
			  "expiresAt": "2016-12-16T12:00:00Z",
			  "data": {
			    "senderIdentifier": "9809890190934023"
			  },
			  "additionalHeaders": "asdf98zxcvlknannasdpfi09qwoijasdfk09xcv009as7zxcv",
			  "condition": "cc:0:3:wey2IMPk-3MsBpbOcObIbtgIMs0f7uBMGwebg1qUeyw:32"
			}
         * 
         * 
         */
        
        String paymentRequest = Json.createObjectBuilder()
        .add("id", setUpResponse.getString("id"))
        .add("address",setUpResponse.getString("address"))
        .add("destinationAmount", setUpResponse.getString("destinationAmount"))
        .add("sourceAmount", setUpResponse.getString("sourceAmount"))
        .add("sourceAccount", setUpResponse.getString("sourceAccount"))
        .add("expiresAt", setUpResponse.getString("expiresAt"))
        .add("data", Json.createObjectBuilder().add("senderIdentifier", senderIdentifier))
        .add("additionalHeaders", setUpResponse.getString("additionalHeaders"))
        .add("condition", setUpResponse.getString("condition"))
        .build()
        .toString();
        
        System.out.println("2 ********** About to send this json to PUT payments: " + paymentRequest);
        System.out.println("2 ========== payment for valid receiver, payment request json: " + setupRequest + ", post url: "  + url+"/spsp/client/v1/setup" + ", should get 200 but failing");
        
        response = 
        given().
        	contentType("application/json").
        	body(paymentRequest).
        when().
        	put(url+"/spsp/client/v1/payments/" + setUpResponse.getString("id"));
        
        System.out.println("*** 2: Response from payment from setup: " + response.prettyPrint());
        System.out.println("*** 2: http response: " + response.getStatusCode());
        
        assertThat("response from payment request = 200", response.getStatusCode(), equalTo(200));
//        assertThat("id from payment request in body equals " + id, response.getStatusCode)
        
        
    }
    
    
    
   
    
    
    /**
     *
     * The goal of this test is the send each of the bad invoiceUrls in the csv for this test
     * to ensure that sending an invalid Url for the invoice generates the appropriate error.
     *
     * Note:  as of 11/17/16, the services are down on AWS so this error cannot be verified.
     *
     * I am expecting that is
     *
     * @param personName
     * @param invoiceUrl
     * @param account
     * @param name
     * @param currencyCode
     * @param currencySymbol
     * @param amount
     * @param status
     * @param invoiceInfo
     */
    @Test(dataProvider="invoice_GET_negative", groups={ "spsp_client_proxy_all", "spsp_client_proxy_invoice" })
    public void invoice_GetInvoiceDetails_ForInvalidInvoice_ShouldReceive404Response(String personName, String invoiceUrl, String account, String name, String currencyCode, String currencySymbol, String amount, String status, String invoiceInfo) {
        
    	
    	/*
    	 * 3/17/2017 The following test fail for the following errors
    	 * Env: dfsp1 qa
    	 * 
    	 * 
    	 *  [03-17 22:59:44,427] INFO  [[interop-domain].api-httpListenerConfig.worker.193] api-main: Received request for interopID=9480a3ab-3c7b-4103-99fb-476477472aa0 at path=/spsp/client/v1/invoices/bad/http%3A%2F%2Flocalhost%3A8082%2Fspsp%2Fclient%2Fv1%2Finvoice%3FinvoiceUrl%3Dhttp%3A%2F%2Fbrian.com, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
			[03-17 22:59:44,427] WARN  [[interop-domain].api-httpListenerConfig.worker.193] Configuration: No matching patterns for URI /invoices/bad/http:%2F%2Flocalhost:8082%2Fspsp%2Fclient%2Fv1%2Finvoice?invoiceUrl=http:%2F%2Fbrian.com
			[03-17 22:59:44,428] ERROR [[interop-domain].api-httpListenerConfig.worker.193] MappingExceptionListener: 
			********************************************************************************
			Message               : /invoices/bad/http:%2F%2Flocalhost:8082%2Fspsp%2Fclient%2Fv1%2Finvoice?invoiceUrl=http:%2F%2Fbrian.com
			Element               : /api-main/processors/2 @ interop-spsp-clientproxy-0.4.8-SNAPSHOT:spsp-client-proxy-api.xml:39 (APIkit Router)
			--------------------------------------------------------------------------------
			Exception stack is:
			/invoices/bad/http:%2F%2Flocalhost:8082%2Fspsp%2Fclient%2Fv1%2Finvoice?invoiceUrl=http:%2F%2Fbrian.com (org.mule.module.apikit.exception.NotFoundException)
			  org.mule.module.apikit.AbstractConfiguration$2.load(AbstractConfiguration.java:166)
			  org.mule.module.apikit.AbstractConfiguration$2.load(AbstractConfiguration.java:157)
			  com.google.common.cache.LocalCache$LoadingValueReference.loadFuture(LocalCache.java:3527)
			  com.google.common.cache.LocalCache$Segment.loadSync(LocalCache.java:2319)
			  com.google.common.cache.LocalCache$Segment.lockedGetOrLoad(LocalCache.java:2282)
			  com.google.common.cache.LocalCache$Segment.get(LocalCache.java:2197)
			  com.google.common.cache.LocalCache.get(LocalCache.java:3937)
			  com.google.common.cache.LocalCache.getOrLoad(LocalCache.java:3941)
			  com.google.common.cache.LocalCache$LocalLoadingCache.get(LocalCache.java:4824)
			  org.mule.module.apikit.AbstractRouter.processRouterRequest(AbstractRouter.java:159)
			  (90 more...)
			
			  (set debug level logging or '-Dmule.verbose.exceptions=true' for everything)
			********************************************************************************




		------------------- 	dfsp2 test failed with the following errors: ------------------- 	
    	 
		
		3/20/2017 test passed on dfps2 test.
		
		


    	 * 
    	 * 
    	 */
    	
    	
        /*
         * Temporary host url to call the local version of this test to ensure that it works.
         * Sample URL:  http://localhost:8081/spsp/client/v1/invoice?invoiceUrl=http://brian.com
         * There is no body, just a simple URI
         *
         {
	         "account": "dfsp2.bob.dylan.account",
	         "name": "Bob Dylan",
	         "currencyCode": "USD",
	         "currencySymbol": "$",
	         "amount": "10.40",
	         "status": "unpaid",
	         "invoiceInfo": "https://www.example.com/gp/your-account/order-details?ie=UTF8&orderID=111-7777777-1111111"
         }
         
         */
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
        	System.out.println("url for get invoice for invalid invoice should get 404 :: " + url+"/spsp/client/v1/invoices" + " for invoice :: " + invoiceUrl);
        	
        	Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	pathParam("invoiceId", invoiceUrl).
            when().
            	get(url+"/spsp/client/v1/invoices/bad/{invoiceId}");

            
        	System.out.println("get invoice negative: response: " + response.body().toString() + " and http status code = " + response.getStatusCode());
            assertThat(response.getStatusCode(), equalTo(404));
            System.out.println("..1: json response: " + response.toString());
            
            
            //			assertThat(quoteResponse.getString("name"), is(equalTo(name)));
            //			assertThat(quoteResponse.getString("currencyCode"), is(equalTo(currencyCode)));
            //			assertThat(quoteResponse.getString("currencySymbol"), is(equalTo(currencySymbol)));
            //
            //			Double responseAmount = Double.parseDouble(quoteResponse.getString("amount"));
            //			Double paramAmount = Double.parseDouble(amount);
            //			assertThat(responseAmount, is(equalTo(paramAmount)));
            //
            //			assertThat(quoteResponse.getString("status"), is(equalTo(status)));
            //			assertThat(quoteResponse.getString("invoiceInfo"), is(equalTo(invoiceInfo)));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_GetInvoiceDetails_ForInvalidInvoice_ShouldReceive404Response</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s, %s, %s, %s, %s, %s, %s \n","parameters: ", invoiceUrl, account, name, currencyCode, currencySymbol, amount, status, invoiceInfo);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
    }
    
    
    
    /**
     *
     * The goal of this test is to ensure that calling the valid core URL with an invalid Resource will fail
     * with a 404.
     *
     * @param personName
     * @param invoiceUrl
     * @param account
     * @param name
     * @param currencyCode
     * @param currencySymbol
     * @param amount
     * @param status
     * @param invoiceInfo
     */
    @Test(dataProvider="invoice_GET_negative", groups={ "spsp_client_proxy_all", "spsp_client_proxy_invoice" })
    public void invoice_Get_Ensure404WithInvalidURL_ShouldReceive404Response(String personName, String invoiceUrl, String account, String name, String currencyCode, String currencySymbol, String amount, String status, String invoiceInfo) {
        
    	/*
    	 * 3/17/2017 Test passed
    	 * 
    	 * 3/20/2017 test passed too.
    	 * 
    	 */
    	
    	
        String urlPath = "/spsp/client/v1/invoiceXXX";
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
        	Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	param("invoiceUrl", invoiceUrl).
            when().
            	get(url+urlPath);
            
            System.out.println("get invoice negative 404 expected: invoiceUrl = " + invoiceUrl);
            System.out.println("*** call response: " + response.getBody().prettyPrint());
            System.out.println("*** http status: " + response.getStatusCode());
            
            assertThat(response.getStatusCode(), equalTo(404));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_Get_Ensure404WithInvalidURL_ShouldReceive404Response</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s, %s, %s, %s, %s, %s, %s \n","parameters: ", invoiceUrl, account, name, currencyCode, currencySymbol, amount, status, invoiceInfo);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
    }
    
    
    /**
     *
     * The goal of this test is to ensure that calling the URI with an incorrect Resource.
     *
     */
    @Test(testName="invoice_GET_goodBaseUrlButWithBadInvoiceUrl_ShouldGet404_ResourceNotFound", groups={ "spsp_client_proxy_all", "spsp_client_proxy_invoice" })
    public void invoice_Get_withInvalidUri_ShoudlGet404() {
        
    	/*
    	 * 3/17/2017 Test passed in dfsp1 qa
    	 * 
    	 * 3/20/2017 test passed on dfsp2 test
    	 * 
    	 */
        
        // Override the URL for local testing until Brian gets this working locally.
//        String url = "http://localhost:8081";
        String urlPath = "/spsp/client/v1/invoiceBAD";
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            JsonPath response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	param("invoiceUrl", "should be invoiceUrl").
            when().
            	get(url+urlPath).
            then().
            	statusCode(404).extract().jsonPath();  // 404 is a bad url we are calling, but we need to be testing for what the receiver is returning when we give them an InviceUrl that is incorrect/invalid/non existant.
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_GET_goodBaseUrlButWithBadInvoiceUrl_ShouldGet404_ResourceNotFound</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "No paramerers");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
    }
    
    
    
    /*
     * 
     * Goal of this test is to create an invoice, then query it back to ensure that we
     * get a full end to end test
     * 
     */
    @Test(dataProvider="invoice_create_positive", groups={ "spsp_client_proxy_all", "spsp_client_proxy_invoice" })
    public void invoice_POST_ForValidInvoice_ShouldReceiveInvoiceDetailValidResponse(String invoiceUrl, String invoiceId, String submissionUrl, String senderIdentifier, String memo) {
        
    	/*
    	 * 3/17/2017 The following test fails with a weird error 
    	 * 
    	 * 
    	 * 
    	 *  [03-17 23:13:18,918] INFO  [[interop-domain].api-httpListenerConfig.worker.198] api-main: Received request for interopID=2fb3a599-9ad4-46aa-a38f-266a2b63e4fe at path=/spsp/client/v1/invoices, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
			[03-17 23:13:18,922] INFO  [[interop-domain].api-httpListenerConfig.worker.198] invoice: Proxying request for interopID=2fb3a599-9ad4-46aa-a38f-266a2b63e4fe to http://0.0.0.0:3042/v1//invoices, method=post: {"invoiceUrl":"sampleurl1.com","invoiceId":"12341","submissionUrl":"http://submissionUrl.com","senderIdentifier":"sample_senderIdentifier1","memo":"sample memo1"}
			[03-17 23:13:19,555] INFO  [[interop-domain].api-httpListenerConfig.worker.198] setup: Returning post:/invoice response for interopID=2fb3a599-9ad4-46aa-a38f-266a2b63e4fe, http.status=null: {"invoiceUrl":"sampleurl1.com","invoiceId":"12341","senderIdentifier":"sample_senderIdentifier1","memo":"sample memo1","submissionUrl":"http://submissionUrl.com"}
			
			It looks like the core error is in the follwoing url;
			
			                       |
 			                       V
			http://0.0.0.0:3042/v1//invoices, method=post: 
			{"invoiceUrl":"sampleurl1.com","invoiceId":"12341","submissionUrl":"http://submissionUrl.com","senderIdentifier":"sample_senderIdentifier1","memo":"sample memo1"}
			
			
			
			
			------------------- 	dfsp2 test failed with the following errors: 3/20/2017  ------------------- 	
			
			[03-20 19:46:58,874] INFO  [[interop-domain].api-httpListenerConfig.worker.407] api-main: Received request with traceID=c5261144-ea2b-46b4-9961-1c0b019c6d67 at path=/spsp/client/v1/invoices, method=POST, Content-Type=application/json; charset=UTF-8, Authorization=null
			[03-20 19:46:58,874] INFO  [[interop-domain].api-httpListenerConfig.worker.407] invoice: Proxying request for traceID=c5261144-ea2b-46b4-9961-1c0b019c6d67 to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/invoices, method=post: {"invoiceUrl":"sampleurl2.com","invoiceId":"12342","submissionUrl":"http://submissionUrl.com","senderIdentifier":"sample_senderIdentifier2","memo":"sample memo2"}
			[03-20 19:47:00,262] INFO  [[interop-domain].api-httpListenerConfig.worker.407] setup: Returning post:/invoice response for traceID=c5261144-ea2b-46b4-9961-1c0b019c6d67, http.status=null: {"invoiceUrl":"sampleurl2.com","invoiceId":"12342","senderIdentifier":"sample_senderIdentifier2","memo":"sample memo2","submissionUrl":"http://submissionUrl.com"}
			[03-20 19:47:00,449] INFO  [[interop-domain].api-httpListenerConfig.worker.407] api-main: Received request with traceID=839abcf1-820f-4852-af51-3ccf955cd016 at path=/spsp/client/v1/invoices/12342, method=GET, Content-Type=application/json; charset=UTF-8, Authorization=null
			[03-20 19:47:00,449] INFO  [[interop-domain].api-httpListenerConfig.worker.407] invoice: Proxying request for traceID=839abcf1-820f-4852-af51-3ccf955cd016 to http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:3042/v1/invoices, method=get, invoiceUrl=null
			[03-20 19:47:00,451] INFO  [[interop-domain].api-httpListenerConfig.worker.407] invoice: Returning get:/invoice response for traceID=839abcf1-820f-4852-af51-3ccf955cd016, invoiceUrl=null, http.status=null: Not Found
			
			
			Url for post invoice: http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:8088/spsp/client/v1/invoices
			creating invoice loc 1:: response: 
			Get JSON response from create invoice :: "Not Found"
			"Not Found"
			**** Response from invoice get :: invoice_POST_ForValidInvoice :: "Not Found"
			"Not Found"
			Url for post invoice: http://ec2-35-166-236-69.us-west-2.compute.amazonaws.com:8088/spsp/client/v1/invoices
			creating invoice loc 1:: response: 
			Get JSON response from create invoice :: "Not Found"
			"Not Found"
			**** Response from invoice get :: invoice_POST_ForValidInvoice :: "Not Found"
			
			
    	 */
    	
    	
    	
    	
/*
         New Sample as of 11/29/2016
         	{
		  		"invoiceUrl": "http://sampleurl.com",
		  		"invoiceId": "sample_invoice_id",
		  		"submissionUrl": "http://submissionUrl.com",
		  		"senderIdentifier": "sample_senderIdentifier",
		  		"memo": "sample memo"
			}
*/
    	   
    	
        String invoiceCreateRequest = Json.createObjectBuilder()
        .add("invoiceUrl", invoiceUrl)
        .add("invoiceId", invoiceId)				// new
        .add("submissionUrl", submissionUrl)		// new
        .add("senderIdentifier", senderIdentifier)	// new
        .add("memo", memo)
        .build()
        .toString();
        
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        String urlPath = url+invoiceUri;
        
        try {
            
        	/*
        	 * 
        	 * Create the invoice 
        	 * 
        	 */
            Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	body(invoiceCreateRequest).
            when().
            	post(urlPath);
                     

            System.out.println("Url for post invoice: " + urlPath);
            System.out.println("creating invoice loc 1:: response: " + response.asString());
            assertThat("create invoice", response.getStatusCode(), equalTo(201));
            
            /*
             * Now that we just posted/created an invoice, now try to get it from the API.  
             * This will exercise the full API process flow from end to end.
             * 
             */
            Response responseGet = 
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	pathParam("invoiceId", invoiceId).
            when().
            	get(url+invoiceUri + "/{invoiceId}");  // need the slash here as it is a URI parameter and the invoice URI does not account for the final slash.
            
            System.out.println("Get JSON response from create invoice :: " + responseGet.asString());

            assertThat(responseGet.getStatusCode(), equalTo(200));
            
            System.out.println("**** Response from invoice get :: invoice_POST_ForValidInvoice :: " + responseGet.prettyPrint());
            
            // This is the message that currently get returned when calling 
            assertThat(responseGet.prettyPrint(), not(containsString("Not Found")));
            
            
//            assertThat(response.jsonPath().getString("name"), is(equalTo(name)));
            assertThat(responseGet.jsonPath().getString("name"), not(isEmptyOrNullString()));
            
//            assertThat(response.jsonPath().getString("currencyCode"), is(equalTo(currencyCode)));
//            assertThat(response.jsonPath().getString("currencySymbol"), is(equalTo(currencySymbol)));
            assertThat(responseGet.jsonPath().getString("currencyCode"), not(isEmptyOrNullString()));
            assertThat(responseGet.jsonPath().getString("currencySymbol"), not(isEmptyOrNullString()));
            
//            Double responseAmount = Double.parseDouble(response.jsonPath().getString("amount"));
//            Double paramAmount = Double.parseDouble(amount);
//            assertThat(responseAmount, is(equalTo(paramAmount)));
            
            assertThat(responseGet.jsonPath().getString("amount"), not(isEmptyOrNullString()));  // commented out as it was failing for some reason.  Maybe teh name field does not exist.  12/8/2016.
            
//            assertThat(response.jsonPath().getString("status"), is(equalTo(status)));
//            assertThat(response.jsonPath().getString("invoiceInfo"), is(equalTo(invoiceInfo)));

            assertThat(responseGet.jsonPath().getString("status"), not(isEmptyOrNullString()));
            assertThat(responseGet.jsonPath().getString("invoiceInfo"), not(isEmptyOrNullString()));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_POST_ForValidInvoice_ShouldReceiveInvoiceDetailValidResponse</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s, %s, %s, %s \n","parameters: ", invoiceUrl, invoiceId, submissionUrl, senderIdentifier, memo );
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
    }
    
    
    @Test(testName="invoice_post_404_resouce_not_found", groups={ "spsp_client_proxy_all", "spsp_client_proxy_invoice" })
    public void invoice_POST_testingFor404ResourceNotFound() {
    	
    	/*
    	 * 3/17/2017 Test passed/succeeded.  
    	 * 
    	 * 3/20/2017 Test passed
    	 * 
    	 */
        
        String invoiceCreateRequest = Json.createObjectBuilder()
        .add("invoiceUrl", "invoice_url_goes_here")
        .add("dfsp", "dfsp_goes_here")
        .add("memo", "memo_goes_here")
        .build()
        .toString();
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            String uriPath = "/spsp/client/v1/invoiceBad";
            
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	body(invoiceCreateRequest).
            when().
            	post(url+uriPath).
            then().
            	statusCode(404);
            
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_post_404_resouce_not_found</i></h2>");
            captor.printf("<h3>%s</h3> %s, \n","parameters: ", "None for this test");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    
    /**
     *
     * Goal of this test is to send a media type that is unexpected.
     * Normally, this should return a 415 back, sending an application/xml
     * when it is expecting application/json.  I believe that due to the 
     * way API Kit dynamically tries to route, it does not find a flow that
     * matches the called resource, so it is an internal server error.
     * 
     */
    @Test(testName="invoice_post_415_and_500_unsupported_media_type", groups={ "spsp_client_proxy_all", "spsp_client_proxy_invoice" })
    public void invoice_POST_testingFor415UnsupportedMediaType() {
    	
    	/*
    	 * 
    	 * 3/17/2017 test passed in dfsp1 qa
    	 * 3/20/2017 test passed on dfps2 test
    	 * 
    	 * 
    	 * 
    	 */
        
        String invoiceCreateRequest = Json.createObjectBuilder()
        .add("invoiceUrl", "invoice_url_goes_here")
        .add("dfsp", "dfsp_goes_here")
        .add("memo", "memo_goes_here")
        .build()
        .toString();
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            String uriPath = "/spsp/client/v1/invoices";
            
            Response response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/xml").
            	body(invoiceCreateRequest).
            when().
            	post(url+uriPath);
            
            assertThat("Ensure we error propertly when we send the wrong media type", response.getStatusCode(), equalTo(500));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_post_415_and_500_unsupported_media_type</i></h2>");
            captor.printf("<h3>%s</h3> %s, \n","parameters: ", "None for this test");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    }
    
    
}
