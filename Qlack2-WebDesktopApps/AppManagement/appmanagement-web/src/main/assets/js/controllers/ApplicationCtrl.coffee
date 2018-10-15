angular
	.module("appManagement")
	.controller "ApplicationCtrl", ["$scope", "ApplicationHttpService", "$stateParams", "SecuritySrv", "$translate", "$window", \
	($scope, ApplicationHttpService, $stateParams, SecuritySrv, $translate, $window) ->
		SecuritySrv.resolvePermission('WD_UPDATE_APPLICATION')
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
		
		ApplicationHttpService.getApplicationById($stateParams.applicationId).then(
			success = (result) ->
				$scope.application = result.data
				return
		)
		
		$scope.save = () ->
			ApplicationHttpService.saveApplication($scope.application).then(
				success = (response) ->
					NotificationSrv.add(
						title: "appmanagement.application_updated_title"
						content: "appmanagement.application_updated_content"
						content_data:
							application: $translate.instant($scope.application.titleKey)
						bubble:
							show: true
					)
			)
		
		$scope.cancel = () ->
			$state.go $state.current, $stateParams,
				reload: true
			
		return
	]
