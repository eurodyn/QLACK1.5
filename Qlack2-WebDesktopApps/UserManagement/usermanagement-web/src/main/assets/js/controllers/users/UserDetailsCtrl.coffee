angular
	.module('userManagement')
	.controller 'UserDetailsCtrl', ['$scope', '$state', '$stateParams', 'GroupHttpService', 'SecuritySrv', \
	($scope, $state, $stateParams, GroupHttpService, SecuritySrv) ->
	
		SecuritySrv.resolvePermission('WD_MANAGE_USERS')
		
		$scope.groupTreeSource = new kendo.data.HierarchicalDataSource(
			transport:
				read: (options) ->
					GroupHttpService.getAllGroups().then(
						success = (result) ->
							options.success(result.data)
							return
					)
			schema:
				model:
					id: "id"
					children: "childGroups"
		)
		$scope.groupTreeTemplate = kendo.template($("#groupTreeTemplate").html())
		$scope.groupCheckboxTemplate = kendo.template($("#groupCheckboxTemplate").html())
		$scope.groupTreeLoaded = (e) ->
			e.sender.expand(".k-item")
			return
		$scope.selectUserGroup = (groupId) ->
			$scope.groupsHaveError = false
			index = $scope.user.groups.indexOf(groupId)
			if (index > -1)
				$scope.user.groups.splice(index, 1)
			else
				$scope.user.groups.push(groupId)
				
		$scope.$on 'VALIDATION_ERROR_groups', (event, data) ->
			$scope.groupsHaveError = true
			$scope.groupsError = data.translation
	]
