var module = angular.module('de.imi.marw.viper.variant-table.viewer', [
  'de.imi.marw.viper.variant-table.service'
])
.controller('VariantTableViewController', function (VariantTableService) {
  var Ctrl = this;

  Ctrl.columnNames     = [];
  Ctrl.currentVariants = []
  Ctrl.tableSize       = 0;
  Ctrl.pageSize        = 10;
  Ctrl.currentPage     = 0;

  Ctrl.init = init;
  Ctrl.onPageChange = onPageChange;
  Ctrl.variantPropertyToString = VariantTableService.variantPropertyToString;

  Ctrl.init();

  function init () {

    VariantTableService.getColumnNames()
      .then(function (columnNames) {
        Ctrl.columnNames = columnNames;

        return VariantTableService.getSize();
      })
      .then(function (tableSize) {

        Ctrl.tableSize   = tableSize;

        if (tableSize == 0) return;

        Ctrl.currentPage = 1;
        Ctrl.onPageChange();
      })
  }

  function onPageChange () {

    var fromIndex = (Ctrl.currentPage - 1) * Ctrl.pageSize;
    var toIndex   = Math.min(Ctrl.currentPage * Ctrl.pageSize, Ctrl.tableSize);

    VariantTableService.getTableRange(fromIndex, toIndex).then(function (data) {
      var rawVariants = data;

      Ctrl.currentVariants = rawVariants.map(function (rawVariant) {

        return Ctrl.columnNames.map(function (columnName) {
          return Ctrl.variantPropertyToString(rawVariant[columnName]);
        })
      })
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
