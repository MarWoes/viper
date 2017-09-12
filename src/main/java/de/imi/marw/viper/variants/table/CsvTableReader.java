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
package de.imi.marw.viper.variants.table;

import de.imi.marw.viper.util.Util;
import de.imi.marw.viper.variants.VariantPropertyType;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author marius
 */
public class CsvTableReader implements TableReader {

    private final String propertyDelimiter;
    private final CSVFormat csvFormat;

    public CsvTableReader(char csvDelimiter, String propertyDelimiter) {
        this.propertyDelimiter = propertyDelimiter;
        this.csvFormat = CSVFormat.RFC4180
                .withDelimiter(csvDelimiter);
    }

    private VariantPropertyType determineType(List<String> columnValues) {

        String doubleRegex = "(NA|(" + Util.FP_REGEX + "))";

        boolean isNumericColumn = columnValues.stream()
                .allMatch((str) -> str.matches(doubleRegex));

        if (isNumericColumn) {
            return VariantPropertyType.NUMERIC;
        }

        String numericCollectionRegex = "^$|" + doubleRegex + "(" + this.propertyDelimiter + "\\s*" + doubleRegex + ")*";

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

    private VariantPropertyType[] guessTypes(List<String[]> stringTable) {

        VariantPropertyType[] types = IntStream.range(0, stringTable.get(0).length).boxed()
                .map((i) -> {

                    List<String> singlePropertyValues = stringTable.stream()
                            .map((rowValues) -> rowValues[i])
                            .collect(Collectors.toList());

                    return singlePropertyValues;
                })
                .map(this::determineType)
                .toArray(VariantPropertyType[]::new);

        return types;
    }

    private VariantPropertyType[] determineTypes(String[] columnNames, List<String[]> stringTable) {

        VariantPropertyType[] guessedTypes;

        if (stringTable.isEmpty()) {

            guessedTypes = new VariantPropertyType[columnNames.length];

            for (int i = 0; i < columnNames.length; i++) {
                guessedTypes[i] = VariantPropertyType.NUMERIC;
            }
        } else {
            guessedTypes = guessTypes(stringTable);
        }

        List<String> mandatoryFields = Arrays.asList(VariantTable.MANDATORY_FIELDS);

        for (int i = 0; i < columnNames.length; i++) {

            String columnName = columnNames[i];
            int foundIndex = mandatoryFields.indexOf(columnName);

            if (foundIndex != -1) {
                guessedTypes[i] = VariantTable.MANDATORY_FIELDS_TYPES[foundIndex];
            }
        }

        return guessedTypes;
    }

    private Double parseDouble(String str) {
        if (str.equals("NA") || str.isEmpty() || str.equals("NaN")) {
            return null;
        } else {
            return Double.parseDouble(str);
        }
    }

    private Object parseProperty(VariantPropertyType type, String rawValue) {
        switch (type) {
            case NUMERIC:
                return parseDouble(rawValue);
            case STRING:
                return rawValue.isEmpty() ? "NA" : rawValue;
            case NUMERIC_COLLECTION:

                Collection<Double> numbers = Arrays.stream(rawValue.split(this.propertyDelimiter))
                        .map(this::parseDouble)
                        .collect(Collectors.toList());

                return (numbers);
            case STRING_COLLECTION:

                Collection<String> strings = Arrays.stream(rawValue.split(this.propertyDelimiter))
                        .map(str -> str.isEmpty() ? "NA" : str.trim())
                        .collect(Collectors.toList());

                return strings;

            default:
                throw new IllegalStateException("Unexpected type " + type + " when parsing " + rawValue);
        }
    }

    private List<Object> parseVariantCall(String[] colNames, String[] colValues, VariantPropertyType[] types) {
        Object[] row = new Object[colNames.length];

        for (int i = 0; i < colNames.length; i++) {
            Object propertyValue = parseProperty(types[i], colValues[i]);

            row[i] = propertyValue;
        }

        return Arrays.asList(row);
    }

    private List<List<Object>> parseVariantCalls(String[] colNames, List<String[]> colValues, VariantPropertyType[] types) {

        List<List<Object>> calls = colValues.stream()
                .map((rowValues) -> parseVariantCall(colNames, rowValues, types))
                .collect(Collectors.toList());

        return calls;
    }

    @Override
    public VariantTable readTable(String fileName) {
        try (Reader reader = new FileReader(fileName)) {

            List<String[]> rawStrings = new ArrayList<>();

            Iterable<CSVRecord> records = this.csvFormat.parse(reader);

            for (CSVRecord record : records) {

                String[] rawData = new String[record.size()];

                for (int i = 0; i < rawData.length; i++) {
                    rawData[i] = record.get(i);
                }

                rawStrings.add(rawData);
            }

            String[] header = rawStrings.remove(0);

            VariantPropertyType[] types = determineTypes(header, rawStrings);

            List<List<Object>> parsedCalls = parseVariantCalls(header, rawStrings, types);

            return new VariantTable(parsedCalls, Arrays.asList(header), Arrays.asList(types));

        } catch (IOException ex) {
            Logger.getLogger(CsvTableReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

}
