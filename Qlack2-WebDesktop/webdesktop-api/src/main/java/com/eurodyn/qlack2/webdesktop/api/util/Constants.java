package com.eurodyn.qlack2.webdesktop.api.util;

public class Constants {
	public static final String OP_ACCESS_APPLICATION = "WD_ACCESS_APPLICATION";
	public static final String OP_UPDATE_APPLICATION = "WD_UPDATE_APPLICATION";
	public static final String OP_MANAGE_GROUPS = "WD_MANAGE_GROUPS";
	public static final String OP_MANAGE_USERS = "WD_MANAGE_USERS";

	// The HTTP header name under which the secure token is stored for WD apps.
	public static final String QLACK_AUTH_HEADER_NAME = "X-Qlack-Fuse-IDM-Token";

	public enum SecurityEvent {
		ALLOW, DENY, REMOVE;
	}
}
