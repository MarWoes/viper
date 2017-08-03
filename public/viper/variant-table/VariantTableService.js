var module = angular.module('de.imi.marw.viper.variant-table.service', [

])
.factory('VariantTableService', function ($http) {

  var Service = { };

  Service.getSize = getSize;
  Service.getTableRow = getTableRow;


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

  return Service;
})
