var module = angular.module('de.imi.marw.viper.util.NumeralService', [

])
.factory('NumeralService', function ($window) {

  var Service = { };
  Service.numeral = null;

  Service.formatNumber = formatNumber;
  Service.init         = init;


  Service.init();

  function init() {
    Service.numeral = $window.numeral;

    Service.numeral.register('locale', 'viper', {
      delimiters: {
        thousands: ' ',
        decimal: '.'
      },
      abbreviations: {
        thousand: 'k',
        million: 'm',
        billion: 'b',
        trillion: 't'
      },
      ordinal : function (number) {
          return number + ".";
      },
      currency: {
          symbol: '$'
      }
    });

    Service.numeral.locale('viper');
  }

  function formatNumber(number) {

    if (Number.isInteger(number)) {
      return Service.numeral(number).format("0,0");
    } else {
      return Service.numeral(number).format("0,0.00");
    }
  }

  return Service;
})
