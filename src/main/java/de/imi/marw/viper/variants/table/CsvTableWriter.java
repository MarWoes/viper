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

import de.imi.marw.viper.variants.VariantPropertyType;
import de.imi.marw.viper.variants.VariantTableCluster;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

/**
 *
 * @author marius
 */
public class CsvTableWriter {

    private final String collectionDelimiter;
    private final CSVFormat csvFormat;

    public CsvTableWriter(char csvDelimiter, String collectionDelimiter) {
        this.csvFormat = CSVFormat.RFC4180
                .withDelimiter(csvDelimiter)
                .withQuoteMode(QuoteMode.MINIMAL);
        this.collectionDelimiter = collectionDelimiter;
    }

    private void writeToCSV(List<List<String>> data, String fileName) {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), csvFormat)) {

            List<String[]> rawData = data.stream()
                    .map(stringList -> stringList.toArray(new String[stringList.size()]))
                    .collect(Collectors.toList());

            for (String[] rowValues : rawData) {
                printer.printRecord((Object[]) rowValues);
            }

        } catch (IOException ex) {
            Logger.getLogger(CsvTableWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeAllToCSV(VariantTableCluster cluster, String fileName) {

        List<List<String>> values = new ArrayList<>();
        List<List<Object>> rawCalls = cluster.getClusteredTable().getRawCalls();
        List<VariantPropertyType> types = cluster.getClusteredTable().getTypes();

        List<String> columnNames = new ArrayList<>(cluster.getClusteredTable().getColumnNames());
        columnNames.add("viperIndices");
        values.add(columnNames);

        for (int i = 0; i < rawCalls.size(); i++) {

            List<Object> call = rawCalls.get(i);
            List<String> callStrings = new ArrayList<>(types.size());

            for (int j = 0; j < types.size(); j++) {

                switch (types.get(j)) {
                    case STRING:
                    case NUMERIC:
                        callStrings.add("" + (call.get(j) == null ? "NA" : call.get(j).toString()));
                        break;
                    case NUMERIC_COLLECTION:
                    case STRING_COLLECTION:
                        String joinedValues = ((Collection) call.get(j)).stream()
                                .map(property -> property == null ? "NA" : property.toString())
                                .collect(Collectors.joining(collectionDelimiter + " ")).toString();
                        callStrings.add(joinedValues);
                }

            }

            String joinedIndices = cluster.getRelatedIndices(i).stream()
                    .map(relatedIndex -> relatedIndex.toString())
                    .collect(Collectors.joining(collectionDelimiter + " "));

            callStrings.add(joinedIndices);

            values.add(callStrings);

        }

        writeToCSV(values, fileName);

    }

}
