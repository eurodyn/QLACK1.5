angular
	.module('WebDesktop')
	.controller 'WindowCtrl', ['$scope', '$rootScope', 'TaskbarService', ($scope, $rootScope, TaskbarService) ->
		$scope.wdApps = []
		
		$scope.$watchCollection (->
			TaskbarService.getApplications()
		), ((newVal, oldVal) ->
			$scope.wdApps = newVal
			return
		)
	
		getAppIndex = (instanceUuid) ->
			windowIndex
			for i in [0..($scope.wdApps.length - 1)]
				if $scope.wdApps[i].instanceUuid == instanceUuid then windowIndex = i
			windowIndex
			
		ensureWindowVisibility = (element) ->
			appWindow = element.parent()
			# Ensure the titlebar is not underneath the taskbar.
			if appWindow.position().top < $('#startbar').height()
				appWindow.offset({top: $('#startbar').height() + 1})
			return
	
		$scope.windowRefresh = (e) ->
			instanceUuid = e.sender.element.data("instance-uuid")
			app = $scope.wdApps[getAppIndex(instanceUuid)]
			e.sender.center().open()
			# Set the icon on the window titlebar
			window = e.sender.element
			windowWrapper = e.sender.element.parent()
			windowWrapper.find('.k-window-title').css('background-image', 'url(' + app.icon + ')')
			# Focus taskbar element on title click
			# TODO also focus on content click
			windowWrapper.on('click', () ->
				TaskbarService.focus(instanceUuid)
			)
			# Re-bind the minimise function, so that the window is totally hidden from screen (Kendo's default behavior is to just
			# minimise the content of the window).
			window.data("kendoWindow").minimize = () ->
				TaskbarService.minimizeWindow windowWrapper, instanceUuid
			return
			
		$scope.windowClose = (e) ->
			instanceUuid = e.sender.element.data("instance-uuid")
			# Close the application in the TaskbarService
			TaskbarService.closeApplication instanceUuid
			return
			
		$scope.windowResize = (e) ->
			instanceUuid = e.sender.element.data("instance-uuid")
			ensureWindowVisibility(e.sender.element)
			TaskbarService.focus instanceUuid
			return
			
		$rootScope.$on('WD_WINDOW_MINIMIZED', (event, instanceUuid) ->
			taskCell = $("#tasklist div[data-instance-uuid='" + instanceUuid + "']")
			appWindowWrapper = $("#wdapp[data-instance-uuid='" + instanceUuid + "']").parent()
			appWindowWrapper.animate(
				{
					top: taskCell.position().top
					left: taskCell.position().left
					height: taskCell.height()
					width: taskCell.width()
					opacity: 0
				}
			)
		)
		
		$rootScope.$on('WD_WINDOW_RESTORED', (event, instanceUuid, positions) ->
			appWindow = $("#wdapp[data-instance-uuid='" + instanceUuid + "']")
			appWindowWrapper = appWindow.parent()
			appWindow.data("kendoWindow").toFront()
			appWindowWrapper.animate(
				{
					top: positions.top
					left: positions.left
					height: positions.height
					width: positions.width
					opacity: 1
				}, ->
					appWindow.data("kendoWindow").restore()
			)
		)
		
		$rootScope.$on('WD_WINDOW_TOFRONT', (event, instanceUuid, positions) ->
			appWindow = $("#wdapp[data-instance-uuid='" + instanceUuid + "']")
			appWindow.data("kendoWindow").toFront()
		)
	]