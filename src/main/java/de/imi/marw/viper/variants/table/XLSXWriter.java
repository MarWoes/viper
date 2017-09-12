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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 *
 * @author marius
 */
public class XLSXWriter {

    private final CallStringifier stringifier;
    private final int windowSize;

    public XLSXWriter(String collectionDelimiter, int windowSize) {
        this.stringifier = new CallStringifier(collectionDelimiter);
        this.windowSize = windowSize;
    }

    private void addStringRow(Sheet sheet, int rowIndex, List<String> values, int cellOffset) {

        Row columnNameRow = sheet.createRow(rowIndex);

        for (int i = 0; i < values.size(); i++) {

            columnNameRow.createCell(i + cellOffset);
            columnNameRow.getCell(i + cellOffset).setCellValue(values.get(i));

        }
    }

    private int addRelatedCalls(VariantTableCluster cluster, int relatedCallIndex, int currentRowIndex, Sheet sheet) {

        int[] relatedCallIndices = cluster.getRowMapCluster().get(relatedCallIndex).stream().mapToInt(i -> i).toArray();

        if (relatedCallIndices.length == 1) {
            return 0;
        }

        List<List<String>> relatedCallStrings = stringifier.callsToStringLists(cluster.getUnclusteredTable(), relatedCallIndices);
        relatedCallStrings.remove(0);

        for (int i = 0; i < relatedCallStrings.size(); i++) {

            List<String> row = relatedCallStrings.get(i);
            addStringRow(sheet, currentRowIndex + i, row, 2);
        }

        sheet.groupRow(currentRowIndex, currentRowIndex + relatedCallIndices.length);
        sheet.setRowGroupCollapsed(currentRowIndex, true);

        return relatedCallIndices.length;

    }

    private void writeToXSLX(VariantTableCluster cluster, int[] indices, String fileName) {
        VariantTable clustered = cluster.getClusteredTable();

        List<List<String>> callStrings = stringifier.callsToStringLists(clustered, indices);
        callStrings.remove(0);

        SXSSFWorkbook wb = new SXSSFWorkbook(windowSize);

        Sheet sheet = wb.createSheet("VIPER variants");

        addStringRow(sheet, 0, cluster.getClusteredTable().getColumnNames(), 0);

        // start at 1 since column names are taking the first row
        int currentRowIndex = 1;

        for (int i = 0; i < callStrings.size(); i++) {

            List<String> strings = callStrings.get(i);

            addStringRow(sheet, currentRowIndex, strings, 0);

            currentRowIndex++;

            currentRowIndex += addRelatedCalls(cluster, indices[i], currentRowIndex, sheet);
        }

        try {
            wb.write(new FileOutputStream(fileName));
            wb.dispose();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(XLSXWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XLSXWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeAllToXSLX(VariantTableCluster cluster, String fileName) {

        int[] indices = IntStream.range(0, cluster.getClusteredTable().getRawCalls().size()).toArray();

        writeToXSLX(cluster, indices, fileName);

    }

    public void writeFilteredToXSLX(VariantTableCluster cluster, String fileName) {

        int[] indices = cluster.getClusteredTable().getSoftFilter();

        writeToXSLX(cluster, indices, fileName);

    }
}
