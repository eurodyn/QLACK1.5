angular
	.module('WebDesktop')
	.controller 'TrayCtrl', ['$scope', 'TraySrv', ($scope, TraySrv) ->
		# Initialise trayApps with any services already registered with the tray.
		$scope.trayApps = TraySrv.getApps()

		# The initial state of the tray notifications panel.
		$scope.panelVisible = TraySrv.getPanelState()

		# Watch TrayService for any changes in the registered tray apps.
		$scope.$watchCollection (()->TraySrv.getApps()), ((newVal, oldVal) ->
			if newVal != oldVal
				$scope.trayApps = newVal
		)
		# Watch TraySrv for any changes in the visibility of the panel.
		$scope.$watch (()->TraySrv.getPanelState()), ((newVal, oldVal) ->
			$scope.panelVisible = newVal
		)

		# A helper scope function to close the tray panel.
		$scope.closePanel = () ->
			TraySrv.setPanelState false

		# Returns the badge counter.
		$scope.getBadge = (appName) ->
			TraySrv.getBadge appName

		# Event listeners for mouse interaction with tray apps. The handler
		# function should return a boolean indicating whether the tray messages
		# panel should be turned on or not (this is due to this being a JS call
		# outside of Angular's scope).
		#$scope.mouseEnter = (e) ->
		#	$scope.trayApps[e]["onMouseEnter"]($("#trayPanelMessages"))
		#$scope.mouseLeave = (e) ->
		#	$scope.trayApps[e]["onMouseLeave"]($("#trayPanelMessages"))
		$scope.click = (e) ->
			$scope.trayApps[e]["onClick"]($("#trayPanelMessages"))
	]
