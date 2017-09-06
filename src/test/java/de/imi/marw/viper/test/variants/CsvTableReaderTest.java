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
import de.imi.marw.viper.variants.VariantPropertyType;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class CsvTableReaderTest {

    private void assertCallEquals(VariantTable table, int index, Object... callValues) {

        assertEquals(Arrays.asList(callValues), table.getRawCalls().get(index));

    }

    private <T> Collection<T> coll(T... vals) {
        return Arrays.asList(vals);
    }

    @Test
    public void correctlyReadsSmallExample() {

        CsvTableReader reader = new CsvTableReader(';', ",");

        VariantTable table = reader.readTable(TestUtil.getResourceFile("examples-small.csv"));

        List<String> expectedColumnNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        expectedColumnNames.addAll(Arrays.asList("strColl", "numColl", "naColl"));

        List<VariantPropertyType> expectedColumnTypes = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        expectedColumnTypes.addAll(Arrays.asList(VariantPropertyType.STRING_COLLECTION, VariantPropertyType.NUMERIC_COLLECTION, VariantPropertyType.NUMERIC));

        assertEquals(expectedColumnNames, table.getColumnNames());
        assertEquals(expectedColumnTypes, table.getColumnTypes());
        assertEquals(4, table.getNumberOfCalls());

        assertCallEquals(table, 0,
                "SAMPLE1", "DELETION", "X", 1000000.0, "X", 2000000.0, coll("hallo", "huhu"), coll(1.0), null
        );

        assertCallEquals(table, 1,
                "SAMPLE1", "DELETION", "1", 1000000.0, "1", 2000000.0, coll("blub"), coll(1.0, 2.0, 3.0, 4.0), null
        );

        assertCallEquals(table, 2,
                "SAMPLE2", "DELETION", "2", 1000000.0, "2", 2000000.0, coll("NA"), (Collection<Double>) coll((Double) null), null
        );

        assertCallEquals(table, 3,
                "SAMPLE2", "TRANSLOCATION", "3", 1000000.0, "X", 2000000.0, coll("NA"), (Collection<Double>) coll((Double) null), null
        );

    }

    @Test
    public void correctlyReadsBiggerExample() {
        CsvTableReader reader = new CsvTableReader(';', ",");

        VariantTable table = reader.readTable(TestUtil.getResourceFile("examples.csv"));

        List<String> expectedColumnNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        expectedColumnNames.addAll(Arrays.asList("supporting", "tool", "genes", "database", "cov1", "cov2", "qual1", "qual2", "combinedValue"));

        List<VariantPropertyType> expectedColumnTypes = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        expectedColumnTypes.addAll(Arrays.asList(
                VariantPropertyType.NUMERIC,
                VariantPropertyType.STRING,
                VariantPropertyType.STRING_COLLECTION,
                VariantPropertyType.NUMERIC,
                VariantPropertyType.NUMERIC,
                VariantPropertyType.NUMERIC,
                VariantPropertyType.NUMERIC,
                VariantPropertyType.NUMERIC,
                VariantPropertyType.NUMERIC_COLLECTION
        ));

        assertEquals(expectedColumnNames, table.getColumnNames());
        assertEquals(expectedColumnTypes, table.getColumnTypes());
        assertEquals(226, table.getNumberOfCalls());

        // just randomly check some calls
        assertCallEquals(table, 109,
                "SIM2",
                "DELETION",
                "4",
                55152172.0,
                "4",
                55152231.0,
                80.8510638298,
                "gustaf",
                coll("FIP1L1", "PDGFRA"),
                null,
                3910.5185185185,
                1867.8765432099,
                34.9043099965,
                35.4605877143,
                coll((Double) null)
        );
    }

    @Test
    public void correctlyReadsEdgeCases() {
        CsvTableReader reader = new CsvTableReader('\t', "_");

        VariantTable table = reader.readTable(TestUtil.getResourceFile("examples-edge-cases.csv"));

        List<String> expectedColumnNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        expectedColumnNames.addAll(Arrays.asList("strColl", "numColl"));

        List<VariantPropertyType> expectedColumnTypes = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        expectedColumnTypes.addAll(Arrays.asList(VariantPropertyType.STRING_COLLECTION, VariantPropertyType.STRING));

        assertEquals(expectedColumnNames, table.getColumnNames());
        assertEquals(expectedColumnTypes, table.getColumnTypes());
        assertEquals(1, table.getNumberOfCalls());

        assertCallEquals(table, 0,
                " 	Åloha|öäüß##",
                "DEL",
                "MT",
                123.0,
                "X",
                250.0,
                coll("blub", "asdf"),
                "1,2,3"
        );
    }

    @Test
    public void correctlyReadsEmptyTable() {

        CsvTableReader reader = new CsvTableReader(';', ",");

        VariantTable table = reader.readTable(TestUtil.getResourceFile("examples-empty.csv"));

        assertEquals(0, table.getNumberOfCalls());

    }

}
