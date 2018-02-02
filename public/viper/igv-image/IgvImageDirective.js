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

  Ctrl.sample       = null;
  Ctrl.chr          = null;
  Ctrl.pos          = null;
  Ctrl.breakpointImageLink = null;
  Ctrl.igvConfigurationHash = null;

  Ctrl.getSnapshotKey = getSnapshotKey;
  Ctrl.isSnapshotAvailable = isSnapshotAvailable;
  Ctrl.onUpdate = onUpdate;

  Ctrl.init = init;

  Ctrl.init();

  function init () {

    $scope.$watch(function () { return { sample: Ctrl.sample, chr: Ctrl.chr, pos: Ctrl.pos, hash: Ctrl.igvConfigurationHash }; }, Ctrl.onUpdate, true);

  }

  function onUpdate (newVal, oldVal) {

    var sample = newVal.sample;
    var chr = newVal.chr;
    var pos = newVal.pos;
    var hash = newVal.hash;

    if (sample == null || chr == null || pos == null || hash == null) return;

    var key = Ctrl.getSnapshotKey(sample, chr, pos, hash);

    Ctrl.isSnapshotAvailable(key)
    .then(function (res) {

      var isAvailable = res.data;

      var currentKey = Ctrl.getSnapshotKey(Ctrl.sample, Ctrl.chr, Ctrl.pos, Ctrl.igvConfigurationHash);

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

  function getSnapshotKey(sample, chr, pos, hash) {
    return sample + '-' + chr + '-' + pos + "-" + hash;
  }

}])
.directive('igvImage', [ function () {
  return {
    scope: {
      sample: '=sample',
      chr: '=chr',
      pos: '=pos',
      igvConfigurationHash: '=igvConfigurationHash'
    },
    restrict: 'E',
    controller: 'IgvImageController',
    controllerAs: 'igvImageController',
    templateUrl: 'viper/igv-image/igv-image.tpl.html',
    bindToController: true
  }
}]);
