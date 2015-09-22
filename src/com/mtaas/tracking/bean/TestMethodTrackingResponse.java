
package com.mtaas.tracking.bean;

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

import com.mtaas.bean.DeviceConfig;
import com.mtaas.bean.TestInfo;
import com.mtaas.bean.TestInfo;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
	"deviceConfig",
	"testConfig",
    "Operations"
})
public class TestMethodTrackingResponse {

    @JsonProperty("deviceConfig")
    private DeviceConfig deviceConfig = null;
    
    @JsonProperty("testConfig")
    private TestInfo testConfig = null;
    
    @JsonProperty("Operations")
    private List<Operation> operations = new ArrayList<Operation>();
    
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("deviceConfig")
    public DeviceConfig getDeviceConfig() {
        return deviceConfig;
    }

    @JsonProperty("deviceConfig")
    public void setDeviceConfig(DeviceConfig deviceConfig) {
        this.deviceConfig = deviceConfig;
    }
    
    @JsonProperty("testConfig")
    public TestInfo getTestConfig() {
        return testConfig;
    }

    @JsonProperty("testConfig")
    public void setTestConfig(TestInfo testConfig) {
        this.testConfig = testConfig;
    }
    
    @JsonProperty("operations")
    public List<Operation> getOperations() {
        return operations;
    }

    @JsonProperty("operations")
    public void setOperations(List<Operation> operations) {
        this.operations = operations;
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
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
