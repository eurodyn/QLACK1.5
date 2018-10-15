angular
	.module("sampleApp")
	.controller "FormCtrl", ['$scope', '$state', '$stateParams', 'FormService', 'WindowService', 'QDateSrv', \
	($scope, $state, $stateParams, FormService, WindowService, QDateSrv) ->
		FormService.getFormById($stateParams.formId, true).then(
			sucess = (result) ->
				form = FormService.getForm()
				$scope.form = form
				if (form.versions?)
					$state.go "ui-resources.form.version", 
						formId: form.id
						versionId: form.versions[0].id
				return
		)
			
		$scope.actionsDataSource = new kendo.data.DataSource(
			data: [
				{name: "Delete", action: "deleteResource", icon: "fa-times"}
				{name: "Create new version", action: "createResourceVersion", icon: "fa-camera-retro" }
			]
		)
		$scope.actionsListTemplate = kendo.template($("#actionsListTemplate").html())
		$scope.executeAction = (e) ->
			e.preventDefault()
			action = e.sender.dataItem(e.item.index()).action
			eval(action + "()")
			
		deleteResource = () ->
			WindowService.openWindow("delete.resource", "views/ui/delete.html")
			return
			
		createResourceVersion = () ->
			WindowService.openWindow("create.resource.version", "views/ui/createVersion.html")
			return
			
		$scope.conditionsDataSource = new kendo.data.DataSource(
			data: [
				{name: "Name 1", type: "Precondition", workingSet: "Working Set 1", rule: "Rule 1", parent_condition: "Parent 1"},
				{name: "Name 2", type: "Precondition", workingSet: "Working Set 2", rule: "Rule 2", parent_condition: "Parent 2"},
				{name: "Name 3", type: "Precondition", workingSet: "Working Set 3", rule: "Rule 3", parent_condition: "Parent 3"}
			]
		)
	]

