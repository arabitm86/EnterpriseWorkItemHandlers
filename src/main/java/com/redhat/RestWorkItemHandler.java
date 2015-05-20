package com.redhat;
//testing egit
import java.net.URI;

import org.apache.http.HttpResponse;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;




public class RestWorkItemHandler implements WorkItemHandler {

	private String jsonPerson = null;
	private final String USERNAME = "username";
	private final String PASSWORD= "password";
	private Credentials credentials = null;


	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
		// call this method when something goes wrong i.e service unavailable to terminate the node execution
		// and end the process

	}

	public void executeWorkItem(WorkItem workItem,
			WorkItemManager workItemManager) {
		System.out.println("executeWorkItem: Starts");
		System.out.println("Grabbing parameters from WorkItem");
		HttpPost conn = null;
		
		Object p = workItem.getParameter("in_person");
		String serviceEndpoint = (String) workItem.getParameter("uri");
		
		jsonPerson = convertToJson(p);

		try {
			
			System.out.println("Building service URL");
			URI uri = new URI(serviceEndpoint);
			
			
			@SuppressWarnings("deprecation")
			HttpClient client = new DefaultHttpClient();
			credentials = new UsernamePasswordCredentials(this.USERNAME, this.PASSWORD);
			
			System.out.println("Opening Connection");
		    
			conn = new HttpPost(uri);
		    conn.addHeader(BasicScheme.authenticate(credentials, "US-ASCII", false));
		    
		    StringEntity entity = new StringEntity(jsonPerson);
		    
		    entity.setContentType(new BasicHeader("Content-Type",
		            "application/json"));  
		    conn.addHeader("Content-Type", "application/json");
		    conn.setEntity(entity);
		    
		    HttpResponse hr = client.execute(conn);
			
			if ( hr.getStatusLine().getStatusCode() != 200) {
				workItemManager.abortWorkItem(workItem.getId());
			}
			else{
				System.out.println("Request to FSW terminal REST Service sent");
				workItemManager.completeWorkItem(workItem.getId(),null);
			}
			

		}

		catch (Exception e) {
			System.err.println("Caught exception " + e.getMessage());
			
			workItemManager.abortWorkItem(workItem.getId());
		}

	}

	private String convertToJson(Object obj) {
		ObjectWriter ow = new ObjectMapper().writer()
				.withDefaultPrettyPrinter();
		String json = null;
		try {
			json = ow.writeValueAsString(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}

}
