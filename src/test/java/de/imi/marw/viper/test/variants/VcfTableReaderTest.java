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
import de.imi.marw.viper.variants.table.VcfTableReader;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class VcfTableReaderTest {

    //TODO: check all combinations
    private void checkVcfCorrectness(boolean simple, boolean excludeReferenceCalls, String expectedFileName) {

        CsvTableReader rd = new CsvTableReader(';', ",");
        CallStringifier strf = new CallStringifier(",");
        VariantTable expected = rd.readTable(TestUtil.getResourceFile(expectedFileName));
        VariantTable actual = new VcfTableReader(simple, excludeReferenceCalls).readTable(TestUtil.getResourceFile("examples.vcf"));

        assertEquals(expected.getNumberOfCalls(), actual.getNumberOfCalls());

        List<List<String>> expectedCallStrings = strf.callsToStringLists(expected, expected.getSoftFilter());
        List<List<String>> actualCallStrings = strf.callsToStringLists(actual, actual.getSoftFilter());

        for (int i = 0; i < expected.getNumberOfCalls(); i++) {

            assertEquals(expectedCallStrings.get(i), actualCallStrings.get(i));
        }

    }

    @Test
    public void allSimpleCallsCorrectlyLoaded() {

        checkVcfCorrectness(true, false, "vcf-import-simple-all.csv");

    }

    @Test
    public void allNonRefCallsCorrectlyLoaded() {

        checkVcfCorrectness(true, true, "vcf-import-simple-nonref.csv");

    }

    @Test
    public void allNonRefComplexCallsCorrectlyLoaded() {

        checkVcfCorrectness(false, true, "vcf-import-complex-nonref.csv");

    }

}
