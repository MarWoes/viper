var module = angular.module('de.imi.marw.viper.variant-table.service', [

])
.factory('VariantTableService', function ($http) {

  var Service = { };

  Service.getColumnNames = getColumnNames;
  Service.getSize = getSize;
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

  function variantPropertyToString (property) {

    if (property.propertyValue == null) return "NA";

    if (Array.isArray(property.propertyValue)) {

      var adjustedArray = property.propertyValue.map(function (value) {
        return value == null ? "NA" : value
      })

      return adjustedArray.join(", ");
    }

    return property.propertyValue;
  }

  return Service;
})
