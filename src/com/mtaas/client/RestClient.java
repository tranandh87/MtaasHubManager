package com.mtaas.client;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.mtaas.bean.Device;
import com.mtaas.tracking.bean.OperationalTracking;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.filter.LoggingFilter;
import com.sun.jersey.api.json.JSONConfiguration;

public class RestClient {
	private static Logger log = Logger.getLogger(RestClient.class.getName());
    
	public static OperationalTracking getOperationalTracking(String endPoint){
		Client client = null;
		OperationalTracking operationalTracking = null;
		try {
			client = getClient();
			WebResource webResource = client.resource(endPoint);
//			t = webResource.accept(MediaType.APPLICATION_JSON).get(OperationalTracking.class);
			
			ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			
			if (response.getStatus() == 200) {
				System.out.println(response.getEntityInputStream().toString());
				operationalTracking = response.getEntity(OperationalTracking.class);
			}
		 
			log.info("Operation tracking response : " + response.getStatus());
		}
		/*} catch (Exception e){
			log.error("exception caught in getTracking", e);
		}*/ finally {
			if (client != null){
				client.destroy();
			}
		}
		
		return operationalTracking;
		
		/*OperationalTracking t = new OperationalTracking();
		List<String> stepList = new ArrayList<String>();
		List<Operation> tList = new ArrayList<Operation>();
			Operation acceptBtn = new Operation();
			acceptBtn.setCommand("find");
			acceptBtn.setAction("click");
			acceptBtn.setElementId("accept_btn");
			acceptBtn.setStatus("Success");
			tList.add(acceptBtn);
			
			Operation register = new Operation();
			register.setCommand("find");
			register.setAction("click");
			register.setElementId("registerBtn");
			register.setStatus("Success");
			tList.add(register);
		String find = "";
		String click = "";
			stepList.add(find);
			stepList.add(click);
		
		t.setOperations(stepList);
		return t;*/
	}
	
	public static void sendMethodExecutioinIdForOperationTracking(String endPoint){
		Client client = null;
		try {
			log.info("In Restclient.sendMethodExecutioinIdForOperationTracking to put methodExec id to appium Node");
			client = Client.create();
			WebResource webResource = client.resource(endPoint);
			
//			webResource.put();
			ClientResponse clientResponse = webResource.accept("text/plain").put(ClientResponse.class);
			log.info(String.format("Client response = %s with response code: %d",clientResponse.getEntity(String.class),clientResponse.getStatus()));
		}
		catch (Exception e){
			log.error("exception caught in getTracking", e);
		}
		finally {
			if (client != null){
				client.destroy();
			}
		}
	}
	
	public static String getOperationalTrackingAsText(String endPoint){
		Client client = null;
		String testMethodOperationalTracking = null;
		try {
			client = getClient();
			WebResource webResource = client.resource(endPoint);
			testMethodOperationalTracking = webResource.accept(MediaType.TEXT_PLAIN).get(String.class);
		} catch (Exception e){
			log.error("exception caught in getTracking", e);
		} finally {
			if (client != null){
				client.destroy();
			}
		}
		
		return testMethodOperationalTracking;
	}
	
	private static Client getClient(){
		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client c = Client.create(clientConfig);
		return c;
	}
	
    public static Device getConnectedDeviceInfo(String endPoint) {
    	Client client = null;
    	Device dev = null;
    	try {
    		client = getClient();
			WebResource webResource = client.resource(endPoint);
    		dev = webResource.accept(MediaType.APPLICATION_JSON).get(Device.class);
    	}catch (Exception e){
    		log.error("Exception caught in getConnectedDeviceInfo", e);
    	}finally {
    		if (client != null){
    			client.destroy();
    		}
    	}
    	return dev;
        /*return webResource
                .path("cluster")
                .path(name)
                .accept(MediaType.APPLICATION_JSON)
                .get(Cluster.class);*/
    	
    }
    
    public static void main(String[] a){
    	Device dev = RestClient.getConnectedDeviceInfo("http://localhost:4723/wd/hub/deviceConfig");
    	System.out.println("deviceInfo: " + dev);
    }
}
