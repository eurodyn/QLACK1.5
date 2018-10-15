angular
	.module("sampleApp")
	.controller "FormVersionCtrl", ['$scope', '$state', '$stateParams', 'FormService', 'WindowService', 'QDateSrv', \
	($scope, $state, $stateParams, FormService, WindowService, QDateSrv) ->
#		Ensure that the form is loaded if this state is accessed directly
		FormService.getFormById($stateParams.formId, false).then(
			success = (result) ->	
				$scope.versionsDataSource = new kendo.data.DataSource(
					data: FormService.getForm().versions
				)	
				FormService.getFormVersionById($stateParams.versionId).then(
					sucess = (result) ->
						$scope.formVersion = FormService.getFormVersion()
						$scope.lockedOn = QDateSrv.localise($scope.formVersion.lockedOn)
						$scope.conditionsDataSource = new kendo.data.DataSource(
							data: $scope.formVersion.conditions
						)
						return
				)
		)
		
		$scope.versionsListTemplate = kendo.template($("#versionsListTemplate").html())
		$scope.selectVersion = (e) ->
			versionId = e.sender.dataItem(e.item.index()).id
			$state.go "ui-resources.form.version", 
				formId: $stateParams.formId
				versionId: versionId
		
		$scope.conditionsToolbar = [{name: "create", text: "Add condition"}]		
		$scope.conditionsColumns = [
			{field: "name", title: "Name"},
			{
				field: "type",
				title: "Condition Type",
				editor: (container, options) ->
					html = "<select kendo-drop-down-list k-data-text-field=\"'name'\" k-data-value-field=\"'id'\" k-option-label=\"'Select parent condition'\" k-data-source=\"parentsDataSource\" data-bind='value:" + options.field + "'></select>"
					el = $(html).appendTo(container)
			},
			{
				field: "workingSet",
				title: "Working Set",
				editor: (container, options) ->
					html = "<select kendo-drop-down-list k-data-text-field=\"'name'\" k-data-value-field=\"'id'\" k-option-label=\"'Select parent condition'\" k-data-source=\"parentsDataSource\" data-bind='value:" + options.field + "'></select>"
					el = $(html).appendTo(container)
			},
			{
				field: "rule",
				title: "Rule",
				editor: (container, options) ->
					html = "<select kendo-drop-down-list k-data-text-field=\"'name'\" k-data-value-field=\"'id'\" k-option-label=\"'Select parent condition'\" k-data-source=\"parentsDataSource\" data-bind='value:" + options.field + "'></select>"
					el = $(html).appendTo(container)
			},
			{
				field: "parent_condition",
				title: "Follows After",
				editor: (container, options) ->
					html = "<select kendo-drop-down-list k-data-text-field=\"'name'\" k-data-value-field=\"'id'\" k-option-label=\"'Select parent condition'\" k-data-source=\"parentsDataSource\" data-bind='value:" + options.field + "'></select>"
					el = $(html).appendTo(container)
			},
			{
				command: [
					{ name: "edit", text: { edit: "", cancel: "", update: "" } },
					{ name: "destroy", text: "" }
				],
				width: 80
			}
		]
	]

