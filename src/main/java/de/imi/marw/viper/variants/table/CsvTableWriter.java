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

import de.imi.marw.viper.variants.VariantTableCluster;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

/**
 *
 * @author marius
 */
public class CsvTableWriter {

    private final CSVFormat csvFormat;
    private final CallStringifier stringifier;

    public CsvTableWriter(char csvDelimiter, String collectionDelimiter) {
        this.csvFormat = CSVFormat.RFC4180
                .withDelimiter(csvDelimiter)
                .withQuoteMode(QuoteMode.MINIMAL);
        this.stringifier = new CallStringifier(collectionDelimiter);
    }

    private void writeStringsToCsv(VariantTableCluster cluster, int[] indices, String fileName) {

        VariantTable clustered = cluster.getClusteredTable();

        List<List<String>> stringValues = stringifier.callsToStringLists(clustered, indices);

        try (CSVPrinter printer = new CSVPrinter(new FileWriter(fileName), csvFormat)) {

            List<String[]> rawData = stringValues.stream()
                    .map(stringList -> stringList.toArray(new String[stringList.size()]))
                    .collect(Collectors.toList());

            for (String[] rowValues : rawData) {
                printer.printRecord((Object[]) rowValues);
            }

        } catch (IOException ex) {
            Logger.getLogger(CsvTableWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeFilteredToCSV(VariantTableCluster cluster, String fileName) {

        int[] indices = cluster.getClusteredTable().getSoftFilter();

        writeStringsToCsv(cluster, indices, fileName);

    }

    public void writeAllToCSV(VariantTableCluster cluster, String fileName) {

        int[] indices = IntStream.range(0, cluster.getClusteredTable().getRawCalls().size()).toArray();

        writeStringsToCsv(cluster, indices, fileName);

    }

}
