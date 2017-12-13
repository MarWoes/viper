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
var module = angular.module('de.imi.marw.viper.igv.image', [
])
.controller('IgvImageController', [ '$scope', '$timeout', '$http',
                          function ( $scope,   $timeout,   $http) {
  var Ctrl = this;

  Ctrl.sleepMillis = 1000;

  Ctrl.variant      = null;
  Ctrl.chrColumn    = null;
  Ctrl.bpColumn     = null;
  Ctrl.breakpointImageLink = null;
  Ctrl.igvConfigurationHash = null;

  Ctrl.getSnapshotKey = getSnapshotKey;
  Ctrl.isSnapshotAvailable = isSnapshotAvailable;
  Ctrl.onUpdate = onUpdate;

  Ctrl.init = init;

  Ctrl.init();

  function init () {

    $scope.$watch(function () { return { variant: Ctrl.variant, hash: Ctrl.igvConfigurationHash }; }, Ctrl.onUpdate, true);

  }

  function onUpdate (newVal, oldVal) {

    var variant = newVal.variant;
    var hash = newVal.hash;

    if (variant == null || hash == null) return;

    var key = Ctrl.getSnapshotKey(variant, hash);

    Ctrl.isSnapshotAvailable(key)
    .then(function (res) {

      var isAvailable = res.data;

      var currentKey = Ctrl.getSnapshotKey(Ctrl.variant, Ctrl.igvConfigurationHash);

      if (currentKey === key && res.data === 'true') {

        // key might contain url component parameters such as '&' or '?' so we
        // need to escape those characters
        Ctrl.breakpointImageLink = '/api/snapshots/' + encodeURIComponent(currentKey);

      } else if (currentKey === key && res.data === 'false') {

        Ctrl.breakpointImageLink = null;

        $timeout(function () {
          Ctrl.onUpdate(newVal, oldVal);
        }, Ctrl.sleepMillis);

      }

    })

  }

  function isSnapshotAvailable(snapshotKey) {
    var promise = $http.get('/api/snapshots/is-available', {
      params: { key: snapshotKey }
    });

    return promise;
  }

  function getSnapshotKey(variant, hash) {
    return variant['sample'] + '-' + variant[Ctrl.chrColumn] + '-' + variant[Ctrl.bpColumn] + "-" + hash;
  }

}])
.directive('igvImage', [ function () {
  return {
    scope: {
      variant: '=variant',
      chrColumn: '=chrColumn',
      bpColumn: '=bpColumn',
      igvConfigurationHash: '=igvConfigurationHash'
    },
    restrict: 'E',
    controller: 'IgvImageController',
    controllerAs: 'igvImageController',
    templateUrl: 'viper/igv-image/igv-image.tpl.html',
    bindToController: true
  }
}]);
