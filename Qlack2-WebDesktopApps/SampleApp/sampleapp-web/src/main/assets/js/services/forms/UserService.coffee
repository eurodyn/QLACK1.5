angular
	.module("sampleApp")
	.service "UserService", ["$http", "SERVICES", ($http, SERVICES) ->
		saveUser: (userObj) ->
			$http
				method: "POST"
				url: SERVICES._PREFIX + SERVICES.USERS
				data: userObj
	]
