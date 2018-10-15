angular
	.module("userManagement")
	.constant("SERVICES", {
		_PREFIX: "../../api/apps/usermanagement"
		GROUPS: "/groups"
		USERS: "/users"
		CONFIG: "/config"
		
		GROUP_ACTION_MOVE: "/move"
		USER_ACTION_RESET: "/reset-password"
		CONFIG_OPERATIONS: "/operations"
		CONFIG_USERS: "/users"
		CONFIG_GROUPS: "/groups"
		CONFIG_SUBJECTS: "/subjects"
	})
