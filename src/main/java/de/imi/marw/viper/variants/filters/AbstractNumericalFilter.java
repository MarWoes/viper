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
package de.imi.marw.viper.variants.filters;

import de.imi.marw.viper.variants.VariantPropertyType;

/**
 *
 * @author marius
 */
public abstract class AbstractNumericalFilter<T> extends SingleColumnFilter<T> {

    private double selectedMin, selectedMax;
    private final double possibleMin, possibleMax;
    private boolean nullAllowed;

    public AbstractNumericalFilter(String columnName, VariantPropertyType type, double possibleMin, double possibleMax) {
        super(columnName, type);
        this.possibleMin = possibleMin;
        this.possibleMax = possibleMax;
        this.selectedMin = possibleMin;
        this.selectedMax = possibleMax;
        this.nullAllowed = true;
    }

    public boolean isNumericValuePassing(Double value) {
        return (value == null && this.nullAllowed) || (value != null && value >= selectedMin && value <= selectedMax);
    }

    public double getSelectedMin() {
        return selectedMin;
    }

    public void setSelectedMin(double selectedMin) {
        this.selectedMin = selectedMin;
    }

    public double getSelectedMax() {
        return selectedMax;
    }

    public void setSelectedMax(double selectedMax) {
        this.selectedMax = selectedMax;
    }

    public double getPossibleMin() {
        return possibleMin;
    }

    public double getPossibleMax() {
        return possibleMax;
    }

    public boolean isNullAllowed() {
        return nullAllowed;
    }

    public void setNullAllowed(boolean nullAllowed) {
        this.nullAllowed = nullAllowed;
    }

}
