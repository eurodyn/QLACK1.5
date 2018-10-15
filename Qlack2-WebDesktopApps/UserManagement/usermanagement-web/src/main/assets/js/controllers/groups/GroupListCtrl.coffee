angular
	.module('userManagement')
	.controller 'GroupListCtrl', ['$scope', '$stateParams', 'GroupHttpService',\
	($scope, $stateParams, GroupHttpService) ->
		$scope.groupTreeSource = new kendo.data.HierarchicalDataSource(
			transport:
				read: (options) ->
					GroupHttpService.getAllGroups().then(
						success = (result) ->
#							Iterate over domains and groups in order to assign to them the correct icons
							nodes = []
							for node in result.data
								children = []
								if (node.childGroups?)
									children.push(node.childGroups)
									i = 0
									while (i < children.length)
										for innerNode in children[i]
											innerNode.icon = "group"
											if (innerNode.childGroups?)
												children.push(innerNode.childGroups)
										i++
								node.icon = "sitemap"
								nodes.push(node)
							nodes
							options.success(nodes)
							return
					)
			schema:
				model:
					id: "id"
					children: "childGroups"
		)
		$scope.groupTreeTemplate = kendo.template($("#groupTreeTemplate").html())
		
		$scope.groupTreeLoaded = (e) ->
			e.sender.expand(".k-item")
#			If a group has been select it select it in the tree
			if $stateParams.groupId?
#				When a tree node is dragged-and-dropped this function is called again
#				and the selectedGroup may be null
				selectedGroup = e.sender.dataSource.get($stateParams.groupId)
				if (selectedGroup?)
					selectedGroupUid = e.sender.findByUid(selectedGroup.uid)
					e.sender.select(selectedGroupUid)
				return
				
		$scope.groupMoved = (e) ->
			newParentId = e.sender.dataItem(e.destinationNode).id
			groupId = e.sender.dataItem(e.sourceNode).id
			GroupHttpService.moveGroup(groupId, newParentId)
		
		return
	]