
package com.mtaas.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "deviceConfig",
    "autDetails",
    "testSuite"
})
public class TestExecutionRequest {

    @JsonProperty("deviceConfig")
    private List<DeviceConfig> deviceConfig = new ArrayList<DeviceConfig>();
    @JsonProperty("autDetails")
    private AutDetails autDetails;
    @JsonProperty("testSuite")
    private TestSuite testSuite;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("deviceConfig")
    public List<DeviceConfig> getDeviceConfig() {
        return deviceConfig;
    }

    @JsonProperty("deviceConfig")
    public void setDeviceConfig(List<DeviceConfig> deviceConfig) {
        this.deviceConfig = deviceConfig;
    }

    @JsonProperty("autDetails")
    public AutDetails getAutDetails() {
        return autDetails;
    }

    @JsonProperty("autDetails")
    public void setAutDetails(AutDetails autDetails) {
        this.autDetails = autDetails;
    }

    @JsonProperty("testSuite")
    public TestSuite getTestSuite() {
        return testSuite;
    }

    @JsonProperty("testSuite")
    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
