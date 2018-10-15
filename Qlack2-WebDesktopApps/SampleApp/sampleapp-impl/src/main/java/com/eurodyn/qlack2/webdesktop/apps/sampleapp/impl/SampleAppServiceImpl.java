/*
 * Copyright 2014 EUROPEAN DYNAMICS SA <info@eurodyn.com>
 *
 * Licensed under the EUPL, Version 1.1 only (the "License").
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package com.eurodyn.qlack2.webdesktop.apps.sampleapp.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.codehaus.jackson.map.ObjectMapper;

import com.eurodyn.qlack2.fuse.eventpublisher.api.EventPublisherService;
import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.fuse.idm.api.annotations.ValidateTicket;
import com.eurodyn.qlack2.fuse.idm.api.signing.GenericSignedRequest;
import com.eurodyn.qlack2.webdesktop.api.rt.RTMessage;
import com.eurodyn.qlack2.webdesktop.api.rt.RTNotification;
import com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.SampleAppService;
import com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.request.SaveUserRequest;
import com.eurodyn.qlack2.webdesktop.apps.sampleapp.api.response.SaveUserResponse;

public class SampleAppServiceImpl implements SampleAppService {
	private static final Logger LOGGER = Logger
			.getLogger(SampleAppServiceImpl.class.getName());
	private static ObjectMapper mapper = new ObjectMapper();
	private EntityManager entityManager;
	@SuppressWarnings("unused")
	private IDMService idmService;
	private EventPublisherService eventPublisherService;

	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setEventPublisherService(
			EventPublisherService eventPublisherService) {
		this.eventPublisherService = eventPublisherService;
	}

	@Override
	@ValidateTicket
	public SaveUserResponse saveUser(SaveUserRequest sreq) {
		// Manipulate request, save in database, etc.
		// ...

		return new SaveUserResponse(UUID.randomUUID().toString());
	}

	@Override
	@ValidateTicket
	public void initLongTask(GenericSignedRequest req) {
		try {
			LOGGER.log(Level.FINE, "Thread sleep...");
			Thread.sleep(3000);
			LOGGER.log(Level.FINE, "Sleep terminated, about to send msg.");

			eventPublisherService.publishAsync(
				RTMessage
					.builder()
					.handler(RTMessage.NOTIFICATION_HANDLER)
					.recipientID(req.getSignedTicket().getUserID())
					.payload(mapper.writeValueAsString(
							RTNotification
								.builder()
								.audio(true)
								.content("Server-side message")
								.icon("fa-coffee")
								.title("Server generated")
								.setBubble(true, 5000)
								.build()))
					.build().asMap(),
				RTMessage.TOPIC_WD);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOGGER.log(Level.FINE, "Msg sent.");
	}

}
