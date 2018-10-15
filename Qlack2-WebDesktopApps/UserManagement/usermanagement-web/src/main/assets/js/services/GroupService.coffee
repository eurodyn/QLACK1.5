angular
	.module('userManagement')
	.service 'GroupService', [() ->
		groupPromise = null
		
		getGroupPromise: () ->
			groupPromise
			
		setGroupPromise: (newGroupPromise) ->
			groupPromise = newGroupPromise
	]