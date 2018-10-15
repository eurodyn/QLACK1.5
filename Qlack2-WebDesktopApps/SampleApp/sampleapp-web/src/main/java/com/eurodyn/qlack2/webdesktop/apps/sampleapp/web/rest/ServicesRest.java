package com.eurodyn.qlack2.webdesktop.apps.sampleapp.web.rest;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.eurodyn.qlack2.fuse.idm.api.signing.GenericSignedRequest;
import com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.SampleAppService;
import com.eurodyn.qlack2.webdesktop.apps.sampleapp.web.util.Utils;

@Path("/services")
public class ServicesRest {
	private static final Logger LOGGER = Logger.getLogger(SampleAppRest.class
			.getName());

	@Context
	private HttpHeaders headers;

	private SampleAppService sampleAppService;

	public void setSampleAppService(SampleAppService sampleAppService) {
		this.sampleAppService = sampleAppService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/notification")
	public String initLongTask() {
		final GenericSignedRequest sreq = new GenericSignedRequest();
		Utils.sign(sreq, headers);

		// This thread-spawn technique is only for demo purposes. DO NOT do such
		// things in your code EVER!
		new Thread() {
			public void run() {
				sampleAppService.initLongTask(sreq);
			}
		}.start();

		return "Your server-side long-task has been started. A server-side "
				+ "notification will appear shortly.";
	}

}
