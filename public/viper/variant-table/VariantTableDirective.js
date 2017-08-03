var module = angular.module('de.imi.marw.viper.variant-table.viewer', [
  'de.imi.marw.viper.variant-table.service'
])
.controller('VariantTableViewController', function (VariantTableService) {
  var Ctrl = this;

  Ctrl.columnNames = [];

  Ctrl.variantPropertyToString = VariantTableService.variantPropertyToString;

  Ctrl.init = init;

  Ctrl.init();

  function init () {
    VariantTableService.getColumnNames().then(function (columnNames) {
      Ctrl.columnNames = columnNames;
    })
  }

})
.directive('variantTableViewer', function () {
  return {
    restrict: 'E',
    controller: 'VariantTableViewController',
    controllerAs: 'variantTableViewCtrl',
    templateUrl: 'viper/variant-table/variant-table.tpl.html'
  }
})
