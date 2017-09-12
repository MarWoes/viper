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
package de.imi.marw.viper.util;

/**
 *
 * @author marius
 */
public class Util {

    public static final String DIGITS = "(\\p{Digit}+)";
    public static final String HEX_DIGETS = "(\\p{XDigit}+)";
    public static final String EXP = "[eE][+-]?" + DIGITS;
    public static final String FP_REGEX
            = ("[\\x00-\\x20]*"
            + "[+-]?("
            + "NaN|"
            + "Infinity|"
            + "(((" + DIGITS + "(\\.)?(" + DIGITS + "?)(" + EXP + ")?)|"
            + "(\\.(" + DIGITS + ")(" + EXP + ")?)|"
            + "(("
            + "(0[xX]" + HEX_DIGETS + "(\\.)?)|"
            + "(0[xX]" + HEX_DIGETS + "?(\\.)" + HEX_DIGETS + ")"
            + ")[pP][+-]?" + DIGITS + "))"
            + "[fFdD]?))"
            + "[\\x00-\\x20]*");

    public static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        }
        if (val > max) {
            return max;
        }
        return val;
    }
}
