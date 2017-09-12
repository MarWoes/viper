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

var module = angular.module('de.imi.marw.viper.variant-table.service', [
  'de.imi.marw.viper.util.NumeralService'
])
.factory('VariantTableService', function ($http, NumeralService) {

  var Service = { };

  Service.currentVariantIndex = 0;
  Service.currentVariantPage = 1;

  Service.applyFilters = applyFilters;
  Service.getColumnNames = getColumnNames;
  Service.getCurrentFilters = getCurrentFilters;
  Service.getRelatedCalls = getRelatedCalls;
  Service.getRelatedColumnNames = getRelatedColumnNames;
  Service.getSize = getSize;
  Service.getTableRange = getTableRange;
  Service.getTableRow = getTableRow;
  Service.getUnfilteredSize = getUnfilteredSize;
  Service.saveProgress = saveProgress;
  Service.scheduleSnapshot = scheduleSnapshot;
  Service.searchStringColumn = searchStringColumn
  Service.sendAllDecisions = sendAllDecisions;
  Service.sendDecision = sendDecision;
  Service.variantPropertyToString = variantPropertyToString;

  function applyFilters (filters) {
    var promise = $http.post('/api/filters/apply',filters)
    .then(function (res) {
      return res.data;
    })

    return promise;
  }

  function performRequest (url) {
    var promise = $http.get(url).then(function (res) {
      return res.data;
    });

    return promise;
  }

  function getCurrentFilters () {
    return performRequest('/api/filters/current');
  }

  function getColumnNames () {
    return performRequest('/api/variant-table/column-names');
  }

  function getSize () {
    return performRequest('/api/variant-table/size');
  }

  function getUnfilteredSize () {
    return performRequest('/api/variant-table/unfiltered-size');
  }

  function getRelatedColumnNames () {
    return performRequest('/api/variant-table/related-calls/column-names');
  }

  function searchStringColumn (columnName, search, limit) {
    var promise = $http.get('/api/variant-table/string-column-search', {
      params: {
        columnName: columnName,
        search: search,
        limit: limit
      }
    }).then(function (res) {
      return res.data;
    })

    return promise;
  }

  function getRelatedCalls (index) {

    var promise = $http.get('/api/variant-table/related-calls', {
      params: { index: index }
    }).then(function (res) {
      return res.data;
    })

    return promise;
  }

  function getTableRow (index) {

    var promise = $http.get('/api/variant-table/row', {
      params: { index: index }
    }).then(function (res) {
      return res.data;
    })

    return promise;
  }

  function getTableRange (fromIndex, toIndex) {

    var promise = $http.get('/api/variant-table/rows', {
      params: { from: fromIndex, to: toIndex }
    }).then(function (res) {
      return res.data;
    })

    return promise;
  }

  function isNumeric(n) {
    return typeof n === "number";
  }

  function saveProgress() {
    var promise = $http.post("/api/decisions/save");
    return promise;
  }

  function scheduleSnapshot(index, relatedCallIndex) {

    var promise = $http.post("/api/snapshots/take-snapshot", {}, {
      params: {
        index: index,
        relatedCallIndex: relatedCallIndex
      }
    });

    return promise;
  }

  function sendAllDecisions (decision) {
    return $http.put('/api/decisions/all', {}, {
      params: {
        decision: decision
      }
    }).then(function (res) {
      return res.data;
    });
  }

  function sendDecision (index, decision) {

    return $http.put('/api/decisions/change', {}, {
      params: {
        index: index,
        decision: decision
      }
    }).then(function (res) {
      return res.data;
    });
  }


  function variantPropertyToString (propertyValue) {

    if (propertyValue == null) return "NA";

    if (Array.isArray(propertyValue)) {

      var propertyCollectionCount = propertyValue
        .map(function (value) {
          if (value == null) return "NA";
          if (isNumeric(value)) NumeralService.formatNumber(value);
          return value;
        })
        .reduce(function (acc, curr) {
          acc[curr] ? acc[curr]++ : acc[curr] = 1;

          return acc;
        }, { });

      var keys = Object.keys(propertyCollectionCount)

      return keys.join(", ");

    }

    if (isNumeric(propertyValue)) return NumeralService.formatNumber(propertyValue);

    return propertyValue;
  }

  return Service;
})
