package com.l1.interop;


import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;

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

import com.l1p.interop.JsonTransformer;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;


public class USSDFunctionalTest {
	
	private static String host;
	private static String port;
	private static String url;

	
	private Properties prop = new Properties();

	FileWriter writer;
    PrintStream captor;
    
	
	@BeforeClass(alwaysRun=true)
    private void beforeClass() throws Exception {
        
//      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1-test.properties");
//      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp2-test.properties");
      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1-qa.properties");
//      InputStream is = ClassLoader.getSystemResourceAsStream("dfsp2-qa.properties");
      
        prop.load(is);
        
        String environment = System.getProperty("env");
        if(environment != null){
            is = ClassLoader.getSystemResourceAsStream("dfsp1-"+environment.toLowerCase()+".properties");
        }
        
        prop.load(is);
        host = prop.getProperty("host");
        port = prop.getProperty("ussd_port");
        url = "http://"+host+":"+port;
        
        /*
         * 
         * Override url for local testing
         * 
         */
//        url = "http://localhost:8081";
        
        System.out.println("****************************************************************************************************************************************");
        System.out.println("*                                                                                                                                      *");
        System.out.println("*             Tests USSD running using the URL of :: " + url + "                    *");
        System.out.println("*                                                                                                                                      *");
        System.out.println("****************************************************************************************************************************************");

        /**
         *
         * The goal of this test is to ensure that we can create an account.
         */
        // will need a CSV that has our test data
        
        if(!(new File("target/failure-reports")).exists())
            new File("target/failure-reports").mkdirs();
        
        writer = new FileWriter("target/failure-reports/USSD-Functional-Tests.html");
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
    
    
    @DataProvider(name = "ussd_createuser1")
    private Iterator<Object []> ussdcreateuser1( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_createuser.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_createuser2")
    private Iterator<Object []> ussdcreateuser2( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_createuser_1.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_sendmoney")
    private Iterator<Object []> ussdsendmoney( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_sendmoney.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_sellgoods")
    private Iterator<Object []> ussdsellgoods( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_sellgoods.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_pendingtrans")
    private Iterator<Object []> ussdpendingtrans( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_pendingtrans.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_manageaccount")
    private Iterator<Object []> ussdmanageaccount( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_manageaccount.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_checkbalance")
    private Iterator<Object []> ussdcheckbalance( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_checkbalance.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_ministatement")
    private Iterator<Object []> ussdministatement( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_ministatement.csv");
        return testCases.iterator();
    }

   @DataProvider(name = "ussd_transaction")
    private Iterator<Object []> ussdTransaction( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_transaction.csv");
        return testCases.iterator();
    }

    @Test(dataProvider="ussd_createuser1",groups={ "ussd_createuser" })
	public void get_ussd_createuser1(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-create user 1 : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response code for create user 1: " + response.getStatusCode());

            /*
        	 * Response for the service will always on test/html format
        	 * We need to check the response from USSD. If an User already exists you will receive the following response
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Welcome Senthil Govindaraj!

                        1. Send money
                        2. Sell Goods
                        3. Pending Transactions
                        4. Manage account
                        5. Switch account
                        6. Check balance
                        7. Ministatement</Message>
                        <DefaultCode>*123#</DefaultCode>
                        <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response: Scenario-create user 1 : ------->>>>: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-create user 1 : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.prettyPrint(), not(containsString("Wrong Input")));
            //assertThat(response.prettyPrint(), not(containsString("HTTP error")));
            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            System.out.println("BP: reponse: " + response.toString());
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));
            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-create user 1</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_createuser2",groups={ "ussd_createuser" })
    public void get_ussd_createuser2(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-create user 2 : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-create user 2 : ------->>>>: " + response.getStatusCode());

            /*
        	 * Response for the service will always on test/html format
        	 * We need to check the response from USSD. If an User already exists you will receive the following response
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Welcome Senthil Govindaraj!

                        1. Send money
                        2. Sell Goods
                        3. Pending Transactions
                        4. Manage account
                        5. Switch account
                        6. Check balance
                        7. Ministatement</Message>
                        <DefaultCode>*123#</DefaultCode>
                        <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response for create user: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-create user 2 : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));
            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-create user 2</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_sendmoney", groups={ "ussd_sendmoney", "ussd_users" })
    public void get_ussd_sendmoney(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-Send Money : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-Send Money : ------->>>>: " + response.getStatusCode());

        	/*
        	 * Response for the service will always on test/html format
        	 * We need to check the response from USSD. If an User already exists you will receive the following response
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Transfer request sent !

                Status: executed
                Destination: Ram Pal
                Amount: 100 USD
                Local fee: 1
                ID: cf:0:ZYYrUk4LUmK2Pf4hue9A5M3d9ryqz4OUBEuoBwstmH4
                0. Home</Message>
                    <DefaultCode>*123#</DefaultCode>
                    <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response for Send Money: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-Send Money : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));
            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-Send Money</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_sellgoods", groups={ "ussd_sellgoods","ussd_users"})
    public void get_ussd_sellgoods(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-Sell Goods : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-Sell Goods : ------->>>>: " + response.getStatusCode());

        	/*
        	 * Response for the service will always on test/html format
        	 * Check the response from USSD. The transaction between the existing users will give the below response
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Invoice request sent!
                Destination: Senthil Govindaraj
                Amount: 50 USD
                0. Home</Message>
                    <DefaultCode>*123#</DefaultCode>
                    <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response for Sell Goods: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-Sell Goods : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));

            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-Sell Goods</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_pendingtrans", groups={ "ussd_pendingtrans","ussd_users" })
    public void get_ussd_pendingtrans(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-Pending Transactions : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-Pending Transactions : ------->>>>: " + response.getStatusCode());

        	/*
        	 * Response for the service will always on test/html format
        	 * Check the response from USSD. Response displays all pending transactions
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Pending Transactions

                1. Invoice from Senthil Govindaraj for 50 USD

                0. Home</Message>
                    <DefaultCode>*123#</DefaultCode>
                    <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response for Pending Transactions: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-Pending Transactions : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));

            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-Pending Transactions</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_manageaccount", groups={ "ussd_manageaccount","ussd_users" })
    public void get_ussd_manageaccount(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-Manage Accounts : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-Manage Accounts : ------->>>>: " + response.getStatusCode());

        	/*
        	 * Response for the service will always on test/html format
        	 * Check the response from USSD. Response displays all pending transactions
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Account information

                Account number: Ftest2
                Account name: Ftest2
                User number: 40554564
                Currency: USD
                Account status: Primary

                Account holders:
                * Ram Pal (Signatory)

                0. Home
                1. Back</Message>
                    <DefaultCode>*123#</DefaultCode>
                    <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response for Manage Accounts: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-Manage Accounts : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));

            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-Manage Accounts</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_checkbalance", groups={ "ussd_checkbalance","ussd_users" })
    public void get_ussd_checkbalance(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-Check Balance : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-Check Balance : ------->>>>: " + response.getStatusCode());

        	/*
        	 * Response for the service will always on test/html format
        	 * Check the response from USSD. Response displays the available balance on account
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Balance: 999.00 USD

                0. Home</Message>
                    <DefaultCode>*123#</DefaultCode>
                    <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response for Check Balance: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-Check Balance : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));
            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-Check Balance</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_ministatement", groups={ "ussd_ministatement","ussd_users" })
    public void get_ussd_ministatement(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-Mini Statement : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-Mini Statement : ------->>>>: " + response.getStatusCode());

        	/*
        	 * Response for the service will always on test/html format
        	 * Check the response from USSD. Response displays all transactions executed by the user
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Ministatement:

                Name;Amount;Date
                Senthil Govindaraj;100.00;20-Feb
                Senthil Govindaraj;-100.00;20-Feb
                fee;-1.00;20-Feb

                0. Home</Message>
                   <DefaultCode>*123#</DefaultCode>
                   <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
            assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            //System.out.println("USSD Response for Mini statement: " + response.prettyPrint());
            System.out.println("USSD Response: Scenario-Mini Statement : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString ("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString ("Message"), not(containsString("HTTP error")));
            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario-Mini Statement</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

   @Test(dataProvider="ussd_transaction", groups={ "ussd_transaction" })
    public void get_ussd_transaction(String phone, String message) {

        Response response;
        String urlPath = "/ussd";

        int http_status;

        Map<String, Object> jsonReponseMap = null;

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */

            String ussdposRequest = Json.createObjectBuilder()
                    .add("phone", phone)
                    .add("message", message)
                    .build()
                    .toString();

            System.out.println("USSD Request: Scenario-Transaction : ------->>>>: " + ussdposRequest);

            response =
                    given().
                            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response: Scenario-Transaction : ------->>>>: " + response.getStatusCode());

        	/*
        	 * Response for the service will always on test/html format
        	 * When an Input is passed with the user/phone that doesn't exists or options that is not available you will receive the below response
        	 * <UssdResponse version="1.0">
                    <Status code="0"></Status>
                    <Message>Wrong Input

                        0. Home
                        1. Back</Message>
                    <DefaultCode>*123#</DefaultCode>
                    <PhoneNumber></PhoneNumber>
                </UssdResponse>
        	 */
           assertThat(response.getStatusCode(), anyOf(equalTo(200)));

            System.out.println("USSD Response: Scenario-Transaction : ------->>>>: " + response.xmlPath().getString ("Message").trim());

            //assertThat(response.xmlPath().getString("Message"), not(containsString("Wrong Input")));
            assertThat(response.xmlPath().getString("Message"), not(containsString("HTTP error")));

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>USSD Function Test : Scenario - Money Transaction</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }
}