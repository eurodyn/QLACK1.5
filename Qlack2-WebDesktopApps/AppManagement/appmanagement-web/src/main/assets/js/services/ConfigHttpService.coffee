angular
	.module('appManagement')
	.service 'ConfigHttpService', ['$http', 'SERVICES', ($http, SERVICES) ->
		getOperations: (appId, subjectId) ->
			url = SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/operations"
			if !!appId
				url = url + "?appId=" + appId
			$http
				method: "GET"
				url:  url
				
		saveOperations: (appId, subjectId, operations) ->
			url = SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/operations"
			if !!appId
				url = url + "?appId=" + appId
			$http
				method: "POST"
				url: url
				data: operations
				
		getManagedUsers: (appId) ->
			url = SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_MANAGED_USERS
			if !!appId
				url = url + "?appId=" + appId
			$http
				method: "GET"
				url: url
				
		getManagedGroups: (appId) ->
			url = SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_MANAGED_GROUPS
			if !!appId
				url = url + "?appId=" + appId
			$http
				method: "GET"
				url: url
				
		manageSubject: (appId, subjectId) ->
			url = SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/manage"
			if !!appId
				url = url + "?appId=" + appId
			$http
				method: "PUT"
				url: url
				
		unmanageSubject: (appId, subjectId) ->
			url = SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_SUBJECTS + "/" + subjectId + "/unmanage"
			if !!appId
				url = url + "?appId=" + appId
			$http
				method: "PUT"
				url: url
				
		getAllUsers: () ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_USERS
				
		getAllGroups: () ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.CONFIG + SERVICES.CONFIG_GROUPS
	]
