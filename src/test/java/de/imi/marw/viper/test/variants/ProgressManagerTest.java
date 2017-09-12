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
package de.imi.marw.viper.test.variants;

import de.imi.marw.viper.test.util.TestUtil;
import de.imi.marw.viper.variants.VariantClusterBuilder;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.DecisionManager;
import de.imi.marw.viper.variants.table.VariantTable;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class ProgressManagerTest {

    private VariantTable loadData() {
        VariantTable unclustered = new CsvTableReader(';', ",").readTable(TestUtil.getResourceFile("examples-unclustered.csv"));
        VariantTable clustered = new VariantClusterBuilder(5, false).clusterVariantTable(unclustered).getClusteredTable();

        return clustered;
    }

    @Test
    public void progressIsProperlySavedAndLoaded() {

        VariantTable clustered1 = loadData();
        VariantTable clustered2 = loadData();

        clustered1.setCallProperty(0, VariantTable.DECISION_COLUMN_NAME, "maybe");
        clustered1.setCallProperty(1, VariantTable.DECISION_COLUMN_NAME, "approved");
        clustered1.setCallProperty(2, VariantTable.DECISION_COLUMN_NAME, "discarded");

        clustered2.setCallProperty(0, VariantTable.DECISION_COLUMN_NAME, "approved");
        clustered2.setCallProperty(1, VariantTable.DECISION_COLUMN_NAME, "approved");
        clustered2.setCallProperty(2, VariantTable.BP1_COLUMN_NAME, 123.0);

        DecisionManager mgr = new DecisionManager("/tmp");

        mgr.saveDecisions(clustered1);
        mgr.saveDecisions(clustered2);

        VariantTable check1 = loadData();
        VariantTable check2 = loadData();
        check2.setCallProperty(2, VariantTable.BP1_COLUMN_NAME, 123.0);

        mgr.loadDecisions(check1);
        mgr.loadDecisions(check2);

        assertEquals(clustered1.getUnfilteredColumn(VariantTable.DECISION_COLUMN_NAME), check1.getUnfilteredColumn(VariantTable.DECISION_COLUMN_NAME));
        assertEquals(clustered2.getUnfilteredColumn(VariantTable.DECISION_COLUMN_NAME), check2.getUnfilteredColumn(VariantTable.DECISION_COLUMN_NAME));

    }

}
