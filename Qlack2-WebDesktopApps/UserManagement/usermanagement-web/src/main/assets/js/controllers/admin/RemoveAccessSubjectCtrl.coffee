angular
	.module('userManagement')
	.controller 'RemoveAccessSubjectCtrl', ['$scope', '$state', '$stateParams', 'ConfigHttpService', 'WindowService',\
	($scope, $state, $stateParams, ConfigHttpService, WindowService) ->
		$scope.cancel = () ->
			WindowService.closeWindow()
			
		$scope.remove = () ->
			ConfigHttpService.unmanageSubject(WindowService.getWindow().data.subjectId).then(
				success = (response) ->
					WindowService.closeWindow()
					$state.go "access", {},
						reload: true
			)
			
		return
	]
			
