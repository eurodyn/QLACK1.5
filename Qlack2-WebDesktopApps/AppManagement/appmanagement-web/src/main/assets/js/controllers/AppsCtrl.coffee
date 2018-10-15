angular
	.module("appManagement")
	.controller "AppsCtrl", ["$scope", "$state", "$stateParams", "ApplicationHttpService", \
	($scope, $state, $stateParams, ApplicationHttpService) ->
		$scope.appTreeSource = new kendo.data.HierarchicalDataSource(
			transport:
				read: (options) ->
					ApplicationHttpService.getApplicationsAsTree().then(
						success = (result) ->
							options.success(result.data)
					)
			schema:
				model:
					id: "id"
					children: "applications"
		)
		
		$scope.appTreeTemplate = kendo.template($("#appTreeTemplate").html())
		
		$scope.initTree = (e) ->
			e.sender.expand(".k-item")
#			If an application has been specified select it in the tree
#			(We get the application ID from the state param of the application child state)
			if $stateParams.applicationId?
#				Find the selected App given its ID according to Kendo treeview documentation
				selectedApp = e.sender.findByUid(e.sender.dataSource.get($stateParams.applicationId).uid)
				e.sender.select(selectedApp)
				return
		
		$scope.selectApp = (e) ->
			item = e.sender.dataItem(e.sender.current())
			# Block user from selecting application groups. We use the icon
			# field to distinguish applications from groups since groups do not
			# have an icon
			if (!item.icon?)
				e.preventDefault()
				return
			else
				$state.go "apps.application",
					applicationId: item.id
				return
	]
