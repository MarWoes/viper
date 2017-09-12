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
package de.imi.marw.viper.clustering;

import algs.model.IPoint;
import algs.model.IRectangle;
import algs.model.kdtree.TwoDTree;
import algs.model.twod.TwoDPoint;
import algs.model.twod.TwoDRectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author marius
 */
public class IntervalClusterBuilder {

    private int tolerance;

    public IntervalClusterBuilder(int tolerance) {
        this.tolerance = tolerance;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    private List<IndexContainingPoint> findPointsInRange(IndexContainingPoint point, TwoDTree tree) {

        IRectangle searchArea = new TwoDRectangle(
                point.getX() - tolerance - 0.5,
                point.getY() - tolerance - 0.5,
                point.getX() + tolerance + 0.5,
                point.getY() + tolerance + 0.5
        );

        Iterator<IPoint> pointsInRangeIterator = tree.range(searchArea);

        List<IndexContainingPoint> pointsInRange = new ArrayList<>();
        pointsInRangeIterator.forEachRemaining((pointInRange) -> pointsInRange.add((IndexContainingPoint) pointInRange));

        return pointsInRange;
    }

    private void updateClusters(IndexContainingPoint point, List<IndexContainingPoint> pointsInRange, int[] clusterIndices, List<Collection<Integer>> clusters) {

        int[] otherClusterIndices = pointsInRange.stream()
                .mapToInt((pointInRange) -> clusterIndices[pointInRange.getIndex()])
                .distinct()
                .toArray();

        if (otherClusterIndices.length == 1) {

            int otherClusterIndex = otherClusterIndices[0];
            clusters.get(otherClusterIndex).add(point.getIndex());
            clusterIndices[point.getIndex()] = otherClusterIndex;

        } else {

            Set<Integer> newCluster = new HashSet<>();

            newCluster.add(point.getIndex());
            for (int otherCluster : otherClusterIndices) {

                newCluster.addAll(clusters.get(otherCluster));
                clusters.get(otherCluster).clear();
            }

            for (int pointInRangeIndex : newCluster) {
                clusterIndices[pointInRangeIndex] = clusters.size();
            }

            clusters.add(newCluster);
        }

    }

    public List<Collection<Integer>> clusterIntervals(List<Interval> intervals) {

        List<Collection<Integer>> clusters = new ArrayList<>();

        Map<Interval, List<Integer>> deduplicatedIntervalMap = IntStream.range(0, intervals.size())
                .boxed()
                .collect(Collectors.groupingBy(i -> intervals.get(i)));

        List<Interval> deduplicatedIntervals = new ArrayList<>();
        deduplicatedIntervals.addAll(deduplicatedIntervalMap.keySet());

        int[] clusterIndices = IntStream.range(0, deduplicatedIntervals.size())
                .map((i) -> -1)
                .toArray();

        IndexContainingPoint[] points = IntStream.range(0, deduplicatedIntervals.size())
                .boxed()
                .map((index) -> new IndexContainingPoint(deduplicatedIntervals.get(index).getStart(), deduplicatedIntervals.get(index).getEnd(), index))
                .toArray(IndexContainingPoint[]::new);

        TwoDTree tree = new TwoDTree();

        for (IndexContainingPoint point : points) {

            List<IndexContainingPoint> pointsInRange = findPointsInRange(point, tree);

            updateClusters(point, pointsInRange, clusterIndices, clusters);

            tree.insert(point);
        }

        clusters = clusters.stream()
                .filter((cluster) -> !cluster.isEmpty())
                .map(cluster -> {
                    return cluster.stream()
                            .flatMap(index -> deduplicatedIntervalMap.get(deduplicatedIntervals.get(index)).stream())
                            .collect(Collectors.toList());
                })
                .collect(Collectors.toList());

        return clusters;
    }

    private static class IndexContainingPoint extends TwoDPoint {

        private final int index;

        public IndexContainingPoint(double x, double y, int intervalIndex) {
            super(x, y);
            this.index = intervalIndex;
        }

        public int getIndex() {
            return index;
        }
    }
}
