angular
	.module('sampleApp')
	.controller 'CreateVersionCtrl', ['$scope', '$state', 'FormService', 'WindowService', \
	($scope, $state, FormService, WindowService) ->
		$scope.versionsDataSource = new kendo.data.DataSource(
			data: FormService.getForm().versions
		)
		
		$scope.cancel = () ->
			WindowService.closeWindow()
			
		$scope.create = () ->
			FormService.createFormVersion($scope.version).then(
				success = (result) ->
					WindowService.closeWindow()
					$state.go "ui-resources.form.version", 
						formId: FormService.getForm().id
						versionId: result
			)
	]