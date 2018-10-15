angular
	.module('userManagement')
	.controller 'MenuCtrl', \
		['$scope', '$state', 'SecuritySrv', 'UserHttpService', '$window', \
		 ($scope,   $state,   SecuritySrv,   UserHttpService,   $window) ->
#		Watching user to resolve permissions since we do not pass
#		through UI router to get to this controller and therefore it
#		might be executed before having a chance to initialise the security service
		$scope.$watch(() ->
			SecuritySrv.getUser()
		, (newVal, oldVal) ->
			SecuritySrv.resolvePermission('WD_MANAGE_USERS')
			SecuritySrv.resolvePermission('WD_MANAGE_GROUPS')
			SecuritySrv.resolvePermission('USERMANAGEMENT_CONFIGURE')
		)

		$scope.$watch(() ->
			SecuritySrv.getUser()
		, (newVal, oldVal) ->
			secutityUser = SecuritySrv.getUser()
			if (secutityUser)
				userId = secutityUser.ticket.userID
				UserHttpService.getUser(userId).then(
					success = (response) ->
						user = response.data
						$scope.userIsSuperadmin = user.superadmin
				)
		)

		$scope.userIsSuperadmin = false

		$scope.groupIsSelected = () ->
			return $state.current.name.indexOf('groups.group') == 0

		$scope.exit = () ->
			taskbarService = $window.parent.WDUtil.service('TaskbarService')
			taskbarService.closeWindow($window)
			$window.parent.WDUtil.service('$timeout')(->
				$window.parent.WDUtil.scope().$apply()
			)
	]
