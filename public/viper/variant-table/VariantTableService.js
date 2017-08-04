var module = angular.module('de.imi.marw.viper.variant-table.service', [

])
.factory('VariantTableService', function ($http) {

  var Service = { };

  Service.getColumnNames = getColumnNames;
  Service.getSize = getSize;
  Service.getTableRange = getTableRange;
  Service.getTableRow = getTableRow;
  Service.variantPropertyToString = variantPropertyToString;

  function getColumnNames () {
    var promise = $http.get('/api/variant-table/column-names').then(function (res) {
      return res.data;
    })

    return promise;
  }

  function getSize () {

    var promise = $http.get('/api/variant-table/size').then(function (res) {
      return res.data;
    });

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
    return !isNaN(parseFloat(n)) && isFinite(n);
  }

  function formatNumber(n) {
    return n.toLocaleString(undefined, { maximumFractionDigits: 2})
  }

  function variantPropertyToString (property) {

    var propertyValue = property.propertyValue;

    if (propertyValue == null) return "NA";

    if (Array.isArray(propertyValue)) {

      var adjustedArray = propertyValue.map(function (value) {
        if (value == null) return "NA";
        if (isNumeric(value)) formatNumber(value);
        return value;
      })

      return adjustedArray.join(", ");
    }

    if (isNumeric(propertyValue)) return formatNumber(propertyValue);

    return propertyValue;
  }

  return Service;
})
