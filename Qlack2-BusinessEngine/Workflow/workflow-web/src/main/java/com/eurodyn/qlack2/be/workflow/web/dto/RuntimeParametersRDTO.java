package com.eurodyn.qlack2.be.workflow.web.dto;

import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RuntimeParametersRDTO {
	
	private Map<String, Object> parameters;

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

}
