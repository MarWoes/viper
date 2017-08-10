var module = angular.module('de.imi.marw.viper.inspector', [
  'ngSanitize',
  'de.imi.marw.viper.variant-table.service'
])
.controller('InspectorPageCtrl', function (VariantTableService) {

  var Ctrl = this;

  Ctrl.tableSize      = null;
  Ctrl.index          = null;
  Ctrl.currentVariant = null;

  Ctrl.init = init;
  Ctrl.onIndexChange = onIndexChange;
  Ctrl.variantPropertyToString = VariantTableService.variantPropertyToString;

  Ctrl.init();

  function init() {

    VariantTableService.getSize().then(function (tableSize) {

      Ctrl.tableSize = tableSize;
      Ctrl.index     = 0;

      Ctrl.onIndexChange();
    });

  }

  function onIndexChange () {

    VariantTableService.getTableRow(Ctrl.index).then(function(tableRow) {

      Ctrl.currentVariant = tableRow;
    })

  }



})
