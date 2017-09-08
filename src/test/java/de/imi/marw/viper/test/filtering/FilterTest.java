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
package de.imi.marw.viper.test.filtering;

import de.imi.marw.viper.test.util.TestUtil;
import de.imi.marw.viper.variants.VariantCallFilter;
import de.imi.marw.viper.variants.filters.NumericCollectionFilter;
import de.imi.marw.viper.variants.filters.NumericFilter;
import de.imi.marw.viper.variants.filters.StringCollectionFilter;
import de.imi.marw.viper.variants.filters.StringFilter;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class FilterTest {

    private VariantTable loadData() {
        CsvTableReader reader = new CsvTableReader(';', ",");
        VariantTable table = reader.readTable(TestUtil.getResourceFile("examples-filtering.csv"));

        return table;
    }

    private void checkIDs(VariantTable table, String... expectedIdsAfterFiltering) {

        assertEquals(expectedIdsAfterFiltering.length, table.getNumberOfCalls());

        for (int i = 0; i < expectedIdsAfterFiltering.length; i++) {

            assertEquals(expectedIdsAfterFiltering[i], table.getCallProperty(i, VariantTable.ID_COLUMN_NAME));

        }
    }

    @Test
    public void numericFiltersWorkingCorrectly() {

        VariantTable table = loadData();

        NumericFilter filter = new NumericFilter("num", 3.5, 5.0);
        filter.setNullAllowed(true);

        table.filter(Arrays.asList(filter));

        checkIDs(table, "V1", "V2", "V6", "V7");

        filter.setNullAllowed(false);
        filter.setSelectedMin(19.1);
        filter.setSelectedMax(19.1);

        table.filter(Arrays.asList(filter));

        checkIDs(table, "V8");
    }

    @Test
    public void numericCollectionFilterWorkingCorrectly() {

        VariantTable table = loadData();

        NumericCollectionFilter filter = new NumericCollectionFilter("numColl", 3.5, 5.0);
        filter.setNullAllowed(true);

        table.filter(Arrays.asList(filter));

        checkIDs(table, "V2", "V3", "V5", "V7", "V8");

        filter.setNullAllowed(false);
        filter.setSelectedMin(7);
        filter.setSelectedMax(9);

        table.filter(Arrays.asList(filter));

        checkIDs(table, "V4", "V6");
    }

    @Test
    public void stringFilterWorkingCorrectly() {

        VariantTable table = loadData();

        Set<String> allowedValues = new HashSet<>(Arrays.asList("NA", "N", "blub"));

        StringFilter filter = new StringFilter("str");
        filter.setAllowedValues(allowedValues);

        table.filter(Arrays.asList(filter));

        checkIDs(table, "V1", "V3", "V4", "V8");

        allowedValues.clear();
        table.filter(Arrays.asList(filter));

        checkIDs(table, "V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8");
    }

    @Test
    public void stringCollectionFilterWorkingCorrectly() {

        VariantTable table = loadData();

        StringCollectionFilter filter = new StringCollectionFilter("strColl");
        filter.setAllowedValues(new HashSet<>(Arrays.asList("NA", "N", "STR1")));

        table.filter(Arrays.asList(filter));

        checkIDs(table, "V1", "V2", "V3", "V6");

        filter.setAllowedValues(new HashSet<>(Arrays.asList("1", "STR3")));

        table.filter(Arrays.asList(filter));

        checkIDs(table, "V4", "V7");

        filter.getAllowedValues().clear();
        table.filter(Arrays.asList(filter));

        checkIDs(table, "V1", "V2", "V3", "V4", "V5", "V6", "V7", "V8");
    }

    @Test
    public void multipleFiltersWorkingCorrectly() {

        VariantTable table = loadData();

        NumericFilter numFilter = new NumericFilter("num", -1, 4);
        NumericCollectionFilter numCollFilter = new NumericCollectionFilter("numColl", 1, 2);
        StringFilter strFilter = new StringFilter("str");
        strFilter.setAllowedValues(new HashSet<>(Arrays.asList("blub", "blub2", "blub3", "NA")));
        StringCollectionFilter strCollFilter = new StringCollectionFilter("strColl");
        strCollFilter.setAllowedValues(new HashSet<>(Arrays.asList("STR3", "STR4", "19", "22")));

        Collection<VariantCallFilter> filters = Arrays.asList(numFilter, numCollFilter, strFilter, strCollFilter);
        table.filter(filters);

        checkIDs(table, "V4", "V5");

    }

}
