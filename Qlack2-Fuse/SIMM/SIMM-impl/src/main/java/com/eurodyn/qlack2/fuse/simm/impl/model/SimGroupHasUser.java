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
package com.eurodyn.qlack2.fuse.simm.impl.model;
// Generated by Hibernate Tools 3.2.4.GA

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * SimGroupHasUser generated by hbm2java
 */
@Entity
@Table(name="sim_group_has_user"
)
public class SimGroupHasUser  implements java.io.Serializable {


     private String id;
     private SimGroup groupId;
     private String userId;
     private byte status;
     private long joinedOnDate;

    public SimGroupHasUser() {
    }

    public SimGroupHasUser(SimGroup groupId, String userId, byte status, long joinedOnDate) {
       this.groupId = groupId;
       this.userId = userId;
       this.status = status;
       this.joinedOnDate = joinedOnDate;
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
    @JoinColumn(name="group_id", nullable=false)
		public SimGroup getGroupId() {
			return this.groupId;
		}

    public void setGroupId(SimGroup groupId) {
        this.groupId = groupId;
    }

    @Column(name="user_id", nullable=false, length=36)
		public String getUserId() {
			return this.userId;
		}

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Column(name="status", nullable=false)
		public byte getStatus() {
			return this.status;
		}

    public void setStatus(byte status) {
        this.status = status;
    }

    @Column(name="joined_on_date", nullable=false)
		public long getJoinedOnDate() {
			return this.joinedOnDate;
		}

    public void setJoinedOnDate(long joinedOnDate) {
        this.joinedOnDate = joinedOnDate;
    }




}


