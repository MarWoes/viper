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
import de.imi.marw.viper.variants.table.CallStringifier;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
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
    public void callsAreCorrectlyTurnedIntoStrings() {

        CallStringifier callStringifier = new CallStringifier(",");

        VariantTable table = new CsvTableReader(';', ",").readTable(TestUtil.getResourceFile("examples-small.csv"));

        List<List<String>> expectedStrings = Arrays.asList(
                Arrays.asList("sample", "svType", "chr1", "bp1", "chr2", "bp2", "strColl", "numColl", "naColl"),
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
