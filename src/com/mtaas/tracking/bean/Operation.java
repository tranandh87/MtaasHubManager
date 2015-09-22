
package com.mtaas.tracking.bean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "command",
    "action",
    "findBy",
    "inputValue",
    "selector",
    "status",
    "elementId",
    "currentAppActivity"
})
public class Operation {

    @JsonProperty("command")
    private String command;
    @JsonProperty("action")
    private String action;
    @JsonProperty("findBy")
    private String findBy;
    @JsonProperty("inputValue")
    private String inputValue;
    @JsonProperty("selector")
    private String selector;
    @JsonProperty("status")
    private String status;
    @JsonProperty("elementId")
    private String elementId;
    @JsonProperty("currentAppActivity")
    private String currentAppActivity;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("command")
    public String getCommand() {
        return command;
    }

    @JsonProperty("command")
    public void setCommand(String command) {
        this.command = command;
    }

    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    @JsonProperty("findBy")
    public String getFindBy() {
        return findBy;
    }

    @JsonProperty("findBy")
    public void setFindBy(String findBy) {
        this.findBy = findBy;
    }

    @JsonProperty("inputValue")
    public String getInputValue() {
        return inputValue;
    }

    @JsonProperty("inputValue")
    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }
    
    @JsonProperty("selector")
    public String getSelector() {
        return selector;
    }

    @JsonProperty("selector")
    public void setSelector(String selector) {
        this.selector = selector;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("elementId")
    public String getElementId() {
        return elementId;
    }

    @JsonProperty("elementId")
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }
    
    @JsonProperty("currentAppActivity")
    public String getCurrentAppActivity() {
        return currentAppActivity;
    }

    @JsonProperty("currentAppActivity")
    public void setCurrentAppActivity(String currentAppActivity) {
        this.currentAppActivity = currentAppActivity;
    }

    @Override
    public String toString() {
        try {
        	ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
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