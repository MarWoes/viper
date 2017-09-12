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
