var module = angular.module('de.imi.marw.viper.export', [

])
.controller('ExportPageCtrl', function ($http) {

  var Ctrl = this;

  Ctrl.downloadAllAsCsv = downloadAllAsCsv;

  function downloadAllAsCsv () {
    $http.get("/api/variant-table/export-clustered")
    .then(function () {
      console.log("complete!");
    });
  }
});
