package com.l1.interop;

import static com.l1.interop.util.Utils.readCSVFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
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

import static io.restassured.RestAssured.*;

public class ILPLedgerAdapterFunctionalTest {
	
	private static String host;
	private static String port;
	private static String url;
	private Properties prop = new Properties();
	private FileWriter writer;
    private PrintStream captor;
	
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
	
	@BeforeTest
    private void setup() throws Exception {
        RestAssured.config = RestAssuredConfig.config().logConfig(LogConfig.logConfig().enablePrettyPrinting(true));
    }
	
	@AfterClass
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
	@Test(dataProvider="prepare_transfer_positive")
	public void prepareTransfer_ForValidSenderAndReceiver_ShouldReturn200_ShouldReturnValidResponse(String sender, String receiver, String amount){
		
		//String uuid = UUID.randomUUID().toString();
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

		try {
			JsonPath fromUserResponse = given().
				contentType("application/json").
			when().
				get(url+"/ledger/accounts/"+sender).jsonPath();
			String fromId = fromUserResponse.getString("id");
			
			JsonPath toUserResponse = given().
					contentType("application/json").
				when().
					get(url+"/ledger/accounts/"+receiver).jsonPath();
			String toId = toUserResponse.getString("id");
			
			String setupRequest = Json.createObjectBuilder()
		            .add("receiver", "http://"+host+":3046/v1/receivers/"+receiver)
		            .add("sourceAccount", "http://"+host+":8088/ledger/accounts/"+sender)
		            .add("destinationAmount", amount)
		            .add("memo", "Hi Bobb!")
		            .add("sourceIdentifier", "")
		            .build()
		            .toString();
			
			JsonPath setupResponse = 
			given().
				contentType("application/json").
				body(setupRequest).
			when().
	         	post(url+"/spsp/client/v1/setup").jsonPath();
				
			String prepareTransferRequest = Json.createObjectBuilder()
											.add("id","http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id"))
											.add("ledger", "http://"+host+":8014"+"/ledger")
											.add("debits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", fromId)
																		.add("amount", amount)))
											.add("credits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", toId)
																		.add("amount", amount)))
											.add("execution_condition", setupResponse.getString("condition"))
											.add("expires_at", setupResponse.getString("expiresAt"))
											.build()
											.toString();
				
				
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id")).
			then().
				statusCode(201).
				body("id",containsString(setupResponse.getString("id"))).
				body("debits[0].account",containsString(sender)).
				body("debits[0].amount",equalTo(amount)).
				body("credits[0].account",containsString(receiver)).
				body("credits[0].amount",equalTo(amount)).
				body("execution_condition",equalTo(setupResponse.getString("condition"))).
				body("expires_at",equalTo(setupResponse.getString("expiresAt"))).
				body("state",equalTo("proposed"));
			
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
	
	@Test(dataProvider="prepare_transfer_negative")
	public void prepareTransfer_ForUnproceesableEntity_ShouldReturn422_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="prepare_transfer_positive")
	public void prepareTransfer_ForEntityThatAlreadyExists_ShouldReturn422_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

		try {
			JsonPath fromUserResponse = given().
				contentType("application/json").
			when().
				get(url+"/ledger/accounts/"+sender).jsonPath();
			String fromId = fromUserResponse.getString("id");
			
			JsonPath toUserResponse = given().
					contentType("application/json").
				when().
					get(url+"/ledger/accounts/"+receiver).jsonPath();
			String toId = toUserResponse.getString("id");
			
			String setupRequest = Json.createObjectBuilder()
		            .add("receiver", "http://"+host+":3046/v1/receivers/"+receiver)
		            .add("sourceAccount", "http://"+host+":8088/ledger/accounts/"+sender)
		            .add("destinationAmount", amount)
		            .add("memo", "Hi Bobb!")
		            .add("sourceIdentifier", "")
		            .build()
		            .toString();
			
			JsonPath setupResponse = 
			given().
				contentType("application/json").
				body(setupRequest).
			when().
	         	post(url+"/spsp/client/v1/setup").jsonPath();
				
			String prepareTransferRequest = Json.createObjectBuilder()
											.add("id","http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id"))
											.add("ledger", "http://"+host+":8014"+"/ledger")
											.add("debits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", fromId)
																		.add("amount", amount)))
											.add("credits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", toId)
																		.add("amount", amount)))
											.add("execution_condition", setupResponse.getString("condition"))
											.add("expires_at", setupResponse.getString("expiresAt"))
											.build()
											.toString();
				
				
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id"));
			
			//Preparing the transfer again for the same id.
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id")).
			then().
				statusCode(422).
				body("error_id",equalTo("AlreadyExistsError")).
				body("message",equalTo("Can't modify transfer after execution."));
			
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
	
	@Test(dataProvider="prepare_transfer_negative")
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
			
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+"123").
			then().
				statusCode(422).
				body("error_id",equalTo("InvalidUriParameterError")).
				body("message",equalTo("id is not a valid Uuid")).
				body("validationErrors[0].message",containsString("String does not match pattern:"));
				//body("code",equalTo("id is not a valid Uuid")).
				
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
	
	@Test(dataProvider="prepare_transfer_negative")
	public void prepareTransfer_ForInvalidRequestBody_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
		try {
			String uuid = UUID.randomUUID().toString();
			
			String prepareTransferRequest = "Not a valid request";
			
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+uuid).
			then().
				statusCode(400).
				body("error_id",equalTo("InvalidBodyError")).
				body("message",equalTo("Body did not match schema Transfer")).
				body("validationErrors[0].message",containsString("Missing required property"));
				//body("code",equalTo("id is not a valid Uuid")).
				
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
	
	@Test(dataProvider="get_prepared_transfer_positive")
	public void getPreparedTransfer_ForValidTransfer_ShouldReturn200_ShouldReturnValidResponse(String sender, String receiver, String amount){
		
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
        try {
		JsonPath fromUserResponse = given().
				contentType("application/json").
			when().
				get(url+"/ledger/accounts/"+sender).jsonPath();
			String fromId = fromUserResponse.getString("id");
			
			JsonPath toUserResponse = given().
					contentType("application/json").
				when().
					get(url+"/ledger/accounts/"+receiver).jsonPath();
			String toId = toUserResponse.getString("id");
			
			String setupRequest = Json.createObjectBuilder()
		            .add("receiver", "http://"+host+":3046/v1/receivers/"+receiver)
		            .add("sourceAccount", "http://"+host+":8088/ledger/accounts/"+sender)
		            .add("destinationAmount", amount)
		            .add("memo", "Hi Bobb!")
		            .add("sourceIdentifier", "")
		            .build()
		            .toString();
			
			JsonPath setupResponse = 
			given().
				contentType("application/json").
				body(setupRequest).
			when().
	         	post(url+"/spsp/client/v1/setup").jsonPath();
				
			String prepareTransferRequest = Json.createObjectBuilder()
											.add("id","http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id"))
											.add("ledger", "http://"+host+":8014"+"/ledger")
											.add("debits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", fromId)
																		.add("amount", amount)))
											.add("credits", Json.createArrayBuilder().
																add(Json.createObjectBuilder()
																		.add("account", toId)
																		.add("amount", amount)))
											.add("execution_condition", setupResponse.getString("condition"))
											.add("expires_at", setupResponse.getString("expiresAt"))
											.build()
											.toString();
				
				
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
				body(prepareTransferRequest).
			when().
				put("http://"+host+":8014"+"/ledger/transfers/"+setupResponse.getString("id"));
			
			//Check the status of the transfer
			given().
			config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
			when().
				get(url+"/ledger/transfers/"+setupResponse.getString("id")).
			then().
				statusCode(200).
				//TODO Need to check why it is proposed instead of prepared
				body("state",equalTo("proposed"));
			
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
	
	@Test(dataProvider="get_prepared_transfer_negative")
	public void getPreparedTransfer_ForTransferThatDoesNotExist_ShouldReturn404_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
        
		try {
			String uuid = UUID.randomUUID().toString();
			
			//Check the status of the transfer
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
			when().
				get(url+"/ledger/transfers/"+uuid).
			then().
				statusCode(404).
				body("error_id",equalTo("NotFoundError")).
				body("message",equalTo("Unknown transfer."));
				
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
	
	@Test(dataProvider="get_prepared_transfer_negative")
	public void getPreparedTransfer_ForInvalidURIParameterInRequest_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_positive")
	public void fulfillTransfer_ForValidRequest_ShouldReturn200_ShouldReturnValidResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative")
	public void fulfillTransfer_ForUnmetCondition_ShouldReturn422_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative")
	public void fulfillTransfer_ForUnproceesableEntity_ShouldReturn422_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative")
	public void fulfillTransfer_ForInvalidURIParameter_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="fulfill_transfer_negative")
	public void fulfillTransfer_ForInvalidRequestBody_ShouldReturn400_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="get_fulfilled_transfer_positive")
	public void getFulfilledTransfer_ForValidFulfilledTransfer_ShouldReturn200_ShouldReturnTransferDetails(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="get_fulfilled_transfer_negative")
	public void getFulfilledTransfer_ForFulfillmentThatDoesNotExist_ShouldReturn404_ShouldReturnErrorResponse(String sender, String receiver, String amount){
		
	}
	
	@Test(dataProvider="get_prepared_transfer_negative")
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
