package com.mtaas.bean;

import java.util.List;

/**
 * Response containing all the suite execution summary for the given suiteId.
 * This will contain all the previous execution summary for this particular suiteId.
 * @author haidang
 *
 */
public class SuiteIdExecutionSummaryResponse {
	private List<DeviceConfig> deviceConfigs;
	
	private String suiteName;
	
	private List<SuiteExecutionIdSummary> suiteIdExecutionSummary;

	public List<DeviceConfig> getDeviceConfigs() {
		return deviceConfigs;
	}

	public void setDeviceConfigs(List<DeviceConfig> deviceConfigs) {
		this.deviceConfigs = deviceConfigs;
	}

	public List<SuiteExecutionIdSummary> getSuiteIdExecutionSummary() {
		return suiteIdExecutionSummary;
	}

	public void setSuiteIdExecutionSummary(
			List<SuiteExecutionIdSummary> testSuiteResultSummary) {
		this.suiteIdExecutionSummary = testSuiteResultSummary;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}
}
