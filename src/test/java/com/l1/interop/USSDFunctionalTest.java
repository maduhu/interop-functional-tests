package com.l1.interop;


import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;

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
        InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1.properties");
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
        
        System.out.println("**************************************************************************************************************");
        System.out.println("*                                                                                                            *");
        System.out.println("*                         Tests USSD running using the URL of :: " + url + "   *******************");
        System.out.println("*                                                                                                            *");
        System.out.println("**************************************************************************************************************");

        /**
         *
         * The goal of this test is to ensure that we can create an account.
         */
        // will need a CSV that has our test data
        
        if(!(new File("target/failure-reports")).exists())
            new File("target/failure-reports").mkdirs();
        
        writer = new FileWriter("target/failure-reports/central_directory.html");
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
    
    
    @DataProvider(name = "ussd_positive")
    private Iterator<Object []> ussdPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_positive.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "ussd_negative")
    private Iterator<Object []> ussdNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ussd/ussd_negative.csv");
        return testCases.iterator();
    }

    @Test(dataProvider="ussd_positive", groups={ "ussd_positive" })
	public void get_ussd_positive(String phone, String message) {

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

            System.out.println("USSD Request for positive scenario: " + ussdposRequest);

            response =
                    given().
                            //auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response code for positive scenario: " + response.getStatusCode());

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
            assertThat(response.getStatusCode(), anyOf(equalTo(200), equalTo(201), equalTo(422)));

            System.out.println("response number: " + response.prettyPrint());
            //jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>test_POST_registering_a_Digital_Financial_Service_Provider</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_negative", groups={ "ussd_negative" })
    public void get_ussd_negative(String phone, String message) {

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

            System.out.println("USSD Request for negative scenario: " + ussdposRequest);

            response =
                    given().
                            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(ussdposRequest).
                            when().
                            post(url+urlPath);

            http_status = response.getStatusCode();

            System.out.println("USSD Response code for positive scenario: " + response.getStatusCode());

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
            assertThat(response.getStatusCode(), anyOf(equalTo(200), equalTo(201), equalTo(422)));

            System.out.println("response number: " + response.prettyPrint());

        } catch(AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>test_POST_registering_a_Digital_Financial_Service_Provider</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }
}