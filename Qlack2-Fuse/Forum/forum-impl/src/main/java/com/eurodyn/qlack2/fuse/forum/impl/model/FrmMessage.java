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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * FrmMessage generated by hbm2java
 */
@Entity
@Table(name="frm_message"
)
public class FrmMessage  implements java.io.Serializable {


     private String id;
     private FrmTopic frmTopicId;
     private FrmMessage parentId;
     private String text;
     private long createdOn;
     private String createdBy;
     private short moderationStatus;
     private Set<FrmMessage> frmMessages = new HashSet<FrmMessage>(0);
     private Set<FrmAttachment> frmAttachments = new HashSet<FrmAttachment>(0);

    public FrmMessage() {
    }


    public FrmMessage(FrmTopic frmTopicId, long createdOn, String createdBy, short moderationStatus) {
        this.frmTopicId = frmTopicId;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.moderationStatus = moderationStatus;
    }
    public FrmMessage(FrmTopic frmTopicId, FrmMessage parentId, String text, long createdOn, String createdBy, short moderationStatus, Set<FrmMessage> frmMessages, Set<FrmAttachment> frmAttachments) {
       this.frmTopicId = frmTopicId;
       this.parentId = parentId;
       this.text = text;
       this.createdOn = createdOn;
       this.createdBy = createdBy;
       this.moderationStatus = moderationStatus;
       this.frmMessages = frmMessages;
       this.frmAttachments = frmAttachments;
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
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="frm_topic_id", nullable=false)
		public FrmTopic getFrmTopicId() {
			return this.frmTopicId;
		}

    public void setFrmTopicId(FrmTopic frmTopicId) {
        this.frmTopicId = frmTopicId;
    }
@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_id")
		public FrmMessage getParentId() {
			return this.parentId;
		}

    public void setParentId(FrmMessage parentId) {
        this.parentId = parentId;
    }

    @Column(name="text", length=65535)
		public String getText() {
			return this.text;
		}

    public void setText(String text) {
        this.text = text;
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

    @Column(name="moderation_status", nullable=false)
		public short getModerationStatus() {
			return this.moderationStatus;
		}

    public void setModerationStatus(short moderationStatus) {
        this.moderationStatus = moderationStatus;
    }
@OneToMany(fetch=FetchType.LAZY, mappedBy="parentId")
		public Set<FrmMessage> getFrmMessages() {
			return this.frmMessages;
		}

    public void setFrmMessages(Set<FrmMessage> frmMessages) {
        this.frmMessages = frmMessages;
    }
@OneToMany(fetch=FetchType.LAZY, mappedBy="frmMessageId")
		public Set<FrmAttachment> getFrmAttachments() {
			return this.frmAttachments;
		}

    public void setFrmAttachments(Set<FrmAttachment> frmAttachments) {
        this.frmAttachments = frmAttachments;
    }




}


