angular
	.module('WebDesktop')
	.service 'TaskbarService', ['$http', '$q', '$rootScope', 'ApplicationService', 'NotificationSrv', '$translate', ($http, $q, $rootScope, ApplicationService, NotificationSrv, $translate) ->
		# A holder variable to know which applications are already running.
		runningApps = new Array()

		# A holder array to know windows position in order to perform animations.
		windowPositions = new Array()

		# The taskbar items available in the web desktop
		taskbarItems = []
		# The applications currently open in the web desktop
		applications = []

		_getApplicationIndex = (instanceUuid) ->
			windowIndex
			for i in [0..(applications.length - 1)]
				if applications[i].instanceUuid == instanceUuid then windowIndex = i
			windowIndex

		_getTaskbarItemIndex = (instanceUuid) ->
			windowIndex
			for i in [0..(taskbarItems.length - 1)]
				if taskbarItems[i].instanceUuid == instanceUuid then windowIndex = i
			windowIndex

		# A private method to close a running application.
		_closeApplication = (instanceUuid) ->
			# Remove taskbar entry.
			taskbarItems.splice(_getTaskbarItemIndex(instanceUuid), 1)
			# Remove the app from running applications list.
			applications.splice(_getApplicationIndex(instanceUuid), 1)
			runningApps[$("#wdapp[data-instance-uuid='"+ instanceUuid + "']").data("application-uuid")] = undefined
			$rootScope.$emit("WD_WINDOW_CLOSED", instanceUuid)
			return

		# A private method to run/open a new application.
		_openApplication = (wdApplication, path) ->
			# Check application is not already running if multiple instances are not permitted.
			if wdApplication.instantiation.multipleInstances == false && runningApps[wdApplication.identification.uniqueId] != undefined
				NotificationSrv.add
					title: "application_running_title"
					content: "application_running_content"
					content_data:
						application: $translate.instant(wdApplication.instantiation.translationsGroup + "." + wdApplication.identification.titleKey)
					audio: true
					error: true
					bubble:
						show: true
				return
			# Check that the application is still active in case it was deactivated and the user has not refreshed the Web Desktop.
			if wdApplication.active == false
				NotificationSrv.add
					title: "application_inactive_title"
					content: "application_inactive_content"
					content_data:
						application: $translate.instant(wdApplication.instantiation.translationsGroup + "." + wdApplication.identification.titleKey)
					audio: true
					error: true
					bubble:
						show: true
				return

			# Helper variables to restrict max widths/heights.
			availableHeight = $(window).height() - $('#startbar').height() - 50	# 50px for the titlebar
			availableWidth = $(window).width()
			instanceUuid = WDUtil.createUUID()
			contentUrl = wdApplication.instantiation.path
			if path
				contentUrl += path

#			Create the application object
			application =
				instanceUuid: instanceUuid
				applicationUuid: wdApplication.identification.uniqueId
				title: wdApplication.instantiation.translationsGroup + "." + wdApplication.identification.titleKey
				content: contentUrl
				icon: wdApplication.instantiation.path + wdApplication.menu.iconSmall
				draggable: wdApplication.window.draggable
				resizable: wdApplication.window.resizable
				width: if wdApplication.window.width != undefined and wdApplication.window.width <= availableWidth then wdApplication.window.width else availableWidth
				minWidth: if wdApplication.window.minWidth != undefined then wdApplication.window.minWidth else undefined
				maxWidth: if wdApplication.window.maxWidth != undefined then wdApplication.window.maxWidth else undefined
				height: if wdApplication.window.height != undefined and wdApplication.window.height <= availableHeight then wdApplication.window.height else availableHeight
				minHeight: if wdApplication.window.minHeight != undefined then wdApplication.window.minHeight else undefined
				maxHeight: if wdApplication.window.maxHeight != undefined then wdApplication.window.maxHeight else undefined
				multipleInstances: wdApplication.instantiation.multipleInstances
				actions: (->
					retVal = []
					if wdApplication.instantiation.config then retVal.push("Custom")
					if wdApplication.window.maximizable then retVal.push("Maximize")
					if wdApplication.window.minimizable then retVal.push("Minimize")
					if wdApplication.window.closable then retVal.push("Close")
					return retVal
				)()

			_openApplicationFromObject(application)
			return
			
		_openApplicationFromObject = (application) ->
			# Update the list of running applications.
			if application.multipleInstances == false
				runningApps[application.applicationUuid] = new Date()

			# Create a new taskbar item for the application
			item =
				title: application.title
				imageUrl: application.icon
				applicationUuid: application.applicationUuid
				instanceUuid: application.instanceUuid
				active: true
			taskbarItems.push(item)
			focus(application.instanceUuid)

			# Add the application object to the applications array to be read by controllers watching the service
			applications.push(application)
			return
		

		# The default public method to request from the taskbar system to run a new application.
		openApplication: (applicationUUID) ->
			openApplication(applicationUUID, "")
		openApplication: (applicationUUID, applicationPath) ->
			deferred = $q.defer()
			ApplicationService.getApplication(applicationUUID).then((result) ->
				app = _openApplication result.data,applicationPath
				deferred.resolve(app)
			)
			deferred.promise
			
		openCustomApplication: (application) ->
			_openApplicationFromObject(application)
			
		# A method allowing callers to open a new window on the taskbar with custom content (ie. not a WD application)
#		openWindow: (contentUrl)

		# Terminate a running app.
		closeApplication: (instanceUuid) ->
			_closeApplication instanceUuid
			
		closeWindow: (window) ->
			instanceUuid = WDUtil.getFrameInstanceUUID(window.frameElement)
			_closeApplication instanceUuid

		# Sets the taskbar focus on the requested applicaton instance and brings the application window on top.
		focus: (instanceUuid) ->
			for item in taskbarItems
				item.active = false
			taskbarItems[_getTaskbarItemIndex(instanceUuid)].active = true

		minimizeWindow: (window, instanceUuid) ->
			$rootScope.$emit("WD_WINDOW_MINIMIZED", instanceUuid)
			windowPositions[instanceUuid] = {
				top: window.position().top
				left: window.position().left
				height: window.height()
				width: window.width()
			}
			# TODO here we need to find the application window with the higher z-order and focus it on the taskbar
			# otherwise its titlebar buttons are not accessible unless the user first clicks on it.

		restoreWindow: (instanceUuid) ->
			if windowPositions[instanceUuid] isnt null and windowPositions[instanceUuid]?
				$rootScope.$emit("WD_WINDOW_RESTORED", instanceUuid, windowPositions[instanceUuid])
				windowPositions[instanceUuid] = null
			else
				$rootScope.$emit("WD_WINDOW_TOFRONT", instanceUuid)

#		Expose applications and taskbar items to allow controllers to watch and respond to changes
		getApplications: () ->
			applications
		getTaskbarItems: () ->
			taskbarItems
	]
