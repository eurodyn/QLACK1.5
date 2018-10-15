angular
	.module("WebDesktop", ["ui.router", "kendo.directives",
	"pascalprecht.translate", "AngularSecurityIDM", "ngCookies", "ngAnimate"])
	.config(["$stateProvider", "$urlRouterProvider", "SecuritySrvProvider",
	"$translateProvider", "$translatePartialLoaderProvider",
	($stateProvider, $urlRouterProvider, SecuritySrvProvider, $translateProvider,
	$translatePartialLoaderProvider) ->
		# Configure security
		SecuritySrvProvider.setRestPrefix("./api/security-proxy")

		# Configure translations
		$translateProvider.fallbackLanguage('en')
		$translateProvider.useLocalStorage()
		# HTML-escape all values passed to translations as arguments
		$translateProvider.useSanitizeValueStrategy('escaped')
		# Log missing translations
#		$translateProvider.useMissingTranslationHandlerLog()
		$translatePartialLoaderProvider.addPart('wd')
		$translatePartialLoaderProvider.addPart('wd_apps')
		$translateProvider.useLoader('$translatePartialLoader',
			urlTemplate: './api/i18n/translations/{part}?lang={lang}'
		)

		# Configure routes
		$urlRouterProvider
			.otherwise("/")

		$stateProvider.state "login",
			url: "/login"
			templateUrl: "views/login.html"
		$stateProvider.state "desktop",
			url: "/"
			templateUrl: "views/desktop.html"
			data:
				isPublic: false
	])
	.run(["$rootScope", "SecuritySrv", "$state", ($rootScope, SecuritySrv, $state) ->
		$rootScope.permissions = []

		$rootScope.$on('$stateChangeStart', (event, to, toParams, from, fromParams) ->
			if (!SecuritySrv.isInit())
				event.preventDefault()
				SecuritySrv.init().then(() ->
					$state.go(to, toParams);
				)
		)

		$rootScope.$on('SECURITYSRV_NOACCESS_AUTH', (event, error) ->
			$state.go("login", {}, {inherit: false })
		)
	])