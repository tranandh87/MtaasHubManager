package com.mtaas.bean;

import java.util.List;

public class SuiteExecutionIdSummary {
	private int deviceConfigId, testSuiteExecutionId;
	
	private ExecutionSummary testClassSummary;
	
	private String executionStartTime, executionEndTime;
	
	private List<ExecutionStatus> testClassExecutionStatus;
	
	private String suiteExecutionStatus;

	public int getDeviceConfigId() {
		return deviceConfigId;
	}

	public void setDeviceConfigId(int deviceConfigId) {
		this.deviceConfigId = deviceConfigId;
	}

	public String getSuiteExecutionStatus() {
		return suiteExecutionStatus;
	}

	public void setSuiteExecutionStatus(String suiteExecutionStatus) {
		this.suiteExecutionStatus = suiteExecutionStatus;
	}

	public ExecutionSummary getTestClassSummary() {
		return testClassSummary;
	}

	public void setTestClassSummary(ExecutionSummary testClassSummary) {
		this.testClassSummary = testClassSummary;
	}

	public int getTestSuiteExecutionId() {
		return testSuiteExecutionId;
	}

	public void setTestSuiteExecutionId(int testSuiteExecutionId) {
		this.testSuiteExecutionId = testSuiteExecutionId;
	}

	public List<ExecutionStatus> getTestClassExecutionStatus() {
		return testClassExecutionStatus;
	}

	public void setTestClassExecutionStatus(
			List<ExecutionStatus> testClassExecutionStatus) {
		this.testClassExecutionStatus = testClassExecutionStatus;
	}

	public String getExecutionStartTime() {
		return executionStartTime;
	}

	public void setExecutionStartTime(String executionStartTime) {
		this.executionStartTime = executionStartTime;
	}

	public String getExecutionEndTime() {
		return executionEndTime;
	}

	public void setExecutionEndTime(String executionEndTime) {
		this.executionEndTime = executionEndTime;
	}
	
}
