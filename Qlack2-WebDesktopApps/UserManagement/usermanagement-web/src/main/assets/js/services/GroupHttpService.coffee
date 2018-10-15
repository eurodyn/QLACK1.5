angular
	.module('userManagement')
	.service 'GroupHttpService', ['$http', 'SERVICES', ($http, SERVICES) ->
		getAllGroups: () ->
			$http
				method: "GET"
				url:  SERVICES._PREFIX + SERVICES.GROUPS
				
		getGroup: (id) ->
			$http
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.GROUPS + "/" + id
				
		createGroup: (group, parentId) ->
			$http
				method: "POST"
				url: SERVICES._PREFIX + SERVICES.GROUPS + "/" + parentId
				data: group
				
		updateGroup: (group) ->
			$http
				method: "PUT"
				url: SERVICES._PREFIX + SERVICES.GROUPS + "/" + group.id
				data: group
				
		deleteGroup: (groupId) ->
			$http
				method: "DELETE"
				url: SERVICES._PREFIX + SERVICES.GROUPS + "/" + groupId
				
		moveGroup: (groupId, newParentId) ->
			$http
				method: "PUT"
				url: SERVICES._PREFIX + SERVICES.GROUPS + "/" + groupId + SERVICES.GROUP_ACTION_MOVE
				params:
					newParentId: newParentId
	]
