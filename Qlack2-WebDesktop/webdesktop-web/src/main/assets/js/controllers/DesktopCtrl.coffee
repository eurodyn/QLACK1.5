angular
	.module('WebDesktop')
	.controller 'DesktopCtrl', ['$scope', '$state', 'ApplicationService',
	'TaskbarService', 'SecuritySrv', '$translate', 'RTSrv', 'NotificationSrv',
	'AudioSrv',
	($scope, $state, ApplicationService, TaskbarService, SecuritySrv, $translate,
	RTSrv, NotificationSrv, AudioSrv) ->
		# Real-time system initialisation.
		RTSrv.init()

		# Notifications system initialisation.
		NotificationSrv.init()

		# Audio system initialisation.
		AudioSrv.init()

		$scope.logout = () ->
			SecuritySrv.logout()
			$state.go("login", {}, {inherit: false })

		# Datasources and templates for menu and desktop applications
		$scope.groupSource = new kendo.data.DataSource(
			transport:
				read: (options) ->
					ApplicationService.getApplicationGroups().then(
						success = (result) ->
							options.success(result.data)
					)
		)
		$scope.menuAppSource = new kendo.data.DataSource(
			transport:
				read: (options) ->
					if ($scope.selectedGroupId?)
						ApplicationService.getApplicationsForGroup($scope.selectedGroupId).then(
							success = (result) ->
								options.success(result.data)
						)
					# If we do not read from the server notify the datasource
					# that the call was successful in order for it not to get blocked
					else
						options.success([])
						return
		)
		$scope.noGroupAppSource = new kendo.data.DataSource(
			transport:
				read: (options) ->
					ApplicationService.getApplicationsForNoGroup().then(
						success = (result) ->
							options.success(result.data)
					)
		)
		$scope.desktopAppSource = new kendo.data.DataSource(
			transport:
				read: (options) ->
					ApplicationService.getDesktopApplications($scope.selectedGroupId).then(
						success = (result) ->
							options.success(result.data)
					)
		)
		$scope.menuGroupTemplate = kendo.template($("#menuGroupTemplate").html())
		$scope.noGroupAppTemplate = kendo.template($("#noGroupAppTemplate").html())
		$scope.appTemplate = kendo.template($("#appTemplate").html())

		$scope.openApplication = ($event) ->
			appUUID = WDUtil.getApplicationUUID($($event.currentTarget).parent())
			TaskbarService.openApplication(appUUID)
			$scope.closeMenu()
			return


		# Menu functionalities
		$scope.openMenu = () ->
			if ($('#menu').position().top > 0)  # if menu is already open, close it.
				$('#menu').animate({top: (-1 * $('#menu').height()) - $("#startbar").height()})
			else  # else, show menu.
				$scope.menuOpenWatch = Date.now()
				$('#menu').animate({top: $('#startbar').height() + 1})
			return

		$scope.closeMenu = () ->
			if (Date.now() - $scope.menuOpenWatch > 500)
				$('#menu').animate({top: (-1 * $('#menu').height()) - $("#startbar").height()})
			return

		$scope.selectMenuGroup = (e) ->
			$scope.selectedGroupId = e.sender.select().data("id")
			$scope.menuAppSource.read()

		$scope.selectFirstMenuGroup = (e) ->
			$scope.selectedGroupId = $scope.groupList.element.children().first().data("id")
			$scope.menuAppSource.read()
			# TODO the below should work however an Error: $rootScope:inprog is thrown
			# Perhaps there is something wrong with how the listview performs $scope.apply() ?
			# $scope.groupList.select(1)

		$scope.makeAppDraggable = (e) ->
			$(e.sender.element).children('.appicon').each (index) ->
				#TODO - drag & drop on mobile devices blocks applications from opening
				$(this).kendoDraggable
					hint: (e) ->
						return $('<div>' + e.html() + '</div>')
			return

		# TODO - implement application filtering. Kendo supports filtering of the
		# datasource as below, however in order to filter we need the translated
		# application names. We could translate application names in schema:parse
		# of the datasource which allows pre-processing of the datasource items after
		# they are retrieved and before they are first used however in order to translate
		# an application name we first need to load the application translation which
		# is something that happens asynchronously.
#		filterMenu = (e) ->
#			this.appSource.filter(
#				field: "translatedTitle"
#				operator: "contains"
#				value: desktopModel.filter
#			)


		# Desktop functionalities
		$scope.initAppContextMenu = (e) ->
			e.sender.element.children('.appicon').each (index, element) ->
				$(element).contextmenu
					width: 200,
					items:[
						{ text: $translate.instant("desktop_open"), icon: "img/start.png", alias: "OPEN", action: (x) ->
							taskbarSystem.openApplication(WDDesktopUtil.getApplicationUUID(x))
						},
						{ text: $translate.instant("desktop_remove"), icon: "img/undo.png", alias: "DEL", action: (e) ->
							ApplicationService.removeApplicationFromDesktop(WDUtil.getApplicationUUID(e)).then(
								() ->
									$scope.desktopAppSource.read()
							)
						}
					]
			return

		# Setup Desktop as a drop target for drag events from the start menu.
		$("#desktop-content").kendoDropTarget
			drop: (e) ->
				# Get the UUID from the dropped object.
				appUUID = WDUtil.getApplicationUUID e.draggable.element[0]
				# Add this application to the user's desktop.
				ApplicationService.addApplicationToDesktop(appUUID).then(
					() ->
						$scope.desktopAppSource.read()
				)

	]