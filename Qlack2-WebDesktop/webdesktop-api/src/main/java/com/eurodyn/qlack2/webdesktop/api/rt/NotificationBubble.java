package com.eurodyn.qlack2.webdesktop.api.rt;

public class NotificationBubble {
	public boolean show;
	public long timeout;

	public NotificationBubble(boolean show, long timeout) {
		this.show = show;
		this.timeout = timeout;
	}

	public NotificationBubble() {
	}
}