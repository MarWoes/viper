var module = angular.module('de.imi.marw.viper.inspector', [
  'de.imi.marw.viper.variant-table.service'
])
.controller('InspectorPageCtrl', function (VariantTableService, $q, $http) {

  var Ctrl = this;

  Ctrl.tableSize      = null;
  Ctrl.index          = null;
  Ctrl.currentVariant = null;
  Ctrl.relatedVariants = [ ];
  Ctrl.columnNames = [ ];

  Ctrl.init = init;
  Ctrl.onIndexChange = onIndexChange;
  Ctrl.variantPropertyToString = VariantTableService.variantPropertyToString;
  Ctrl.sendDecision = sendDecision;

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

  function sendDecision (decision) {

    var promise = $http.put('/api/variant-table/decision', {}, {
      params: {
        index: Ctrl.index,
        decision: decision
      }
    });

    if (Ctrl.index >= 0 && Ctrl.index < Ctrl.tableSize - 1) {
      Ctrl.index++;
      Ctrl.onIndexChange();
    } else {
      promise.then(Ctrl.onIndexChange);
    }
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
