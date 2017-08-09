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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author marius
 */
public class IntervalClusterBuilder {

    private int tolerance = 3;

    public IntervalClusterBuilder() {
    }

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
                point.getX() - tolerance,
                point.getY() - tolerance,
                point.getX() + tolerance,
                point.getY() + tolerance
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
            clusters.get(clusterIndices[otherClusterIndex]).add(point.getIndex());
            clusterIndices[point.getIndex()] = clusterIndices[otherClusterIndex];

        } else {

            Set<Integer> newCluster = new HashSet<>();

            clusterIndices[point.getIndex()] = clusters.size();
            for (IndexContainingPoint pointInRange : pointsInRange) {
                clusterIndices[pointInRange.getIndex()] = clusters.size();
            }

            newCluster.add(point.getIndex());
            for (int otherCluster : otherClusterIndices) {

                newCluster.addAll(clusters.get(otherCluster));
                clusters.get(otherCluster).clear();

            }

            clusters.add(newCluster);
        }

    }

    public List<Collection<Integer>> clusterIntervals(List<Interval> intervals) {

        List<Collection<Integer>> clusters = new ArrayList<>();
        int[] clusterIndices = IntStream.range(0, intervals.size())
                .map((i) -> -1)
                .toArray();

        IndexContainingPoint[] points = IntStream.range(0, intervals.size())
                .boxed()
                .map((index) -> new IndexContainingPoint(intervals.get(index).getStart(), intervals.get(index).getEnd(), index))
                .toArray(IndexContainingPoint[]::new);

        TwoDTree tree = new TwoDTree();

        for (IndexContainingPoint point : points) {

            List<IndexContainingPoint> pointsInRange = findPointsInRange(point, tree);

            updateClusters(point, pointsInRange, clusterIndices, clusters);

            tree.insert(point);
        }

        clusters = clusters.stream()
                .filter((cluster) -> !cluster.isEmpty())
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

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + this.index;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IndexContainingPoint other = (IndexContainingPoint) obj;
            return this.index == other.index;
        }

    }
}
