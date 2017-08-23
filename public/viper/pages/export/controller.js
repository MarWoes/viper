var module = angular.module('de.imi.marw.viper.export', [
  'ngFileSaver'
])
.controller('ExportPageCtrl', function ($http, FileSaver) {

  var Ctrl = this;

  Ctrl.downloadAllAsCsv = downloadAllAsCsv;

  function downloadAllAsCsv () {
    $http({
      url: '/api/variant-table/export-clustered',
      method: 'GET',
      responseType: 'blob'
    })
    .then(function (res) {
      FileSaver.saveAs(res.data, 'viper-all.csv');
    });
  }
});
