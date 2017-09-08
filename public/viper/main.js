var module = angular.module('de.imi.marw.viper',
  [
    'de.imi.marw.viper.filtering',
    'de.imi.marw.viper.inspector',
    'de.imi.marw.viper.export',
    'de.imi.marw.viper.variant-table.service',
    'ngRoute',
    'ui.bootstrap'
  ]
)
.controller('ViperMainController', function($location, VariantTableService) {
  var Ctrl = this;

  Ctrl.getCurrentRoute = getCurrentRoute;
  Ctrl.saveProgress = saveProgress;

  function getCurrentRoute() {
    return $location.path();
  }

  function saveProgress () {
    VariantTableService.saveProgress()
    .then(function (success) {
      alert('Succesfully saved progress.');
    }, function (error) {
      alert('An error occurred during saving.');
    })
  }
})
.config(function ($routeProvider) {
  $routeProvider
    .when('/inspector', {
      templateUrl : 'viper/pages/inspector/inspector.html',
      controller: 'InspectorPageCtrl',
      controllerAs: 'inspectorCtrl'
    })
    .when('/filtering', {
      templateUrl : 'viper/pages/filtering/filtering.html',
      controller: 'FilteringPageCtrl',
      controllerAs: 'filteringPageCtrl'
    })
    .when('/export', {
      templateUrl : 'viper/pages/export/export.html',
      controller: 'ExportPageCtrl',
      controllerAs: 'exportPageCtrl'
    })
    .otherwise({redirectTo : '/inspector'})
});
