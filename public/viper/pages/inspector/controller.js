var module = angular.module('de.imi.marw.viper.inspector', [
  'ngSanitize',
  'de.imi.marw.viper.variant-table.service'
])
.controller('InspectorPageCtrl', function (VariantTableService, $q) {

  var Ctrl = this;

  Ctrl.tableSize      = null;
  Ctrl.index          = null;
  Ctrl.currentVariant = null;
  Ctrl.relatedVariants = [ ];
  Ctrl.columnNames = [ ];

  Ctrl.init = init;
  Ctrl.onIndexChange = onIndexChange;
  Ctrl.variantPropertyToString = VariantTableService.variantPropertyToString;

  Ctrl.init();

  function init() {

    $q.all([
      VariantTableService.getSize(),
      VariantTableService.getRelatedColumnNames()
    ]).then(function (data) {

      var tableSize = data[0];
      var columnNames = data[1];

      Ctrl.tableSize = tableSize;
      Ctrl.index     = 0;
      Ctrl.columnNames = columnNames;

      Ctrl.onIndexChange();
    });

  }

  function onIndexChange () {

    $q.all([
        VariantTableService.getTableRow(Ctrl.index),
        VariantTableService.getRelatedCalls(Ctrl.index),
    ]).then(function (data) {
      Ctrl.currentVariant = data[0];
      Ctrl.relatedVariants = data[1];
    });

  }



})
