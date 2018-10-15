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
package com.eurodyn.qlack2.fuse.mailing.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import com.eurodyn.qlack2.fuse.mailing.api.DistributionListService;
import com.eurodyn.qlack2.fuse.mailing.api.dto.ContactDTO;
import com.eurodyn.qlack2.fuse.mailing.api.dto.DistributionListDTO;
import com.eurodyn.qlack2.fuse.mailing.impl.model.Contact;
import com.eurodyn.qlack2.fuse.mailing.impl.model.DistributionList;
import com.eurodyn.qlack2.fuse.mailing.impl.util.ConverterUtil;

/**
 * Provide distribution list related services. For details regarding the
 * functionality offered see the respective interfaces.
 *
 * @author European Dynamics SA.
 */
public class DistributionListServiceImpl implements DistributionListService {
	private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	/**
	 * Create a new distribution list.
	 *
	 * @param dto
	 */
	@Override
	public void createDistributionList(DistributionListDTO dto) {
		DistributionList dlist = ConverterUtil.dlistConvert(dto);
		em.persist(dlist);
	}

	/**
	 * Edit an existing distribution list.
	 *
	 * @param dto
	 */
	@Override
	public void editDistributionList(DistributionListDTO dto) {
		String id = dto.getId();
		DistributionList dlist = ConverterUtil.dlistConvert(dto);
		dlist.setId(id);
		em.merge(dlist);
	}

	/**
	 * Delete a distribution list.
	 *
	 * @param id
	 */
	@Override
	public void deleteDistributionList(String id) {
		DistributionList dlist = findById(id);
		em.remove(dlist);
	}

	/**
	 * Find a specific distribution list.
	 *
	 * @param id
	 * @return
	 */
	@Override
	public DistributionListDTO find(Object id) {
		DistributionList dlist = findById(id);
		return ConverterUtil.dlistConvert(dlist);
	}

	/**
	 * Find DistributionList Entity object for provided id.
	 *
	 * @param id
	 * @return DistributionList
	 */
	private DistributionList findById(Object id) {
		return em.find(DistributionList.class, id);
	}

	/**
	 * Search for a specific distribution list, with the criteria provided.
	 * (Only the name can be provided as criteria at the moment.)
	 *
	 * @param dto
	 * @return
	 */
	@Override
	public List<DistributionListDTO> search(String name) {
		List<DistributionList> distributionList = null;
		if (name == null) {
			distributionList = DistributionList.findAll(em);
		}
		else {
			distributionList = DistributionList.findByName(em, name);
		}

		List<DistributionListDTO> distributionDtoList = new ArrayList<>();
		for (DistributionList distribution : distributionList) {
			DistributionListDTO distributionListDto = ConverterUtil.dlistConvert(distribution);
			distributionDtoList.add(distributionListDto);
		}

		return distributionDtoList;
	}

	/**
	 * Create a new contact.
	 *
	 * @param dto
	 */
	@Override
	public void createContact(ContactDTO dto) {
		Contact contact = ConverterUtil.contactConvert(dto);
		em.persist(contact);
	}

	/**
	 * Add a contact to a distribution list.
	 *
	 * @param distributionId
	 * @param contactId
	 */
	@Override
	public void addContactToDistributionList(String distributionId, String contactId) {
		// This will probably load the parent dlist and all sibling contacts, otoh
		// it keeps updated the loaded instances on both sides of the relationship.
		DistributionList dlist = findById(distributionId);
		Contact contact = findContactById(contactId);
		dlist.getContacts().add(contact);
	}

	/**
	 * Remove a contact from a distribution list.
	 *
	 * @param distributionId
	 * @param contactId
	 */
	@Override
	public void removeContactFromDistributionList(String distributionId, String contactId) {
		// This will probably load the parent dlist and all sibling contacts, otoh
		// it keeps updated the loaded instances on both sides of the relationship.
		DistributionList dlist = findById(distributionId);
		Contact contact = findContactById(contactId);
		dlist.getContacts().remove(contact);
	}

	private Contact findContactById(String contactId) {
		return em.find(Contact.class, contactId);
	}

}
