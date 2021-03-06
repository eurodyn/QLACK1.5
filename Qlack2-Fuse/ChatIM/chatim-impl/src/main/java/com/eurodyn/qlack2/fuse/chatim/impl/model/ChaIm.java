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
package com.eurodyn.qlack2.fuse.chatim.impl.model;

// Generated by Hibernate Tools 3.2.4.GA

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ChaIm generated by hbm2java
 */
@Entity
@Table(name = "cha_im")
public class ChaIm implements java.io.Serializable {

	private String id;
	private long sentOn;
	private String message;
	private String fromUserId;
	private String toUserId;

	public ChaIm() {
	}

	public ChaIm(long sentOn, String message, String fromUserId, String toUserId) {
		this.sentOn = sentOn;
		this.message = message;
		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
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

	@Column(name = "sent_on", nullable = false)
	public long getSentOn() {
		return this.sentOn;
	}

	public void setSentOn(long sentOn) {
		this.sentOn = sentOn;
	}

	@Column(name = "message", nullable = false, length = 1024)
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name = "from_user_id", nullable = false, length = 36)
	public String getFromUserId() {
		return this.fromUserId;
	}

	public void setFromUserId(String fromUserId) {
		this.fromUserId = fromUserId;
	}

	@Column(name = "to_user_id", nullable = false, length = 36)
	public String getToUserId() {
		return this.toUserId;
	}

	public void setToUserId(String toUserId) {
		this.toUserId = toUserId;
	}

}
