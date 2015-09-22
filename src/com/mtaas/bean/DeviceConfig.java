/**
 * This class is used for representing the device info to the Automation server or to the UI team
 */
package com.mtaas.bean;

import java.util.HashMap;
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
	"deviceConfigId",
    "device",
    "version",
    "isEmulator",
    "manufacturer",
    "model"
})
public class DeviceConfig {

    @JsonProperty("device")
    private String device;
    @JsonProperty("version")
    private String version;
    @JsonProperty("isEmulator")
    private boolean isEmulator;
    @JsonProperty("manufacturer")
    private String manufacturer;
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("deviceConfigId")
    private int deviceConfigId;
    
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
	private String platform;

    @JsonProperty("device")
    public String getDevice() {
        return device;
    }

    @JsonProperty("device")
    public void setDevice(String device) {
        this.device = device;
    }

    @JsonProperty("isEmulator")
    public boolean getIsEmulator() {
        return isEmulator;
    }

    @JsonProperty("isEmulator")
    public void setIsEmulator(boolean isEmulator) {
        this.isEmulator = isEmulator;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("manufacturer")
    public String getManufacturer() {
        return manufacturer;
    }

    @JsonProperty("manufacturer")
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    @JsonProperty("model")
    public String getModel() {
        return model;
    }

    @JsonProperty("model")
    public void setModel(String model) {
        this.model = model;
    }
    
    @JsonProperty("deviceConfigId")
    public int getDeviceConfigId() {
		return deviceConfigId;
	}

    @JsonProperty("deviceConfigId")
	public void setDeviceConfigId(int deviceConfigId) {
		this.deviceConfigId = deviceConfigId;
	}
	
	public void setPlatform(String platform) {
		this.platform = platform;
		
	}
	
	public String getPlatform(){
		return platform;
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
