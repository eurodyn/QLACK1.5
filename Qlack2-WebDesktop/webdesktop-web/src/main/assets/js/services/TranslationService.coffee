angular
	.module("WebDesktop")
	.service "TranslationService", ["$http", "SERVICES", ($http, SERVICES) ->
		getLanguages: () ->
			$http.get SERVICES._PREFIX + SERVICES.LANGUAGES
	]