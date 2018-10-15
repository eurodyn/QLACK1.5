###
	Provides an interface to manipulate the tray of the Web Desktop environment.
	Tray entries should be added using the following format:
	{
		appName: A unique ID of your application.
		icon: A fontawesome icon to display.
		iconStacked: A fontawesome icon to displayed as stacked.
		iconStackedColor: The color of the stacked icon.
		tooltip: The translation key to be displayed when mouse is over a tray application.
		onClick: A callback handler for clicking on the icon of the application.
		onMouseEnter: A callback handler for mouse-enter event.
		onMouseLeave: A callback handler for mouse-leave event.
	}
###
angular
	.module('WebDesktop')
	.service 'TraySrv', ['$filter', ($filter) ->
		# All Tray applications holder.
		trayApps = {}
		# Badge counters for each application
		badges = {}
		# The initial state of the tray panel.
		panelVisible = false

		# Return the apps currently in tray. Note that AngularJS returns the
		# object array ordered by the keyname (i.e. the original insert order
		# is not preserved).
		getApps: () ->
			trayApps

		# Add a new app to the tray.
		registerApp: (app) ->
			app.order = Object.keys(trayApps).length
			trayApps[app.name] = app
			return

		setIcon: (app, icon) ->
			trayApps[app]["icon"] = icon
			return

		setIconStacked: (app, iconStacked) ->
			trayApps[app]["iconStacked"] = iconStacked
			return

		clearIconStacked: () ->
			trayApps[app]["iconStacked"] = undefined
			return

		setIconStackedColor: (app, iconStackedColor) ->
			trayApps[app]["iconStackedColor"] = iconStackedColor
			return

		setTooltip: (app, tooltip) ->
			trayApps[app]["tooltip"] = tooltip
			return

		clearTooltip: (app) ->
			trayApps[app]["tooltip"] = undefined
			return

		setPanelState: (newState) ->
			panelVisible = newState
			return

		togglePanelState: () ->
			panelVisible = !panelVisible
			return

		getPanelState: () ->
			panelVisible

		increaseBadge: (appName) ->
			if badges[appName]?
				badges[appName]++
			else
				badges[appName] = 1
			return

		increaseBadgeBy: (appName, increment) ->
			if badges[appName]?
				badges[appName] = badges[appName] + increment
			else
				badges[appName] = increment
			return

		decreaseBadge: (appName) ->
			if badges[appName]?
				badges[appName] = 0
			else
				badges[appName]--
			return

		decreaseBadgeBy: (appName, decrement) ->
			if badges[appName]?
				badges[appName] = 0
			else
				badges[appName] = badges[appName] - decrement
			return

		resetBadge: (appName) ->
			badges[appName] = 0
			return

		setBadge: (appName, badgeValue) ->
			badges[appName] = badgeValue

		getBadge: (appName) ->
			if badges[appName]?
				badges[appName]
			else
				0
	]