package stepDefinition;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.runner.RunWith;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.Cucumber;

@RunWith(Cucumber.class)
public class stepDefinition {
		
	static String tokenNumber  = null;
	static String instanceUrl  = null;
	static String successId    = null;
	
	 @Given("^User login to Salesforce via API$")
	    public void user_login_to_salesforce_via_api() throws Exception {
		 
		 //Reading credentials from Property file
		    String filePath = "src/test/java/resources/inputData.properties";
		    InputStream input = new FileInputStream(filePath);
		    Properties prop = new Properties();
		    prop.load(input);
			String userName = prop.getProperty("userName");
			String password = prop.getProperty("password");
			String grantType = prop.getProperty("grantType");
			String clientId = prop.getProperty("clientId");
			String clientSecret = prop.getProperty("clientSecret");		 

		//Using URL connection method to establish connection and retrieve token from Salesforce
		    String getUrl = "username="+userName+"&password="+password+"&grant_type="+grantType+"&client_id="+clientId+"&client_secret="+clientSecret;
			URL obj = new URL("https://login.salesforce.com/services/oauth2/token?"+getUrl);
			URLConnection urlConn = obj.openConnection();
			HttpsURLConnection postConn = null;
			
			if(urlConn instanceof HttpsURLConnection){
				postConn = (HttpsURLConnection)obj.openConnection();;
				postConn.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						// TODO Auto-generated method stub
						return true;
					}
				});
			}
			
			postConn.setRequestMethod("POST");
			postConn.setRequestProperty("Accept", "application/json");
			postConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			postConn.setDoOutput(true);
			int responseCode = postConn.getResponseCode();
			System.out.println(postConn.getHeaderFields().get("Set-Cookie"));
			System.out.println("POST Response code : "+responseCode);
			System.out.println("POST Response message : "+postConn.getResponseMessage());
			String readLine = null;
			
			if(responseCode == HttpURLConnection.HTTP_OK){
				StringBuffer response = new StringBuffer();
				BufferedReader in = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
				while ((readLine = in .readLine()) != null){
					response.append(readLine);
				}
				in .close();
				System.out.println("Get response is: "+response.toString());
				JSONParser parse = new JSONParser();
				JSONObject jobj = (JSONObject) parse.parse(response.toString());
				tokenNumber = (String) jobj.get("access_token");
				instanceUrl = (String)jobj.get("instance_url");
				System.out.println("token is: "+tokenNumber);
				System.out.println("instance url is: "+instanceUrl);
			}else{
				System.out.println("POST didnot work");
			}
	    }

	    @When("^User assign the permission set to a another user$")
	    public void user_assign_the_permission_set_to_a_another_user() throws Exception {
	       
	   //Reading input parameter like userId and Permission set Id from property file
	    	String filePath = "src/test/java/resources/inputData.properties";
		    InputStream input = new FileInputStream(filePath);
		    Properties prop = new Properties();
		    prop.load(input);
			String assigneeId = prop.getProperty("assigneeId");
			String permissionSetId = prop.getProperty("permissionSetId");
			String jsonBody = "{\"AssigneeId\":\"" + assigneeId + "\",\"PermissionSetId\":\""+permissionSetId+"\"}";
			System.out.println(jsonBody);
			
	       URL obj = new URL(instanceUrl+"/services/data/v50.0/sobjects/PermissionSetAssignment");
			URLConnection urlCon = obj.openConnection();
			HttpsURLConnection postConn = null;
			
			if(urlCon instanceof HttpsURLConnection){
				postConn = (HttpsURLConnection)obj.openConnection();;
				postConn.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						// TODO Auto-generated method stub
						return true;
					}
				});
			}
			
			postConn.setRequestMethod("POST");
			postConn.setRequestProperty("Accept", "application/json");
			postConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			postConn.setRequestProperty("Authorization", "Bearer "+tokenNumber);
			postConn.setDoOutput(true);
			OutputStreamWriter os = new OutputStreamWriter(postConn.getOutputStream());
			os.write(jsonBody);
			os.flush();
			os.close();
			
			int responseCode = postConn.getResponseCode();
			System.out.println("POST Response code : "+responseCode);
			System.out.println("POST Response message : "+postConn.getResponseMessage());
			String readLine = null;
			
			if(responseCode == HttpURLConnection.HTTP_CREATED){
				StringBuffer response = new StringBuffer();
				BufferedReader in = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
				while ((readLine = in .readLine()) != null){
					response.append(readLine);
				}
				in .close();
				System.out.println("Permission Set assigned successfully and response is: "+response.toString());
				
				JSONParser parse = new JSONParser();
				JSONObject jobj = (JSONObject) parse.parse(response.toString());
				successId = (String) jobj.get("id");
				System.out.println("Success id is: "+successId);
			}else{
				System.out.println("POST didnot work");
			}
	    }

	    @Then("^Permission set should be assinged successfully$")
	    public void permission_set_should_be_assinged_successfully() throws Throwable {
	       if(successId != null){
	    	   System.out.println("Successfully assigned permission set to user");	    	   
	       }else{
	    	   System.out.println("Unable to assign Permission to user");
	       }
	    	
	    }
	
	
}
