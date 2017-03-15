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
        InputStream is = ClassLoader.getSystemResourceAsStream("dfsp1-qa.properties");
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
		
        String urlPath = "/spsp/backend/v1/receivers/26547070";
        
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
            
            
            // TODO:  Add data valiation to the data level after the fetch.  Compare the returned data from the 
            
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
