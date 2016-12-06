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
        
        System.out.println(">>>>>>>>  host being used in the functional test: " + host + " and the port # : " + port);
        
        dfsp_username = prop.getProperty("dfsp.username");
        dfsp_password = prop.getProperty("dfsp.password");
        
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
    
	
    @AfterClass
    private void afterClass() throws Exception {
        captor.println( "</body>\n" );
        captor.println( "</html>\n" );
    }
    
    
    @BeforeTest
    private void setup() throws Exception {
        RestAssured.config = RestAssuredConfig.config().logConfig(LogConfig.logConfig().enablePrettyPrinting(true));
    }
    
    
//    @DataProvider(name = "get_directory_resources_positive")
//    private Iterator<Object []> dpSPSPClientProxyQueryPositive( ) throws Exception
//    {
//        List<Object []> testCases = readCSVFile("test-data/directory/resource_positive.csv");
//        return testCases.iterator();
//    }
    
    
    /**
	 * 
	 * The goal of this test is to ensure that we get back the key attributes back and a http status of 200
	 * 
	 */
	@Test(description="get_backend_services_receiver_positive", enabled=true)
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
		
        String urlPath = "/spsp/backend/v1/receivers/Bob";
        
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
	
	
	
	/**
	 * 
	 * The goal of this test is to complete a full end to end test of 
	 */
	@Test
	public void test_full_end_to_end_invoice_positive() {
		
		String urlPath = "/spsp/backend/v1/receivers/Bob";
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
	}
	
	
	@Test
	public void test_update_an_existing_invoice_positive() {
		
	}
	
	
	@Test
	public void test_get_an_existing_invoice_positive() {
		
	}
	
	
	@Test
	public void test_paying_an_invoice_positive() {
		
	}
    
}
