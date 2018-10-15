angular
	.module("cloudmanager", ["ui.router", "ui.bootstrap", "AngularSecurityIDM"])
	.config(["$stateProvider", "SecuritySrvProvider", "$httpProvider", \
	($stateProvider, SecuritySrvProvider, $httpProvider) ->
		# Configure security
		SecuritySrvProvider.setRestPrefix("/api/security-proxy")
		
		# When the $http call returns error check if it is a validation
		# error and if it is not show a generic error messages. Validation
		# errors are handled by the controller.
		$httpProvider.interceptors.push(($q, $injector) ->
			'responseError': (rejection) ->
				$window = $injector.get '$window'
				NotificationSrv = $window.parent.WDUtil.service('NotificationSrv')
				if (rejection.status is 401)
					NotificationSrv.add(
						title: "error_session_expired_title"
						content: "error_session_expired_body"
						audio: true
						error: true
						bubble:
							show: true
					)
				else
					if (rejection.data.validationErrors?)
						NotificationSrv.add(
							title: "error_validation_title"
							content: "error_validation_body"
							audio: true
							error: true
							bubble:
								show: true
						)
					else
						NotificationSrv.add(
							title: "error_generic_request_title"
							content: "error_generic_request_body"
							audio: true
							error: true
							bubble:
								show: true
						)
				$q.reject(rejection)
		)
	])
	.run(["$rootScope", "SecuritySrv", "$state",
	($rootScope, SecuritySrv, $state) ->
		console.debug "hello!"
		# #####################################################################
		# Configure security
		# #####################################################################
		$rootScope.permissions = []
		$rootScope.$on "$stateChangeStart", (event, to, toParams, from, fromParams) ->
			unless SecuritySrv.isInit()
				event.preventDefault()
				SecuritySrv.init().then ->
					$state.go to, toParams
					return
			return

		$rootScope.$on "$stateChangeSuccess", (event, to, toParams, from, fromParams) ->
			if to.data?
				unless to.data.notScrollToTop
					smoothScroll document.getElementById('top'), {
						duration: 1
						easing: 'easeInOutQuart'
						offset: 120
					}
			return

		$rootScope.$on "SECURITYSRV_AUTH_SUCCESS", () ->
			# If the user had requested a specific state redirect to it
			# otherwise go to the homepage.
			if ($rootScope.requestedState?)
				$state.go $rootScope.requestedState.name, $rootScope.requestedState.params,
					location: true
					inherit: false
				$rootScope.requestedState = null
			else
				$state.go "home", {},
					location: true
					inherit: false
			return

		#$rootScope.$on "SECURITYSRV_AUTH_FAIL", () ->
		#	growl.clear()
		#	growl.error "notification_bad_credentials", {ttl: 10000}
		#	return

		#$rootScope.$on "SECURITYSRV_AUTH_LOGOUT", () ->
		#	growl.clear()
		#	$state.go "login", {},
		#		location: true
		#		inherit: false
		#	return

		#$rootScope.$on "SECURITYSRV_AUTH_HTTP_ERROR", () ->
		#	growl.clear()
		#	growl.error "notification_security_generic_error", {ttl: 10000}
		#	return

		$rootScope.$on "SECURITYSRV_NOACCESS_AUTH", (event, attributes) ->
			# Save the scope the user attempted to access in order to redirect
			# to it after login (see SECURITYSRV_AUTH_SUCCESS event).
			$rootScope.requestedState =
				name: attributes.to.name
				params: attributes.toParams
			$state.go "login", {},
				location: false
				inherit: false
			return

		#$rootScope.$on "SECURITYSRV_NOACCESS_PERM", (event, attributes) ->
		#	growl.error "notification_permission_required", {ttl: 10000}
		#	return
	])
