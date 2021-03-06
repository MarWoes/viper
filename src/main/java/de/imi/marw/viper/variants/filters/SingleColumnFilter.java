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

import de.imi.marw.viper.variants.VariantCallFilter;
import de.imi.marw.viper.variants.VariantPropertyType;
import java.util.List;
import java.util.Map;

/**
 *
 * @author marius
 */
public abstract class SingleColumnFilter<T> implements VariantCallFilter {

    private final String columnName;
    private final VariantPropertyType columnType;

    public SingleColumnFilter(String columnName, VariantPropertyType columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    @Override
    public boolean isPassing(List values, Map<String, Integer> indexMap) {
        T value = (T) values.get(indexMap.get(columnName));
        return isSingleColumnValuePassing(value);
    }

    protected abstract boolean isSingleColumnValuePassing(T value);
}
