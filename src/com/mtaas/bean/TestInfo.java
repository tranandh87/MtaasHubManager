
package com.mtaas.bean;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "suiteName",
    "suiteExecutionId",
    "suiteStatus",
    "testName",
    "testExecutionId",
    "testStatus",
    "testMethodExecutionId",
    "testMethodName",
    "testMethodStatus"
})
public class TestInfo {

    @JsonProperty("suiteName")
    private String suiteName;
    @JsonProperty("suiteExecutionId")
    private Integer suiteExecutionId;
    @JsonProperty("suiteStatus")
    private String suiteStatus;
    @JsonProperty("testName")
    private String testName;
    @JsonProperty("testExecutionId")
    private Integer testExecutionId;
    @JsonProperty("testStatus")
    private String testStatus;
    @JsonProperty("testMethodExecutionId")
    private Integer testMethodExecutionId;
    @JsonProperty("testMethodName")
    private String testMethodName;
    @JsonProperty("testMethodStatus")
    private String testMethodStatus;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("suiteName")
    public String getSuiteName() {
        return suiteName;
    }

    @JsonProperty("suiteName")
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    @JsonProperty("suiteExecutionId")
    public Integer getSuiteExecutionId() {
        return suiteExecutionId;
    }

    @JsonProperty("suiteExecutionId")
    public void setSuiteExecutionId(Integer suiteExecutionId) {
        this.suiteExecutionId = suiteExecutionId;
    }

    @JsonProperty("suiteStatus")
    public String getSuiteStatus() {
        return suiteStatus;
    }

    @JsonProperty("suiteStatus")
    public void setSuiteStatus(String suiteStatus) {
        this.suiteStatus = suiteStatus;
    }

    @JsonProperty("testName")
    public String getTestName() {
        return testName;
    }

    @JsonProperty("testName")
    public void setTestName(String testName) {
        this.testName = testName;
    }

    @JsonProperty("testExecutionId")
    public Integer getTestExecutionId() {
        return testExecutionId;
    }

    @JsonProperty("testExecutionId")
    public void setTestExecutionId(Integer testExecutionId) {
        this.testExecutionId = testExecutionId;
    }

    @JsonProperty("testStatus")
    public String getTestStatus() {
        return testStatus;
    }

    @JsonProperty("testStatus")
    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    @JsonProperty("testMethodName")
    public String getTestMethodName() {
        return testMethodName;
    }

    @JsonProperty("testMethodName")
    public void setTestMethodName(String testMethodName) {
        this.testMethodName = testMethodName;
    }

    @JsonProperty("testMethodExecutionId")
    public Integer getTestMethodExecutionId() {
        return testMethodExecutionId;
    }

    @JsonProperty("testMethodExecutionId")
    public void setTestMethodExecutionId(Integer testMethodExecutionId) {
        this.testMethodExecutionId = testMethodExecutionId;
    }

    @JsonProperty("testMethodStatus")
    public String getTestMethodStatus() {
        return testMethodStatus;
    }

    @JsonProperty("testMethodStatus")
    public void setTestMethodStatus(String testMethodStatus) {
        this.testMethodStatus = testMethodStatus;
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
