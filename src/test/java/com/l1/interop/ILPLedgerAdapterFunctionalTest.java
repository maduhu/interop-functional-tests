package com.l1.interop;

import static com.l1.interop.util.Utils.readCSVFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.json.Json;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.*;

public class ILPLedgerAdapterFunctionalTest {
	
	private static String host;
	private static String port;
	private static String url;
	private Properties prop = new Properties();
	
	@BeforeClass
	public void beforeClass() throws Exception {
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
		
	}
	
	@DataProvider(name = "ledgeradapter_positive")
	public Iterator<Object []> dpSPSPClientProxyQueryPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter_positive.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "ledgeradapter_negative")
	public Iterator<Object []> dpSPSPClientProxyQueryNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/ledgeradapter_negative.csv");
        return testCases.iterator();
    }
	
	@Test
	public void accounts(){
		given().
			contentType("application/json").
		when().
			get(url+"/ledger/accounts/").
		then().
			//TODO Need to check why the status is an error
			statusCode(404);
	}
	
	@Test(dataProvider="ledgeradapter_positive")
	public void createNewAccount(String fromUserName, String toUserName){
		
		int statusCode = given().
							contentType("application/json").
					    when().
					         get(url+"/ledger/accounts"+fromUserName).statusCode();
		
						
		if(statusCode != 200){
			
		} else {
			Assert.assertEquals(statusCode, 404);
		}
		     
	}
	
	@Test(dataProvider="ledgeradapter_positive")
	public void prepareTransferForExistingUsers(String fromUserName, String toUserName){
		
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
																	.add("authorized",true)))
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
			body("state",equalTo("proposed"));
		
		//Check the status of the transfer
		given().
			contentType("application/json").
		when().
			get(url+"/ledger/transfers/"+uuid).
		then().
			statusCode(200).
			body("state",equalTo("proposed"));
	}
	
	@Test(dataProvider="ledgeradapter_positive")
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
			body("state",equalTo("proposed"));
		
		
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
	}

}
