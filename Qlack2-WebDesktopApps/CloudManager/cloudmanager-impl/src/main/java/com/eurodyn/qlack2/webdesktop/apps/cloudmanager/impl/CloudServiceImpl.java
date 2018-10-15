package com.eurodyn.qlack2.webdesktop.apps.cloudmanager.impl;

import javax.persistence.EntityManager;

import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.webdesktop.apps.cloudmanager.api.CloudService;

public class CloudServiceImpl implements CloudService {
	private EntityManager entityManager;
	@SuppressWarnings("unused")
	private IDMService idmService;

	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

//	@Override
//	@ValidateTicket
//	public SaveUserResponse saveUser(SaveUserRequest sreq) {
//		// Manipulate request, save in database, etc.
//		// ...
//
//		return new SaveUserResponse(UUID.randomUUID().toString());
//	}

}
