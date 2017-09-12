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
import java.util.Collection;

/**
 *
 * @author marius
 */
public class StringCollectionFilter extends AbstractStringFilter<Collection<String>> {

    public StringCollectionFilter(String columnName) {
        super(columnName, VariantPropertyType.STRING_COLLECTION);
    }

    @Override
    protected boolean isSingleColumnValuePassing(Collection<String> values) {
        return getAllowedValues().isEmpty() || getAllowedValues().stream()
                .anyMatch((allowedValue) -> values.contains(allowedValue));
    }

}
