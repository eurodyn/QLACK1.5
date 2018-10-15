angular
	.module('userManagement')
	.controller 'DeleteGroupCtrl', ['$scope', '$state', '$stateParams', 'GroupService', 'GroupHttpService', 'WindowService', '$window', \
	($scope, $state, $stateParams, GroupService, GroupHttpService, WindowService, $window) ->
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
		
		if ($stateParams.domain is "true")
			$scope.domain = true
		else
			$scope.domain = false
		
		$scope.cancel = () ->
			WindowService.closeWindow()
			
		$scope.delete = () ->
			GroupService.getGroupPromise().then(
				success = (group) ->
					GroupHttpService.deleteGroup(group.id).then(
						success = (result) ->
							notificationTitle
							notificationContent
							if ($stateParams.domain is "true")
								notificationTitle = "usermanagement.domain_deleted_title"
								notificationContent = "usermanagement.domain_deleted_content"
							else
								notificationTitle = "usermanagement.group_deleted_title"
								notificationContent = "usermanagement.group_deleted_content"
							NotificationSrv.add(
								title: notificationTitle
								content: notificationContent
								content_data:
									group: group.name
								bubble:
									show: true
							)
					
							WindowService.closeWindow()
							$state.go "groups", {},
								reload: true
						)
			)
			return
			
		return
	]
			
