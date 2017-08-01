var module = angular.module('de.imi.marw.viper',
  [
    'ngRoute',
    'ui.bootstrap'
  ]
)
.controller('ViperMainController', function($location) {
  var Ctrl = this;

  Ctrl.getCurrentRoute = function () {
    return $location.path();
  }
})
.config(function ($routeProvider, $locationProvider) {
  $routeProvider
    .when("/inspector", {
      templateUrl : "viper/pages/inspector/inspector.html"
    })
    .when("/filtering", {
      templateUrl : "viper/pages/filtering/filtering.html"
    })
    .otherwise({redirectTo : '/inspector'})
});
