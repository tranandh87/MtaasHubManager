package com.mtaas.bean;

import java.sql.Timestamp;

import com.mtaas.util.MtaasConstants;

public class TestMethodExecution {
	int uniqueId;
	int testMethodId;
	int deviceId;
	int testExecutionId;
	MtaasConstants.TestResult status = null;
    Timestamp executionStartTime = null;
    Timestamp executionEndTime = null;
    
	public int getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(int uniqueId) {
		this.uniqueId = uniqueId;
	}
	public int getTestMethodId() {
		return testMethodId;
	}
	public void setTestMethodId(int testMethodId) {
		this.testMethodId = testMethodId;
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
	
	public int getTestExecutionId() {
		return testExecutionId;
	}
	public void setTestExecutionId(int testExecutionId) {
		this.testExecutionId = testExecutionId;
	}
}
