/* Copyright (c) 2017 Marius Wöste
 *
 * This file is part of VIPER.
 *
 * VIPER is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VIPER is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VIPER.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
.controller('ViperMainController', [ '$location', 'VariantTableService',
                             function($location,   VariantTableService) {
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
}])
.config([  '$routeProvider',
  function ($routeProvider) {
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
}]);
