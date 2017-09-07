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
package de.imi.marw.viper.test.variants;

import de.imi.marw.viper.test.util.TestUtil;
import de.imi.marw.viper.variants.VariantClusterBuilder;
import de.imi.marw.viper.variants.VariantTableCluster;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class VariantTableClusterTest {

    private <T> Collection<T> coll(T... vals) {
        return Arrays.asList(vals);
    }

    @Test
    public void variantsAreCorrectlyClustered() {

        CsvTableReader reader = new CsvTableReader(';', ",");

        VariantTable unclustered = reader.readTable(TestUtil.getResourceFile("examples-unclustered.csv"));

        VariantClusterBuilder builder = new VariantClusterBuilder(5, false);

        VariantTableCluster cluster = builder.clusterVariantTable(unclustered);

        List<Collection<Integer>> expectedClusterIndices = Arrays.asList(
                coll(7, 5, 0),
                coll(1),
                coll(2),
                coll(3, 6),
                coll(4)
        );

        List<List<Object>> expectedCalls = Arrays.asList(
                Arrays.asList("VAR1",
                        "NA",
                        coll("SAMPLE5", "SAMPLE3", "SAMPLE4"),
                        coll("DUPLICATION"),
                        coll("2"),
                        100000005.0,
                        coll("2"),
                        100000105.0,
                        coll("GEN3"),
                        null),
                Arrays.asList("VAR2",
                        "NA",
                        coll("SAMPLE1"),
                        coll("DELETION"),
                        coll("X"),
                        123000000.0,
                        coll("X"),
                        123000010.0,
                        coll("NA"),
                        null),
                Arrays.asList("VAR3",
                        "NA",
                        coll("SAMPLE2"),
                        coll("TRANSLOCATION"),
                        coll("1"),
                        19000000.0,
                        coll("2"),
                        20000000.0,
                        coll("NA"),
                        null
                ),
                Arrays.asList("VAR4",
                        "NA",
                        coll("SAMPLE1", "SAMPLE2"),
                        coll("DELETION"),
                        coll("12"),
                        123000000.0,
                        coll("12"),
                        123000010.0,
                        coll("GEN1", "GEN2"),
                        null),
                Arrays.asList("VAR5",
                        "NA",
                        coll("SAMPLE2"),
                        coll("INVERSION"),
                        coll("12"),
                        123000000.0,
                        coll("12"),
                        123000010.0,
                        coll("GEN1", "GEN2"),
                        null)
        );

        assertEquals(expectedCalls.size(), cluster.getClusteredTable().getNumberOfCalls());
        assertEquals(expectedClusterIndices, cluster.getRowMapCluster());
        assertEquals(unclustered, cluster.getUnclusteredTable());

        for (int i = 0; i < expectedCalls.size(); i++) {

            List<Map<String, Object>> relatedMaps = new ArrayList<>();

            for (int clusterIndex : expectedClusterIndices.get(i)) {
                relatedMaps.add(cluster.getUnclusteredTable().getCall(clusterIndex));
            }

            assertEquals(relatedMaps, cluster.getUnclusteredCalls(i));
            assertEquals(expectedCalls.get(i), cluster.getClusteredTable().getRawCalls().get(i));
            assertEquals(expectedClusterIndices.get(i), cluster.getRelatedIndices(i));
        }
    }

}
