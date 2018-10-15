angular
	.module("sampleApp")
	.controller "FileSimpleCtrl", ["$scope", "$filter", ($scope, $filter) ->
		$scope.user = {}

		$scope.attachments = {}
		$scope.certificates = {}

		$scope.submit = () ->
			attachments = []
			attachments.push(f.uniqueIdentifier) for f in $scope.attachments.flow.files
			$scope.user.attachments = attachments

			certificates = []
			certificates.push(f.uniqueIdentifier) for f in $scope.certificates.flow.files
			$scope.user.certificates = certificates

			alert JSON.stringify $scope.user
	]
