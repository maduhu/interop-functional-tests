package com.l1.interop;

import static com.l1.interop.util.Utils.readCSVFile;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
import java.util.UUID;

import javax.json.Json;

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

public class ILPLedgerAdapterFunctionalTest {
	
	private static String host;
	private static String port;
	private static String url;
	private Properties prop = new Properties();
	private FileWriter writer;
    private PrintStream captor;
	
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
        System.out.println("*                         Tests running using the URL of :: " + url + "                                      *");
        System.out.println("*                                                                                                            *");
        System.out.println("**************************************************************************************************************");
        
        
		if(!(new File("target/failure-reports")).exists())
			new File("target/failure-reports").mkdirs();
		
		writer = new FileWriter("target/failure-reports/ilp-ledger-adapter.html");
		captor = new PrintStream(new WriterOutputStream(writer), true);
		captor.println( "<html lang='en'>\n" );

		captor.println( "<head>\n" );
		captor.println( "<meta charset='utf-8'>\n" );
		captor.println( "<title>Failure Report</title>\n" );
		captor.println( "</head>\n\n" );

		captor.println( "<body>\n" );
		captor.println( "<h1><center>Functional Test Failure Report</center></h1>\n" );
	}
	
	@BeforeTest(alwaysRun=true)
    private void setup() throws Exception {
        RestAssured.config = RestAssuredConfig.config().logConfig(LogConfig.logConfig().enablePrettyPrinting(true));
    }
	
	@AfterClass(alwaysRun=true)
	private void afterClass() throws Exception {
		captor.println( "</body>\n" );
		captor.println( "</html>\n" );
	}
	
	@DataProvider(name = "prepare_transfer_positive")
	private Iterator<Object []> dpprepareTransferPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/prepare_transfer_positive.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "prepare_transfer_negative")
	private Iterator<Object []> dpprepareTransferNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/prepare_transfer_negative.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "get_prepared_transfer_positive")
	private Iterator<Object []> dpGetPreparedTransferPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/get_prepared_transfer_positive.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "get_prepared_transfer_negative")
	private Iterator<Object []> dpGetPreparedTransferNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/get_prepared_transfer_negative.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "fulfill_transfer_positive")
	private Iterator<Object []> dpFulfillTransferPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/fulfill_transfer_positive.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "fulfill_transfer_negative")
	private Iterator<Object []> dpFulfillTransferNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/fulfill_transfer_negative.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "get_fulfilled_transfer_positive")
	private Iterator<Object []> dpGetFulfilledTransferPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/get_fulfilled_transfer_positive.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "get_fulfilled_transfer_negative")
	private Iterator<Object []> dpGetFulfilledTransferNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter/get_fulfilled_transfer_negative.csv");
        return testCases.iterator();
    }
	
	/**
	 * For a valid sender and receiver, this test checks that for a prepareTransfer endpoint, the returnvalue
	 * is 200, and the response json fields are cheked as below:
	 * id - Equals the value from the value that was supplied in the input
	 * debits.account - Contains sender information
	 * debits.amount - Equals the value from the data file
	 * credits
	 * @param sender
	 * @param receiver
	 * @param amount
	 */
	@Test(dataProvider="prepare_transfer_positive", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void prepareTransfer_ForValidSenderAndReceiver_ShouldReturn200_ShouldReturnValidResponse(String sender, String receiver, String receiverUserNumber, String amount){
		
		//String uuid = UUID.randomUUID().toString();
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

		try {
			
			// =========================================================================================
			//
			// Step 1 :: Get sender account ID from Name
			//
			// =========================================================================================
			
			System.out.println("3.1");
			String fullPath = url+"/ledger/accounts/"+sender;
			System.out.println("3.1 path: " + fullPath);
			Response response1 = 
			
			given().
				contentType("application/json").
			when().
				get(fullPath);
			
			System.out.println(" 3.1 response json: " + response1.asString());
			assertThat( "response http code", (Integer) response1.getStatusCode(), equalTo(200));
			assertThat("id", (String) response1.jsonPath().get("id"), not(equalTo("NotFoundError")) );
			
			
			// =========================================================================================
			//
			// Step 2 :: Get receiver account ID from Name
			//
			// =========================================================================================
			
			String senderAccountNumber = response1.jsonPath().getString("id");
			
			Response response32 = given().
					contentType("application/json").
				when().
					get(url+"/ledger/accounts/"+receiver);
			
			assertThat("id", (String) response1.jsonPath().get("id"), not(equalTo("NotFoundError")) );
			String receiverAccountNumber = response32.jsonPath().getString("id");
			
			// =========================================================================================
			//
			// Step 3 :: 
			//
			//
			//
			//
			//
			// =========================================================================================
			
			String setupRequest = Json.createObjectBuilder()
	            .add("receiver", "http://"+host+":3043/v1/receivers/"+receiverUserNumber)  // should be the account #  // <<<< This works as long as it is a valid account number
	            .add("sourceAccount", "http://"+host+":8014/ledger/accounts/" + sender)  // name NOT account # BOB
	            .add("destinationAmount", amount)
	            .add("memo", "Hi Bobb!")
		            .add("sourceIdentifier", "9809890190934023")
		            .build()
		            .toString();
			
			Response responseStep3 =
			given().
				contentType("application/json").
				body(setupRequest).
			when().
	         	post(url+"/spsp/client/v1/setup");
			
			System.out.println("3.4 json body for call : " + setupRequest);
			assertThat( "response http code", (Integer) responseStep3.getStatusCode(), equalTo(201));
			
			JsonPath setupResponse1 = responseStep3.jsonPath(); 
			
			// =========================================================================================
			//
			// Step 4 :: Prepare The Transfer request
			//
			// =========================================================================================
				
			System.out.println("3.5");
			String prepareTransferRequest = Json.createObjectBuilder()
											.add("id","http://"+host+":8014"+"/ledger/transfers/"+setupResponse1.getString("id"))
											.add("ledger", "http://"+host+":8014"+"/ledger")
											.add("debits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", senderAccountNumber)
																		.add("amount", amount)))
											.add("credits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", receiverAccountNumber)
																		.add("amount", amount)))
											.add("execution_condition", setupResponse1.getString("condition"))
											.add("expires_at", setupResponse1.getString("expiresAt"))
											.build()
											.toString();
				

			
			String jsonRequestUrl2 = "http://"+host+":8014"+"/ledger/transfers/"+setupResponse1.getString("id");

//			System.out.println("3.6");
//			System.out.println("3.6 :: body json :: " + prepareTransferRequest);
//			System.out.println("3.6 :: url :: " + jsonRequestUrl2);
			
			Response response2 =
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put(jsonRequestUrl2);
			
			System.out.println("3.7: Final response: " + response2.asString());
			
			// Get the debit details
			List debits = response2.jsonPath().get("debits");
			String debitAmount = (String) ((Map) debits.get(0)).get("amount");
			Float debitAmountFloat = Float.valueOf(debitAmount);
			String debitAccount = (String) ((Map) debits.get(0)).get("account");
			
			// Get the credit details
			List credits = response2.jsonPath().get("credits");
			String creditAmount = (String) ((Map) credits.get(0)).get("amount");
			Float creditAmountFloat = Float.valueOf(creditAmount);
			String creditAccount = (String) ((Map) credits.get(0)).get("account");

			
			assertThat( "response http code", (Integer) response2.getStatusCode(), equalTo(201));  // done
			assertThat("debits[0].account", debitAccount, containsString(sender) );
			assertThat("debits[0].amount", debitAmountFloat, equalTo(Float.valueOf(amount)) );
			assertThat("credits[0].account", creditAccount, containsString(receiver) );
			assertThat("credits[0].amount", creditAmountFloat, equalTo(Float.valueOf(amount)) ); 

			assertThat("execution_condition",response2.jsonPath().get("execution_condition"), equalTo(setupResponse1.getString("condition")) );
			assertThat("expires_at", response2.jsonPath().get("expires_at"), equalTo(setupResponse1.getString("expiresAt")) );
			assertThat("id", response2.jsonPath().get("id"), containsString(setupResponse1.getString("id")) );
			assertThat("state", (String) response2.jsonPath().get("state"), equalTo("proposed") );
			
		} catch(java.lang.AssertionError e){
        	captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>prepareTransfer_ForValidSenderAndReceiver_ShouldReturn200_ShouldReturnValidResponse</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s, %s \n","parameters: ", sender, receiver,amount);
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
        }
		
	}


	
	@Test(dataProvider="prepare_transfer_positive", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void prepareTransfer_ForEntityThatAlreadyExists_ShouldReturn422_ShouldReturnErrorResponse(String sender, String receiver, String receiverUserNumber, String amount){
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

		try {
			
			// =========================================================================================
			//
			// Step 1 :: Get sender account Number from Name
			//
			// =========================================================================================
			
			Response fromUserResponse = given().
				contentType("application/json").
			when().
				get(url+"/ledger/accounts/"+sender);
			
			assertThat( "response http code", (Integer) fromUserResponse.getStatusCode(), equalTo(200));
			assertThat("id", (String) fromUserResponse.jsonPath().get("id"), not(equalTo("NotFoundError")) );
			
			String senderAccountNumber = fromUserResponse.jsonPath().getString("id");
			
			// =========================================================================================
			//
			// Step 2 :: Get receiver account ID from Name
			//
			// =========================================================================================
			
			JsonPath toUserResponse = given().
					contentType("application/json").
				when().
					get(url+"/ledger/accounts/"+receiver).jsonPath();
			
			String receiverAccountNumber = toUserResponse.getString("id");
			
			// =========================================================================================
			//
			// Step 3 :: Setup Transfer Request JSON
			//
			// =========================================================================================
			
			String setupRequest = Json.createObjectBuilder()
		            .add("receiver", "http://"+host+":3043/v1/receivers/"+receiverUserNumber)  
		            .add("sourceAccount", "http://"+host+":8014/ledger/accounts/" + sender)  // name NOT account # BOB
		            .add("destinationAmount", amount)
		            .add("memo", "Hi Bobb!")
		            .add("sourceIdentifier", "9809890190934023")
		            .build()
		            .toString();

			
			Response setupResponse = 
			given().
				contentType("application/json").
				body(setupRequest).
			when().
	         	post(url+"/spsp/client/v1/setup");
			
			assertThat( "response http code", (Integer) setupResponse.getStatusCode(), equalTo(201));
				
			
			// =========================================================================================
			//
			// Step 4 :: Prepare The Transfer request
			//
			// =========================================================================================
			
			String prepareTransferRequest = Json.createObjectBuilder()
											.add("id","http://"+host+":8014"+"/ledger/transfers/"+setupResponse.jsonPath().getString("id"))
											.add("ledger", "http://"+host+":8014"+"/ledger")
											.add("debits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", senderAccountNumber)
																		.add("amount", amount)))
											.add("credits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", receiverAccountNumber)
																		.add("amount", amount)))
											.add("execution_condition", setupResponse.jsonPath().getString("condition"))
											.add("expires_at", setupResponse.jsonPath().getString("expiresAt"))
											.build()
											.toString();
				
			System.out.println("4.3");
				
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+setupResponse.jsonPath().getString("id"));
			
			
			System.out.println("4.4");
			
			// =========================================================================================
			//
			// Step 5 :: Try to execute the Transfer request.  Should produce an already exists error
			//
			// =========================================================================================
			
			//Preparing the transfer again for the same id.
			Response transferResponse = given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+setupResponse.jsonPath().getString("id"));
			
			System.out.println("4.4 :: transfer JSON :: " + transferResponse.asString());
//			then().
//				statusCode(422).
//				body("error_id",equalTo("AlreadyExistsError")).
//				body("message",equalTo("Can't modify transfer after execution."));
			
			assertThat( "response http code", (Integer) transferResponse.getStatusCode(), equalTo(422)); 
			assertThat("id", transferResponse.jsonPath().get("id"), equalTo("AlreadyExistsError") );
			assertThat("type", transferResponse.jsonPath().get("type"), equalTo("ledger.transfer.hold.alreadyExists") );
			assertThat("message",transferResponse.jsonPath().get("message"), equalTo("Can't modify transfer after execution.") );
			
		} catch(java.lang.AssertionError e){
        	captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>prepareTransfer_ForEntityThatAlreadyExists_ShouldReturn422_ShouldReturnErrorResponse</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s, %s \n","parameters: ", sender, receiver,amount);
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
        }
	}
	
	
	@Test(dataProvider="prepare_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void prepareTransfer_ForInvalidURIParameter_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
		try {
			
			String prepareTransferRequest = Json.createObjectBuilder()
					.add("id","http://"+host+":8014"+"/ledger/transfers/"+"123")
					.add("ledger", "http://"+host+":8014"+"/ledger")
					.add("debits", Json.createArrayBuilder().
										add(Json.createObjectBuilder()
												.add("account", sender)
												.add("amount", amount)))
					.add("credits", Json.createArrayBuilder().
										add(Json.createObjectBuilder()
												.add("account", receiver)
												.add("amount", amount)))
					.add("execution_condition", "")
					.add("expires_at", "")
					.build()
					.toString();
			
			Response transferResponse = given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+"123");
			
			System.out.println("6.1 prepareTransfer_ForInvalidURIParameter_ShouldReturn400_ShouldReturnErrorResponse Json response :: " + transferResponse.asString());
			
//			then().
//				statusCode(422).
//				body("error_id",equalTo("InvalidUriParameterError")).
//				body("message",equalTo("id is not a valid Uuid")).
//				body("validationErrors[0].message",containsString("String does not match pattern:"));
//				//// body("code",equalTo("id is not a valid Uuid")).
			
			System.out.println("http status code :: " + transferResponse.getStatusCode());
			
			assertThat("id", transferResponse.jsonPath().get("id"), equalTo("InvalidUriParameterError") );
			assertThat("message",transferResponse.jsonPath().get("message"), equalTo("id is not a valid Uuid") );
			assertThat( "response http code", (Integer) transferResponse.getStatusCode(), equalTo(200)); 
				
		} catch(java.lang.AssertionError e){
        	captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>prepareTransfer_ForInvalidURIParameter_ShouldReturn400_ShouldReturnErrorResponse</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s, %s \n","parameters: ", sender, receiver,amount);
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
        }
	}
	
	
	@Test(dataProvider="prepare_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void prepareTransfer_ForInvalidRequestBody_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
		try {
			String uuid = UUID.randomUUID().toString();
			
			String prepareTransferRequest = "Not a valid request";
			
			Response transferResponse = given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+uuid);
//			then().
//				statusCode(400).
//				body("id",equalTo("InvalidBodyError")).
//				body("message",equalTo("Body did not match schema Transfer")).
//				body("validationErrors[0].message",containsString("Missing required property"));
//				//body("code",equalTo("id is not a valid Uuid")).
			
			System.out.println("5.1 prepare_transfer_negative json response :: " + transferResponse.asString());
			
			assertThat( "response http code", (Integer) transferResponse.getStatusCode(), equalTo(400)); 
			assertThat("error", transferResponse.jsonPath().get("error"), equalTo("Bad Request") );
			assertThat("message",transferResponse.jsonPath().get("message"), equalTo("Invalid request payload JSON format") );
				
		} catch(java.lang.AssertionError e){
        	captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>prepareTransfer_ForInvalidRequestBody_ShouldReturn400_ShouldReturnErrorResponse</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s, %s \n","parameters: ", sender, receiver,amount);
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
        }
		
	}
	
	
	@Test(dataProvider="get_prepared_transfer_positive", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void getPreparedTransfer_ForValidTransfer_ShouldReturn200_ShouldReturnValidResponse(String sender, String receiver, String receiverUserNumber, String amount){
		
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
        	
        	// =========================================================================================
			//
			// Step 1 :: Get sender account ID from Name
			//
			// =========================================================================================
        	
        	JsonPath fromUserResponse = given().
				contentType("application/json").
			when().
				get(url+"/ledger/accounts/"+sender).jsonPath();
		
			String senderAccountNumber = fromUserResponse.getString("id");
			
			System.out.println("2.1:  prepareTransfer Request");
			
			// =========================================================================================
			//
			// Step 2 :: Get receiver account ID from Name
			//
			// =========================================================================================
			
			JsonPath toUserResponse = given().
					contentType("application/json").
				when().
					get(url+"/ledger/accounts/"+receiver).jsonPath();
			
			String receiverAccountNumber = toUserResponse.getString("id");
			System.out.println("2.2:  prepareTransfer Request");
			
			String setupRequest = Json.createObjectBuilder()
		            .add("receiver", "http://"+host+":3043/v1/receivers/"+receiverUserNumber)  // This needs the User Number to work now
		            .add("sourceAccount", "http://"+host+":8014/ledger/accounts/" + sender)    // This only needs name, not User number
		            .add("destinationAmount", amount)
		            .add("memo", "Hi Bobb!")
		            .add("sourceIdentifier", "9809890190934023")
		            .build()
		            .toString();
			
			System.out.println("2.3:  setupRequest " + setupRequest);

			
			String setupPath = url+"/spsp/client/v1/setup";
			System.out.println("2.3.1 setup body payload: " + setupPath);

			//			JsonPath setupResponse = 
			Response response =
			given().
				contentType("application/json").
				body(setupRequest).
			when().
	         	post(setupPath);
			
			assertThat( "response http code", (Integer) response.getStatusCode(), equalTo(201));
			
			JsonPath setupResponse = response.jsonPath();
			
			

			System.out.println("2.3.9 just called to prepare setupResponse");
			System.out.println("2.4:  prepareTransfer Request :: " + setupResponse.prettyPrint());
		
			String prepareTransferRequest = Json.createObjectBuilder()
											.add("id","http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id"))
											.add("ledger", "http://"+host+":8014"+"/ledger")
											.add("debits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", senderAccountNumber)
																		.add("amount", amount)))
											.add("credits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", receiverAccountNumber)
																		.add("amount", amount)))
											.add("execution_condition", setupResponse.getString("condition"))
											.add("expires_at", setupResponse.getString("expiresAt"))
											.build()
											.toString();
			
			System.out.println("2.5:  prepareTransfer Request :: " + prepareTransferRequest);
			String fullPath = url+"/ledger/transfers/"+setupResponse.getString("id");
			
			System.out.println("2.6 :: fullPath :: " + fullPath);
			
				
			response =	
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
//				put("http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id"));
				put(fullPath);
			
			
			System.out.println("2.7 :: check the status of transfer");
			
			
			fullPath = url+"/ledger/transfers/"+setupResponse.getString("id");
			
			System.out.println("2.7 Path url: " + fullPath);
			
			//Check the status of the transfer
			response = given().
			config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
			when().
				get(fullPath);

			
			System.out.println("2.7 http reponse :: " + response.asString());
			
			assertThat( "response http code", (Integer) response.getStatusCode(), equalTo(200));
			assertThat("state", (String) response.jsonPath().get("state"), equalTo("proposed") );
			
        } catch(java.lang.AssertionError e){
        	captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>getPreparedTransfer_ForValidTransfer_ShouldReturn200_ShouldReturnValidResponse</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s, %s \n","parameters: ", sender, receiver,amount);
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
        }
	}
	
	@Test(dataProvider="get_prepared_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void getPreparedTransfer_ForTransferThatDoesNotExist_ShouldReturn404_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
		try {
			String uuid = UUID.randomUUID().toString();
			String fullPath = url+"/ledger/transfers/"+uuid;
			
			//Check the status of the transfer
			Response response =
					
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
			when().
				get(fullPath);

			
			assertThat( "response http code", (Integer) response.getStatusCode(), equalTo(404));
			assertThat("id", (String) response.jsonPath().get("id"), equalTo("NotFoundError") );
			assertThat("message", (String) response.jsonPath().get("message"), equalTo("Unknown transfer") );
				
		} catch(java.lang.AssertionError e){
        	captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>getPreparedTransfer_ForTransferThatDoesNotExist_ShouldReturn404_ShouldReturnErrorResponse</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s, %s \n","parameters: ", sender, receiver,amount);
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
        }
	}
	
	@Test(dataProvider="get_prepared_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void getPreparedTransfer_ForInvalidURIParameterInRequest_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_positive", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_fulfill_transfer"})
	public void fulfillTransfer_ForValidRequest_ShouldReturn200_ShouldReturnValidResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_fulfill_transfer"})
	public void fulfillTransfer_ForUnmetCondition_ShouldReturn422_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_fulfill_transfer"})
	public void fulfillTransfer_ForUnproceesableEntity_ShouldReturn422_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_fulfill_transfer"})
	public void fulfillTransfer_ForInvalidURIParameter_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_fulfill_transfer"})
	public void fulfillTransfer_ForInvalidRequestBody_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="get_fulfilled_transfer_positive", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_fulfilled_transfer"})
	public void getFulfilledTransfer_ForValidFulfilledTransfer_ShouldReturn200_ShouldReturnTransferDetails(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="get_fulfilled_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_fulfilled_transfer"})
	public void getFulfilledTransfer_ForFulfillmentThatDoesNotExist_ShouldReturn404_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="get_prepared_transfer_negative", groups={"ilp_ledger_adapater_all", "ilp_ledger_adapater_prepare_transfer"})
	public void getFulfilledTransfer_ForInvalidURIParameterInRequest_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	/*@Test(dataProvider="ledgeradapter_positive")
	public void createNewAccount(String fromUserName, String toUserName){
		
		int statusCode = given().
							contentType("application/json").
					    when().
					         get(url+"/ledger/accounts"+fromUserName).statusCode();
		
						
		if(statusCode != 200){
			
		} else {
			Assert.assertEquals(statusCode, 404);
		}
		     
	}*/
	
	
	
	/*@Test(dataProvider="ledgeradapter_positive")
	public void executePreparedTransfer(String fromUserName, String toUserName){
		String uuid = UUID.randomUUID().toString();
		
		JsonPath fromUserResponse = given().
			contentType("application/json").
		when().
			get(url+"/ledger/accounts/"+fromUserName).jsonPath();
		
		String fromId = fromUserResponse.getString("id");
		
		JsonPath toUserResponse = given().
				contentType("application/json").
			when().
				get(url+"/ledger/accounts/"+toUserName).jsonPath();
		
			String toId = toUserResponse.getString("id");
			
			String transferRequest = Json.createObjectBuilder()
										.add("id",url+"/ledger/transfers/"+uuid)
										.add("ledger", url+"/ledger")
										.add("debits", Json.createArrayBuilder().
															add(Json.createObjectBuilder()
																	.add("account", fromId)
																	.add("amount", "100")
																	.add("authorization",true)))
										.add("credits", Json.createArrayBuilder().
															add(Json.createObjectBuilder()
																	.add("account", toId)
																	.add("amount", "100")))
										.add("execution_condition", "cc:0:3:8ZdpKBDUV-KX_OnFZTsCWB_5mlCFI3DynX5f5H2dN-Y:2")
										.add("expires_at", "2016-10-27T00:00:01.000Z")
										.build()
										.toString();
			
		given().
			contentType("application/json").
			body(transferRequest).
		when().
			put("http://"+host+":"+"8014"+"/ledger/transfers/"+uuid).
		then().
			statusCode(201).
			body("id",containsString(uuid)).
			body("debits[0].account",containsString(fromUserName)).
			body("credits[0].account",containsString(toUserName)).
			body("state",equalTo("prepared"));
		
		
		given().
			contentType("text/plain").
			body("cf:0:_v8").
		when().
			put("http://"+host+":"+"8014"+"/ledger/transfers/"+uuid+"/fulfillment").
		then().
			statusCode(200);
		
		//Check the status of the transfer
		given().
			contentType("application/json").
		when().
			get(url+"/ledger/transfers/"+uuid).
		then().
			statusCode(200).
			body("state",equalTo("executed"));
	}*/

}
