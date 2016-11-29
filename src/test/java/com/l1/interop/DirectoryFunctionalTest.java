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


public class DirectoryFunctionalTest {
	
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
    
    
    @DataProvider(name = "get_directory_resources_positive")
    private Iterator<Object []> dpSPSPClientProxyQueryPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/directory/resource_positive.csv");
        return testCases.iterator();
    }
	
	
	/**
	 * 
	 * The goal of this test is to ensure that we get back the key attributes back and a http status of 200
	 * 
	 */
	@Test(description="get_metadata_positive_no_auth_required", enabled=true)
	public void get_metadata_positive() {

		/*
		 * This is the data that is returned
			{
				  "directory": "http://central-directory-dev.us-west-2.elasticbeanstalk.com",
				  "urls": {
				    "health": "http://central-directory-dev.us-west-2.elasticbeanstalk.com/health",
				    "identifier_types": "http://central-directory-dev.us-west-2.elasticbeanstalk.com/identifier-types",
				    "resources": "http://central-directory-dev.us-west-2.elasticbeanstalk.com/resources"
				  }
			}
		 * 
		 */
		
        String urlPath = "/directory/v1/";
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
        	
            JsonPath response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            when().
            	get(url+urlPath).
            then().
            	statusCode(200).extract().jsonPath();
            
            assertThat(response.getString("directory"), not(isEmptyOrNullString()));
            assertThat(response.getString("urls.health"), not(isEmptyOrNullString()));
            assertThat(response.getString("urls.identifier_types"), not(isEmptyOrNullString()));
            assertThat(response.getString("urls.resources"), not(isEmptyOrNullString()));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>get_metadata_positive_no_auth_required</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "No paramerers");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
		
	}
	
	
	
	@Test(dataProvider="get_directory_resources_positive", description="Description: get directory resources and uses BASIC AUTH")
	public void get_directory_resources_positive_using_BASIC_AUTH(Float identifier, String identifierType) {
		
        // Sample URL:  http://127.0.0.1:8081/directory/resources?identifier=test&identifierType=test
        String urlPath = "/directory/v1/resources";
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            JsonPath response =
            given().
            	auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	queryParam("identifier", identifier).
            	queryParam("identifierType", identifierType).
            when().
            	get(url+urlPath).
            then().
            	statusCode(200).extract().jsonPath();
            
            System.out.println("response: " + response.toString());
            assertThat(response.getString("spspReceiver"), not(isEmptyOrNullString()));
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>get_directory_resources_positive</i></h2>");
            captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", identifier, identifierType);
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
	}
	
	
	
	/**
	 * 
	 * The goal of this test is to ensure that we can create a DFSP and/or and query it back.
	 * Since the API does not allow for us to delete a DFSP, we will check to see of the 
	 * HTTP response status is a 40x (we will see what is implemented for the response) indicating
	 * that the DFSP already exists.  
	 * 
	 * In either case, we will query back the DFSP to ensure we get a full end-to-end test.
	 * 
	 */
	// will need a CSV that has our test data
	@Test
	public void registering_a_Digital_Financial_Service_Provider_POST() {
		
		Response response;
        String urlPath = "/directory/v1/commands/register";
        
        String dfsp_username="admin";
        String dfsp_password="admin";
        int http_status;
        
        Map<String, Object> jsonReponseMap = null;
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
        	/*
        	 * Create the JSON needed for the Create.
        	 * Note:  as of 11/22/16, the key and secret json fields are not being accepted.
        	 */
        	String dfspCreateRequest = Json.createObjectBuilder()
		        .add("name", "507")
		        .build()
		        .toString();
        	
        	
//            JsonPath response =
        	response =
            given().
            	auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	body(dfspCreateRequest).
            when().
            	post(url+urlPath);
        	
        	http_status = response.getStatusCode();
        	
        	/*
        	 * We need to check for both 201 or a 422.
        	 * For a newly create DFSP, we will get a 201
        	 * If the "name" already exists, then we will get a 422.  This still confirms a successful call.
        	 */
        	assertThat(response.getStatusCode(), anyOf(equalTo(201), equalTo(422)));
            
            jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );
            
        } catch(java.lang.AssertionError e){
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
	
	
	
	@Test(description="Description: get available identifier-types from service.  Requires BASIC AUTH")
	public void get_identifier_types_resources_positive_requires_BASIC_AUTH() {
		
		/*
		 * Sample Response:
		 * 
		 * [
			  {
			    "identifierType": "test",
			    "description": "test"
			  },
			  {
			    "identifierType": "eur",
			    "description": "Central end user registry"
			  }
			]
		 * 
		 */
		
        String urlPath = "/directory/v1/identifier-types";
        
        Map<String, String> jsonReponseMap = null;
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
            
            JsonPath response =
            given().
            	auth().preemptive().basic(dfsp_username, dfsp_password).  // Must use the preemptive as this is the type of basic auth that the end system needs.  If you use just basic, it fails the challenge.
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            when().
            	get(url+urlPath).
            then().
            	statusCode(200).extract().jsonPath();
            
            System.out.println("json response from get identifier-types: " + response.prettyPrint());
            
            // get the whole json response body
            List<Object> jsonElementList = response.getList("");
            
            // We need to ensure that we have at least 1 element.
            assertThat(jsonElementList, hasSize(greaterThan(0)));
            
            for (Object elementMap : jsonElementList) {
            	
            	if (elementMap instanceof Map) {
            		jsonReponseMap = (Map<String, String>) elementMap;
            		
            		// We need to test the two json elements according to the spec.  
            		// We don't care about the key's value, just that they exist.
            		assertThat(jsonReponseMap.get("identifierType"), not(isEmptyOrNullString()));
            		assertThat(jsonReponseMap.get("description"), not(isEmptyOrNullString()));
            	}
            	
			}
            
            
        } catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>test_get_identifier_types_resources_positive_requires_BASIC_AUTH</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "None");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
        
	}
	
	

}
