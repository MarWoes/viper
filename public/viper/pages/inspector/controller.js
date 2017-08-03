var module = angular.module('de.imi.marw.viper.inspector', [
  'de.imi.marw.viper.variant-table.service'
])
.controller('InspectorCtrl', function (VariantTableService) {

  var Ctrl = this;

  Ctrl.tableSize      = null;
  Ctrl.index          = null;
  Ctrl.currentVariant = null;

  Ctrl.init = init;
  Ctrl.onIndexChange = onIndexChange;
  Ctrl.variantPropertyToString = variantPropertyToString;

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

  function variantPropertyToString (property) {

    if (property.propertyValue == null) return "N/A";

    if (Array.isArray(property.propertyValue)) {

      var adjustedArray = property.propertyValue.map(function (value) {
        return value == null ? "N/A" : value
      })

      return adjustedArray.join(", ");
    }

    return property.propertyValue;
  }

})
