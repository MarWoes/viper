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
