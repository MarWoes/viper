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
package de.imi.marw.viper.main;

import com.google.gson.Gson;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import java.io.IOException;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        CsvTableReader rd = new CsvTableReader("/home/marius/workspace/sftp/results/all_analysis.csv", ';', ",");
//        Collection<VariantCallFilter> filters = new ArrayList<>();
//        filters.add(new NumericFilter("bp1", 1000000, 3e8));
//        filters.add(new NumericFilter("bp2", 2000000, 3e8));
//        filters.add(new NumericCollectionFilter("similarToolRows", 1, 10000));
//
//        Set<String> s = new HashSet<>();
//        s.add("TET2");
//
//        Set<String> s2 = new HashSet<>();
//        s2.add("DELETION");
//        s2.add("DUPLICATION");
//        s2.add("INVERSION");
//
//        filters.add(new StringCollectionFilter("genes", s));
//        filters.add(new StringFilter("svType", s2));
//
        VariantTable tab = rd.readTable();
        long start = System.currentTimeMillis();
//        tab = tab.filter(filters);
//
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(tab.getCall(0));

//        port(8090);
//
//        staticFiles.externalLocation("public");
//
//        init();

int blub = 17;
        System.out.println(new Gson().toJson(tab.getCall(0)));
    }
}
