var module = angular.module('de.imi.marw.viper.igv.image', [
])
.controller('IgvImageController', function ($scope, $interval, $http) {
  var Ctrl = this;

  Ctrl.variant      = null;
  Ctrl.chrColumn    = null;
  Ctrl.bpColumn     = null;
  Ctrl.imagePromise = null;
  Ctrl.breakpointImageLink = null;

  $scope.$watch(function() { return Ctrl.variant }, function (newVal, oldVal) {

    if (Ctrl.imagePromise != null) $interval.cancel(Ctrl.imagePromise);

    Ctrl.imagePromise = $interval(function () {

      var key = newVal['sample'] + '-' + newVal[Ctrl.chrColumn] + '-' + newVal[Ctrl.bpColumn];
      $http.get('/api/variant-table/is-snapshot-available', {
        params: { key: key }
      })
      .then(function (res) {

        if (res.data === 'true') {

            if (Ctrl.imagePromise != null) $interval.cancel(Ctrl.imagePromise);

            var currentKey = Ctrl.variant['sample'] + '-' + Ctrl.variant[Ctrl.chrColumn] + '-' + Ctrl.variant[Ctrl.bpColumn];

            if (currentKey === key) {
              Ctrl.breakpointImageLink = '/api/variant-table/snapshot/' + currentKey;
            }
        }

      })

    }, 250);

  })
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
