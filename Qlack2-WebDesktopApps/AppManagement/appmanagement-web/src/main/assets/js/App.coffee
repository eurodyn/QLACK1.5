angular
	.module("appManagement", ["ui.router", "kendo.directives", "pascalprecht.translate", "ngCookies", "ui.bootstrap", "AngularSecurityIDM"])
	.config(["$stateProvider", "$urlRouterProvider", "$translateProvider", "$translatePartialLoaderProvider", "SecuritySrvProvider", "$httpProvider", \
	($stateProvider, $urlRouterProvider, $translateProvider, $translatePartialLoaderProvider, SecuritySrvProvider, $httpProvider) ->
		# Configure translations
		$translateProvider.fallbackLanguage('en')
		$translateProvider.preferredLanguage("en")
		$translateProvider.useLocalStorage()
		# HTML-escape all values passed to translations as arguments
		$translateProvider.useSanitizeValueStrategy('escaped')
		$translatePartialLoaderProvider.addPart('appmanagement')
		$translatePartialLoaderProvider.addPart('wd')
		$translatePartialLoaderProvider.addPart('wd_apps')
		$translateProvider.useLoader('$translatePartialLoader',
			urlTemplate: '../../api/i18n/translations/{part}?lang={lang}'
		)
		
		# Configure security
		SecuritySrvProvider.setRestPrefix("../../api/security-proxy")
		
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
		
		# Configure routes
		$urlRouterProvider
			.otherwise("/apps")
		
		$stateProvider.state "apps",
			url: "/apps"
			templateUrl: "views/apps.html"
			data:
				isPublic: false
		$stateProvider.state "apps.application",
			url: "/:applicationId"
			templateUrl: "views/application.html"
			data:
				isPublic: false
				
		$stateProvider.state "help",
			url: "/help"
			templateUrl: "views/help.html"
			data:
				isPublic: false
				
		# Administration
		$stateProvider.state "access",
			url: "/access"
			templateUrl: "views/admin/access.html"
			data:
				isPublic: false
				permissions: ['APPMANAGEMENT_CONFIGURE']
				permissionMinMatches: 1
		$stateProvider.state "access.application",
			url: "/:appId"
			templateUrl: "views/admin/applicationAccess.html"
			data:
				isPublic: false
				permissions: ['APPMANAGEMENT_CONFIGURE']
				permissionMinMatches: 1
		$stateProvider.state "access.application.edit",
			url: "/:ownerId"
			templateUrl: "views/admin/accessDetails.html"
			data:
				isPublic: false
				permissions: ['APPMANAGEMENT_CONFIGURE']
				permissionMinMatches: 1
	])
	.run(["$state", "$rootScope", "SecuritySrv", ($state, $rootScope, SecuritySrv) ->
		$rootScope.permissions = []
		$rootScope.$on "$stateChangeStart", (event, to, toParams, from, fromParams) ->
			unless SecuritySrv.isInit()
				event.preventDefault()
				SecuritySrv.init().then ->
					$state.go to, toParams
					return
			return
	])
