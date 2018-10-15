###
#Notification service to display balloon/bubble as well as tray notifications.
#The structure of the notification object is:
#
#		notification =
#			title:	"some title"
#			content: "some content"
#			audio:	true
#			icon:	"fa-ban"
#			error:	true
#			board:
#				show:	true
#				badge:	true
#			bubble:
#				show: true
#				timeout: 5000
###
angular
	.module('WebDesktop')
	.service 'NotificationSrv', ['TraySrv',	'AudioSrv', '$rootScope', '$translate', 'RTSrv',
	(TraySrv, AudioSrv, $rootScope, $translate, RTSrv) ->
		# A holder for notifications.
		notifications = new Array()

		# ######################################################################
		# LISTENERS
		# ######################################################################
		# In addition to the add() method below, the Notification service also
		# listens for notification events; this is an alternative way to display
		# notifications without having to obtain a reference to the service.
		$rootScope.$on "qbe_notification", (event, notification) ->
			_add notification

		# ######################################################################
		# PRIVATE functions
		# ######################################################################
		_add = (notification) ->
			# Set defaults.
			if !notification.icon? then notification.icon = "fa-info"

			# Play audio.
			if notification.audio
				AudioSrv.playAlert()

			# Show buble/board.
			if notification.bubble?
				if notification.bubble.show
					notificationJSON =
						title: $translate.instant(notification.title, notification.title_data)
						content: $translate.instant(notification.content,  notification.content_data)
						icon: notification.icon
						error: notification.error
						timeout: if notification.bubble.timeout? then notification.bubble.timeout else 10000
					$.notification notificationJSON
			if notification.board?
				if notification.board.show
					notifications.push notification
				if notification.board.badge
					TraySrv.increaseBadge "Notifications"

		# Appends the HTML representation of an individual notification.
		_createNotificationElement = (n) ->
			$("<div class='message'>
					<div class='icon'><i class='fa " + n.icon + "'></i></div>
					<div class='body'>
						<div class='title'>" + n.title + "</div>
						<div class='content'>" + n.content + "</div>
					</div>
					<div class='clear'></div>
				</div>")

		# Appends a clear notifications button.
		_createClearNotificationsButton = ->
			if notifications.length > 0
				$("<div class='msgButtonWrapper'>
						<button class='msgButton glass-button' type='button'
							onclick='WDUtil.scope().$apply(function(){WDUtil.service(\"NotificationSrv\").clear();})'>
							<span translate>clear</span></button>
					</div>")
			else
				$("<div class='noMessages'>No pending notifications</div>")

		# Returns the complete HTML for all notifications to be attached to
		# tray's glass panel.
		_renderNotifications = (e) ->
			for n in notifications
				e.append _createNotificationElement(n)
			e.append _createClearNotificationsButton

		# ######################################################################
		# PUBLIC functions
		# ######################################################################
		init: () ->
			# Initialise the notification service, placing an icon in the tray.
			TraySrv.registerApp
				name:	"Notifications"
				tooltip: "notifications_title"
				icon:  "fa-list-ul"
				onClick: (trayPanelMessages) ->
					# Render notifications only if panel is about to become
					# visible.
					if !TraySrv.getPanelState()
						trayPanelMessages.empty()
						_renderNotifications trayPanelMessages
					TraySrv.togglePanelState()
			# Register a callback to the RTService to handle RTNotifications type
			# of real-time events.
			RTSrv.registerHandler("RTNotifications", (payload) ->
				_add JSON.parse(payload)
				return
			)

		# Add a new notification to the panel and display a bubble with it.
		add: (notification) ->
			_add notification
		clear: ->
			notifications = new Array()
			TraySrv.setPanelState false
			TraySrv.resetBadge "Notifications"
	]
