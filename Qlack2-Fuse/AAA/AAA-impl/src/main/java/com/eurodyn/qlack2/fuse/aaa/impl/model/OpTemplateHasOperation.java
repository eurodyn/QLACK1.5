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
package com.eurodyn.qlack2.fuse.aaa.impl.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Version;


/**
 * The persistent class for the aaa_op_template_has_operation database table.
 *
 */
@Entity
@Table(name="aaa_op_template_has_operation")
public class OpTemplateHasOperation implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	@Version
	private long dbversion;
	
	private boolean deny;

	//bi-directional many-to-one association to OpTemplate
	@ManyToOne
	@JoinColumn(name="template")
	private OpTemplate template;

	//bi-directional many-to-one association to Operation
	@ManyToOne
	@JoinColumn(name="operation")
	private Operation operation;
	
	//bi-directional many-to-one association to Resource
	@ManyToOne
	@JoinColumn(name="resource_id")
	private Resource resource;

	public OpTemplateHasOperation() {
		id = UUID.randomUUID().toString();
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDeny() {
		return deny;
	}

	public void setDeny(boolean deny) {
		this.deny = deny;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public OpTemplate getTemplate() {
		return this.template;
	}

	public void setTemplate(OpTemplate template) {
		this.template = template;
	}

	public Operation getOperation() {
		return this.operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public static OpTemplateHasOperation findByTemplateIDAndOperationName(String templateID, String operationName, EntityManager em) {
        Query q = em.createQuery ("SELECT o FROM OpTemplateHasOperation o WHERE "
                + "o.template.id = :templateID AND o.operation.name = :operationName AND o.resource IS NULL");
        q.setParameter("templateID", templateID);
        q.setParameter("operationName", operationName);
        List<OpTemplateHasOperation> queryResults = q.getResultList();
        if (queryResults.isEmpty()) {
            return null;
        }
        return queryResults.get(0);
    }
	
	public static OpTemplateHasOperation findByTemplateAndResourceIDAndOperationName(
			String templateID, String operationName, String resourceID, EntityManager em) {
        Query q = em.createQuery ("SELECT o FROM OpTemplateHasOperation o WHERE "
                + "o.template.id = :templateID AND o.operation.name = :operationName AND o.resource.id = :resourceID");
        q.setParameter("templateID", templateID);
        q.setParameter("operationName", operationName);
        q.setParameter("resourceID", resourceID);
        List<OpTemplateHasOperation> queryResults = q.getResultList();
        if (queryResults.isEmpty()) {
            return null;
        }
        return queryResults.get(0);
    }
}