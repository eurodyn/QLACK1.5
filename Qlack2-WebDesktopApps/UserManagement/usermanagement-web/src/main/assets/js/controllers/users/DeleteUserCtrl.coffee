angular
	.module('userManagement')
	.controller 'DeleteUserCtrl', ['$scope', '$state', '$stateParams', 'UserService', 'UserHttpService', 'WindowService', '$window',\
	($scope, $state, $stateParams, UserService, UserHttpService, WindowService, $window) ->
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
		
		$scope.cancel = () ->
			WindowService.closeWindow()
			
		$scope.delete = () ->
			UserService.getUserPromise().then(
				success = (user) ->
					UserHttpService.deleteUser(user.id).then(
						success = (result) ->
							NotificationSrv.add(
								title: "usermanagement.user_deleted_title"
								content: "usermanagement.user_deleted_content"
								content_data:
									user: user.username
								bubble:
									show: true
							)
					
							WindowService.closeWindow()
							$state.go "users", {},
								reload: true
						)
			)
			return
			
		return
	]
			
