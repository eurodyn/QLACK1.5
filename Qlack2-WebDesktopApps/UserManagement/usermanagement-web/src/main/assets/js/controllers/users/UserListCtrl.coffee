angular
	.module('userManagement')
	.controller 'UserListCtrl', ['$scope', '$stateParams', 'UserHttpService', \
	($scope, $stateParams, UserHttpService) ->
		$scope.userListSource = new kendo.data.DataSource(
			transport:
				read: (options) ->
					UserHttpService.getAllUsers().then(
						success = (result) ->
							options.success(result.data)
					)
		)
		$scope.userTemplate = kendo.template($("#userTemplate").html())
		$scope.userAltTemplate = kendo.template($("#userAltTemplate").html())
		
		$scope.userListLoaded = (e) ->
#			If a user has been select it select it in the list
			if $stateParams.userId?
				selectedUser = e.sender.dataSource.get($stateParams.userId)
				userIndex = e.sender.dataSource.indexOf(selectedUser)
				e.sender.select(e.sender.element.children().eq(userIndex))
				return
				
		$scope.filterUsers = () ->
			$scope.userListSource.filter(
				logic: "or"
				filters: [
					{field: "username", operator: "contains", value: $scope.filter},
					{field: "firstName", operator: "contains", value: $scope.filter},
					{field: "lastName", operator: "contains", value: $scope.filter}
				]
			)
		
		return
	]