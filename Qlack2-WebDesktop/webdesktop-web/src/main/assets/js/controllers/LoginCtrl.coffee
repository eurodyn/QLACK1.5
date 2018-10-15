angular
	.module('WebDesktop')
	.controller 'LoginCtrl', ['$scope', '$rootScope', '$state', '$translate', 'SecuritySrv', 'TranslationService', ($scope, $rootScope, $state, $translate, SecuritySrv, TranslationService) ->
		$scope.user = {}
		$scope.authenticationError = false
		$scope.login = () ->
			SecuritySrv.login($scope.user.username, $scope.user.password)	
			
		$scope.selectedLanguage = $translate.storage().get($translate.storageKey())
			
#		If there is no language already selected (from a previous login) then
#		determine the preferred language based on the user browser locale. Only
#		take into account languages for which we have translations
		TranslationService.getLanguages().then(
			success = (result) ->
#				Checking for "undefined" below since the value of $scope.selectedLanguage is read from 
#				the local storage and therefore in the case where angular translate writes undefined 
#				in the local storage this is read as "undefined" (string)
				if (!$scope.selectedLanguage?) or ($scope.selectedLanguage is "undefined")
					nav = window.navigator
					locale = (nav.language || nav.browserLanguage || nav.systemLanguage || nav.userLanguage || '').split('-').join('_')
					for supportedLanguage in result.data
						if (locale.indexOf(supportedLanguage.locale) == 0)
							$translate.use supportedLanguage.locale
				$scope.languageSource = new kendo.data.DataSource(
					data: result.data
				)
		)
			
			
		$rootScope.$on('SECURITYSRV_AUTH_SUCCESS', (event) ->
			$state.go("desktop", {}, {inherit: false })
		)
		
		$rootScope.$on('SECURITYSRV_AUTH_FAIL', (event) ->
			# TODO animate this to slide
			$scope.authenticationError = true
		)
		
		$scope.selectLanguage = (e) ->
			$translate.use(e.sender.element.val())
	]