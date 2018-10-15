package com.eurodyn.qlack2.webdesktop.web.util;

import javax.ws.rs.core.HttpHeaders;

import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.fuse.idm.api.exception.QInvalidTicketException;
import com.eurodyn.qlack2.fuse.idm.api.request.ValidateTicketRequest;
import com.eurodyn.qlack2.fuse.idm.api.signing.QSignedRequest;
import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.webdesktop.api.util.Constants;

public class Utils {
	private IDMService idmService;

	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}

	public void sign(QSignedRequest request, HttpHeaders headers) {
		SignedTicket ticket = getSignedTicket(headers);
		request.setSignedTicket(ticket);
	}

	public void validateTicket(HttpHeaders headers) {
		SignedTicket ticket = getSignedTicket(headers);
		if (!idmService.validateTicket(new ValidateTicketRequest(ticket)).isValid()) {
			throw new QInvalidTicketException(ticket);
		}
	}

	public SignedTicket getSignedTicket(HttpHeaders headers) {
		String header = headers.getRequestHeaders().getFirst(Constants.QLACK_AUTH_HEADER_NAME);
		return SignedTicket.fromVal(header);
	}
}
