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
package com.eurodyn.qlack2.fuse.aaa.impl;

import java.util.Collection;

import javax.persistence.EntityManager;

import com.eurodyn.qlack2.fuse.aaa.api.ResourceService;
import com.eurodyn.qlack2.fuse.aaa.api.dto.ResourceDTO;
import com.eurodyn.qlack2.fuse.aaa.impl.model.Resource;
import com.eurodyn.qlack2.fuse.aaa.impl.util.ConverterUtil;

/**
 *
 * @author European Dynamics SA
 */
public class ResourceServiceImpl implements ResourceService {
    private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public String createResource(ResourceDTO resourceDTO) {
		Resource resource = new Resource();
		resource.setName(resourceDTO.getName());
		resource.setDescription(resourceDTO.getDescription());
		resource.setObjectId(resourceDTO.getObjectID());
		em.persist(resource);
		return resource.getId();
	}
	
	@Override
	public void updateResource(ResourceDTO resourceDTO) {
		Resource resource = em.find(Resource.class, resourceDTO.getId());
		resource.setName(resourceDTO.getName());
		resource.setDescription(resourceDTO.getDescription());
		resource.setObjectId(resourceDTO.getObjectID());
	}

	@Override
	public void deleteResource(String resourceID) {
		em.remove(Resource.find(resourceID, em));
	}

	@Override
	public void deleteResources(Collection<String> resourceIDs) {
		for (String resourceID : resourceIDs) {
			em.remove(Resource.find(resourceID, em));
		}
	}

	@Override
	public void deleteResourceByObjectId(String objectID) {
		em.remove(Resource.findByObjectID(objectID, em));
	}

	@Override
	public void deleteResourcesByObjectIds(Collection<String> objectIDs) {
		for (String objectID : objectIDs) {
			em.remove(Resource.findByObjectID(objectID, em));
		}
	}

	@Override
	public ResourceDTO getResourceById(String resourceID) {
		return ConverterUtil.resourceToResourceDTO(Resource.find(resourceID, em));
	}

	@Override
	public ResourceDTO getResourceByObjectId(String objectID) {
		return ConverterUtil.resourceToResourceDTO(Resource.findByObjectID(
				objectID, em));
	}

}
