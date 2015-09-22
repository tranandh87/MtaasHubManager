/**
 * This class is used for registering the device from appium node to hub mangaer.
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
    "devicePlatform",
    "deviceVersion",
    "isEmulator",
    "appiumNodeIp",
    "deviceStatus",
    "hubUrl",
    "hubStatus",
    "hubPlatform",
    "manufacturer",
    "model"
})
public class Device {

    @JsonProperty("devicePlatform")
    private String devicePlatform;
    
    @JsonProperty("deviceVersion")
    private String deviceVersion;
    
    @JsonProperty("isEmulator")
    private boolean isEmulator;
    
    @JsonProperty("appiumNodeIp")
    private String appiumNodeIp;
    
    @JsonProperty("deviceStatus")
    private int deviceStatus = 1;
    
    @JsonProperty("hubUrl")
    private String hubUrl;
    
    @JsonProperty("hubStatus")
    private int hubStatus = 1;
    
    @JsonProperty("hubPlatform")
    private String hubPlatform;
    
    @JsonProperty("manufacturer")
    private String manufacturer;
    
    @JsonProperty("model")
    private String model;
    
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("devicePlatform")
    public String getDevicePlatform() {
        return devicePlatform;
    }

    @JsonProperty("devicePlatform")
    public void setDevicePlatform(String platform) {
        this.devicePlatform = platform;
    }

    @JsonProperty("deviceVersion")
    public String getDeviceVersion() {
        return deviceVersion;
    }

    @JsonProperty("deviceVersion")
    public void setDeviceVersion(String version) {
        this.deviceVersion = version;
    }

    @JsonProperty("isEmulator")
    public boolean getIsEmulator() {
        return isEmulator;
    }

    @JsonProperty("isEmulator")
    public void setIsEmulator(boolean isEmulator) {
        this.isEmulator = isEmulator;
    }

    @JsonProperty("hubUrl")
    public String getHubUrl() {
        return hubUrl;
    }

    @JsonProperty("hubUrl")
    public void setHubUrl(String hubIp) {
        this.hubUrl = hubIp;
    }

    @JsonProperty("hubStatus")
    public int getHubStatus() {
        return hubStatus;
    }

    @JsonProperty("hubStatus")
    public void setHubStatus(int hubStatus) {
        this.hubStatus = hubStatus;
    }

    @JsonProperty("hubPlatform")
    public String getHubPlatform() {
        return hubPlatform;
    }

    @JsonProperty("hubPlatform")
    public void setHubPlatform(String hubPlatform) {
        this.hubPlatform = hubPlatform;
    }

    @JsonProperty("appiumNodeIp")
    public String getAppiumNodeIp() {
        return appiumNodeIp;
    }

    @JsonProperty("appiumNodeIp")
    public void setAppiumNodeIp(String appiumNodeIp) {
        this.appiumNodeIp = appiumNodeIp;
    }
    
    @JsonProperty("deviceStatus")
    public int getDeviceStatus() {
        return deviceStatus;
    }

    @JsonProperty("deviceStatus")
    public void setDeviceStatus(int deviceStatus) {
        this.deviceStatus = deviceStatus;
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
