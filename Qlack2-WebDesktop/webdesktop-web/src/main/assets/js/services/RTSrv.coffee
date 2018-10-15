angular
	.module('WebDesktop')
	.service 'RTSrv', ['TraySrv', 'SECURITY_CONSTANTS', (TraySrv, SECURITY_CONSTANTS) ->
		# An indicator to know when the underlying connection is established.
		connected = false
		# Callback functions to handle messages received.
		messageHandlers = new Array()
		# Local reference to CometD.
		cometd = $.cometd;

		# ######################################################################
		# PRIVATE functions
		# ######################################################################
		# Function that manages the connection status with the Bayeux server.
		metaConnect = (message) ->
			if cometd.isDisconnected()
				connected = false
				return

			wasConnected = connected
			connected = (message.successful == true)
			if !wasConnected && connected
				TraySrv.clearIconStacked "RT"
				TraySrv.setTooltip "RT", "network_on"
			else if wasConnected && !connected
				TraySrv.setIconStacked "RT", "fa-ban"
				TraySrv.setTooltip "RT", "network_off"

		# Invoked when first contacting the server and when the server has
		# lost the state of this client (and re-connects).
		metaHandshake = (handshake) ->
			if handshake.successful
				# Subscribe to the private & public channels.
				cometd.subscribe "/service/private", (msg) ->
					handleCometMsg(msg)
				console.debug "Subscribed to /service/private channel."
				cometd.subscribe "/public", (msg) ->
					handleCometMsg(msg)
				console.debug "Subscribed to /public channel."
				TraySrv.clearIconStacked "RT"
				TraySrv.setTooltip "RT", "network_on"
			else
				TraySrv.setIconStacked "RT", "fa-ban"
				TraySrv.setTooltip "RT", "network_off"

		# Initial handler for all CometD messages. Every message arrives on this
		# function where it gets dispatched to the appropriate service handler.
		handleCometMsg = (msg) ->
			if msg? and msg.data? and msg.data.handlerID? and messageHandlers[msg.data.handlerID]?
				messageHandlers[msg.data.handlerID](msg.data.payload)

		getSecurityTicket = () ->
			retVal = ""
			ticket = sessionStorage.getItem SECURITY_CONSTANTS.QLACK_AUTH_LOCAL_STORAGE_NAME
			if ticket != null
				retVal = JSON.stringify(JSON.parse(ticket).ticket)
			return retVal

		# ######################################################################
		# PUBLIC functions
		# ######################################################################
		init: () ->
			# Initialise the connection.
			cometd.configure
				url: window.location.protocol + "//" + window.location.host + window.location.pathname + "/rt"
				logLevel: "warn"

				
			cometd.addListener '/meta/handshake', metaHandshake
			cometd.addListener '/meta/connect', metaConnect
			cometd.websocketEnabled = Modernizr.websockets
			#cometd.websocketEnabled = false
			console.debug "RT system initialised."
			TraySrv.registerApp
				name:	"RT"
				tooltip: "network_on"
				icon:  "fa-cloud"
				iconStackedColor: "red"
			cometd.handshake
				ext:
					authentication:
						ticket: getSecurityTicket()

		debug: ->
			return cometd

		# Provides an entry-point for applications to register their handler
		# for CometD messages.
		registerHandler: (service, handler) ->
			messageHandlers[service] = handler
			console.debug "Registering RT handler for service: " + service
			return

	]