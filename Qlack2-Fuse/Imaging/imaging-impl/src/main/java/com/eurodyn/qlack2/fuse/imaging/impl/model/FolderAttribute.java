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
package com.eurodyn.qlack2.fuse.imaging.impl.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

@Entity
@Table(name = "img_folder_attribute")
public class FolderAttribute {

	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "folder_id")
	private Folder folderId;
	private String name;
	private String value;

	public FolderAttribute() {
		id = java.util.UUID.randomUUID().toString();
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Folder getFolderId() {
		return this.folderId;
	}

	public void setFolderId(Folder folderId) {
		this.folderId = folderId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static FolderAttribute findByNameAndFolderID(EntityManager em, String name, String folderID) {
		Query q = em
				.createQuery("SELECT a FROM FolderAttribute a WHERE a.name = :name AND a.folderId.id = :id");
		q.setParameter("name", name);
		q.setParameter("id", folderID);
		List<FolderAttribute> resultList = q.getResultList();
		return (resultList.isEmpty()) ? null : resultList.get(0);
	}

}
