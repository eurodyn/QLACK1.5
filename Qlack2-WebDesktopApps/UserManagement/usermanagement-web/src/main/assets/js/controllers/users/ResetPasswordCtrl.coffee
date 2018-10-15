angular
	.module('userManagement')
	.controller 'ResetPasswordCtrl', ['$scope', '$state', '$stateParams', 'UserService', 'UserHttpService', 'WindowService', 'QFormValidation', '$window', \
	($scope, $state, $stateParams, UserService, UserHttpService, WindowService, QFormValidation, $window) ->
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
		
		$scope.user = {}
	
		$scope.cancel = () ->
			WindowService.closeWindow()
			
		$scope.save = () ->
			UserService.getUserPromise().then(
				success = (selectedUser) ->
					UserHttpService.resetUserPassword(selectedUser.id, $scope.user).then(
						success = (result) ->
							NotificationSrv.add(
								title: "usermanagement.password_reset_title"
								content: "usermanagement.password_reset_content"
								content_data:
									user: selectedUser.username
								bubble:
									show: true
							)
							
							WindowService.closeWindow()
							$state.go "users.user",
								userId : selectedUser.id
							,
								reload: true
						error = (response) ->
							QFormValidation.renderFormErrors($scope, $scope.userForm, response)
						)
			)
			return
			
		return
	]
			
