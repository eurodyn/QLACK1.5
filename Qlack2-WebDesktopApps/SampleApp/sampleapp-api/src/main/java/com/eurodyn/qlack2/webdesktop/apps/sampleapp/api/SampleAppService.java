package com.eurodyn.qlack2.webdesktop.apps.sampleapp.api;

import com.eurodyn.qlack2.fuse.idm.api.signing.GenericSignedRequest;
import com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.request.SaveUserRequest;
import com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.response.SaveUserResponse;

public interface SampleAppService {

	SaveUserResponse saveUser(SaveUserRequest sreq);
	void initLongTask(GenericSignedRequest req);
}
