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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

@Entity
@Table(name = "img_folder")
public class Folder {

	@Id
	private String id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Folder parentId;
	private String name;
	private String description;
	@Column(name = "owner_id")
	private String ownerId;
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "folderId")
	private Set<Image> imgImages = new HashSet<Image>(0);
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentId")
	private Set<Folder> children = new HashSet<Folder>(0);
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "folderId")
	private Set<FolderAttribute> folderAttributes = new HashSet<FolderAttribute>(0);

	public Folder() {
		id = java.util.UUID.randomUUID().toString();
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Folder getParentId() {
		return this.parentId;
	}

	public void setParentId(Folder parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerId() {
		return this.ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public Set<Image> getImgImages() {
		return this.imgImages;
	}

	public void setImgImages(Set<Image> imgImages) {
		this.imgImages = imgImages;
	}

	public Set<Folder> getChildren() {
		return this.children;
	}

	public void setChildren(Set<Folder> imgFolders) {
		this.children = imgFolders;
	}

	public Set<FolderAttribute> getFolderAttributes() {
		return this.folderAttributes;
	}

	public void setFolderAttributes(
			Set<FolderAttribute> folderAttributes) {
		this.folderAttributes = folderAttributes;
	}
	
	public static Folder findByName(EntityManager em, String name, String parentId) {
		String query = "SELECT f FROM Folder f WHERE f.name = :name";
		if (parentId != null) {
			query = query.concat(" AND f.parentId.id = :parentId");
		} else {
			query = query.concat(" AND f.parentId IS NULL");
		}
		Query q = em.createQuery(query);
		q.setParameter("name", name);
		if (parentId != null) {
			q.setParameter("parentId", parentId);
		}
		List<Folder> queryResult = q.getResultList();
		return queryResult.isEmpty() ? null : queryResult.get(0);
	}

}
