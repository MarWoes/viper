var module = angular.module('de.imi.marw.viper.export', [
  'de.imi.marw.viper.variant-table.service'
])
.controller('ExportPageCtrl', function ($q, VariantTableService) {

  var Ctrl = this;

  Ctrl.size = null;
  Ctrl.unfilteredSize = null;

  $q.all([
    VariantTableService.getSize(),
    VariantTableService.getUnfilteredSize()
  ]).then(function (data) {
    Ctrl.size = data[0];
    Ctrl.unfilteredSize = data[1];
  })

});
