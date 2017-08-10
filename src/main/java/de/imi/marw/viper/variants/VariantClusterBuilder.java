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
package de.imi.marw.viper.variants;

import de.imi.marw.viper.clustering.Interval;
import de.imi.marw.viper.clustering.IntervalClusterBuilder;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author marius
 */
public class VariantClusterBuilder {

    private final IntervalClusterBuilder intervalClusterer = new IntervalClusterBuilder();

    private Collection<String> getChromosomeKeys(VariantTable table) {
        Collection<String> keys = table.getAllCalls().stream()
                .map((variantCall) -> {
                    String chr1 = variantCall.getProperty(variantCall.CHR1_COLUMN_NAME).getValue().toString();
                    String chr2 = variantCall.getProperty(variantCall.CHR2_COLUMN_NAME).getValue().toString();

                    return chr1 + "-" + chr2;
                })
                .distinct()
                .collect(Collectors.toList());

        return keys;
    }

    private List<Collection<Integer>> clusterTableByKey(VariantTable unclustered, String chromosomeKey) {
        List<Interval> matchingCalls = IntStream.range(0, unclustered.getNumberOfCalls())
                .boxed()
                .map((rowIndex) -> {

                    VariantCall variantCall = unclustered.getCall(rowIndex);
                    String chr1 = variantCall.getProperty(variantCall.CHR1_COLUMN_NAME).getValue().toString();
                    String chr2 = variantCall.getProperty(variantCall.CHR2_COLUMN_NAME).getValue().toString();

                    int bp1 = ((Double) variantCall.getProperty(variantCall.BP1_COLUMN_NAME).getValue()).intValue();
                    int bp2 = ((Double) variantCall.getProperty(variantCall.BP2_COLUMN_NAME).getValue()).intValue();

                    return new VariantInterval(chr1 + "-" + chr2, bp1, bp2, rowIndex);
                })
                .filter((variantInterval) -> variantInterval.getChromosomeKey().equals(chromosomeKey))
                .collect(Collectors.toList());

        List<Collection<Integer>> clusters = intervalClusterer.clusterIntervals(matchingCalls).stream()
                .map((cluster) -> {

                    return cluster.stream()
                            .map((i) -> ((VariantInterval) matchingCalls.get(i)).getTableIndex())
                            .collect(Collectors.toList());
                })
                .collect(Collectors.toList());

        return clusters;
    }

    private List<Collection<Integer>> computeClusterIndices(VariantTable unclustered) {

        Collection<String> chromosomeKeys = getChromosomeKeys(unclustered);

        List<Collection<Integer>> clusters = new ArrayList<>();

        for (String chromosomeKey : chromosomeKeys) {
            List<Collection<Integer>> keyClusters = clusterTableByKey(unclustered, chromosomeKey);

            clusters.addAll(keyClusters);
        }

        return clusters;
    }

    private VariantProperty combineStrings(Collection<String> values) {
        return new VariantProperty(VariantPropertyType.STRING_COLLECTION, values);
    }

    private VariantProperty combineNumeric(Collection<Double> values) {
        double[] sortedValues = values.stream()
                .filter((d) -> d != null)
                .sorted()
                .mapToDouble(d -> d)
                .toArray();

        Double val = sortedValues.length == 0 ? null : sortedValues[sortedValues.length / 2];
        return new VariantProperty(VariantPropertyType.NUMERIC, val);
    }

    private <T> VariantProperty combineCollection(Collection<Collection<T>> values, VariantPropertyType type) {

        Collection<T> combinedCollection = values.stream()
                .flatMap((collection) -> collection.stream())
                .collect(Collectors.toList());

        return new VariantProperty(type, combinedCollection);
    }

    private VariantProperty combineProperties(Collection<VariantProperty> properties, VariantPropertyType type) {

        Collection values = properties.stream()
                .map((property) -> property.getValue())
                .collect(Collectors.toList());

        switch (type) {
            case STRING:
                return combineStrings((Collection<String>) values);
            case STRING_COLLECTION:
                return combineCollection((Collection<Collection<String>>) values, type);
            case NUMERIC:
                return combineNumeric((Collection<Double>) values);
            case NUMERIC_COLLECTION:
                return combineCollection((Collection<Collection<Double>>) values, type);
            default:
                throw new IllegalStateException("Unrecognized variant type " + type + " when combining multiple variant properties.");
        }
    }

    private String getClusterName(int index, int numClusters) {
        int maxIndexLength = ("" + numClusters).length();

        return String.format("VAR%0" + maxIndexLength + "d", index);
    }

    private VariantCall combineCalls(Collection<VariantCall> callCluster, Collection<String> keys, String clusterName) {
        Map<String, VariantProperty> combinedProperties = new HashMap<>();

        combinedProperties.put("viperId", new VariantProperty(VariantPropertyType.STRING, clusterName));

        for (String key : keys) {

            VariantPropertyType type = callCluster.stream()
                    .findAny()
                    .get()
                    .getProperty(key)
                    .getType();

            Collection<VariantProperty> properties = callCluster.stream()
                    .map((call) -> call.getProperty(key))
                    .collect(Collectors.toList());

            VariantProperty combinedProperty = combineProperties(properties, type);
            combinedProperties.put(key, combinedProperty);
        }

        return new VariantCall(combinedProperties);
    }

    public VariantTableCluster clusterVariantTable(VariantTable unclustered) {

        List<Collection<Integer>> indexClusters = computeClusterIndices(unclustered);

        List<VariantCall> clusteredCalls = new ArrayList<>();

        for (int i = 0; i < indexClusters.size(); i++) {

            Collection<Integer> indexCluster = indexClusters.get(i);

            Collection<VariantCall> callCluster = indexCluster.stream()
                    .map((index) -> unclustered.getCall(index))
                    .collect(Collectors.toList());

            String clusterName = getClusterName(i, indexClusters.size());
            VariantCall combinedCall = combineCalls(callCluster, unclustered.getColumnNames(), clusterName);
            clusteredCalls.add(combinedCall);
        }

        List<String> clusteredColumnNames = new ArrayList<>();
        clusteredColumnNames.add("viperId");
        clusteredColumnNames.addAll(unclustered.getColumnNames());

        VariantTable clusteredTable = new VariantTable(clusteredCalls, clusteredColumnNames);
        return new VariantTableCluster(unclustered, clusteredTable, indexClusters);
    }

    private static final class VariantInterval extends Interval {

        private final int tableIndex;
        private final String chromosomeKey;

        public VariantInterval(String chromosomeKey, int start, int end, int tableIndex) {
            super(start, end);
            this.tableIndex = tableIndex;
            this.chromosomeKey = chromosomeKey;
        }

        public int getTableIndex() {
            return tableIndex;
        }

        public String getChromosomeKey() {
            return chromosomeKey;
        }

    }
}
