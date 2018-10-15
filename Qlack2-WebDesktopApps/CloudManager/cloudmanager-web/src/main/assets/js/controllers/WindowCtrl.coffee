angular
	.module('cloudmanager')
	.controller 'WindowCtrl', ['$scope', '$rootScope', 'WindowService', \
	($scope, $rootScope, WindowService) ->
		$scope.windowService = WindowService
		$scope.window = null

		$scope.$watch('windowService.getWindow()', (newVal) ->
			$scope.window = newVal
		)

		$scope.windowOpen = (e) ->
			e.sender.center().open()

		$scope.windowClose = (e) ->
			# Close the application in the WindowService
			WindowService.closeWindow()
			return

#		When a window is closed destroy the Kendo widget
		$rootScope.$on('WINDOW_CLOSED', (event) ->
			$scope.modalWindow.destroy()
			return
		)
	]