package com.l1.interop;

import static io.restassured.RestAssured.*;
import static com.l1.interop.util.Utils.readCSVFile;
import static org.hamcrest.CoreMatchers.containsString;
import static com.l1.interop.util.StringContainsIgnoringCase.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.json.Json;

import org.hamcrest.core.IsNot;
import org.hamcrest.text.IsEmptyString;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class DirectoryFunctionalTest {
	
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
	
	
	/*@DataProvider(name="ExistingUsers")
	public Iterator<Object[]> getExistingUsers(){
		ArrayList lst = new ArrayList();
		lst.add(new String[]{"bob","http://centraldirectory.com/bob"});
		lst.add(new String[]{"alice","http://centraldirectory.com/alice"});
		return lst.iterator();
	}
	
	@DataProvider(name="NonExistingUsers")
	public Iterator<Object[]> getNonExistingUsers(){
		ArrayList lst = new ArrayList();
		lst.add(new String[]{"murthy1","http://centraldirectory.com/murthy1"});
		return lst.iterator();
	}*/
	
	@DataProvider(name = "directory_positive")
	public Iterator<Object []> dpDirectoryPositive( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/directory_positive.csv");
        return testCases.iterator();
    }
	
	@DataProvider(name = "directory_negative")
	public Iterator<Object []> dpDirectoryNegative( ) throws Exception
    {
        List<Object []> testCases = readCSVFile("test-data/directory_negative.csv");
        return testCases.iterator();
    }

	@Test(dataProvider="directory_positive",description="Look up users that are already added to the Directory.")
	public void lookupExistingUser(String userName){
		
		String requestBody = getJSONData(userName);
		
		given().
			contentType("application/json").
	         body(requestBody).
		when().
	         post(url+"/directory/v1/user/get").
	     then().
	     	statusCode(200).
	     	body("id",equalTo("")).
	        body("result.account", containsString(userName));		
	}
	
	
	@Test(dataProvider="directory_negative",description="Look up users that are not added to the Directory.")
	public void lookupNonExistingUser(String userName){
		
		String requestBody = getJSONData(userName);
		
		given().
			contentType("application/json").
	         body(requestBody).
		when().
	         post(url+"/directory/v1/user/get").
	     then().
	     	statusCode(200).
	     	body("error.message",IsNot.not(""));
	        		
	}
	
	public String getJSONData(String userName){
		return Json.createObjectBuilder()
	            .add("jsonrpc", "2.0")
	            .add("id", "45567")
	            .add("method", "directory.user.get")
	            .add("params", Json.createObjectBuilder().add("userURI", "http://centraldirectory.com/"+userName))
	            .add("sourceIdentifier", "")
	            .build()
	            .toString();
	}

}
