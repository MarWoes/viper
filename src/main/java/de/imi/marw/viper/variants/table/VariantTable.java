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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private static final String[] MANDATORY_FIELDS = {
        TYPE_COLUMN_NAME,
        SAMPLE_COLUMN_NAME,
        CHR1_COLUMN_NAME,
        CHR2_COLUMN_NAME,
        BP1_COLUMN_NAME,
        BP2_COLUMN_NAME
    };

    private final List<List> rows;
    private final List<String> columnNames;
    private final List<VariantPropertyType> types;
    private final Map<String, Integer> indexMap;

    public VariantTable(Collection<List> calls, List<String> columnNames, List<VariantPropertyType> types) {
        this.indexMap = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            indexMap.put(columnNames.get(i), i);
        }
        this.rows = new ArrayList<>();
        this.columnNames = columnNames;
        this.rows.addAll(calls);
        this.types = types;
    }

    public synchronized VariantTable filter(Collection<VariantCallFilter> filters) {

        Collection<List> callsAfterFiltering = this.rows.stream()
                .filter((call) -> filters.stream().allMatch((filter) -> filter.isPassing(call, indexMap)))
                .collect(Collectors.toList());

        return new VariantTable(callsAfterFiltering, columnNames, this.types);
    }

    public synchronized Map<String, Object> getCall(int rowIndex) {

        Map<String, Object> variantCall = new LinkedHashMap<>(columnNames.size());
        List rawRow = this.rows.get(rowIndex);

        for (int i = 0; i < columnNames.size(); i++) {
            variantCall.put(this.columnNames.get(i), rawRow.get(i));
        }

        return variantCall;
    }

    public synchronized List<List> getRawCalls() {
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
        return this.rows.size();
    }

    public synchronized List<String> getColumnNames() {
        return columnNames;
    }

    public synchronized Map<String, Integer> getColumnIndexMap() {
        return indexMap;
    }

    public synchronized List<VariantPropertyType> getTypes() {
        return types;
    }
//    public void setValue(Object newValue) {
//        switch (type) {
//            case STRING:
//                checkCorrectType(newValue, String.class);
//                break;
//            case STRING_COLLECTION:
//                checkCorrectCollectionType(newValue, String.class);
//                break;
//            case NUMERIC:
//                checkCorrectType(newValue, Double.class);
//                break;
//            case NUMERIC_COLLECTION:
//                checkCorrectCollectionType(newValue, Double.class);
//                break;
//            default:
//                throw new IllegalStateException("Unexpected type " + type + "when setting variant value");
//        }
//
//        this.propertyValue = newValue;
//    }
//    private void checkCorrectCollectionType(Object value, Class someClass) {
//        if (value instanceof Collection) {
//            ((Collection) value).stream().forEach((o) -> checkCorrectType(o, someClass));
//        } else {
//            throw new IllegalPropertyValueException(type, value, Collection.class);
//        }
//    }
//
//    private void checkCorrectType(Object value, Class someClass) {
//        if (value != null && !someClass.isInstance(value)) {
//            throw new IllegalPropertyValueException(type, value, someClass);
//        }
//    }
}
