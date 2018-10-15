angular
	.module('userManagement')
	.controller 'CreateGroupCtrl', ['$scope', '$state', '$stateParams', 'GroupService', 'GroupHttpService', 'QFormValidation', '$window', \
	($scope, $state, $stateParams, GroupService, GroupHttpService, QFormValidation, $window) ->
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
	
		$scope.group = {}
		GroupService.setGroupPromise(null)
			
		$scope.cancel = () ->
			$state.go "groups.group",
				groupId: $stateParams.groupId
			,
				reload: true
				
		$scope.save = () ->
			GroupHttpService.createGroup($scope.group, $stateParams.groupId).then(
				success = (response) ->
					notificationTitle
					notificationContent
					if ($stateParams.groupId == '0')
						notificationTitle = "usermanagement.domain_created_title"
						notificationContent = "usermanagement.domain_created_content"
					else
						notificationTitle = "usermanagement.group_created_title"
						notificationContent = "usermanagement.group_created_content"
					NotificationSrv.add(
						title: notificationTitle
						content: notificationContent
						content_data:
							group: $scope.group.name
						bubble:
							show: true
					)
					
					$state.go "groups.group",
						groupId: response.data
					,
						reload: true
				error = (response) ->
					QFormValidation.renderFormErrors($scope, $scope.groupForm, response)
			)
	]
