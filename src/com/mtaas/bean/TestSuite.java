
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
    "className",
    "suiteName"
})
public class TestSuite {

    @JsonProperty("className")
    private List<String> className = new ArrayList<String>();
    @JsonProperty("suiteName")
    private String suiteName;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("className")
    public List<String> getClassName() {
        return className;
    }

    @JsonProperty("className")
    public void setClassName(List<String> className) {
        this.className = className;
    }

    @JsonProperty("suiteName")
    public String getSuiteName() {
        return suiteName;
    }

    @JsonProperty("suiteName")
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
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
