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
