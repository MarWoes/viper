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
