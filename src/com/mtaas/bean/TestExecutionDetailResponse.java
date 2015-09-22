package com.mtaas.bean;

import java.util.List;

public class TestExecutionDetailResponse {
	
	private DeviceConfig deviceConfigs;
	private TestInfo testInfo;
	private List<TestInfo> testMethodInfo;
	
	public DeviceConfig getDeviceConfigs() {
		return deviceConfigs;
	}
	public void setDeviceConfig(DeviceConfig deviceConfigs) {
		this.deviceConfigs = deviceConfigs;
	}
	public TestInfo getTestInfo() {
		return testInfo;
	}
	public void setTestInfo(TestInfo testConfig) {
		this.testInfo = testConfig;
	}
	public List<TestInfo> getTestMethodInfo() {
		return testMethodInfo;
	}
	public void setTestMethodInfo(List<TestInfo> testMethodConfig) {
		this.testMethodInfo = testMethodConfig;
	}
}
