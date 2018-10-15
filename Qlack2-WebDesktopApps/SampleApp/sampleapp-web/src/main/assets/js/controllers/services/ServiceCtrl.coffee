angular
	.module("sampleApp")
	.controller "ServicesCtrl", ["$scope", "$http", "SERVICES",
	($scope, $http, SERVICES) ->
		# Get a reference to WD's Notification service.
		notificationSrv = window.parent.WDUtil.service("NotificationSrv")

		$scope.n1 = ()->
			notificationSrv.add
				title: "A notification msg!"
				content: "A notification message from the SampleApp app"
				icon: "fa-cutlery"
				bubble:
					show: true

		$scope.n2 = ()->
			notificationSrv.add
				title: "A notification msg!"
				content: "A notification message from the SampleApp app"
				board:
					show: true

		$scope.n3 = ()->
			notificationSrv.add
				title: "A notification msg!"
				content: "A notification message from the SampleApp app"
				icon: "fa-flag-checkered "
				board:
					show: true
				bubble:
					show: true

		$scope.n4 = ()->
			# Since we are updating the badge here (which is part of an Angular
			# model being directly rendered on screen) we need to update the
			# respective scope for the change to become visible.
			window.parent.WDUtil.scope().$apply () ->
				notificationSrv.add
					title: "A notification msg!"
					content: "A notification message from the SampleApp app"
					board:
						show: true
						badge: true
					bubble:
						show: true

		$scope.n5 = ()->
			notificationSrv.add
				title: "A notification msg!"
				content: "A notification message from the SampleApp app"
				icon: "fa-music"
				bubble:
					show: true
				audio: true

		$scope.n6 = ()->
			$http(
				method: "GET"
				url: SERVICES._PREFIX + SERVICES.LONGTASK_NOTIFICATION
			).then(
				success = (result) ->
					form = result.data
					alert result.data
					return
				error = (result) ->
					alert result
					return
			)
	]
