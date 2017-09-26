/* Copyright (c) 2017 Marius Wöste
 *
 * This file is part of VIPER.
 *
 * VIPER is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VIPER is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VIPER.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
var module = angular.module('de.imi.marw.viper.util.NumeralService', [

])
.factory('NumeralService', [ '$window',
                    function ($window) {

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
}]);
