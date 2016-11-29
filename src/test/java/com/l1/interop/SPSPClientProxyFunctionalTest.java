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
    private Properties prop = new Properties();
    FileWriter writer;
    PrintStream captor;
    
    private static final boolean IS_QUERY_TEST_ENABLED = false;
    private static final boolean IS_QUOTE_TEST_ENABLED = false;
    private static final boolean IS_SETUP_TEST_ENABLED = false;
    private static final boolean IS_PAYMENT_TEST_ENABLED = false;
    private static final boolean IS_INVOICE_TEST_ENABLED = true;
    
    @BeforeClass
    private void beforeClass() throws Exception {
        InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1.properties");
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
        
        
        System.out.println("base URL using for services :: " + url);
        
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
    
    
    @AfterClass
    private void afterClass() throws Exception {
        captor.println( "</body>\n" );
        captor.println( "</html>\n" );
    }
    
    
    @BeforeTest
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
    @Test(dataProvider="query_positive", enabled=IS_QUERY_TEST_ENABLED)
    public void query_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse(String receiverName, String receiverURI) throws Exception {
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
        	Response response =
        	given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	param("receiver", receiverURI).
            when().
            	get(url+"/spsp/client/v1/query");
//            then().
//            	statusCode(200).
//            	body("address", StringContainsIgnoringCase.containsStringIgnoringCase(receiverName)).
//            	body("imageUrl", StringContainsIgnoringCase.containsStringIgnoringCase(receiverName)).
//            	body("name",containsString(receiverName)).
//            	body("currencySymbol",equalTo("$")).
//            	body("type",equalTo("payee")).
//            	body("currencyCode",equalTo("USD")).
//            	body("status",nullValue()).
//            	body("amount",nullValue()).
//            	body("invoiceInfo",nullValue());
        	
        	
        	System.out.println("For query_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse: http status::" + response.getStatusCode());
        	System.out.println("**** json response payload: " + response.prettyPrint());
        	
        	assertThat("response http code", response.getStatusCode(), equalTo(200));
        	
        	
//        	assertThat("status", response.jsonPath().get("status"), isEmptyOrNullString() );
//        	assertThat("type", response.jsonPath().get("type"), not(isEmptyOrNullString()) );
        	
        	assertThat("type", response.jsonPath().get("type"), not(isEmptyOrNullString()) );
        	assertThat("type", response.jsonPath().get("type"), equalTo("payee") );
        	
        	assertThat("account", response.jsonPath().get("account"), not(isEmptyOrNullString()) );
        	
        	assertThat("currencyCode", response.jsonPath().get("currencyCode"), not(isEmptyOrNullString()) );
        	assertThat("currencyCode", response.jsonPath().get("currencyCode"), equalTo("USD") );

        	assertThat("currencyCode", response.jsonPath().get("currencyCode"), not(isEmptyOrNullString()) );
        	assertThat("currencySymbol", response.jsonPath().get("currencySymbol"), equalTo("$") );
        	
        	assertThat("name", response.jsonPath().get("name"), not(isEmptyOrNullString()) );
        	assertThat("name", response.jsonPath().get("name"), containsString(receiverName) );

        	assertThat("imageUrl", response.jsonPath().get("imageUrl"), not(isEmptyOrNullString()) );
        	assertThat("imageUrl", response.jsonPath().get("imageUrl"), StringContainsIgnoringCase.containsStringIgnoringCase(receiverName) );
        	
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
    @Test(dataProvider="query_negative", enabled=IS_QUERY_TEST_ENABLED)
    public void query_InValidReceiver_ShouldReceive404_ShouldReceiveErrorResponse(String receiverName, String receiverURI){
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
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
    @Test(dataProvider="quoteSourceAmount_positive", enabled=IS_QUOTE_TEST_ENABLED)
    public void quoteSourceAmount_ForValidReceiver_ShouldRecive200_ShouldReceiveValidResponse(String userAddress, String amount) {
        
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
     * <ul>
     * 	<li> id - "Error"</li>
     *  <li> message - contains text that has "500 Internal Server Error"</li>
     *  <li> debug->stack - contains text that has "500 Internal Server Error" ""</li>
     * </ul>
     * @param userAddress - Address of the user
     * @param amount - source amount that needs to be transferred
     */
    @Test(dataProvider="quoteSourceAmount_negative", enabled=IS_QUOTE_TEST_ENABLED)
    public void quoteSourceAmount_ForInValidReceiver_ShouldRecive404_ShouldReceiveErrorResponse(String userAddress, String amount) {
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	param("receiver", userAddress).
            	param("destinationAmount",amount).
            when().
            	get(url+"/spsp/client/v1/quoteSourceAmount").
            then().
            	statusCode(404).
            	body("id",equalTo("Error")).
            	body("message",containsString("500 Internal Server Error")).
            	body("debug.stack",containsString("500 Internal Server Error"));
            
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
    @Test(dataProvider="quoteDestinationAmount_positive", enabled=IS_QUOTE_TEST_ENABLED)
    public void quoteDestinationAmount_ForValidReceiver_ShouldReceive200_ShouldReceiveValidResponse(String userAddress, String amount){
        
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
     * <ul>
     * 	<li> id - "Error"</li>
     *  <li> message - contains text that has "500 Internal Server Error"</li>
     *  <li> debug->stack - contains text that has "500 Internal Server Error" ""</li>
     * </ul>
     * @param userAddress - Address of the user
     * @param amount - source amount that needs to be transferred
     */
    @Test(dataProvider="quoteDestinationAmount_negative", enabled=IS_QUOTE_TEST_ENABLED)
    public void quoteDestinationAmount_ForInValidReceiver_ShouldReceive404_ShouldReceiveErrorResponse(String userAddress, String amount){
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            given().
            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            contentType("application/json").
            param("receiver", userAddress).
            param("destinationAmount",amount).
            when().
            get(url+"/spsp/client/v1/quoteDestinationAmount").
            then().
            statusCode(404).
            body("id",equalTo("Error")).
            body("message",containsString("500 Internal Server Error")).
            body("debug.stack",containsString("500 Internal Server Error"));
            
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
    @Test(dataProvider="setup_positive", enabled=IS_SETUP_TEST_ENABLED)
    public void setUp_ForValidReceiver_ShouldReturn201_ShouldReturnValidResponse(String sender, String receiver, String amount){
        
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
            
            given().
            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            contentType("application/json").
            body(json).
            when().
            post(url+"/spsp/client/v1/setup").
            then().
            statusCode(201).
            body("id",is(not(""))).
            body("receiver",equalTo("http://"+host+":3046/v1/receivers/"+receiver));
            
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
    @Test(dataProvider="setup_negative", enabled=IS_SETUP_TEST_ENABLED)
    public void setUp_ForInValidReceiver_ShouldReturn404_ShouldReturnErrorResponse(String sender, String receiver, String amount){
        
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
            
            given().
            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            contentType("application/json").
            body(json).
            when().
            post(url+"/spsp/client/v1/setup").
            then().
            statusCode(404).
            body("id",equalTo("Error")).
            body("message",containsString("500 Internal Server Error")).
            body("debug.stack",containsString("500 Internal Server Error"));
            
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
    
    
    @Test(dataProvider="payment_positive", enabled=IS_PAYMENT_TEST_ENABLED)
    public void payment_ForValidReceiver_ShouldReceive200_ShouldReturnValidResponse(String sender, String receiver, String amount){
        
        String setupRequest = Json.createObjectBuilder()
        .add("receiver", "http://"+host+":3046/v1/receivers/"+receiver)
        .add("sourceAccount", "http://"+host+":8088/ledger/accounts/"+sender)
        .add("destinationAmount", amount)
        .add("memo", "Hi Bobb!")
        .add("sourceIdentifier", "")
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
        
        
        
        /*
         * Sample json as of 11/28/2016
         * 
         * {
			  "id": "b51ec534-ee48-4575-b6a9-ead2955b8069",
			  "address": "ilpdemo.red.bob.b9c4ceba-51e4-4a80-b1a7-2972383e98af",
			  "destinationAmount": "10.40",
			  "sourceAmount": "9.00",
			  "sourceAccount": "http://dfsp1:8014/ledger/accounts/alice",
			  "expiresAt": "2016-08-16T12:00:00Z",
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
    @Test(dataProvider="invoice_GET_negative", enabled=IS_INVOICE_TEST_ENABLED)
    public void invoice_GetInvoiceDetails_ForInvalidInvoice_ShouldReceive404Response(String personName, String invoiceUrl, String account, String name, String currencyCode, String currencySymbol, String amount, String status, String invoiceInfo) {
        
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
    @Test(dataProvider="invoice_GET_negative", enabled=IS_INVOICE_TEST_ENABLED)
    public void invoice_Get_Ensure404WithInvalidURL_ShouldReceive404Response(String personName, String invoiceUrl, String account, String name, String currencyCode, String currencySymbol, String amount, String status, String invoiceInfo) {
        
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
    @Test(testName="invoice_GET_goodBaseUrlButWithBadInvoiceUrl_ShouldGet404_ResourceNotFound", enabled=IS_INVOICE_TEST_ENABLED)
    public void invoice_Get_withInvalidUri_ShoudlGet404() {
        
        
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
    @Test(dataProvider="invoice_create_positive", enabled=IS_INVOICE_TEST_ENABLED)
    public void invoice_POST_ForValidInvoice_ShouldReceiveInvoiceDetailValidResponse(String invoiceUrl, String invoiceId, String submissionUrl, String senderIdentifier, String memo) {
        
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
//        .add("invoiceUrl", "http://"+host+":3046/spsp/client/v1/invoice")
        .add("invoiceUrl", invoiceUrl)
        .add("invoiceId", invoiceId)				// new
        .add("submissionUrl", submissionUrl)		// new
        .add("senderIdentifier", senderIdentifier)	// new
        .add("memo", memo)
        .build()
        .toString();
        
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
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
	         	post(url+"/spsp/client/v1/invoices");

            System.out.println("creating invoice loc 1: response: " + response.prettyPrint());
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
            	get(url+"/spsp/client/v1/invoices/{invoiceId}");

            assertThat(responseGet.getStatusCode(), equalTo(200));
            
            System.out.println("** Response from invoice get: " + responseGet.prettyPrint());
            
            // This is the message that currently get returned when calling 
            assertThat(responseGet.prettyPrint(), not(containsString("Not Found")));
            
            
//            assertThat(response.jsonPath().getString("name"), is(equalTo(name)));
            assertThat(response.jsonPath().getString("name"), not(isEmptyOrNullString()));
            
//            assertThat(response.jsonPath().getString("currencyCode"), is(equalTo(currencyCode)));
//            assertThat(response.jsonPath().getString("currencySymbol"), is(equalTo(currencySymbol)));
            assertThat(response.jsonPath().getString("currencyCode"), not(isEmptyOrNullString()));
            assertThat(response.jsonPath().getString("currencySymbol"), not(isEmptyOrNullString()));
            
//            Double responseAmount = Double.parseDouble(response.jsonPath().getString("amount"));
//            Double paramAmount = Double.parseDouble(amount);
//            assertThat(responseAmount, is(equalTo(paramAmount)));
            
            assertThat(response.jsonPath().getString("amount"), not(isEmptyOrNullString()));
            
//            assertThat(response.jsonPath().getString("status"), is(equalTo(status)));
//            assertThat(response.jsonPath().getString("invoiceInfo"), is(equalTo(invoiceInfo)));

            assertThat(response.jsonPath().getString("status"), not(isEmptyOrNullString()));
            assertThat(response.jsonPath().getString("invoiceInfo"), not(isEmptyOrNullString()));
            
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
    
    
    @Test(testName="invoice_post_404_resouce_not_found", enabled=IS_INVOICE_TEST_ENABLED)
    public void invoice_POST_testingFor404ResourceNotFound() {
        
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
    @Test(testName="invoice_post_415_and_500_unsupported_media_type", enabled=IS_INVOICE_TEST_ENABLED)
    public void invoice_POST_testingFor415UnsupportedMediaType() {
        
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
