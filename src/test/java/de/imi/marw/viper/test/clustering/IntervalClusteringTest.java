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
package de.imi.marw.viper.test.clustering;

import de.imi.marw.viper.clustering.Interval;
import de.imi.marw.viper.clustering.IntervalClusterBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class IntervalClusteringTest {

    private int[] nums(int... vals) {
        return vals;
    }

    private List<Interval> asIntervals(int[] starts, int[] ends) {

        List<Interval> intervals = new ArrayList<>();

        for (int i = 0; i < starts.length; i++) {
            intervals.add(new Interval(starts[i], ends[i]));
        }

        return intervals;
    }

    private List<Collection<Integer>> asCluster(int[]... convenientClusters) {

        List<Collection<Integer>> clusters = new ArrayList<>();

        for (int[] convenientCluster : convenientClusters) {
            Collection<Integer> cluster = new HashSet<>();
            Arrays.stream(convenientCluster).forEach(i -> cluster.add(i));
            clusters.add(cluster);
        }

        return clusters;
    }

    private void checkClusterFunctionality(int[] starts, int[] ends, int tolerance, int[]... clusters) {

        IntervalClusterBuilder clusterer = new IntervalClusterBuilder(tolerance);

        List<Interval> intervals = asIntervals(starts, ends);

        List<Collection<Integer>> expectedClusters = asCluster(clusters);

        List<Collection<Integer>> actualClusters = clusterer.clusterIntervals(intervals);

        assertEquals(expectedClusters.size(), actualClusters.size());

        for (int i = 0; i < actualClusters.size(); i++) {
            Collection<Integer> expectedCluster = expectedClusters.get(i);
            Collection<Integer> actualCluster = actualClusters.get(i);
            assertTrue("clusters are equal", expectedCluster.containsAll(actualCluster) && actualCluster.containsAll(expectedCluster));
        }
    }

    @Test
    public void sameValuesYieldSingleCluster() {

        checkClusterFunctionality(
                nums(0, 0, 0, 0, 0),
                nums(0, 0, 0, 0, 0),
                2,
                nums(0, 1, 2, 3, 4)
        );

    }

    @Test
    public void singleValueYieldsSingleCluster() {

        checkClusterFunctionality(
                nums(0),
                nums(0),
                0,
                nums(0)
        );

    }

    @Test
    public void closeValuesYieldSameClusters() {

        checkClusterFunctionality(
                nums(0, 1, 2),
                nums(2, 2, 9),
                1,
                nums(0, 1),
                nums(2)
        );

    }

    @Test
    public void closeValuesYieldDifferentClusters() {

        checkClusterFunctionality(
                nums(0, 2),
                nums(2, 4),
                1,
                nums(1),
                nums(0)
        );

    }

    @Test
    public void manyValuesYieldCorrectClusters() {
        checkClusterFunctionality(
                nums(0, 3, 20, 21, 5, 22, 19, 2, 5),
                nums(10, 11, 100, 200, 100, 102, 98, 11, 13),
                2,
                nums(4),
                nums(2, 5, 6),
                nums(3),
                nums(0,1,7,8)
        );
    }

}
