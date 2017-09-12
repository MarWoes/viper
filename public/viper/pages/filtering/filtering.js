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

  Ctrl.decideAll = decideAll;
  Ctrl.reloadTable = reloadTable;
  Ctrl.onFilterApplied = onFilterApplied;
  Ctrl.init = init;
  Ctrl.onInspectorLinkClicked = onInspectorLinkClicked;
  Ctrl.onPageChange = onPageChange;

  Ctrl.variantPropertyToString = VariantTableService.variantPropertyToString;

  Ctrl.init();

  function decideAll (decision) {
    if(confirm("Are you sure you want to set all current variant decisions to '" + decision + "'?")) {
      VariantTableService.sendAllDecisions(decision)
      .then(Ctrl.onFilterApplied);
    }
  }

  function init () {
    VariantTableService.getColumnNames()
    .then(function (columnNames) {
      Ctrl.columnNames = columnNames;

      Ctrl.reloadTable();
    })
  }

  function reloadTable () {

    VariantTableService.getSize()
    .then(function (tableSize) {

      Ctrl.tableSize = tableSize;
      Ctrl.currentPage = VariantTableService.currentVariantPage;
      Ctrl.onPageChange();
    });
  }

  function onInspectorLinkClicked(index) {
    var variantIndex = Ctrl.pageSize * (Ctrl.currentPage - 1) + index;
    VariantTableService.currentVariantIndex = variantIndex;
  }

  function onFilterApplied() {

    VariantTableService.currentVariantPage = 1;
    VariantTableService.currentVariantIndex = 0;
    Ctrl.reloadTable();
  }

  function onPageChange () {

    VariantTableService.currentVariantPage = Ctrl.currentPage;

    if (Ctrl.tableSize == 0) {
      Ctrl.currentVariants = [ ];
      return;
    }

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
.controller('ColumnFiltersController', function (VariantTableService, $timeout, $scope) {
  var Ctrl = this;

  Ctrl.filters = undefined;

  Ctrl.applyFilters = applyFilters;
  Ctrl.init = init;
  Ctrl.onSelectRefresh = onSelectRefresh;
  Ctrl.resetFilters = resetFilters;
  Ctrl.resultLimit = 50;

  Ctrl.init();


  function init () {
    VariantTableService.getCurrentFilters()
    .then(function (filters) {
      Ctrl.filters = filters;

      resetSelections(filters);
    });
  }

  function applyFilters () {
    VariantTableService.applyFilters(Ctrl.filters)
    .then(Ctrl.onFilterApplied);

  }

  function onSelectRefresh (search, columnName) {

    if (columnName === 'viperDecision') return Ctrl.possibleValues[columnName] =  ['NA', 'declined', 'maybe', 'approved'];

    VariantTableService.searchStringColumn(columnName, search, Ctrl.resultLimit)
    .then(function (strings) {
      Ctrl.possibleValues[columnName] = strings;
    })
  }

  function resetSelections (filters) {
    Ctrl.possibleValues = { };

    for (var i = 0; i < filters.length; i++) {

      var filter = filters[i];

      if (filter.columnType == 'STRING' || filter.columnType == 'STRING_COLLECTION') {
        Ctrl.possibleValues[filter.columnName] = [ ];
      }

    }
  }

  function resetFilters () {

    for (var i = 0; i < Ctrl.filters.length; i++) {

      var filter = Ctrl.filters[i];

      if (filter.columnType == 'STRING' || filter.columnType == 'STRING_COLLECTION') {
        filter.allowedValues = [ ];
      }

      if (filter.columnType == 'NUMERIC' || filter.columnType == 'NUMERIC_COLLECTION') {
        filter.selectedMin = filter.possibleMin;
        filter.selectedMax = filter.possibleMax;
        filter.nullAllowed = true;
      }

    }

    Ctrl.applyFilters();

  }
})
.directive('columnFilters', function () {
  return {
    scope: {
      onFilterApplied: "="
    },
    restrict: 'E',
    controller: 'ColumnFiltersController',
    controllerAs: 'columnFiltersCtrl',
    templateUrl: 'viper/pages/filtering/column-filters.tpl.html',
    bindToController: true
  }
})
