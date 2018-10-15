class window.WDUtil
	constructor: () ->

	# A convenience method to allow JS code to invoke Angular services.
	# Note that changes happening inside a service this way will NOT be picked
	# up by Angular's watches, as they do not happen within a specific Angular
	# $scope (you will need to $scope.$apply to make such changes visible).
	@service: (serviceName) ->
		angular.element('*[ng-app]').injector().get(serviceName)

	# Returns an Angular $scope object. For convenience, the top-level scope
	# is returned, so that any $scope.$apply is also taking into account children
	# at different nested level.
	@scope: ->
		angular.element('*[ng-app]').scope()

	# Creates a UUID.
	@createUUID: ->
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) ->
			r = Math.random() * 16 | 0
			v = if c is 'x' then r else (r & 0x3|0x8)
			v.toString(16)
		)

	# Get server URL.
	@serverURL: ->
		return window.location.protocol + "//" + window.location.host

	# Obtains the application UID from an HTML element (containing a data-application-uuid attribute).
	@getApplicationUUID: (e) ->
		return $(e).attr("data-application-uuid")
		
	@getFrameInstanceUUID: (frame) ->
		src = frame.src
		src.substring(src.indexOf("instanceUUID") + "instanceUUID=".length, src.indexOf("&"))
