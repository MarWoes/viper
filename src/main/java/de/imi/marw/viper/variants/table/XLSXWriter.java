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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author marius
 */
public class XLSXWriter {

    private final CallStringifier stringifier;

    public XLSXWriter(String collectionDelimiter) {
        this.stringifier = new CallStringifier(collectionDelimiter);
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

        SXSSFWorkbook wb = new SXSSFWorkbook(1000);

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
