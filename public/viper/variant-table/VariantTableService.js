var module = angular.module('de.imi.marw.viper.variant-table.service', [
  'de.imi.marw.viper.util.NumeralService'
])
.factory('VariantTableService', function ($http, NumeralService) {

  var Service = { };

  Service.getColumnNames = getColumnNames;
  Service.getCurrentFilters = getCurrentFilters;
  Service.getRelatedCalls = getRelatedCalls;
  Service.getRelatedColumnNames = getRelatedColumnNames;
  Service.getSize = getSize;
  Service.getTableRange = getTableRange;
  Service.getTableRow = getTableRow;
  Service.variantPropertyToString = variantPropertyToString;
  Service.saveProgress = saveProgress;
  Service.scheduleSnapshot = scheduleSnapshot;
  Service.searchStringColumn = searchStringColumn

  function performRequest (url) {
    var promise = $http.get(url).then(function (res) {
      return res.data;
    });

    return promise;
  }

  function getCurrentFilters () {
    return performRequest('/api/variant-table/current-filters');
  }

  function getColumnNames () {
    return performRequest('/api/variant-table/column-names');
  }

  function getSize () {
    return performRequest('/api/variant-table/size');
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
    var promise = $http.post("/api/variant-table/save");
    return promise;
  }

  function scheduleSnapshot(index) {

    var promise = $http.post("/api/variant-table/snapshot", {}, {
      params: {
        index: index
      }
    });

    return promise;
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
