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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private boolean areIntervalsClose(Interval i1, Interval i2) {
        return Math.abs(i1.getStart() - i2.getStart()) <= tolerance
                && Math.abs(i1.getEnd() - i2.getEnd()) <= tolerance;
    }

    //TODO: this is slow currently, maybe revisit this so users don't need
    //to wait too long
    public List<Collection<Integer>> clusterIntervals(List<Interval> intervals) {

        List<Collection<Integer>> clusters = new ArrayList<>();

        List<Integer> indicesLeft = IntStream.range(0, intervals.size())
                .boxed()
                .collect(Collectors.toList());

        while (!indicesLeft.isEmpty()) {

            int nextElement = indicesLeft.remove(0);

            Collection<Integer> cluster = new ArrayList<>();

            Collection<Integer> inspectedElements = new ArrayList<>();
            inspectedElements.add(nextElement);

            while (!inspectedElements.isEmpty()) {

                Collection<Integer> similarIntervalIndices = inspectedElements.stream()
                        .map((index) -> {
                            Interval source = intervals.get(index);

                            Collection<Integer> similar = indicesLeft.stream()
                                    .filter((otherIndex) -> areIntervalsClose(source, intervals.get(otherIndex)))
                                    .collect(Collectors.toList());

                            return similar;
                        })
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

                cluster.addAll(inspectedElements);
                inspectedElements.clear();
                inspectedElements.addAll(similarIntervalIndices);

                indicesLeft.removeAll(similarIntervalIndices);
            }

            clusters.add(cluster);
        }

        return clusters;
    }

}
