var module = angular.module('de.imi.marw.viper.igv.image', [
])
.controller('IgvImageController', function ($scope, $timeout, $http) {
  var Ctrl = this;

  Ctrl.sleepMillis = 1000;

  Ctrl.variant      = null;
  Ctrl.chrColumn    = null;
  Ctrl.bpColumn     = null;
  Ctrl.breakpointImageLink = null;

  Ctrl.getSnapshotKey = getSnapshotKey;
  Ctrl.isSnapshotAvailable = isSnapshotAvailable;
  Ctrl.onVariantChange = onVariantChange;

  Ctrl.init = init;

  Ctrl.init();

  function init () {

    $scope.$watch(function () { return Ctrl.variant}, Ctrl.onVariantChange);

  }

  function onVariantChange (newVal, oldVal) {

    if (newVal == null) return;

    var key = Ctrl.getSnapshotKey(newVal);

    Ctrl.isSnapshotAvailable(key)
    .then(function (res) {

      var isAvailable = res.data;

      var currentKey = Ctrl.getSnapshotKey(Ctrl.variant);

      if (currentKey === key && res.data === 'true') {

        Ctrl.breakpointImageLink = '/api/snapshots/' + currentKey;

      } else if (currentKey === key && res.data === 'false') {

        Ctrl.breakpointImageLink = null;

        $timeout(function () {
          Ctrl.onVariantChange(newVal, oldVal);
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

  function getSnapshotKey(variant) {
    return variant['sample'] + '-' + variant[Ctrl.chrColumn] + '-' + variant[Ctrl.bpColumn];
  }

})
.directive('igvImage', function () {
  return {
    scope: {
      variant: '=variant',
      chrColumn: '=chrColumn',
      bpColumn: '=bpColumn'
    },
    restrict: 'E',
    controller: 'IgvImageController',
    controllerAs: 'igvImageController',
    templateUrl: 'viper/igv-image/igv-image.tpl.html',
    bindToController: true
  }
})
