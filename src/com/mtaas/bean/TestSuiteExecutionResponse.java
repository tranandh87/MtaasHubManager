package com.mtaas.bean;


public class TestSuiteExecutionResponse {
	
	private boolean isRequestPassed = false;
	private int suiteId, executionRequestId;
	private String message = null;
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getSuiteId() {
		return suiteId;
	}

	public void setSuiteId(int suiteId) {
		this.suiteId = suiteId;
	}

	public void setIsRequestPassed(boolean requestResult){
		this.isRequestPassed = requestResult;
	}
	
	public boolean getIsRequestPassed(){
		return isRequestPassed;
	}

	public void setExecutionRequestId(int requestId) {
		this.executionRequestId = requestId;
	}
	
	public int getExecutionRequestId(){
		return executionRequestId;
	}

}
