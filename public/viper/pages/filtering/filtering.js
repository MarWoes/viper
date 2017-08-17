var module = angular.module('de.imi.marw.viper.filtering', [
  'angular.filter',
  'de.imi.marw.viper.variant-table.service',
  'ngSanitize',
  'rzModule',
  'ui.select'
])
.controller('FilteringPageCtrl', function (VariantTableService, $http) {

  var Ctrl = this;

  Ctrl.columnNames     = undefined;
  Ctrl.currentVariants = undefined;
  Ctrl.tableSize       = 0;
  Ctrl.pageSize        = 10;
  Ctrl.currentPage     = 0;

  Ctrl.reloadTable = reloadTable;
  Ctrl.onPageChange = onPageChange;

  Ctrl.variantPropertyToString = VariantTableService.variantPropertyToString;

  Ctrl.reloadTable();

  function reloadTable () {

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
.controller('ColumnFiltersController', function (VariantTableService) {
  var Ctrl = this;

  Ctrl.filters = undefined;

  Ctrl.applyFilters = applyFilters;
  Ctrl.init = init;
  Ctrl.onSelectRefresh = onSelectRefresh;
  Ctrl.resultLimit = 50;

  Ctrl.init();


  function init () {
    VariantTableService.getCurrentFilters()
    .then(function (filters) {
      Ctrl.filters = filters;

      Ctrl.possibleValues = { };

      for (filter in Ctrl.filters) {

        if (filter.columnType == 'STRING' || filter.columnType == 'STRING_COLLECTION') {
          Ctrl.possibleValues[filter.columnName] = [ ];
        }

      }
    });
  }

  function applyFilters () {
    console.log(Ctrl.filters);
  }

  function onSelectRefresh (search, columnName) {

    if (columnName === 'viperDecision') return Ctrl.possibleValues[columnName] =  ['NA', 'declined', 'maybe', 'approved'];

    VariantTableService.searchStringColumn(columnName, search, Ctrl.resultLimit)
    .then(function (strings) {
      Ctrl.possibleValues[columnName] = strings;
    })
  }
})
.directive('columnFilters', function () {
  return {
    scope: {
      onFilterApplied: "&"
    },
    restrict: 'E',
    controller: 'ColumnFiltersController',
    controllerAs: 'columnFiltersCtrl',
    templateUrl: 'viper/pages/filtering/column-filters.tpl.html',
    bindToController: true
  }
})
