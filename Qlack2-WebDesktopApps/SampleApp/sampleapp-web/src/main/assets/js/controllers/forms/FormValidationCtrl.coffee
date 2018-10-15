angular
	.module("sampleApp")
	.controller "FormValidationCtrl", ["$scope", "$compile", "UserService", "QFormValidation",
	($scope, $compile, UserService, QFormValidation) ->
		$scope.user = {}
		$scope.user.website = {}
		$scope.user.cars = new kendo.data.ObservableArray([
			{ make: "", model: "X5", year: 1950 }
			{ make: "Mercedes", model: "GL350", year: 1970 }
		])
		$scope.carsHaveError = false

		$scope.gridColumns = [
			{
				title: 'Make'
				field: 'make'
			}
			{
				title: 'Model'
				field: 'model'
			}
			{
				title: 'Year'
				field: 'year'
			}
		]

		$scope.save = () ->
			$scope.carsHaveError = false
			UserService.saveUser($scope.user).then(
				success = (result) ->

				error = (result) ->
					QFormValidation.renderFormErrors($scope, $scope.userForm, result)
			)

		$scope.$on 'VALIDATION_ERROR_cars', (event, data) ->
			console.debug data
			$scope.carsHaveError = true
			uid = $scope.carsGrid.dataSource.at(data.fieldIndex)['uid']
			cell = $('tr[data-uid="' + uid + '"] td span[ng-bind="dataItem.' + data.propertyName + '"]').parent()
			existingErrors = cell.find('.form-control-feedback').remove()
			error = "<i popover='" + data.translation + "' popover-placement='left' popover-trigger='mouseenter' class='fa fa-exclamation-triangle form-control-feedback pull-right'></i>"
			cell.prepend($compile(error)($scope))
	]

