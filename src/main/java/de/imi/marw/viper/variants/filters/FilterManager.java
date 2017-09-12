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
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author marius
 */
public class FilterManager {

    private Collection<VariantCallFilter> filters;

    public void loadFromTable(VariantTable table) {

        Collection<VariantCallFilter> generatedFilters = new ArrayList<>();

        for (String columnName : table.getColumnNames()) {

            VariantPropertyType type = table.getColumnType(columnName);
            List<Object> columnCalls = table.getUnfilteredColumn(columnName);

            VariantCallFilter filter = generateFilterFromColumn(columnCalls, type, columnName);

            generatedFilters.add(filter);
        }

        this.filters = generatedFilters;
    }

    public Collection<VariantCallFilter> getFilters() {
        return filters;
    }

    public void setFilters(Collection<VariantCallFilter> filters) {
        this.filters = filters;
    }

    private VariantCallFilter generateFilterFromColumn(List<Object> columnCalls, VariantPropertyType type, String columnName) {

        switch (type) {

            case NUMERIC: {
                double[] values = columnCalls.stream()
                        .filter(d -> d != null)
                        .mapToDouble(i -> (Double) i)
                        .distinct()
                        .sorted()
                        .toArray();

                double min = values.length > 0 ? values[0] : 0;
                double max = values.length > 0 ? values[values.length - 1] : 1;

                // HACK: we use ceil and floor to prevent potential errors because of rounding when using rz-sliders
                min = Math.floor(min);
                max = Math.ceil(max);

                return new NumericFilter(columnName, min, max);
            }

            case NUMERIC_COLLECTION: {
                double[] values = columnCalls.stream()
                        .filter(d -> d != null)
                        .flatMapToDouble(c -> ((Collection<Double>) c).stream().filter(d -> d != null).mapToDouble(e -> e))
                        .distinct()
                        .sorted()
                        .toArray();

                double min = values.length > 0 ? values[0] : 0;
                double max = values.length > 0 ? values[values.length - 1] : 1;

                return new NumericCollectionFilter(columnName, min, max);
            }

            case STRING:
                return new StringFilter(columnName);

            case STRING_COLLECTION:
                return new StringCollectionFilter(columnName);

            default: {
                throw new IllegalStateException("Unexpected column type " + type + " when generating filters");
            }
        }

    }

}
