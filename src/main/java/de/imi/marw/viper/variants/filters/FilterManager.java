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

import de.imi.marw.viper.variants.VariantCallFilter;
import de.imi.marw.viper.variants.VariantPropertyType;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                max = Math.floor(max);

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
