angular
	.module("appManagement")
	.service "ApplicationHttpService", ["$http", "SERVICES", ($http, SERVICES) ->
		getApplicationsAsTree: () ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.APPLICATIONS
				
		getApplicationById: (appId) ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.APPLICATIONS + "/" + appId
				
		saveApplication: (application) ->
			$http
				method: "PUT"
				url: SERVICES._PREFIX + SERVICES.APPLICATIONS + "/" + application.id
				data: application
	]