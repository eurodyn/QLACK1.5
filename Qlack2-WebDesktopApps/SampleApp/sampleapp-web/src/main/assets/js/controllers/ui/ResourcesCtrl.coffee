angular
	.module("sampleApp")
	.controller "ResourcesCtrl", ['$scope', '$state', 'WindowService', 'QDateSrv', ($scope, $state, WindowService, QDateSrv) ->
		$scope.resourceTreeSource = new kendo.data.HierarchicalDataSource(
			data: [
				{ text: "Category 1", items: [
					{ id: "1", text: "Tables & Chairs" }
					{ id: "2", text: "Sofas" }
					{ id: "3", text: "Occasional Furniture" }
				] }
				{ text: "Category 2", items: [
					{ id: "4", text: "Bed Linen" }
					{ id: "5", text: "Curtains & Blinds" }
					{ id: "6", text: "Carpets" }
				] }
			]
		)
		
		$scope.expandTree = (e) ->
			e.sender.expand(".k-item")
		
		$scope.selectForm = (e) ->
			item = e.sender.dataItem(e.sender.current())
			$state.go "ui-resources.form", 
				formId: item.id
			return
	]

