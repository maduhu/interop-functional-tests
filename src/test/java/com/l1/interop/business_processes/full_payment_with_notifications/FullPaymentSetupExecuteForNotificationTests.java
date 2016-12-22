package com.l1.interop.business_processes.full_payment_with_notifications;

import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

public class FullPaymentSetupExecuteForNotificationTests {

	private static String host;
	private static String port;
	private static String url;
	
	private String account = "http://usd-ledger.example/accounts/bob";
	
	private Properties prop = new Properties();
	WebSocketContainer container = null;
	WebsocketClientEndpoint socketClient = null;

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
        url = "http://localhost:8081"; host="localhost";
        
        
        System.out.println("**************************************************************************************************************");
        System.out.println("*                                                                                                            *");
        System.out.println("*                         Tests running using the URL of :: " + url);
        System.out.println("*                                                                                                            *");
        System.out.println("**************************************************************************************************************");
        
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
        
        
        /*
         * 
         * Create the web socket server container so it can be started and listening 
         * for messages before the invoice is sent.
         * 
         */
        container = ContainerProvider.getWebSocketContainer();
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
    
    @DataProvider(name = "notification_configuration_and_data")
    private Iterator<Object []> dpNotificationConfigurationAndData( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/notifications/notification_test_data.csv");
        return testCases.iterator();
    }
    
    
    
    @Test(groups = { "notificationSetup" })
    public void setupWebSocketListener() {
    	
    	String webSocketResponseMessage = new String();
    	String webSocketListenerUri = "ws://" + host + ":8089/websocket";
    	
    	try {
    		
    		// Create the websocket "listener" for the account we want to listen on for notifications.
			socketClient = new WebsocketClientEndpoint(webSocketResponseMessage, account);
			
			System.out.println("Connecting to " + webSocketListenerUri + " and using WebSocket config: " + webSocketListenerUri);
			container.connectToServer(socketClient, URI.create(webSocketListenerUri));
			
			System.out.println("socket container just started and listening");
			
		} catch (DeploymentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
  	      
    }

    
    
    
    @Test(dataProvider="setup_positive", dependsOnGroups = {"notificationSetup"}, groups = { "paymentSetup", "payment_setup_and_execute_with_notification" })
    public void test_fullPaymentSetupAndPaymentExecution(String sender, String receiver, String amount) {
          
    	final StringWriter twriter = new StringWriter();
        
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
	         * =========================================================================================
	         *  Build JSON payload for the PUT Transfer/{id} with payload to activate the notification
	         * =========================================================================================
	         * 
	         * 
	         * 
	         */
	        
//	        {
//	        	  "id": "http://d5cad9621db2:3000/transfers/3a2a1d9e-8640-4d2d-b06c-84f2cd613204",
//	        	  "ledger": "http://d5cad9621db2:3000",
//	        	  "debits": [{
//	        	    "account": "http://d5cad9621db2:3000/accounts/alice",
//	        	    "amount": "50",
//	        	    "authorized": true
//	        	  }],
//	        	  "credits": [{
//	        	    "account": "http://d5cad9621db2:3000/accounts/bob",
//	        	    "amount": "50"
//	        	  }],
//	        	  "execution_condition": "cc:0:3:8ZdpKBDUV-KX_OnFZTsCWB_5mlCFI3DynX5f5H2dN-Y:2",
//	        	  "expires_at": "2016-09-12T00:00:01.000Z"
//	        	}
	        
        	String transferRequest = Json.createObjectBuilder()
    	        .add("id", "http://d5cad9621db2:3000/transfers/3a2a1d9e-8640-4d2d-b06c-84f2cd613204")
    	        .add("ledger", "http://d5cad9621db2:3000")
    	        .add("debits", Json.createObjectBuilder()
        			.add("account", "http://d5cad9621db2:3000/accounts/alice")
        			.add("amount", "50")
        			.add("authorized", true))
    			.add("credits", Json.createObjectBuilder()
					.add("account", "http://d5cad9621db2:3000/accounts/bob")
        			.add("amount", "50"))
    	        .add("execution_condition", "cc:0:3:8ZdpKBDUV-KX_OnFZTsCWB_5mlCFI3DynX5f5H2dN-Y:2")
    	        .add("expires_at", "2016-09-12T00:00:01.000Z")
    	        .build()
    	        .toString();
	        
        	/*
			 * 
			 * 
			 * =========================================================================================
	         *                      Step 3 -- Call service to Transfer funds 
	         * =========================================================================================
			 * 
			 * 
			 */
	        Response transferResponse = 
	        given().
	        	contentType("application/json").
	        	body(transferRequest).
	        when().
	        	put(url+"/ilp/ledger/v1/transfers/1231");
	        
	        System.out.println("*** 3: Response from transfer PUT: " + transferResponse.prettyPrint());
	        System.out.println("*** 3: http response: " + transferResponse.getStatusCode());

	        assertThat("response from transfer request = 200", transferResponse.getStatusCode(), equalTo(200));
	        
	        JsonPath transferJsonPath = transferResponse.jsonPath();
	      
    	        
    	} catch(java.lang.AssertionError e){
            captor.println("<ul>");
            captor.println("<h2>Test Case: <i>get_transfer_generate_notification_positive</i></h2>");
            captor.printf("<h3>%s</h3> %s \n","parameters: ", "No parameters");
            captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
            captor.print("<h3>Request and Response: </h3>");
            captor.println("<pre>"+twriter.toString()+"</pre>");
            captor.println("</ul>");
            
            throw e;
        }
    	
    }
    
       
    
    @Test(timeOut = 10000, dependsOnGroups = { "notificationSetup", "paymentSetup" }, groups={"payment_setup_and_execute_with_notification"}, description="test an asynchronous process receive")
    public void test_receiving_message_from_websocket() {
    	
    	boolean gotResponse = false;
    	String websocketResponseMessage = null;
    	Map<String, String> responseMap = null;
    	Object resourceObj = null;
    	boolean foundProperDebitAccount = false;
    	boolean foundProperCreditAccount = false;
    	Map<String, String> debitEntryMap = null;
    	Map<String, String> creditEntryMap = null;

	  	try {
	  		
			while (!gotResponse) {
				
				System.out.println("About to check for response from websocket");
				
			    if (socketClient.getSocketResponseMessage() != null && socketClient.getSocketResponseMessage().length() > 0) {
			    	websocketResponseMessage = socketClient.getSocketResponseMessage();
			    	System.out.println("*** Yeah!  Got a response :: " + websocketResponseMessage + " in the TestNG test.  ");
			    	
			    	Map<String, Object> creditsDebitsMap = parseResponse(websocketResponseMessage);
			    	
			    	List<String> creditsJson = (List<String>) creditsDebitsMap.get("credits");
			    	List<String> debitsJson = (List<String>) creditsDebitsMap.get("debits");
			    	
			    	if ( creditsJson != null && debitsJson != null) {
			    		
			    		if (creditsJson.size() > 0) {
			    			resourceObj = creditsJson.get(0);
			    			creditEntryMap = ((Map<String,String>) resourceObj);
			    		}
			    		
			    		if (debitsJson.size() > 0) {
			    			resourceObj = debitsJson.get(0);
			    			debitEntryMap =  ((Map<String,String>) resourceObj);
			    		}
			    		
			    		
			    		if (creditEntryMap.get("account").contains("http://usd-ledger.example/accounts/bob") && debitEntryMap.get("account").contains("http://usd-ledger.example/accounts/alice")) {
			    			
			    			System.out.println("Credit Account: " + creditEntryMap.get("account"));
			    			System.out.println("Debit Account: " + debitEntryMap.get("account"));
			    			
			    			foundProperDebitAccount = true;
			    			foundProperCreditAccount = true;
			    			gotResponse = true;
			    			break;
			    			
			    		} else {
			    			System.out.println("Did not find an appropriate response from the WebSocket response yet");
			    			Thread.sleep(1000);
			    		}
			    		
			    	} else {
		    			System.out.println("Did not find an appropriate response from the WebSocket response yet");
		    			Thread.sleep(1000);
		    		}
			    	
			    } else {
			    	Thread.sleep(1000);
			    	System.out.println("...thread sleep timer of 1 second expired waiting to hear back from websocket...");
			    }
			}
			
			assertThat(gotResponse, equalTo(true));
			assertThat(websocketResponseMessage.length(), greaterThan(0));
			assertThat(websocketResponseMessage, not(isEmptyOrNullString()));
			assertThat("Credit Account Matched", foundProperCreditAccount, equalTo(true));
			assertThat("Debit Account Matched", foundProperDebitAccount, equalTo(true));
			
			System.out.println("after message has a length of > 0!!!  That means we got a message back.");
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    
    
    	
    }
    
    
    
    
    private Map<String, Object> parseResponse(String jsonResponse) {
    	
    	Map<String, Object> creditsDebits = new HashMap<String, Object>();
    	Object credits = JsonPath.from(jsonResponse).get("params.resource.credits");
    	Object debits = JsonPath.from(jsonResponse).get("params.resource.debits");
    	creditsDebits.put("debits", debits);
    	creditsDebits.put("credits", credits);
    	
    	return creditsDebits;
    }

    
}
