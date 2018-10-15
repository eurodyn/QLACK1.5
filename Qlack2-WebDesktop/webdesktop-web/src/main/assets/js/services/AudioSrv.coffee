angular
	.module('WebDesktop')
	.service 'AudioSrv', ['TraySrv', '$rootScope', (TraySrv, $rootScope) ->
		# Audio service system status.
		audioReady = false

		# Audio service user status.
		audioEnabled = true

		# ######################################################################
		# PUBLIC functions
		# ######################################################################
		init: () ->
			TraySrv.registerApp
				name:	"Audio"
				tooltip: "audio_on"
				icon:  "fa-volume-up"
				onClick: (trayPanelMessages) ->
					audioEnabled = !audioEnabled
					if audioEnabled
						TraySrv.setIcon "Audio", "fa-volume-up"
						$rootScope.$emit "qbe_notification",
							title: "Audio service"
							content: "Audio has been switched on."
							icon: "fa-volume-up"
							audio: true
							bubble:
								show: true
					else
						TraySrv.setIcon "Audio", "fa-volume-off"
						$rootScope.$emit "qbe_notification",
							title: "Audio service"
							content: "Audio has been switched off."
							icon: "fa-volume-off"
							bubble:
								show: true
			soundManager.audioFormats = {
				'mp3': {
					'type': ['audio/mpeg; codecs="mp3"', 'audio/mpeg', 'audio/mp3',
						'audio/MPA', 'audio/mpa-robust'],
					'required': false
				},
				'ogg': {
					'type': ['audio/ogg; codecs=vorbis'],
					'required': false
				},
				'wav': {
					'type': ['audio/wav; codecs="1"', 'audio/wav', 'audio/wave',
						'audio/x-wav'],
					'required': false
				},
				'mp4': {
					'related': ['aac','m4a'],
					'type': ['audio/mp4; codecs="mp4a.40.2"', 'audio/aac',
						'audio/x-m4a', 'audio/MP4A-LATM', 'audio/mpeg4-generic'],
					'required': false
				}
			}

			soundManager.defaultOptions = {
				autoLoad: true,
				autoPlay: true,
			}

			soundManager.setup
				debugMode: false,
				debugFlash: false,
				preferFlash: false,
				useHTML5Audio: true,
				url: "js/vendor/soundmanager2",
				flashVersion: 8,
				useFlashBlock: false,
				onready: ->
					audioReady = true
					console.debug "Audio system initialised."

		# Smartly chooses the correct version of the file to play according to the
		# browser's capabilities. You should, of course, have all 4 different
		# versions present. The file should be passed with the .mp3 extension, so
		# that flash version works on browsers with no HTML5 Audio support. The
		# extension will be replaced if other formats are natively available
		# via HTML5.
		playSmart: (file) ->
			if !(audioReady && audioEnabled)
				return

			if Modernizr != undefined
				if Modernizr.audio.ogg == "probably"
					file = file.slice(0, -4) + ".ogg"
				else if Modernizr.audio.m4a == "probably"
					file = file.slice(0, -4) + ".m4a"
				else if Modernizr.audio.wav == "probably"
					file = file.slice(0, -4) + ".wav"
				else if Modernizr.audio.mp3 == "probably"
					file = file
				else if Modernizr.audio.ogg == "maybe"
					file = file.slice(0, -4) + ".ogg"
				else if Modernizr.audio.m4a == "maybe"
					file = file.slice(0, -4) + ".m4a"
				else if Modernizr.audio.wav == "maybe"
					file = file.slice(0, -4) + ".wav"
				else if Modernizr.audio.mp3 == "maybe"
					file = file
				if soundManager.getSoundById('MD5' + $.md5(file)) == undefined
					soundManager.createSound
							id: 'MD5' + $.md5(file),
							url: file,
							onload: ->
								this.play()
				else
					soundManager.stop('MD5' + $.md5(file))
					soundManager.play('MD5' + $.md5(file))
			else
				console.debug("Could not use playSmart since Modernizr does not" +
					"seem to have been initialised.")

		# Play the default alert sound.
		playAlert: ->
			@playSmart("js/vendor/soundmanager2/alert.mp3");
	]
