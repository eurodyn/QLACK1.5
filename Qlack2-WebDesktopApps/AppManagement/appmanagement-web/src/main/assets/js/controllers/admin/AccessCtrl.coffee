angular
	.module("appManagement")
	.controller "AccessCtrl", ["$scope", "$state", "$stateParams", "ApplicationHttpService", \
	($scope, $state, $stateParams, ApplicationHttpService) ->
		$scope.appTreeSource = new kendo.data.HierarchicalDataSource(
			transport:
				read: (options) ->
					ApplicationHttpService.getApplicationsAsTree().then(
						success = (result) ->
							tree = []
							tree.push(
								id: ''
								key: 'generic_permissions'
							)
							for element in result.data
								tree.push element
							options.success(tree)
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
			if $stateParams.appId?
#				Find the selected App given its ID according to Kendo treeview documentation
				selectedApp = e.sender.findByUid(e.sender.dataSource.get($stateParams.appId).uid)
				e.sender.select(selectedApp)
				return
		
		$scope.selectApp = (e) ->
			item = e.sender.dataItem(e.sender.current())
			# Block user from selecting application groups.
			if (item.applications?)
				e.preventDefault()
				return
	]