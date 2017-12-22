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
import de.imi.marw.viper.variants.table.CallStringifier;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class CallStringifierTest {

    @Test
    public void callsAreCorrectlyTurnedIntoStrings() throws IOException {

        CallStringifier callStringifier = new CallStringifier(",");

        VariantTable table = new CsvTableReader(';', ",").readTable(TestUtil.getResourceFile("examples-small.csv"));

        List<List<String>> expectedStrings = Arrays.asList(
                Arrays.asList("sample", "type", "chr1", "bp1", "chr2", "bp2", "strColl", "numColl", "naColl"),
                Arrays.asList("SAMPLE1", "DELETION", "X", "1000000", "X", "2000000", "hallo, huhu", "1", "NA"),
                Arrays.asList("SAMPLE1", "DELETION", "1", "1000000", "1", "2000000", "blub", "1, 2, 3, 4", "NA"),
                Arrays.asList("SAMPLE2", "DELETION", "2", "1000000", "2", "2000000", "NA", "NA", "NA"),
                Arrays.asList("SAMPLE2", "TRANSLOCATION", "3", "1000000", "X", "2000000", "NA", "NA", "NA")
        );

        List<List<String>> actualStrings = callStringifier.callsToStringLists(table, table.getSoftFilter());

        assertEquals(expectedStrings.size(), actualStrings.size());

        for (int i = 0; i < expectedStrings.size(); i++) {
            assertEquals(expectedStrings.get(i), actualStrings.get(i));
        }

    }

}
