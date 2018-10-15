package com.eurodyn.qlack2.webdesktop.apps.cloudmanager.web.util;

import javax.ws.rs.core.HttpHeaders;

import com.eurodyn.qlack2.common.Constants;
import com.eurodyn.qlack2.fuse.idm.api.signing.QSignedRequest;
import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;

public class Utils {

	private Utils() {
	}

	public static void sign(QSignedRequest request, HttpHeaders headers) {
		String ticket = headers.getRequestHeaders().getFirst(Constants.QLACK_AUTH_HEADER_NAME);
		request.setSignedTicket(SignedTicket.fromVal(ticket));
	}

}
