angular
	.module('userManagement')
	.service 'ConfigHttpService', ['$http', 'SERVICES', ($http, SERVICES) ->
		getOperations: (subjectId) ->
			$http
				method: "GET"
				url:  SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/operations"
				
		saveOperations: (subjectId, operations) ->
			$http
				method: "POST"
				url:  SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/operations"
				data: operations
				
		getManagedUsers: () ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_USERS
				
		getManagedGroups: () ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_GROUPS
				
		manageSubject: (subjectId) ->
			$http
				method: "PUT"
				url: SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/manage"
				
		unmanageSubject: (subjectId) ->
			$http
				method: "PUT"
				url: SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/unmanage"
	]
