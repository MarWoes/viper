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

import de.imi.marw.viper.variants.table.VariantTable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author marius
 */
public class VariantTableCluster {

    private final VariantTable unclusteredTable;
    private final VariantTable clusteredTable;
    private final List<Collection<Integer>> rowMapCluster;

    public VariantTableCluster(VariantTable unclusteredTable, VariantTable clusteredTable, List<Collection<Integer>> rowMapCluster) {
        this.unclusteredTable = unclusteredTable;
        this.clusteredTable = clusteredTable;
        this.rowMapCluster = rowMapCluster;
    }

    public Collection<Integer> getRelatedIndices(int clusteredRowIndex) {
        return rowMapCluster.get(clusteredTable.getSoftFilteredIndex(clusteredRowIndex));
    }

    public List<Collection<Integer>> getRowMapCluster() {
        return rowMapCluster;
    }

    public synchronized List<Map<String, Object>> getUnclusteredCalls(int clusteredRowIndex) {
        return getRelatedIndices(clusteredRowIndex).stream()
                .map((rowIndex) -> unclusteredTable.getCall(rowIndex))
                .collect(Collectors.toList());
    }

    public VariantTable getUnclusteredTable() {
        return unclusteredTable;
    }

    public VariantTable getClusteredTable() {
        return clusteredTable;
    }

}