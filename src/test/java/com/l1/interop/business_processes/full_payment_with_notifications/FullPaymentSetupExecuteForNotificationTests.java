package com.l1.interop.business_processes.full_payment_with_notifications;

import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.IsNot.not;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

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

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class FullPaymentSetupExecuteForNotificationTests {

	private static String host;
	private static String port;
	private static String url;
	private static String dfsp_username;
	private static String dfsp_password;
	
	private Properties prop = new Properties();

	FileWriter writer;
    PrintStream captor;
    
    // used for sample test of asynchronous
    boolean m_success = false;
    int testCountFromThread = 0;
    
    

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
        port = prop.getProperty("port");
        url = "http://"+host+":"+port;
        
        /*
         * 
         * Override url for local testing
         * 
         */
        url = "http://localhost:8081";
        
        
        System.out.println("**************************************************************************************************************");
        System.out.println("*                                                                                                            *");
        System.out.println("*                         Tests running using the URL of :: " + url + "   *******************");
        System.out.println("*                                                                                                            *");
        System.out.println("**************************************************************************************************************");
        
        dfsp_username = prop.getProperty("dfsp.username");
        dfsp_password = prop.getProperty("dfsp.password");
        
        if(!(new File("target/failure-reports")).exists())
            new File("target/failure-reports").mkdirs();
        
        writer = new FileWriter("target/failure-reports/FullPaymentSetupExecuteForNotificationTests.html");
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
    
    
    @DataProvider(name = "setup_positive")
    private Iterator<Object []> dpSPSPClientProxy_setupPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy/setup_positive.csv");
        return testCases.iterator();
    }
    
    
    @Test(dataProvider="setup_positive", groups = { "paymentSetup", "payment_setup_and_execute_with_notification" })
    public void test_fullPaymentSetupAndPaymentExecution(String sender, String receiver, String amount) {
          
    	String ppjson = null;
    	final StringWriter twriter = new StringWriter();
//        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
    	try {
    		
//    		What the new data format is for Bob
//    		http://ec2-35-163-231-111.us-west-2.compute.amazonaws.com:3043/v1/receivers/26547070
    		
    		String setupRequest = Json.createObjectBuilder()
    	        .add("receiver", "http://"+host+":3046/v1/receivers/"+receiver)
    	        .add("sourceAccount", "http://"+host+":8088/ledger/accounts/"+sender)
    	        .add("destinationAmount", amount)
    	        .add("memo", "Setup and Notification test")
    	        .add("sourceIdentifier", "9809890190934023")
    	        .build()
    	        .toString();
    	        
				System.out.println("1a ------------------------------- setupRequest for payment post: " + setupRequest);
	        
				
			/*
			 * 
			 * 
			 * =========================================================================================
	         *                    Step 1 -- Call service to Setup Payment request 
	         * =========================================================================================
			 * 
			 * 
			 */
	        Response response =
	        given().
	        	contentType("application/json").
	        	body(setupRequest).
	        when().
	        	post(url+"/spsp/client/v1/setup");
	        
	        System.out.println("response from HTTP POST for setup payment: " + response.prettyPrint());
	        System.out.println("============= After POST response for setup =============");
	        
	        assertThat("setup for payment worked before calling ", response.getStatusCode(), equalTo(201));
	        
	        JsonPath setUpResponse = response.jsonPath();
	       
	        
	        /*
	         * =========================================================================================
	         *                                 Validate the Setup Response 
	         * =========================================================================================
	         * 
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

	        assertThat("get senderIdentifier", senderIdentifier, not(isEmptyOrNullString()));
	        
	        
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
	        
	        
	        
	        /*
	         * =========================================================================================
	         *                         Create JSON PUT request to Execute Payment 
	         *                         
	         *              This uses generated senderIdentifier from the setup service response
	         * =========================================================================================
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
	        
	        
	        /*
			 * 
			 * 
			 * =========================================================================================
	         *                  Step 2 -- Call service to Prepare the Payment request 
	         * =========================================================================================
			 * 
			 * 
			 */
	        Response paymentResponse = 
	        given().
	        	contentType("application/json").
	        	body(paymentRequest).
	        when().
	        	put(url+"/spsp/client/v1/payments/" + setUpResponse.getString("id"));
	        
	        System.out.println("*** 2: Response from payment PUT: " + paymentResponse.prettyPrint());
	        
	        System.out.println("*** 2: http response: " + paymentResponse.getStatusCode());
	        assertThat("response from payment request = 200", paymentResponse.getStatusCode(), equalTo(200));
	        
	        
	        JsonPath paymentJsonPath = paymentResponse.jsonPath();
	        
	        
	        /*
	         * 
	         * Sample Response JSON from Payment execute:
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
				  "condition": "cc:0:3:wey2IMPk-3MsBpbOcObIbtgIMs0f7uBMGwebg1qUeyw:32",
				  "fulfillment": "cf:0:qUAo3BNo49adBtbYTab2L5jAWLpAhnrkNQamsMYjWvM",
				  "status": "executed"
				}
	         * 
	         * 
	         */
	        
	        
	        
	        
	        /*
	         * =========================================================================================
	         *                      Validate the Payment "Prepared" Response 
	         * =========================================================================================
	         * 
	         * Since we need to build a request from the data from the call above, we should 
	         * have a bunch of assertThat() to ensure all is good.  This is a bit more detailed testing
	         * 
	         */
	        assertThat("status is equal to executed", paymentJsonPath.getString("status"), equalTo("executed"));
    	        
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
    	
    	
    	
    	/*
		 * 
		 * 
		 * =========================================================================================
         *               Step 3 -- Call service to get the Transfer object request 
         * =========================================================================================
		 * 
		 * 
		 */
    	
    	
    	
    	
    }
    
    
//    @Test(description="test an asynchronous process send", groups={"send", "payment_setup_and_execute_with_notification"})
//    public void test_sample_asynchrous_call() {
//    	Thread x = new Thread() {
//    		public synchronized void run() {
//                    
//    			try {
//                    Thread.sleep(1000);
//                    m_success = true;
//                    testCountFromThread = 15;
//                    
//                    System.out.println("child thread just completed.  m_success = true and testCountFromThread = " + testCountFromThread);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//    	};
//    	
//    	x.run();
//    }
    
    
//    @Test(timeOut = 10000, dependsOnGroups = { "send" }, groups={"send", "payment_setup_and_execute_with_notification"})
//    public void waitForAnswer() throws InterruptedException {
//      while (! m_success) {
//        Thread.sleep(1000);
//      }
//      
//      assertThat("See if the sender thread set the message", testCountFromThread, equalTo(15));
//    }
    
    
    @Test(timeOut = 10000, dependsOnGroups = { "paymentSetup" }, groups={"send", "payment_setup_and_execute_with_notification"}, description="test an asynchronous process receive")
    public void test_receiving_message_from_websocket() {
    	
    	String socketeMessage = new String();
    	
    	final CountDownLatch messageLatch = new CountDownLatch(1);
    	
    	/*
    	 * Or maybe I can pass an anonomyous function into teh websociket clent endpoint as a call back.  Like to pass a function reference into the class 
    	 */
    	WebsocketClientEndpoint x = new WebsocketClientEndpoint(socketeMessage, "https://ledger.example/accounts/alice");  // TODO this needs to be pulled from a property 
    	
    	try {
    	      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    	      String uri = "ws://localhost:8089/websocket";  // TODO this needs to be pulled from a property 
    	      System.out.println("Connecting to " + uri);
    	      container.connectToServer(x, URI.create(uri));
//    	      messageLatch.await(300, TimeUnit.SECONDS);
    	      
    	      System.out.println("after messageLatch...");
    	      
    	  	while (socketeMessage.length() == 0) {
                Thread.sleep(1000);
                System.out.println("...thread sleep time expired...");
    	  	}
    	  	
    	  	System.out.println("after message has a length of > 0!!!  That means we got a message back.");
    	      
	    } catch (DeploymentException ex) {
	    	ex.printStackTrace();
	    } catch ( InterruptedException ex) {
	    	ex.printStackTrace();
	    } catch ( IOException ex) {
	    	ex.printStackTrace();
	    }
    	
    
    	
    }

    
}
