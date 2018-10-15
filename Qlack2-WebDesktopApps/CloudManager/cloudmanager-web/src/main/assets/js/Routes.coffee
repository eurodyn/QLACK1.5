angular
	.module("cloudmanager")
	.config ["$stateProvider", "$urlRouterProvider",
	($stateProvider, $urlRouterProvider) ->
		# #####################################################################
		# Configure routing
		# #####################################################################
		$stateProvider.state "home",
			url: ""
			templateUrl: "views/index.html"
	]