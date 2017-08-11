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

    private String getSequenceKey(List variantCall, Map<String, Integer> indexMap) {

        String chr1 = variantCall.get(indexMap.get(VariantTable.CHR1_COLUMN_NAME)).toString();
        String chr2 = variantCall.get(indexMap.get(VariantTable.CHR2_COLUMN_NAME)).toString();
        String type = variantCall.get(indexMap.get(VariantTable.TYPE_COLUMN_NAME)).toString();

        return chr1 + "-" + chr2 + "-" + type;
    }

    private Collection<String> getSequenceKeys(VariantTable table) {
        Collection<String> keys = table.getRawCalls().stream()
                .map((variantCall) -> getSequenceKey(variantCall, table.getColumnIndexMap()))
                .distinct()
                .collect(Collectors.toList());

        return keys;
    }

    private List<Collection<Integer>> clusterTableByKey(VariantTable unclustered, String chromosomeKey) {
        List<List> rawCalls = unclustered.getRawCalls();
        List<Interval> matchingCalls = IntStream.range(0, unclustered.getNumberOfCalls())
                .boxed()
                .map((rowIndex) -> {

                    List call = rawCalls.get(rowIndex);
                    Map<String, Integer> indexMap = unclustered.getColumnIndexMap();

                    String sequenceKey = getSequenceKey(call, indexMap);
                    int bp1 = ((Double) call.get(indexMap.get(VariantTable.BP1_COLUMN_NAME))).intValue();
                    int bp2 = ((Double) call.get(indexMap.get(VariantTable.BP2_COLUMN_NAME))).intValue();

                    return new VariantInterval(sequenceKey, bp1, bp2, rowIndex);
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

        Collection<String> chromosomeKeys = getSequenceKeys(unclustered);

        List<Collection<Integer>> clusters = new ArrayList<>();

        for (String chromosomeKey : chromosomeKeys) {
            List<Collection<Integer>> keyClusters = clusterTableByKey(unclustered, chromosomeKey);

            clusters.addAll(keyClusters);
        }

        return clusters;
    }

    private Object combineStrings(Collection<String> values) {
        return values.stream().collect(Collectors.toList());
    }

    private Object combineNumeric(Collection<Double> values) {
        double[] sortedValues = values.stream()
                .filter((d) -> d != null)
                .sorted()
                .mapToDouble(d -> d)
                .toArray();

        Double val = sortedValues.length == 0 ? null : sortedValues[sortedValues.length / 2];
        return val;
    }

    private <T> Object combineCollection(Collection<Collection<T>> values, VariantPropertyType type) {

        Collection<T> combinedCollection = values.stream()
                .flatMap((collection) -> collection.stream())
                .collect(Collectors.toList());

        return combinedCollection;
    }

    private Object combineProperties(Collection values, VariantPropertyType type) {

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

        return String.format("VAR%0" + maxIndexLength + "d", index + 1);
    }

    private List combineCalls(Collection<List> callCluster, Map<String, Integer> indexMap, List<VariantPropertyType> types) {

        Object[] combinedRow = new Object[indexMap.size()];

        indexMap.forEach((key, value) -> {

            Collection values = callCluster.stream()
                    .map((call) -> call.get(value))
                    .collect(Collectors.toList());

            combinedRow[value] = combineProperties(values, types.get(value));

        });

        List combinedCall = new ArrayList<>();
        for (Object object : combinedRow) {
            combinedCall.add(object);
        }

        return combinedCall;
    }

    public List<VariantPropertyType> getNewClusterTypes(List<VariantPropertyType> unclusteredTypes) {
        return unclusteredTypes.stream()
                .map((type) -> {
                    switch (type) {
                        case NUMERIC:
                            return VariantPropertyType.NUMERIC;
                        case STRING:
                            return VariantPropertyType.STRING_COLLECTION;
                        case NUMERIC_COLLECTION:
                        case STRING_COLLECTION:
                            return type;
                        default:
                            throw new IllegalStateException("Unhandled type " + type);
                    }
                })
                .collect(Collectors.toList());
    }

    public VariantTableCluster clusterVariantTable(VariantTable unclustered) {

        List<Collection<Integer>> indexClusters = computeClusterIndices(unclustered);

        List<List> clusteredCalls = new ArrayList<>();

        for (int i = 0; i < indexClusters.size(); i++) {

            Collection<Integer> indexCluster = indexClusters.get(i);

            Collection<List> callCluster = indexCluster.stream()
                    .map((index) -> unclustered.getRawCalls().get(index))
                    .collect(Collectors.toList());

            List combinedCall = combineCalls(callCluster, unclustered.getColumnIndexMap(), unclustered.getTypes());
            clusteredCalls.add(combinedCall);
        }

        for (int i = 0; i < clusteredCalls.size(); i++) {
            clusteredCalls.get(i).add(0, getClusterName(i, indexClusters.size()));
        }

        List<String> clusteredColumnNames = new ArrayList<>();
        clusteredColumnNames.add("viperId");
        clusteredColumnNames.addAll(unclustered.getColumnNames());

        List<VariantPropertyType> clusteredTypes = getNewClusterTypes(unclustered.getTypes());
        clusteredTypes.add(0, VariantPropertyType.STRING);

        VariantTable clusteredTable = new VariantTable(clusteredCalls, clusteredColumnNames, clusteredTypes);
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
