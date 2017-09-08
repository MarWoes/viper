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
package de.imi.marw.viper.variants.table;

import de.imi.marw.viper.variants.VariantCallFilter;
import de.imi.marw.viper.variants.VariantPropertyType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author marius
 */
public class VariantTable {

    public static final String TYPE_COLUMN_NAME = "svType";
    public static final String SAMPLE_COLUMN_NAME = "sample";
    public static final String CHR1_COLUMN_NAME = "chr1";
    public static final String CHR2_COLUMN_NAME = "chr2";
    public static final String BP1_COLUMN_NAME = "bp1";
    public static final String BP2_COLUMN_NAME = "bp2";

    public static final String DECISION_COLUMN_NAME = "viperDecision";
    public static final String ID_COLUMN_NAME = "viperId";

    public static final String[] MANDATORY_FIELDS = {
        SAMPLE_COLUMN_NAME,
        TYPE_COLUMN_NAME,
        CHR1_COLUMN_NAME,
        BP1_COLUMN_NAME,
        CHR2_COLUMN_NAME,
        BP2_COLUMN_NAME
    };

    public static final VariantPropertyType[] MANDATORY_FIELDS_TYPES = {
        VariantPropertyType.STRING,
        VariantPropertyType.STRING,
        VariantPropertyType.STRING,
        VariantPropertyType.NUMERIC,
        VariantPropertyType.STRING,
        VariantPropertyType.NUMERIC
    };

    private final List<List<Object>> rows;
    private final List<String> columnNames;
    private final List<VariantPropertyType> types;
    private final Map<String, Integer> indexMap;
    private int[] softFilter;

    public VariantTable(Collection<List<Object>> calls, List<String> columnNames, List<VariantPropertyType> types) {

        this.indexMap = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {

            String columnName = columnNames.get(i);

            if (indexMap.containsKey(columnName)) {
                throw new IllegalArgumentException("table contains duplicates of column " + columnName);
            }

            indexMap.put(columnNames.get(i), i);
        }
        this.rows = new ArrayList<>();
        this.columnNames = columnNames;
        this.rows.addAll(calls);
        this.types = types;
        this.softFilter = IntStream.range(0, this.rows.size())
                .toArray();

        checkDataIntegrity();
    }

    public synchronized void filter(Collection<VariantCallFilter> filters) {

        int[] callsAfterFiltering = IntStream.range(0, this.rows.size())
                .boxed()
                .filter((callIndex) -> filters.stream().allMatch((filter) -> filter.isPassing(this.rows.get(callIndex), indexMap)))
                .mapToInt(i -> i)
                .toArray();

        this.softFilter = callsAfterFiltering;
    }

    public synchronized List<String> searchStringColumn(String columnName, String search, int limit) {

        VariantPropertyType type = getColumnType(columnName);

        Stream<String> values;

        switch (type) {
            case STRING:
                values = this.rows.stream()
                        .map(call -> (String) call.get(indexMap.get(columnName)));
                break;
            case STRING_COLLECTION:
                values = this.rows.stream()
                        .flatMap(call -> ((Collection<String>) call.get(indexMap.get(columnName))).stream());
                break;
            default:
                throw new IllegalStateException("Unexpected non-string type " + type + " when trying to access column " + columnName);
        }

        return values
                .filter(str -> str.contains(search))
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());

    }

    public synchronized Map<String, Object> getCall(int rowIndex) {

        Map<String, Object> variantCall = new LinkedHashMap<>(columnNames.size());
        List rawRow = this.rows.get(softFilter[rowIndex]);

        for (int i = 0; i < columnNames.size(); i++) {
            variantCall.put(this.columnNames.get(i), rawRow.get(i));
        }

        return variantCall;
    }

    public synchronized Object getCallProperty(int index, String columnName) {
        return this.rows.get(softFilter[index]).get(indexMap.get(columnName));
    }

    public int getSoftFilteredIndex(int unfilteredIndex) {
        return this.softFilter[unfilteredIndex];
    }

    public synchronized List<List<Object>> getRawCalls() {
        return this.rows;
    }

    public synchronized List<Map<String, Object>> getCallRange(int lower, int upper) {
        return IntStream
                .range(lower, upper)
                .boxed()
                .map((index) -> getCall(index))
                .collect(Collectors.toList());
    }

    public synchronized int getNumberOfCalls() {
        return this.softFilter.length;
    }

    public synchronized List<String> getColumnNames() {
        return columnNames;
    }

    public synchronized List<VariantPropertyType> getColumnTypes() {
        return types;
    }

    public synchronized Map<String, Integer> getColumnIndexMap() {
        return indexMap;
    }

    public synchronized List<VariantPropertyType> getTypes() {
        return types;
    }

    public synchronized VariantPropertyType getColumnType(String column) {
        return types.get(indexMap.get(column));
    }

    public ColumnProperty[] getColumnProperties() {
        ColumnProperty[] properties = new ColumnProperty[this.types.size()];

        for (int i = 0; i < this.types.size(); i++) {
            properties[i] = new ColumnProperty(this.columnNames.get(i), this.types.get(i));
        }

        return properties;
    }

    public synchronized void setCallProperty(int rowIndex, String column, Object newValue) {
        checkCorrectType(newValue, getColumnType(column));

        this.rows.get(softFilter[rowIndex]).set(indexMap.get(column), newValue);
    }

    public synchronized List<Object> getUnfilteredColumn(String columnName) {

        return this.rows.stream()
                .map(call -> call.get(indexMap.get(columnName)))
                .collect(Collectors.toList());

    }

    private void checkCorrectCollectionType(Object value, Class someClass) {
        if (value instanceof Collection) {
            ((Collection) value).stream().forEach((o) -> checkCorrectType(o, someClass));
        } else {
            throw new IllegalArgumentException("Variant table contained value "
                    + value + " that could not be interpreted as a collection of "
                    + someClass.getSimpleName() + ", is " + (value == null ? "null" : value.getClass().getSimpleName()));
        }
    }

    private void checkCorrectType(Object value, Class someClass) {
        if (value != null && !someClass.isInstance(value)) {
            throw new IllegalArgumentException("Variant table contained value "
                    + value.toString() + " that could not be interpreted as "
                    + someClass.getSimpleName() + ", is " + value.getClass().getSimpleName());
        }
    }

    private void checkCorrectType(Object newValue, VariantPropertyType type) {
        switch (type) {
            case STRING:
                checkCorrectType(newValue, String.class);
                break;
            case STRING_COLLECTION:
                checkCorrectCollectionType(newValue, String.class);
                break;
            case NUMERIC:
                checkCorrectType(newValue, Double.class);
                break;
            case NUMERIC_COLLECTION:
                checkCorrectCollectionType(newValue, Double.class);
                break;
            default:
                throw new IllegalStateException("Unexpected type " + type + " when checking variant value");
        }
    }

    private String getSingleColumnValue(List call, String column) {

        VariantPropertyType type = getColumnType(column);
        Object value = call.get(indexMap.get(column));

        switch (type) {
            case NUMERIC:
            case STRING:
                return value.toString();
            case NUMERIC_COLLECTION:
            case STRING_COLLECTION:
                List values = (List) value;

                if (values.size() != 1) {
                    throw new IllegalStateException("multiple chromosomes found");
                }

                return values.get(0).toString();
            default:
                throw new IllegalStateException("chr were no strings");
        }
    }

    private void checkDataIntegrity() {

        if (Arrays.stream(MANDATORY_FIELDS).anyMatch((mandatory) -> !columnNames.contains(mandatory))) {
            throw new IllegalArgumentException("Data must contain all mandatory columns");
        }

        if (types.size() != columnNames.size()) {
            throw new IllegalArgumentException("Type and column names differ in length");
        }

        if (this.rows.stream().anyMatch(call -> call.size() != types.size())) {
            throw new IllegalArgumentException("number of columns in calls differ from column names / types");
        }

        indexMap.forEach((key, value) -> {

            this.rows.stream()
                    .map((row) -> row.get(value))
                    .forEach((property) -> checkCorrectType(property, types.get(value)));

        });

        // TODO: is it possible to do this a nicer way?
        this.rows.forEach((call) -> {

            String chr1 = getSingleColumnValue(call, CHR1_COLUMN_NAME);
            String chr2 = getSingleColumnValue(call, CHR2_COLUMN_NAME);

            Double bp1 = (Double) call.get(indexMap.get(VariantTable.BP1_COLUMN_NAME));
            Double bp2 = (Double) call.get(indexMap.get(VariantTable.BP2_COLUMN_NAME));

            if (bp1 > bp2) {
                call.set(indexMap.get(CHR1_COLUMN_NAME), chr2);
                call.set(indexMap.get(CHR2_COLUMN_NAME), chr1);

                call.set(indexMap.get(BP1_COLUMN_NAME), bp2);
                call.set(indexMap.get(BP2_COLUMN_NAME), bp1);
            }
        });
    }

    public int[] getSoftFilter() {
        return this.softFilter;
    }

    public static class ColumnProperty {

        private final String name;
        private final VariantPropertyType type;

        public ColumnProperty(String name, VariantPropertyType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public VariantPropertyType getType() {
            return type;
        }

    }
}
