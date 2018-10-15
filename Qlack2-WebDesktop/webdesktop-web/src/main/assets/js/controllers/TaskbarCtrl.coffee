angular
	.module('WebDesktop')
	.controller 'TaskbarCtrl', ['$scope', 'TaskbarService', ($scope, TaskbarService) ->
		$scope.taskbarItems = []
		
		$scope.$watch (->
			TaskbarService.getTaskbarItems()
		), ((newVal, oldVal) ->
			$scope.taskbarItems = newVal
			return
		)
		
		$scope.selectItem = (e) ->
			instanceUuid = angular.element(e.currentTarget).data("instance-uuid")
			TaskbarService.focus(instanceUuid)
			TaskbarService.restoreWindow(instanceUuid)
	]