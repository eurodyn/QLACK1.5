angular
	.module('userManagement')
	.service 'UserHttpService', ['$http', 'SERVICES', ($http, SERVICES) ->
		getAllUsers: (filter) ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.USERS
				
		getUser: (id) ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.USERS + "/" + id
			
		createUser: (user) ->
			$http
				method: "POST"
				url: SERVICES._PREFIX + SERVICES.USERS
				data: user
			
		updateUser: (user) ->
			$http
				method: "PUT"
				url: SERVICES._PREFIX + SERVICES.USERS + "/" + user.id
				data: user
				
		deleteUser: (userId) ->
			$http
				method: "DELETE"
				url: SERVICES._PREFIX + SERVICES.USERS + "/" + userId
				
		resetUserPassword: (userId, password) ->
			$http
				method: "PUT"
				url: SERVICES._PREFIX + SERVICES.USERS + "/" + userId + SERVICES.USER_ACTION_RESET
				data: password
	]