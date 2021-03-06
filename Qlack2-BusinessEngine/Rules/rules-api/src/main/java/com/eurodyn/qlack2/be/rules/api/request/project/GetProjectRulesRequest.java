package com.eurodyn.qlack2.be.rules.api.request.project;

import com.eurodyn.qlack2.fuse.idm.api.signing.QSignedRequest;

public class GetProjectRulesRequest extends QSignedRequest {

	private final String projectId;
	private final boolean filterEmpty;

	public GetProjectRulesRequest(String projectId, boolean filterEmpty) {
		this.projectId = projectId;
		this.filterEmpty = filterEmpty;
	}

	public String getProjectId() {
		return projectId;
	}

	public boolean isFilterEmpty() {
		return filterEmpty;
	}

}
