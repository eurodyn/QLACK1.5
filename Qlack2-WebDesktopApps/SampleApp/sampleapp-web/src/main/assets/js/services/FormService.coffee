angular
	.module("sampleApp")
	.service("FormService", ["SERVICES", "$http", "$q", (SERVICES, $http, $q) ->
		form = {}
		formVersion = {}
		
		getForm: () ->
			form
			
		getFormVersion: () ->
			formVersion
			
		getFormById: (formId, forceReload) ->
			deferred = $q.defer()
#			Only load the form from the server if force reload has been requested or if the existing form is stale
			if (forceReload or !form? or (form.id isnt formId))
				$http(
					method: "GET"
					url: SERVICES._PREFIX + SERVICES.FORM + "/" + formId
				).then(
					success = (result) ->
						form = result.data
						deferred.resolve(result.data)
						return
					error = (result) ->
						deferred.resolve("error")
						return
				)
			else 
				deferred.resolve(form)
			deferred.promise
			
		getFormVersionById: (versionId) ->
			deferred = $q.defer()
#			Normally you would get this from an REST call the same way we do for the form
			formVersion = 
				id: versionId
				name: "Version " + versionId
				lockedBy: "Christina"
				lockedOn: 1304375948024
				conditions: [
					{name: "Name 1", type: "Precondition", workingSet: "Working Set 1", rule: "Rule 1", parent_condition: "Parent 1"},
					{name: "Name 2", type: "Precondition", workingSet: "Working Set 2", rule: "Rule 2", parent_condition: "Parent 2"},
					{name: "Name 3", type: "Precondition", workingSet: "Working Set 3", rule: "Rule 3", parent_condition: "Parent 3"},
					{name: "Name 4", type: "Precondition", workingSet: "Working Set 4", rule: "Rule 4", parent_condition: "Parent 4"}
				]
			deferred.resolve(formVersion)
			deferred.promise
			
		createFormVersion: (version) ->
			deferred = $q.defer()
#			In a real implementation (ie. not sample) we should perform a REST call 
#			to save the new version and reload the form from the server in order
#			to get the updated versions list. Then resolve the deferred with 
#			the new version ID in order for it to be picked up by the caller
			versionId = form.id + "-" + (form.versions.length + 1)
			newVersion = 
				id: versionId
				name: version.name
			form.versions.push(newVersion)
			deferred.resolve(versionId)
			
			deferred.promise
	])