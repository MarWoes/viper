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
