package com.mtaas.bean;

public class TestMethodExecutionStatus extends ExecutionStatus {
	private int testMethodExecutionId;
	private String testMethodName;
	public int getTestMethodExecutionId() {
		return testMethodExecutionId;
	}
	public void setTestMethodExecutionId(int testMethodExecutionId) {
		this.testMethodExecutionId = testMethodExecutionId;
	}
	public String getTestMethodName() {
		return testMethodName;
	}
	public void setTestMethodName(String testMethodName) {
		this.testMethodName = testMethodName;
	}
}
