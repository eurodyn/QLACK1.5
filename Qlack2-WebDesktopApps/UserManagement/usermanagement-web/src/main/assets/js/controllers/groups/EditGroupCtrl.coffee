angular
	.module('userManagement')
	.controller 'EditGroupCtrl', ['$scope', '$rootScope', '$state', '$stateParams', 'GroupService', 'GroupHttpService', 'UserHttpService', 'WindowService', 'SecuritySrv', 'QFormValidation', '$window', \
	($scope, $rootScope, $state, $stateParams, GroupService, GroupHttpService, UserHttpService, WindowService, SecuritySrv, QFormValidation, $window) ->
		NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
	
		securityPromise = SecuritySrv.resolvePermission('WD_MANAGE_GROUPS')

		if $stateParams.groupId isnt "0"
			groupPromise = GroupHttpService.getGroup($stateParams.groupId).then(
				success = (response) ->
					$scope.group = response.data
					return response.data
			)
		else
			groupPromise = null
		GroupService.setGroupPromise(groupPromise)
		
		$scope.actionsListTemplate = kendo.template($("#actionsListTemplate").html())
		securityPromise.then(
			success = (result) ->
				if $rootScope.permissions['WD_MANAGE_GROUPS']
					if ($stateParams.domain is "true")
						$scope.actionsDataSource = new kendo.data.DataSource(
							data: [
								key: "delete_domain"
								icon: "fa-times"
								onSelect: () ->
									deleteGroup()
							]
						)
					else
						$scope.actionsDataSource = new kendo.data.DataSource(
							data: [
								key: "delete_group"
								icon: "fa-times"
								onSelect: () ->
									deleteGroup()
							]
						)
		)
		
		$scope.userDataSource = new kendo.data.DataSource(
			transport:
				read: (options) ->
					UserHttpService.getAllUsers().then(
						success = (result) ->
							options.success(result.data)
					)
		)
		$scope.userColumns = [
			field: "username"
			headerTemplate: "<span translate>username</span>"
		,
			field: "firstName"
			headerTemplate: "<span translate>first_name</span>"
		,
			field: "lastName"
			headerTemplate: "<span translate>last_name</span>"
		,
			field: "id"
			headerTemplate: "<span translate>member</span>"
			template: "<input type='checkbox' name='groupUsers' value='#:id#' ng-checked='group.users.indexOf(\"#=id#\") > -1' ng-click='selectGroupUser(\"#:id#\")' ng-disabled='!permissions[\"WD_MANAGE_GROUPS\"]'/>"
			filterable: false
			sortable: false
		]
		
		$scope.selectGroupUser = (userId) ->
			index = $scope.group.users.indexOf(userId)
			if (index > -1)
				$scope.group.users.splice(index, 1)
			else
				$scope.group.users.push(userId)
		
		$scope.executeAction = (e) ->
			e.preventDefault()
			item = e.sender.dataItem(e.item.index())
			if (item.onSelect?)
				item.onSelect()
		
		deleteGroup = () ->
			if ($stateParams.domain is "true")
				WindowService.openWindow("delete_domain", "views/groups/deleteGroup.html")
			else
				WindowService.openWindow("delete_group", "views/groups/deleteGroup.html")
			
		$scope.cancel = () ->
			$state.go $state.current, $stateParams,
				reload: true
				
		$scope.save = () ->
			GroupHttpService.updateGroup($scope.group).then(
				success = (response) ->
					notificationTitle
					notificationContent
					if ($stateParams.domain is "true")
						notificationTitle = "usermanagement.domain_updated_title"
						notificationContent = "usermanagement.domain_updated_content"
					else
						notificationTitle = "usermanagement.group_updated_title"
						notificationContent = "usermanagement.group_updated_content"
					NotificationSrv.add(
						title: notificationTitle
						content: notificationContent
						content_data:
							group: $scope.group.name
						bubble:
							show: true
					)
					
					$state.go "groups.group",
						groupId: $stateParams.groupId
					,
						reload: true
				error = (response) ->
					QFormValidation.renderFormErrors($scope, $scope.groupForm, response)
			)
	]
