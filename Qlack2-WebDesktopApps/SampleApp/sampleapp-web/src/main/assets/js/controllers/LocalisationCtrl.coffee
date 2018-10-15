angular
	.module("sampleApp")
	# $translate service is only needed to support translating inside the controller
	.controller "LocalisationCtrl", ["$scope", "$translate", "QDateSrv", ($scope, $translate, QDateSrv) ->
		$scope.translationKey = "dynamic_translation"
		$scope.translationValue = {value: 12}
		$scope.dynamicTranslationKey = "value_translation"
		$scope.translationAlert = () ->
			$translate('controller_translation').then(
				(result) ->
					alert result
			)
			
		$scope.dataSource = new kendo.data.DataSource(
			data: [
				{id:1, key:"dynamic_translation"}
				{id:2, key:"dynamic_translation"}
				{id:3, key:"dynamic_translation"}
			]
		)
		$scope.dropDownTemplate = kendo.template($("#dropDownTemplate").html())
		
		$scope.dateAlert = () ->
			alert QDateSrv.localise(1304375948024, 'lll')
	]