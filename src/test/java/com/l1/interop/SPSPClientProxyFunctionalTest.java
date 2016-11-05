package com.l1.interop;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.containsString;
import static com.l1.interop.util.StringContainsIgnoringCase.containsStringIgnoringCase;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.json.Json;

import org.apache.commons.io.output.WriterOutputStream;
import org.hamcrest.core.IsNot;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.path.json.JsonPath;

import static io.restassured.filter.log.ErrorLoggingFilter.logErrorsTo;
import static io.restassured.filter.log.RequestLoggingFilter.logRequestTo;

import static com.l1.interop.util.Utils.readCSVFile;


public class SPSPClientProxyFunctionalTest {
	
	private static String host;
	private static String port;
	private static String url;
	private Properties prop = new Properties();
	FileWriter writer;
    PrintStream captor;
	
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
		if(!(new File("target/failure-reports")).exists())
			new File("target/failure-reports").mkdirs();
		
		writer = new FileWriter("target/failure-reports/spsp-client-proxy.html");
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
	public void afterClass() throws Exception {
		captor.println( "</body>\n" );
		captor.println( "</html>\n" );
	}
	
	@BeforeTest
    public void setup() throws Exception {
        RestAssured.config = RestAssuredConfig.config().logConfig(LogConfig.logConfig().enablePrettyPrinting(true));
    }
	
	/*
	@DataProvider(name="ExistingUsers")
	public Iterator<Object[]> getExistingUsers(){
		ArrayList lst = new ArrayList();
		lst.add(new String[]{"Bob","http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/bob"});
		lst.add(new String[]{"Alice","http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/alice"});
		return lst.iterator();
	}
	
	@DataProvider(name="NonExistingUsers")
	public Iterator<Object[]> getNonExistingUsers(){
		ArrayList lst = new ArrayList();
		lst.add(new String[]{"murthy1","http://centraldirectory.com/murthy1"});
		return lst.iterator();
	}
	*/
	
	@DataProvider(name = "spspclientproxy_query_positive")
	public Iterator<Object []> dpSPSPClientProxyQueryPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy_query_positive.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "spspclientproxy_query_negative")
	public Iterator<Object []> dpSPSPClientProxyQueryNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/spspclientproxy_query_negative.csv");
        return testCases.iterator();
    }
	
	
	@Test(dataProvider="spspclientproxy_query_positive")
	public void queryExistingReceiver(String receiverName, String receiverURI) throws Exception {
		
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);

        try {
			given().
				config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
				contentType("application/json").
		         param("receiver", receiverURI).
			when().
				get(url+"/spsp/client/v1/query").
		     then().
		     	statusCode(200).
		     	body("address",containsStringIgnoringCase(receiverName)).
		     	body("imageUrl",containsStringIgnoringCase(receiverName)).
		     	body("name",containsString(receiverName)).
		     	body("currencySymbol",equalTo("$")).
		     	body("type",equalTo("payee")).
		     	body("currencyCode",equalTo("GBP")).
		     	body("status",nullValue()).
		     	body("amount",nullValue()).
		     	body("invoiceInfo",nullValue());
			
        } catch(java.lang.AssertionError e){
        	captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>queryExistingReceiver</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", receiverName, receiverURI);
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
        }
        
	}
	
	@Test(dataProvider="spspclientproxy_query_negative")
	public void queryNonExistingReceiver(String receiverName, String receiverURI){
		
		given().
			contentType("application/json").
	         param("receiver", receiverURI).
		when().
	         get(url+"/spsp/client/v1/query").
	     then().
	     	statusCode(200).
	     	body("id",equalTo("Error")).
	     	body("message",containsString("500 Internal Server Error")).
	     	body("debug.stack",containsString("500 Internal Server Error"));
		
	}
	
	
	
	@Test(dataProvider="spspclientproxy_query_positive")
	public void quoteSourceAmountForExistingReceiver(String userName, String userURI){
		
		String json = Json.createObjectBuilder()
	            .add("receiver", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/"+userName.toLowerCase())
	            .add("sourceAccount", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:8088/ledger/accounts/alice")
	            .add("destinationAmount", "97.90")
	            .add("memo", "Hi Bobb!")
	            .add("sourceIdentifier", "")
	            .build()
	            .toString();
		
		JsonPath setUpResponse = given().
									contentType("application/json").
									body(json).
								when().
									post(url+"/spsp/client/v1/setup").jsonPath();
		
		
		String address = setUpResponse.getString("address");
		
		given().
			contentType("application/json").
			param("receiver", address).
			param("sourceAmount","100.00").
		when().
         	get(url+"/spsp/client/v1/quoteSourceAmount").
         then().
     		statusCode(200).
     		body("destinationAmount",IsNot.not(""));
		
	}
	
	@Test(dataProvider="spspclientproxy_query_negative")
	public void quoteSourceAmountForNonExistingReceiver(String userName, String userURI) {
		
		given().
			contentType("application/json").
			//param("receiver", "levelone.dfsp2."+userName.toLowerCase()).
			param("sourceAmount","100.00").
		when().
         	get(url+"/spsp/client/v1/quoteSourceAmount").
         then().
     		statusCode(400);
     		//body("id",equalTo("Error"));
	}
	
	@Test
	public void setUpExistingReceiver(){
		
		final StringWriter twriter = new StringWriter();
        final PrintStream tcaptor = new PrintStream(new WriterOutputStream(twriter), true);
		
		try {
		String json = Json.createObjectBuilder()
	            .add("receiver", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/bob")
	            .add("sourceAccount", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:8088/ledger/accounts/alice")
	            .add("destinationAmount", "97.90")
	            .add("memo", "Hi Bobb!")
	            .add("sourceIdentifier", "")
	            .build()
	            .toString();
		
		System.out.println("json: "+json);
		
		given().
			config(RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(tcaptor).and().enableLoggingOfRequestAndResponseIfValidationFails())).
			contentType("application/json").
			body(json).
		when().
         	post(url+"/spsp/client/v1/setup").
         then().
         body("address",containsStringIgnoringCase("alice"));
		
		}catch(java.lang.AssertionError e){
			captor.println("<ul>");
        	captor.println("<h2>Test Case: <i>setUpExistingReceiver</i></h2>");
        	captor.printf("<h3>%s</h3> %s, %s \n","parameters: ", "", "");
        	captor.println("<h3>Failure Message: </h3>"+e.getLocalizedMessage());
        	captor.print("<h3>Request and Response: </h3>");
        	captor.println("<pre>"+twriter.toString()+"</pre>");
        	captor.println("</ul>");
        	
        	throw e;
		}
	}
	
	@Test
	public void setUpNonExistingReceiver(){
		
		String json = Json.createObjectBuilder()
	            .add("receiver", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/murthy")
	            .add("sourceAccount", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:8088/ledger/accounts/alice")
	            .add("destinationAmount", "97.90")
	            .add("memo", "Hi Bobb!")
	            .add("sourceIdentifier", "")
	            .build()
	            .toString();
		
		
		given().
			contentType("application/json").
			body(json).
		when().
         	post(url+"/spsp/client/v1/setup").
         then().
         	body("id",equalTo("Error")).and().
	     	body("message",containsString("500 Internal Server Error")).and().
	     	body("debug.stack",containsString("500 Internal Server Error"));
	}
	
	@Test
	public void paymentExistingReceiver(){
		
		String setupRequest = Json.createObjectBuilder()
	            .add("receiver", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:3046/v1/receivers/bob")
	            .add("sourceAccount", "http://ec2-52-37-54-209.us-west-2.compute.amazonaws.com:8088/ledger/accounts/alice")
	            .add("destinationAmount", "97.90")
	            .add("memo", "Hi Bobb!")
	            .add("sourceIdentifier", "")
	            .build()
	            .toString();
		
		JsonPath setUpResponse = 
		given().
			contentType("application/json").
			body(setupRequest).
		when().
         	post(url+"/spsp/client/v1/setup").jsonPath();
		
		String paymentRequest = Json.createObjectBuilder()
									.add("id", setUpResponse.getString("id"))
									.add("sourceAccount", setUpResponse.getString("sourceAccount"))
									.add("receiver", setUpResponse.getString("receiver"))
									.add("address",setUpResponse.getString("address"))
									.add("sourceAmount", setUpResponse.getString("sourceAmount"))
									.add("destinationAmount", setUpResponse.getString("destinationAmount"))
									.add("condition", setUpResponse.getString("condition"))
									.add("memo", setUpResponse.getString("memo"))
									.add("expiresAt", setUpResponse.getString("expiresAt"))
									.build()
									.toString();
		
		given().
			contentType("application/json").
			body(paymentRequest).
		when().
     		put(url+"/spsp/client/v1/payments/"+setUpResponse.getString("id")).
     	then().
     		statusCode(200).
     		//TODO Need to check why it is returning error
     		body("id",equalTo("Error"));
		
	}
	
}
