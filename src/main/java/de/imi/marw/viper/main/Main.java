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

import de.imi.marw.viper.clustering.Interval;
import de.imi.marw.viper.clustering.IntervalClusterBuilder;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        ViperServerConfig config = new ViperServerConfig("../results/all_analysis.csv");
//        ViperServer server = new ViperServer(config);
//        server.start();
        VariantTable tb = new CsvTableReader("../results-france1/all_analysis.csv", ';', ",").readTable();
        List<Integer> bp1 = IntStream.range(0, tb.getNumberOfCalls())
                .boxed()
                .map((index) -> ((Double) tb.getCall(index).getProperty("bp1").getValue()).intValue())
                .collect(Collectors.toList());

        List<Integer> bp2 = IntStream.range(0, tb.getNumberOfCalls())
                .boxed()
                .map((index) -> ((Double) tb.getCall(index).getProperty("bp2").getValue()).intValue())
                .collect(Collectors.toList());

        List<Interval> intervals = IntStream.range(0, tb.getNumberOfCalls())
                .boxed()
                .map((index) -> new Interval(bp1.get(index), bp2.get(index)))
                .collect(Collectors.toList());

        long start = System.currentTimeMillis();
        System.out.println(new IntervalClusterBuilder().clusterIntervals(intervals).size());
        System.out.println(System.currentTimeMillis() - start);
    }
}
