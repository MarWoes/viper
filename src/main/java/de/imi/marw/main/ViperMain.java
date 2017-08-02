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

package de.imi.marw.main;

import de.imi.marw.variants.table.CsvTableReader;
import de.imi.marw.variants.table.VariantTable;
import java.io.IOException;


public class ViperMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        CsvTableReader rd = new CsvTableReader("/home/marius/workspace/sftp/results-france1/all_analysis.csv", ';', ",");
        System.in.read();
        VariantTable tab = rd.readTable();
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(tab.getCall(0));


//        port(8090);
//
//        staticFiles.externalLocation("public");
//
//        init();
    }
}
