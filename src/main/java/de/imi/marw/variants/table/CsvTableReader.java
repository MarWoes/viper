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
package de.imi.marw.variants.table;

import au.com.bytecode.opencsv.CSVReader;
import de.imi.marw.util.Util;
import de.imi.marw.variants.VariantCall;
import de.imi.marw.variants.VariantProperty;
import de.imi.marw.variants.VariantPropertyType;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author marius
 */
public class CsvTableReader implements TableReader {

    private final String fileName;
    private final char csvDelimiter;
    private final String propertyDelimiter;

    public CsvTableReader(String fileName, char csvDelimiter, String propertyDelimiter) {
        this.fileName = fileName;
        this.csvDelimiter = csvDelimiter;
        this.propertyDelimiter = propertyDelimiter;
    }

    private VariantPropertyType determineType(List<String> columnValues) {

        boolean isNumericColumn = columnValues.stream()
                .allMatch((str) -> str.matches(Util.FP_REGEX));

        if (isNumericColumn) {
            return VariantPropertyType.NUMERIC;
        }

        String numericCollectionRegex = "(" + Util.FP_REGEX + ")" + this.propertyDelimiter + "(" + Util.FP_REGEX + ")*";

        boolean isNumericCollectionColumn = columnValues.stream()
                .allMatch((str) -> str.matches(numericCollectionRegex));

        if (isNumericCollectionColumn) {
            return VariantPropertyType.NUMERIC_COLLECTION;
        }

        boolean isStringCollection = columnValues.stream()
                .anyMatch((str) -> str.contains(this.propertyDelimiter));

        if (isStringCollection) {
            return VariantPropertyType.STRING_COLLECTION;
        }

        return VariantPropertyType.STRING;
    }

    private VariantPropertyType[] determineTypes(List<String[]> stringTable) {

        VariantPropertyType[] types = IntStream.range(0, stringTable.size()).boxed()
                .map((i) -> {

                    List<String> singlePropertyValues = stringTable.stream()
                            .map((rowValues) -> i >= rowValues.length ? "" : rowValues[i])
                            .collect(Collectors.toList());

                    return singlePropertyValues;
                })
                .map(this::determineType)
                .toArray(VariantPropertyType[]::new);

        return types;
    }

    private Object parseProperty(VariantPropertyType type, String rawValue) {
        switch (type) {
            case NUMERIC:
                return (Double.parseDouble(rawValue));
            case STRING:
                return rawValue;
            case NUMERIC_COLLECTION:

                Collection<Double> numbers = Arrays.stream(rawValue.split(this.propertyDelimiter))
                        .map(Double::parseDouble)
                        .collect(Collectors.toList());

                return (numbers);
            case STRING_COLLECTION:

                Collection<String> strings = Arrays.asList(rawValue.split(this.propertyDelimiter));
                return strings;

            default:
                throw new IllegalStateException("Unexpected type " + type + " when parsing " + rawValue);
        }
    }

    private VariantCall parseVariantCall(String[] colNames, String[] colValues, VariantPropertyType[] types) {
        Map<String, VariantProperty> propertyMap = new HashMap<>();

        for (int i = 0; i < colNames.length; i++) {
            Object propertyValue = parseProperty(types[i], colValues[i]);

            propertyMap.put(colNames[i], new VariantProperty(types[i], propertyValue));
        }

        return new VariantCall(propertyMap);
    }

    private List<VariantCall> parseVariantCalls(String[] colNames, List<String[]> colValues) {
        VariantPropertyType[] types = determineTypes(colValues);

        List<VariantCall> calls = colValues.stream()
                .map((rowValues) -> parseVariantCall(colNames, rowValues, types))
                .collect(Collectors.toList());

        return calls;
    }

    @Override
    public VariantTable readTable() {
        try (CSVReader reader = new CSVReader(new FileReader(this.fileName), this.csvDelimiter)) {

            String[] header = reader.readNext();
            List<String[]> rawData = reader.readAll();

            List<VariantCall> parsedCalls = parseVariantCalls(header, rawData);

            return new VariantTable(parsedCalls);

        } catch (FileNotFoundException ex) {
            System.out.println("[ERROR] File " + this.fileName + " not found!");
        } catch (IOException ex) {
            System.out.println("[ERROR] IOException when reading csv file:");
            System.err.print(ex);
        }

        return null;
    }

}
