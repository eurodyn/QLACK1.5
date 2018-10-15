angular
	.module("sampleApp")
	.controller "DataModelCtrl", ['$scope', 'WindowService', ($scope, WindowService) ->
		$scope.resourceTreeSource = new kendo.data.HierarchicalDataSource(
			data: [
				{ text: "Furniture", items: [
					{ text: "Tables & Chairs" }
					{ text: "Sofas" }
					{ text: "Occasional Furniture" }
				] }
				{ text: "Decor", items: [
					{ text: "Bed Linen" }
					{ text: "Curtains & Blinds" }
					{ text: "Carpets" }
				] }
			]
		)
		
		$scope.expandTree = (e) ->
			e.sender.expand(".k-item")
			
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
				{name: "Name 1", type: "String", version: "Version 1"},
				{name: "Name 2", type: "String", version: "Version 1"},
				{name: "Name 3", type: "String", version: "Version 1"}
			]
		)
		
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
				field: "version",
				title: "Version",
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

