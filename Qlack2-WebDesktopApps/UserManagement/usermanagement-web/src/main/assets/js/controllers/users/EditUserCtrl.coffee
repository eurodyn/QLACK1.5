angular
	.module('userManagement')
	.controller 'EditUserCtrl', ['$scope', '$rootScope', '$state', '$stateParams', 'UserService', 'UserHttpService', 'GroupHttpService', 'WindowService', 'SecuritySrv', 'QFormValidation', '$window', \
	($scope, $rootScope, $state, $stateParams, UserService, UserHttpService, GroupHttpService, WindowService, SecuritySrv, QFormValidation, $window) ->
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
	
		securityPromise = SecuritySrv.resolvePermission('WD_MANAGE_USERS')
	
		$scope.newUser = false
		userPromise = UserHttpService.getUser($stateParams.userId).then(
			success = (response) ->
				$scope.user = response.data
				return response.data
		)
		UserService.setUserPromise(userPromise)
		
		$scope.actionsListTemplate = kendo.template($("#actionsListTemplate").html())
		securityPromise.then(
			success = (result) ->
				$scope.actionsDataSource = new kendo.data.DataSource(
					data: [{
						key: "delete_user"
						icon: "fa-times"
						onSelect: () ->
							deleteUser()
					},{
						key: "reset_password"
						icon: "fa-refresh"
						onSelect: () ->
							resetUserPassword()
					}]
				)
		)
		
		$scope.executeAction = (e) ->
			e.preventDefault()
			item = e.sender.dataItem(e.item.index())
			if (item.onSelect?)
				item.onSelect()
		
		deleteUser = () ->
			WindowService.openWindow("delete_user", "views/users/deleteUser.html")
			
		resetUserPassword = () ->
			WindowService.openWindow("reset_password", "views/users/resetPassword.html")
		
		$scope.save = () ->
			UserHttpService.updateUser($scope.user).then(
				success = (response) ->
					NotificationSrv.add(
						title: "usermanagement.user_updated_title"
						content: "usermanagement.user_updated_content"
						content_data:
							user: $scope.user.username
						bubble:
							show: true
					)
					
					$state.go $state.current, $stateParams,
						reload: true
				error = (response) ->
					QFormValidation.renderFormErrors($scope, $scope.userForm, response)
			)
		
		$scope.cancel = () ->
			$state.go $state.current, $stateParams,
				reload: true
	]
