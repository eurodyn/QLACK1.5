angular
	.module('appManagement')
	.controller 'RemoveAccessSubjectCtrl', ['$scope', '$state', '$stateParams', 'ConfigHttpService', 'WindowService',\
	($scope, $state, $stateParams, ConfigHttpService, WindowService) ->
		$scope.cancel = () ->
			WindowService.closeWindow()
			
		$scope.remove = () ->
			ConfigHttpService.unmanageSubject($stateParams.appId, WindowService.getWindow().data.subjectId).then(
				success = (response) ->
					WindowService.closeWindow()
					$state.go "access.application", $stateParams.appId,
						reload: true
			)
			
		return
	]
			