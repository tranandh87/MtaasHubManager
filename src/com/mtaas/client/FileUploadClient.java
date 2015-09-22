package com.mtaas.client;

import java.io.File;
import java.io.FileNotFoundException;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;

public class FileUploadClient {

	public static void main(String[] args) throws FileNotFoundException {
//		String restEndpoint = "http://localhost:8080/RestfulAPIs/hubManager/uploadTestJar";
//		String filePath = "/home/haidang/Desktop/CMPE295/wordPressTest.jar";
		
		String restEndpoint = "http://localhost:8080/RestfulAPIs/hubManager/uploadApk";
		String filePath = "/home/haidang/Desktop/CMPE295/wordpress_2.4.5.apk";
		
		final ClientResponse clientResp = uploadFile(restEndpoint, filePath);
		
		System.out.println("Reponse Status code : " + clientResp.getStatus());
		System.out.println("Resonse message : " + clientResp.getEntity(String.class));
		System.out.println("Response: " + clientResp.getClientResponseStatus());

	}

	public static ClientResponse uploadFile(String restEndpoint,
			String filePath) {
		final ClientConfig config = new DefaultClientConfig();
		final Client client = Client.create(config);

		final WebResource resource = client.resource(restEndpoint);

		final File fileToUpload = new File(filePath);

		final FormDataMultiPart multiPart = new FormDataMultiPart();
		if (fileToUpload != null) 
		{
			multiPart.bodyPart(new FileDataBodyPart("file", fileToUpload,
					MediaType.APPLICATION_OCTET_STREAM_TYPE));
		}

		final ClientResponse clientResp = resource.type(
				MediaType.MULTIPART_FORM_DATA_TYPE).post(ClientResponse.class,
				multiPart);
		
		client.destroy();
		return clientResp;
	}
}

