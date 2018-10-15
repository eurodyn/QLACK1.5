angular
	.module("WebDesktop")
	.constant "SERVICES",
		_PREFIX: "api"
		GROUPS: "/desktop/application-groups"
		GROUP_APPLICATIONS: "/desktop/applications-for-group"
		APPLICATION: "/desktop/application"
		DESKTOP_APPLICATIONS: "/desktop/desktop-app-icons"
		DESKTOP_APPLICATION: "/desktop/desktop-app-icon"
		LANGUAGES: "/i18n/languages"
	.constant "SECURITY_CONSTANTS",
		QLACK_AUTH_HEADER_NAME: "X-Qlack-Fuse-IDM-Token"
		QLACK_AUTH_LOCAL_STORAGE_NAME: "X-Qlack-Fuse-IDM-Token"