var module = angular.module('de.imi.marw.viper.inspector', [
  'de.imi.marw.viper.igv.image',
  'de.imi.marw.viper.variant-table.service',
  'rzModule',
])
.controller('InspectorPageCtrl', function (VariantTableService, $q, $http, $interval) {

  var Ctrl = this;

  Ctrl.tableSize      = null;
  Ctrl.index          = null;
  Ctrl.relatedCallIndex = null;
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
      Ctrl.index     = VariantTableService.currentVariantIndex;
      Ctrl.columnNames = columnNames;

      Ctrl.onIndexChange();
    });

  }

  function sendDecision (decision) {

    var promise = VariantTableService.sendDecision(Ctrl.index, decision);

    if (Ctrl.index >= 0 && Ctrl.index < Ctrl.tableSize - 1) {
      Ctrl.index++;
      Ctrl.onIndexChange();
    } else {
      promise.then(Ctrl.onIndexChange);
    }
  }

  function onRelatedCallIndexChange (sliderId, modelValue) {
    VariantTableService.scheduleSnapshot(Ctrl.index, modelValue);
  }

  function onIndexChange () {

    if (Ctrl.tableSize == 0) return Ctrl.currentVariant = null;

    VariantTableService.currentVariantIndex = Ctrl.index;
    VariantTableService.scheduleSnapshot(Ctrl.index, 0);

    $q.all([
        VariantTableService.getTableRow(Ctrl.index),
        VariantTableService.getRelatedCalls(Ctrl.index),
    ]).then(function (data) {
      Ctrl.currentVariant = data[0];
      Ctrl.relatedVariants = data[1];

      var samples = [];

      for(var i = 0; i < Ctrl.relatedVariants.length; i++) {
        samples.push(Ctrl.relatedVariants[i].sample);
      }

      Ctrl.relatedCallIndex = 0;

      Ctrl.sliderOptions = {
        floor: 0,
        ceil: samples.length - 1,
        showTicks: true,
        onEnd: onRelatedCallIndexChange
      }
    });

  }

})