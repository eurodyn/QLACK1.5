angular
	.module('sampleApp')
	.controller 'WindowCtrl', ['$scope', '$compile', '$rootScope', 'WindowService', '$timeout', \
	($scope, $compile, $rootScope, WindowService, $timeout) ->
		$scope.windowService = WindowService
		$scope.window = null
		
		$scope.$watch('windowService.getWindow()', (newVal) ->
			$scope.window = newVal
		)
		
		$scope.windowOpen = (e) ->
#			Force the window to open after DOM manipulation in order for it to be correctly
#			centered based on its content
			$timeout(() ->
				e.sender.center().open()
			)
	
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