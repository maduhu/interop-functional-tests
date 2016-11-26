package com.l1.interop;

import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
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


public class UserRegistrationFunctionalTests {

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
        
        System.out.println(">>>>>>>>  host being used in the User-Registration functional test: " + host + " and the port # : " + port);
        
//        dfsp_username = prop.getProperty("dfsp.username");
//        dfsp_password = prop.getProperty("dfsp.password");
        
        if(!(new File("target/failure-reports")).exists())
            new File("target/failure-reports").mkdirs();
        
        writer = new FileWriter("target/failure-reports/user-registration.html");
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
	@Test
	public void test_positive_Create_New_User_full_end_to_end() {
		
		Response response;
        String urlPath = "/directory/v1/user-registration/users";
        
        int http_status;
        
        Map<String, Object> jsonReponseMap = null;
        
        final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        String createUserUrl = "http://somefancydomain/create/user/" + Long.toString(System.currentTimeMillis());
        System.out.println("userUrl: " + createUserUrl);
        
        try {
            
        	/*
        	 * Create JSON request body 
        	 */
        	String createUserRegistration = Json.createObjectBuilder()
		        .add("url", createUserUrl )
		        .build()
		        .toString();
        	
        	response =
            given().
            	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
            	contentType("application/json").
            	body(createUserRegistration).
            when().
            	post(url+urlPath);
        	
        	http_status = response.getStatusCode();
        	
        	assertThat(response.getStatusCode(), equalTo(201));
            
        	/*
        	 * Here is a sample response:
        	 * 
        	 * {
				  "url": "http://somedomain/users/2016163702",
				  "number": "25433748"
				}
			 *
        	 */
            jsonReponseMap = JsonTransformer.stringToMap( response.prettyPrint() );
            
            System.out.println("response number: " + jsonReponseMap.get("number"));
            
            
            
            
            
            
            
            /*
             * Now call resource to query back our newly created user
             * sample Url: http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:8088/directory/v1/user-registration/users/31493294
             */
            
            // create path using the number returned on the post to create the URI parameter
            urlPath = urlPath + "/" + jsonReponseMap.get("number");
            
            JsonPath getResponse =
                    given().
                    	config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
                    	contentType("application/json").
                    when().
                    	get(url+urlPath).
                    then().
                    	statusCode(200).extract().jsonPath();
            
            System.out.println("response to get user: " + getResponse.toString());
            
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
}
