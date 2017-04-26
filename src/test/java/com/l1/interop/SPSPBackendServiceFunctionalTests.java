package com.l1.interop;

import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
//import static org.hamcrest.Matchers.isEmptyOrNullString;
//import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import javax.json.Json;

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

    @DataProvider(name = "ussd_sellgoods")
    private Iterator<Object []> ussdsellgoods( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/backend_services/ussd_sellgoods.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "receiver_get_positive")
    private Iterator<Object []> dpSPSPClientProxy_receiver_GetURLS_positive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/backend_services/receiver_get_positive.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "invoice_get_positive")
    private Iterator<Object []> dpSPSPClientProxy_invoice_Get_positive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/backend_services/invoice_get_positive.csv");
        return testCases.iterator();
    }

    @DataProvider(name = "payment_put_positive")
    private Iterator<Object []> dpSPSPClientProxy_payment_put_positive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/backend_services/payment_put_positive.csv");
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


    @Test(dataProvider="receiver_get_positive", groups={ "spsp_backend_service_all" })
    public void spsp_Get_receiver_info(String payee) {



        String urlPath = "/spsp/backend/v1/receivers/";

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

            Response response =
                    given().
                            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            param("payee", payee).
                            when().
                            get(url+urlPath+payee);

            System.out.println("get Payee Information: Payee = " + payee);
            System.out.println("*** call response: " + response.getBody().prettyPrint());
            System.out.println("*** http status: " + response.getStatusCode());

            assertThat(response.getStatusCode(), equalTo(200));

        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_Get_Ensure404WithInvalidURL_ShouldReceive404Response</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s, %s, %s, %s, %s, %s, %s \n","parameters: ", payee);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="invoice_get_positive", groups={ "spsp_backend_service_all" })
    public void spsp_get_invoice_info(String invoiceid) {



        String urlPath = "/spsp/backend/v1/receivers/invoices/";

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

            Response response =
                    given().
                            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            queryParam("invoiceid", invoiceid).
                            when().
                            get(url+urlPath+invoiceid);

            System.out.println("get Payee Information: Payee = " + invoiceid);
            System.out.println("*** call response: " + response.getBody().prettyPrint());
            System.out.println("*** http status: " + response.getStatusCode());

            assertThat(response.getStatusCode(), equalTo(200));

        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>invoice_Get_Ensure404WithInvalidURL_ShouldReceive404Response</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s, %s, %s, %s, %s, %s, %s \n","parameters: ", invoiceid);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="ussd_sellgoods", groups={ "spsp_backend_service_all"})
    public void spsp_ussd_sellgoods(String phone, String message) {

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
                            post("http://"+host+":8019/ussd");

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

    @Test(dataProvider="invoice_create_positive", groups={ "spsp_backend_service_all" })
    public void spsp_Post_invoice(String invoiceUrl, String senderIdentifier, String memo, String type, String account, String currencyCode, String amount, String status) {



        String urlPath = "/spsp/backend/v1/invoices";

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

            System.out.println("******* Inside Invoice POST request");

            String paymentputRequest = Json.createObjectBuilder()
                    .add("invoiceUrl", invoiceUrl)
                    .add("senderIdentifier", senderIdentifier)
                    .add("memo", memo)
                    .add("type", type)
                    .add("account", account)
                    .add("currencyCode", currencyCode)
                    .add("currencySymbol", "$")
                    .add("amount", amount)
                    .add("status", status)
                    .build()
                    .toString();

            Response response =
                    given().
                            //config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                                    config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(paymentputRequest).
                            when().
                            post(url+urlPath);

            //System.out.println("get Payee Information: Payee = " + payee);
            System.out.println("*** call response: " + response.getBody().prettyPrint());
            System.out.println("*** http status: " + response.getStatusCode());

            assertThat(response.getStatusCode(), equalTo(201));

        } catch(java.lang.AssertionError e){
            //captor.println("<ul>");
            //captor.println("<h2>Test Case: <i>invoice_Get_Ensure404WithInvalidURL_ShouldReceive404Response</i></h2>");
            //captor.printf("<h3>%s</h3> %s, %s, %s, %s, %s, %s, %s, %s \n","parameters: ", payee);
            //captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            //captor.print("<h3>Request and Response: </h3>");
            //captor.println("<pre>"+twriter.toString()+"</pre>");
            //captor.println("</ul>");

            throw e;
        }

    }

    @Test(dataProvider="payment_put_positive", groups={ "spsp_backend_service_all" })
    public void spsp_Put_payment(String destinamtionAmount, String memo, String status, String transferId) {



        String urlPath = "/spsp/backend/v1/receivers/20343032/payments/4e385f2e-f4f9-428c-8d0f-4eead13c6ad0";

        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {

            System.out.println("******* Inside payment put request");

            String paymentputRequest = Json.createObjectBuilder()
                    .add("destinationAmount", destinamtionAmount)
                    .add("memo", memo)
                    .add("status", status)
                    .add("transferId", transferId)
                    .build()
                    .toString();

            Response response =
                    given().
                            //config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                            contentType("application/json").
                            body(paymentputRequest).
                            when().
                            put(url+urlPath);

            //System.out.println("get Payee Information: Payee = " + payee);
            System.out.println("*** call response: " + response.getBody().prettyPrint());
            System.out.println("*** http status: " + response.getStatusCode());

            assertThat(response.getStatusCode(), equalTo(200));

        } catch(java.lang.AssertionError e){
            //captor.println("<ul>");
            //captor.println("<h2>Test Case: <i>invoice_Get_Ensure404WithInvalidURL_ShouldReceive404Response</i></h2>");
            //captor.printf("<h3>%s</h3> %s, %s, %s, %s, %s, %s, %s, %s \n","parameters: ", payee);
            //captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            //captor.print("<h3>Request and Response: </h3>");
            //captor.println("<pre>"+twriter.toString()+"</pre>");
            //captor.println("</ul>");

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
}
