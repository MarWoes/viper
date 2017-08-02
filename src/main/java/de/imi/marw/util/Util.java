/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.imi.marw.util;

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
}
