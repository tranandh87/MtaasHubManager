package com.mtaas.api;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;

import com.mtaas.bean.Device;
import com.mtaas.bean.GetDevicesResponse;
import com.mtaas.bean.GridDetails;
import com.mtaas.bean.SuiteIdExecutionSummaryResponse;
import com.mtaas.bean.TestExecutionDetailResponse;
import com.mtaas.bean.TestExecutionRequest;
import com.mtaas.bean.TestSuiteExecutionResponse;
import com.mtaas.db.DeviceInfoDAO;
import com.mtaas.db.DeviceInfoUpdater;
import com.mtaas.db.TestExecutionResultDAO;
import com.mtaas.db.TestSuiteExecutionResultDAO;
import com.mtaas.testRunner.InitiateTestExecution;
import com.mtaas.testware.manager.TestwareManager;
import com.mtaas.testware.manager.TestwareManager.Status;
import com.mtaas.tracking.TrackingTestMethodDAO;
import com.mtaas.tracking.TrackingTestMethodUpdater;
import com.mtaas.tracking.bean.OperationalTracking;
import com.mtaas.tracking.bean.TestMethodTrackingResponse;
import com.mtaas.util.MtaasConstants;
import com.mtaas.util.MtaasConstants.FailedTestRerunRequestType;
import com.mtaas.util.Util;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;


@Path("/hubManager")
public class HubManagerAPI{
	
	private static Logger log = Logger.getLogger(HubManagerAPI.class.getName());
//	private Response response = null;
//	String testResult = null;
	
	@Path("/gridUrl")
	@GET
	public Response getTimeOutException() {
		
		String gridName = buildGridDetails().getUrl();
		ResponseBuilder responseBuilder = Response.ok(gridName); 
		return responseBuilder.build();
	}

	@PUT
	@Path("/test")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
//	public Response createTrackInJSON(DeviceConfig_back req) {
	public Response runTest(TestExecutionRequest req) {
	/*public void asynRunTest(@Suspended final AsyncResponse asyncResponse){
		AsyncResponse asyncResponse1;*/
		log.info("received test execution request : " + req);
//		TestNGRunner.runTest(req);
		BlockingQueue<TestSuiteExecutionResponse> suiteUpdaterQueue = new LinkedBlockingQueue<TestSuiteExecutionResponse>();
		
		InitiateTestExecution runner = new InitiateTestExecution(req, suiteUpdaterQueue);
		String result = runner.runTest(0,null);
		
		System.out.println("returned from runner.runTest(): " + result);
		
		TestSuiteExecutionResponse testSuiteExecutionResponse = null;
		try {
			testSuiteExecutionResponse = suiteUpdaterQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*TestSuiteExecutionResponse resp = new TestSuiteExecutionResponse();
		resp.setMessage("Suite Ids build for your request");
		for (Integer suiteId: suiteIds){
			resp.addSuiteId(suiteId);
		}*/
		
		if (testSuiteExecutionResponse.getIsRequestPassed())
			return Response.status(201).entity(testSuiteExecutionResponse).build();
		else if (testSuiteExecutionResponse.getMessage().contains(MtaasConstants.NO_GRID_RESPONSE_MESSAGE))
			return Response.status(503).entity(testSuiteExecutionResponse).build();
		else 
			return Response.status(400).entity(testSuiteExecutionResponse).build();
	}
	
	@PUT
	@Path("/device")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateDeviceAndHubInfo(Device device) {
		
		log.info("received device registration request: " + device);
		DeviceInfoUpdater deviceInfoUpdater = new DeviceInfoUpdater(device);
		deviceInfoUpdater.start();
		
		String result = "Device is Registered: " + device.toString();
		return Response.status(200).entity(result).build();
	}
	
	private GridDetails buildGridDetails(){
		GridDetails gridDetails = new GridDetails();
		gridDetails.setUrl("http:127.0.0.1/4444/wb/hub");
		
		return gridDetails;
	}

	
	@GET
	@Path("/testMethodExecutionIdDetails/{testMethodExecId: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestMethodTracking(@PathParam("testMethodExecId") int testMethodExecId){
		log.info("received test method tracking request for MethodId: " + testMethodExecId);
		TestMethodTrackingResponse testMethodTrackingResponse = TrackingTestMethodDAO.getTestMethodTracking(testMethodExecId);
		
		return Response.status(200).entity(testMethodTrackingResponse).build();
//		return Response.status(200).entity("mock test").build();
	}
	
	@GET
	@Path("/devices")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDevices(){
		GetDevicesResponse resp = new GetDevicesResponse();
		
		resp.setDevices(new DeviceInfoDAO().getDevices());
		
		return Response.status(200).entity(resp).build();
	}
	
	/**
	 * Upload the file using jersey multipart and mimepull
	 * @param uploadedInputStream
	 * @param fileDetails
	 * @return
	 */
	@POST
	@Path("/uploadTestClass")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadTestClass(
		@FormDataParam("file") InputStream uploadedInputStream,
		@FormDataParam("file") FormDataContentDisposition fileDetails){
		log.info("In upload file of GridDetailsAPI");
		
		String fileName = fileDetails.getFileName();
		Status status = TestwareManager.addTestClassToRepository(uploadedInputStream, fileName);
		
		if (status == Status.SUCCESS){
			return Response.status(200).entity("Test uploaded successfully").build();
		}
		else {
			return Response.status(500).entity("General error occurred while uploading test").build();
		}
	}
	
	/**
	 * Upload the file using jersey multipart and mimepull
	 * @param uploadedInputStream
	 * @param fileDetails
	 * @return
	 */
	@POST
	@Path("/uploadApk")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadApk(
		@FormDataParam("file") InputStream uploadedInputStream,
		@FormDataParam("file") FormDataContentDisposition fileDetails){
		log.info("In uploadApk of GridDetailsAPI");
		
		String fileName = fileDetails.getFileName();
		Status status = TestwareManager.addApkToRepository(uploadedInputStream, fileName);
		
		if (status == Status.SUCCESS){
			return Response.status(200).entity("Apk uploaded successfully\n").build();
		}
		else {
			return Response.status(500).entity("General error occurred while uploading apk\n").build();
		}
	}
	
	/**
	 * Upload the file using jersey multipart and mimepull
	 * @param uploadedInputStream
	 * @param fileDetails
	 * @return
	 */
	@POST
	@Path("/uploadTestJar")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadTestJar(
		@FormDataParam("file") InputStream uploadedInputStream,
		@FormDataParam("file") FormDataContentDisposition fileDetails){
		log.info("In upload file of GridDetailsAPI");
		
		String fileName = fileDetails.getFileName();
		Status status = TestwareManager.addTestJarToRepository(uploadedInputStream, fileName);
		
		if (status == Status.SUCCESS){
			return Response.status(200).entity("Test jar uploaded successfully\n").build();
		}
		else {
			return Response.status(500).entity("General error occurred while uploading test jar\n").build();
		}
	}

	/**
	 * Returns all the execution summary for this particular suiteId
	 * @param suiteId
	 * @return
	 */
	@GET
	@Path("/suiteIdExecutionHistory/{suiteId: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSuiteIdExecutionSummary(@PathParam("suiteId") int suiteId){
		log.info("inside getTestSuiteExecutionResultSummary");
		SuiteIdExecutionSummaryResponse resp = TestSuiteExecutionResultDAO.getSuiteIdExecutionSummary(suiteId);
		log.info("returning response to client");
		return Response.status(200).entity(resp).build();
	}
	
	/**
	 * Get the summary of the execution request id
	 * @param executionRequestId
	 * @return
	 */
	@GET
	@Path("/requestIdExecutionSummary/{executionRequestId: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getExecutionSummary(@PathParam("executionRequestId") int executionRequestId){
		log.info("inside getExecutionSummary");
		SuiteIdExecutionSummaryResponse resp = TestSuiteExecutionResultDAO.getExecutionSummary(executionRequestId);
		log.info("returning response to client");
		return Response.status(200).entity(resp).build();
	}
	
	/**
	 * Returns all the method details for a particular Test Execution id.
	 * @param suiteId
	 * @return
	 */
	@GET
	@Path("/testClassExecutionIdDetail/{testExecId: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTestClassExecutionResultDetail(@PathParam("testExecId") int testExecId){
		log.info("inside getTestClassExecutionResultDetail");
		TestExecutionDetailResponse resp = TestExecutionResultDAO.getTestExecutionIdDetail(testExecId);
		log.info("returning response to client");
		return Response.status(200).entity(resp).build();
	}
	
	/**
	 * Run only the failed test method based on the request id
	 */
	@PUT
	@Path("/failedTestMethods/{requestId: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runFailedTestMethods(@PathParam("requestId") int requestId) {
		log.info("received failed test method execution request");
		
		TestSuiteExecutionResponse testSuiteExecutionResponse = null;
		TestExecutionRequest request = Util.BuildRequestToRunFailedTestCase(requestId,FailedTestRerunRequestType.TESTMETHOD);
		if (request.getDeviceConfig().size() > 0){
			log.info("Request build programatically to run failed test case : " + request);
			BlockingQueue<TestSuiteExecutionResponse> suiteUpdaterQueue = new LinkedBlockingQueue<TestSuiteExecutionResponse>();
			
			InitiateTestExecution runner = new InitiateTestExecution(request, suiteUpdaterQueue);
			String result = runner.runTest(requestId,FailedTestRerunRequestType.TESTMETHOD);
			
			System.out.println("returned from runner.runTest(): " + result);
			
			try {
				testSuiteExecutionResponse = suiteUpdaterQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else{
			testSuiteExecutionResponse = Util.buildFailedTestExectionResponse(MtaasConstants.NO_FAILED_TEST_FOUND + requestId);
		}
		
		/*TestSuiteExecutionResponse resp = new TestSuiteExecutionResponse();
		resp.setMessage("Suite Ids build for your request");
		for (Integer suiteId: suiteIds){
			resp.addSuiteId(suiteId);
		}*/
		
		if (testSuiteExecutionResponse.getIsRequestPassed())
			return Response.status(201).entity(testSuiteExecutionResponse).build();
		else if (testSuiteExecutionResponse.getMessage().contains(MtaasConstants.NO_FAILED_TEST_FOUND))
			return Response.status(400).entity(testSuiteExecutionResponse).build();
		else {
			log.error("In HubManagerAPI.runFailedTestMethod(). This is not expected as only reponse should be passed or if "
					+ "failed should have the contants MtaasConstants.NO_FAILED_TEST_FOUND. Something went wrong");
			return Response.status(400).entity(testSuiteExecutionResponse).build();
		}
	}
	
	/**
	 * Run only the failed test class based on the request id
	 */
	@PUT
	@Path("/failedTestClass/{requestId: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runFailedTestClass(@PathParam("requestId") int requestId) {
		log.info("received failed test class execution request");
		
		TestExecutionRequest request = Util.BuildRequestToRunFailedTestCase(requestId,FailedTestRerunRequestType.TESTCLASS);
		TestSuiteExecutionResponse testSuiteExecutionResponse = null;
		
		if (request.getDeviceConfig().size() > 0){
			log.info("Request build programitcally to run failed test case : " + request);
			BlockingQueue<TestSuiteExecutionResponse> suiteUpdaterQueue = new LinkedBlockingQueue<TestSuiteExecutionResponse>();
			
			InitiateTestExecution runner = new InitiateTestExecution(request, suiteUpdaterQueue);
			String result = runner.runTest(requestId,FailedTestRerunRequestType.TESTCLASS);
			
			System.out.println("returned from runner.runTest(): " + result);
			
			try {
				testSuiteExecutionResponse = suiteUpdaterQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else{
			testSuiteExecutionResponse = Util.buildFailedTestExectionResponse(MtaasConstants.NO_FAILED_TEST_FOUND + requestId);
		}
		
		if (testSuiteExecutionResponse.getIsRequestPassed())
			return Response.status(201).entity(testSuiteExecutionResponse).build();
		else if (testSuiteExecutionResponse.getMessage().contains(MtaasConstants.NO_FAILED_TEST_FOUND))
			return Response.status(400).entity(testSuiteExecutionResponse).build();
		else {
			log.error("In HubManagerAPI.runFailedTestMethod(). This is not expected as only reponse should be passed or if "
					+ "failed should have the contants MtaasConstants.NO_FAILED_TEST_FOUND. Something went wrong");
			return Response.status(400).entity(testSuiteExecutionResponse).build();
		}
	}
	
	/**
	 * Run only the failed test class based on the request id
	 */
	@PUT
	@Path("/failedTestSuite/{requestId: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runFailedTestSuite(@PathParam("requestId") int requestId) {
		log.info("received failed test execution request");
		
		TestExecutionRequest request = Util.BuildRequestToRunFailedTestCase(requestId,FailedTestRerunRequestType.TESTSUITE);
		TestSuiteExecutionResponse testSuiteExecutionResponse = null;
		
		if (request.getDeviceConfig().size() > 0){
			log.info("Request build programitcally to run failed test case : " + request);
			BlockingQueue<TestSuiteExecutionResponse> suiteUpdaterQueue = new LinkedBlockingQueue<TestSuiteExecutionResponse>();
			
			InitiateTestExecution runner = new InitiateTestExecution(request, suiteUpdaterQueue);
			String result = runner.runTest(requestId,FailedTestRerunRequestType.TESTSUITE);
			
			System.out.println("returned from runner.runTest(): " + result);
			
			try {
				testSuiteExecutionResponse = suiteUpdaterQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else{
			testSuiteExecutionResponse = Util.buildFailedTestExectionResponse(MtaasConstants.NO_FAILED_TEST_FOUND + requestId);
		}
		
		if (testSuiteExecutionResponse.getIsRequestPassed())
			return Response.status(201).entity(testSuiteExecutionResponse).build();
		else if (testSuiteExecutionResponse.getMessage().contains(MtaasConstants.NO_GRID_RESPONSE_MESSAGE))
			return Response.status(503).entity(testSuiteExecutionResponse).build();
		else 
			return Response.status(400).entity(testSuiteExecutionResponse).build();
	}

	@PUT
	@Path("/methodOperationTracking/{methodExecutionId: [0-9]+}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response methodOperationTracking(@PathParam("methodExecutionId") int methodExecutionId, OperationalTracking tracking) {
		log.info("methodExecutionId: " + methodExecutionId + ", tracking: " + tracking);
		TrackingTestMethodUpdater trackingTestMethodUpdater = new TrackingTestMethodUpdater(methodExecutionId,tracking);
		trackingTestMethodUpdater.start();
		
		return Response.status(200).entity("tracking is done").build();
	}
	
	/*@PUT
	@Produces("text/plain")
	@Path("/testMethodExecId/{testMethodExecId: [0-9]+}")
	public String getTestMethodExecId(@PathParam("testMethodExecId") int testMethodExecId) {
		log.info("testMethodExecution Id received = " + testMethodExecId);
//		return Response.status(400).entity("testMethodExecution Id received = " + testMethodExecId).build();
		return "testMethodExecution Id received = " + testMethodExecId;
	}*/
	
	
}
