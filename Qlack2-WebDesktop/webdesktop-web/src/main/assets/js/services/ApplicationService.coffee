angular
	.module("WebDesktop")
	.service "ApplicationService", ["$http", "SERVICES", ($http, SERVICES) ->
		getApplicationGroups: () ->
			$http.get SERVICES._PREFIX + SERVICES.GROUPS
				
		getApplicationsForGroup: (groupId) ->
			$http.get SERVICES._PREFIX + SERVICES.GROUP_APPLICATIONS + "/" + groupId
			
		getApplicationsForNoGroup: () ->
			$http.get SERVICES._PREFIX + SERVICES.GROUP_APPLICATIONS
			
		getDesktopApplications: () ->
			$http.get SERVICES._PREFIX + SERVICES.DESKTOP_APPLICATIONS
			
		getApplication: (appUUID) ->
			$http.get SERVICES._PREFIX + SERVICES.APPLICATION + "/" + appUUID
			
		addApplicationToDesktop: (appUUID) ->
			$http.put SERVICES._PREFIX + SERVICES.DESKTOP_APPLICATION + "/" + appUUID
			
		removeApplicationFromDesktop: (appUUID) ->
			$http.delete SERVICES._PREFIX + SERVICES.DESKTOP_APPLICATION + "/" + appUUID
	]