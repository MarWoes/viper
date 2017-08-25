/* Copyright (c) 2017 Marius Wöste
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
