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
import de.imi.marw.viper.variants.table.VcfTableReader;
import java.io.IOException;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class VcfTableReaderTest {

    //TODO: check all combinations
    private void checkVcfCorrectness(boolean simple, boolean excludeReferenceCalls, String targetVcf, String expectedFileName) throws IOException {

        CsvTableReader rd = new CsvTableReader(';', ",");
        CallStringifier strf = new CallStringifier(",");
        VariantTable expected = rd.readTable(TestUtil.getResourceFile(expectedFileName));
        VariantTable actual = new VcfTableReader(simple, excludeReferenceCalls).readTable(TestUtil.getResourceFile(targetVcf));

        assertEquals(expected.getNumberOfCalls(), actual.getNumberOfCalls());

        List<List<String>> expectedCallStrings = strf.callsToStringLists(expected, expected.getSoftFilter());
        List<List<String>> actualCallStrings = strf.callsToStringLists(actual, actual.getSoftFilter());

        for (int i = 0; i < expected.getNumberOfCalls(); i++) {

            assertEquals(expectedCallStrings.get(i), actualCallStrings.get(i));
        }

    }

    @Test
    public void allSimpleCallsCorrectlyLoaded() throws IOException {

        checkVcfCorrectness(true, false, "examples.vcf", "vcf-import-simple-all.csv");

    }

    @Test
    public void allNonRefCallsCorrectlyLoaded() throws IOException {

        checkVcfCorrectness(true, true, "examples.vcf", "vcf-import-simple-nonref.csv");

    }

    @Test
    public void allNonRefComplexCallsCorrectlyLoaded() throws IOException {

        checkVcfCorrectness(false, true, "examples.vcf", "vcf-import-complex-nonref.csv");

    }

    @Test
    public void minimalVcfNonRefComplexCallsCorrectlyLoaded() throws IOException {

        checkVcfCorrectness(false, false, "example-minimal.vcf", "vcf-import-minimal-complex-nonref.csv");

    }

}
