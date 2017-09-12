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
