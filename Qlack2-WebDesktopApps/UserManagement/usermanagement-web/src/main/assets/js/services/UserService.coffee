angular
	.module('userManagement')
	.service 'UserService', [() ->
		userPromise = null
		
		getUserPromise: () ->
			userPromise
			
		setUserPromise: (newPromise) ->
			userPromise = newPromise
	]