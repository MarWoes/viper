/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.imi.marw.viper.variants;

import de.imi.marw.viper.variants.table.VariantTable;
import java.util.Collection;
import java.util.List;
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

    public List<VariantCall> getUnclusteredCalls(int clusteredRowIndex) {
        return rowMapCluster.get(clusteredRowIndex).stream()
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
