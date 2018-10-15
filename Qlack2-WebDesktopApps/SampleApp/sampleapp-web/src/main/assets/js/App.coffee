angular
	.module("sampleApp", ["ui.router", "kendo.directives", "ui.bootstrap",
	"pascalprecht.translate", "ngCookies", "QDate", "flow", "AngularSecurityIDM",
	"QFormValidation"])
	.config(["$stateProvider", "$translateProvider", "$translatePartialLoaderProvider",
	"flowFactoryProvider", "SecuritySrvProvider", "$tooltipProvider", "$httpProvider",
	($stateProvider, $translateProvider, $translatePartialLoaderProvider,
	flowFactoryProvider, SecuritySrvProvider, $tooltipProvider, $httpProvider) ->
		# Configure translations
		$translateProvider.fallbackLanguage('en')
		$translateProvider.preferredLanguage("en")
		$translateProvider.useLocalStorage()
		# HTML-escape all values passed to translations as arguments
		$translateProvider.useSanitizeValueStrategy('escaped')
		# Log missing translations
		#$translateProvider.useMissingTranslationHandlerLog()
		$translatePartialLoaderProvider.addPart('sampleapp_ui')
		$translateProvider.useLoader('$translatePartialLoader',
			urlTemplate: '/api/i18n/translations/{part}?lang={lang}'
		)

		flowFactoryProvider.defaults =
			target: "/api/apps/sampleapp/service/file-upload/upload"
			permanentErrors: [404, 500, 501]
			maxChunkRetries: 1
			chunkRetryInterval: 5000
			simultaneousUploads: 4
			headers:
				"X-Qlack-Fuse-IDM-Token": JSON.stringify(JSON.parse(sessionStorage.getItem("X-Qlack-Fuse-IDM-Token")).ticket)
			generateUniqueIdentifier: () ->
				"xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace /[xy]/g, (c) ->
					r = Math.random() * 16 | 0
					v = (if c is "x" then r else (r & 0x3 | 0x8))
					v.toString 16

#		Disable tooltip animation since it causes the window content to 'move' in Chrome
		$tooltipProvider.options(animation: false)

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

		# Configure routes
		$stateProvider.state "home",
			url: ""
			templateUrl: "views/index.html"

		$stateProvider.state "layout",
			url: "/layout"
			templateUrl: "views/layout/index.html"
		$stateProvider.state "layout.2columns",
			url: "/layout-2columns"
			templateUrl: "views/layout/2columns.html"

		$stateProvider.state "forms",
			url: "/forms"
			templateUrl: "views/forms/index.html"
		$stateProvider.state "forms-validation",
			url: "/forms-validation"
			templateUrl: "views/forms/validation.html"

		$stateProvider.state "localisation",
			url: "/localisation"
			templateUrl: "views/localisation.html"

		$stateProvider.state "fileupload",
			url: "/fileupload"
			templateUrl: "views/fileupload/index.html"
		$stateProvider.state "fileupload-simple",
			url: "/fileupload-simple"
			templateUrl: "views/fileupload/simple.html"

		$stateProvider.state "ui-welcome",
			url: "/ui-welcome"
			templateUrl: "views/ui/welcome.html"

		$stateProvider.state "services",
			url: "/services"
			templateUrl: "views/services/index.html"

		$stateProvider.state "ui-resources",
			url: "/ui-resources"
			templateUrl: "views/ui/resources.html"
		$stateProvider.state "ui-resources.form",
			url: "/form/:formId"
			templateUrl: "views/ui/form.html"
		$stateProvider.state "ui-resources.form.version",
			url: "/version/:versionId"
			templateUrl: "views/ui/formVersion.html"

		$stateProvider.state "ui-data-models",
			url: "/ui-data-models"
			templateUrl: "views/ui/dataModels.html"
		$stateProvider.state "ui-working-sets",
			url: "/ui-working-sets"
			templateUrl: "views/ui/workingSets.html"


	])
	.run(["$rootScope", "SecuritySrv", "$state",
	($rootScope, SecuritySrv, $state) ->
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
	])
