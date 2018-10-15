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
package com.eurodyn.qlack2.fuse.forum.impl.model;
// Generated by Hibernate Tools 3.2.4.GA

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * FrmForum generated by hbm2java
 */
@Entity
@Table(name="frm_forum"
    , uniqueConstraints = @UniqueConstraint(columnNames="title")
)
public class FrmForum  implements java.io.Serializable {


     private String id;
     private String title;
     private String description;
     private byte[] logo;
     private long createdOn;
     private String createdBy;
     private short status;
     private short moderated;
     private boolean archived;
     private Set<FrmTopic> frmTopics = new HashSet<FrmTopic>(0);

    public FrmForum() {
    }


    public FrmForum(String title, long createdOn, String createdBy, short status, short moderated, boolean archived) {
        this.title = title;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.status = status;
        this.moderated = moderated;
        this.archived = archived;
    }
    public FrmForum(String title, String description, byte[] logo, long createdOn, String createdBy, short status, short moderated, boolean archived, Set<FrmTopic> frmTopics) {
       this.title = title;
       this.description = description;
       this.logo = logo;
       this.createdOn = createdOn;
       this.createdBy = createdBy;
       this.status = status;
       this.moderated = moderated;
       this.archived = archived;
       this.frmTopics = frmTopics;
    }

		@Id
		public String getId() {
		if (this.id == null) {
             this.id = java.util.UUID.randomUUID().toString();
         }

		 return this.id;
		}

    public void setId(String id) {
        this.id = id;
    }

    @Column(name="title", unique=true, nullable=false, length=254)
		public String getTitle() {
			return this.title;
		}

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name="description", length=1000)
		public String getDescription() {
			return this.description;
		}

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name="logo")
		public byte[] getLogo() {
			return this.logo;
		}

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    @Column(name="created_on", nullable=false)
		public long getCreatedOn() {
			return this.createdOn;
		}

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    @Column(name="created_by", nullable=false, length=36)
		public String getCreatedBy() {
			return this.createdBy;
		}

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name="status", nullable=false)
		public short getStatus() {
			return this.status;
		}

    public void setStatus(short status) {
        this.status = status;
    }

    @Column(name="moderated", nullable=false)
		public short getModerated() {
			return this.moderated;
		}

    public void setModerated(short moderated) {
        this.moderated = moderated;
    }

    @Column(name="archived", nullable=false)
		public boolean isArchived() {
			return this.archived;
		}

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
@OneToMany(fetch=FetchType.LAZY, mappedBy="frmForumId")
		public Set<FrmTopic> getFrmTopics() {
			return this.frmTopics;
		}

    public void setFrmTopics(Set<FrmTopic> frmTopics) {
        this.frmTopics = frmTopics;
    }




}


