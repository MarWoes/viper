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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    private void addColumnNames(Sheet sheet, VariantTableCluster cluster) {

        List<String> columnNames = cluster.getClusteredTable().getColumnNames();

        Row columnNameRow = sheet.createRow(0);

        for (int i = 0; i < columnNames.size(); i++) {

            columnNameRow.createCell(i);
            columnNameRow.getCell(i).setCellValue(columnNames.get(i));

        }
    }

    public void writeAllToXSLX(VariantTableCluster cluster, String fileName) {

        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("VIPER variants");

        addColumnNames(sheet, cluster);

        // start at 1 since column names are taking the first row
        int currentRowIndex = 1;

        for (int i = 0; i < cluster.getClusteredTable().getUnfilteredRawCalls().size(); i++) {

            List<Object> call = cluster.getClusteredTable().getUnfilteredRawCalls().get(i);
            List<String> strings = stringifier.convertVariantCallsToString(call, cluster.getClusteredTable().getTypes());

            Row row = sheet.createRow(currentRowIndex);

            for (int j = 0; j < strings.size(); j++) {

                Cell cell = row.createCell(j);
                cell.setCellValue(strings.get(j));

            }

            currentRowIndex++;
        }

        try {
            wb.write(new FileOutputStream(fileName));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(XLSXWriter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XLSXWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
