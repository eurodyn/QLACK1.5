angular
	.module('userManagement')
	.controller 'CreateUserCtrl', ['$scope', '$state', '$stateParams', 'UserService', 'UserHttpService', 'QFormValidation', '$window', \
	($scope, $state, $stateParams, UserService, UserHttpService, QFormValidation, $window) ->
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
	
		$scope.newUser = true
		$scope.user =
			active: true
			superadmin: false
			groups: []
		UserService.setUserPromise(null)
		
		$scope.save = () ->
			UserHttpService.createUser($scope.user).then(
				success = (response) ->
					NotificationSrv.add(
						title: "usermanagement.user_created_title"
						content: "usermanagement.user_created_content"
						content_data:
							user: $scope.user.username
						bubble:
							show: true
					)
					$state.go "users.user",
						userId: response.data
					,
						reload: true
				error = (response) ->
					QFormValidation.renderFormErrors($scope, $scope.userForm, response)
			)
		
		$scope.cancel = () ->
			$state.go "users",
				reload: true
	]
