package com.mtaas.bean;

import java.sql.Timestamp;

import com.mtaas.util.MtaasConstants;

public class TestExecution { 
	
	int uniqueId;
	int testId;
	int testSuiteId;
	int deviceId;
	MtaasConstants.TestResult status = null;
	Timestamp executionStartTime = null;
	Timestamp executionEndTime = null;
	
	public int getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(int uniqueId) {
		this.uniqueId = uniqueId;
	}
	public int getTestId() {
		return testId;
	}
	public void setTestId(int testId) {
		this.testId = testId;
	}
	public int getTestSuiteId() {
		return testSuiteId;
	}
	public void setTestSuiteId(int testSuiteId) {
		this.testSuiteId = testSuiteId;
	}
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	public MtaasConstants.TestResult getStatus() {
		return status;
	}
	public void setStatus(MtaasConstants.TestResult status) {
		this.status = status;
	}
	
	public Timestamp getExecutionStartTime() {
		return executionStartTime;
	}
	public void setExecutionStartTime(Timestamp executionStartTime) {
		this.executionStartTime = executionStartTime;
	}
	public Timestamp getExecutionEndTime() {
		return executionEndTime;
	}
	public void setExecutionEndTime(Timestamp executionEndTime) {
		this.executionEndTime = executionEndTime;
	}

}
