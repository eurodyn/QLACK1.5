package com.eurodyn.qlack2.webdesktop.api.rt;

import java.util.Map;

import org.apache.commons.beanutils.BeanMap;

public class RTMessage {
	// The default topic names to post events to be picked-up by WD's RT monitor.
	public final static String TOPIC_WD = "QLACK2/WD/RT";
	// A convenience constant to deliver RT messages to WD's notification service.
	public final static String NOTIFICATION_HANDLER = "RTNotifications";
	private String handlerID;
	private String recipientID;
	private String payload;

	public String getHandlerID() {
		return handlerID;
	}

	public void setHandlerID(String handlerID) {
		this.handlerID = handlerID;
	}

	public String getRecipientID() {
		return recipientID;
	}

	public void setRecipientID(String recipientID) {
		this.recipientID = recipientID;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public static String getTopicWd() {
		return TOPIC_WD;
	}

	public RTMessage() {
	}

	public RTMessage(String handlerID, String recipientID, String payload) {
		this.handlerID = handlerID;
		this.recipientID = recipientID;
		this.payload = payload;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> asMap() {
		return (Map) (new BeanMap(this));
	}

	public static class Builder {
		private String handlerID;
		private String recipientID;
		private String payload;

		public Builder() {
		}

		public Builder recipientID(String recipientID) {
			this.recipientID = recipientID;
			return this;
		}

		public Builder payload(String payload) {
			this.payload = payload;
			return this;
		}

		public Builder handler(String handlerID) {
			this.handlerID = handlerID;
			return this;
		}

		public RTMessage build() {
			return new RTMessage(handlerID, recipientID, payload);
		}
	}

}
