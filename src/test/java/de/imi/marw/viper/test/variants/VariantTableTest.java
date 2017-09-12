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

import de.imi.marw.viper.variants.VariantPropertyType;
import de.imi.marw.viper.variants.filters.StringFilter;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class VariantTableTest {

    @Test(expected = IllegalArgumentException.class)
    public void duplicateColumnsThrowException() {

        List<List<Object>> calls = new ArrayList<>();
        List<VariantPropertyType> types = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        types.add(VariantPropertyType.NUMERIC_COLLECTION);

        List<String> colNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        colNames.add(VariantTable.BP1_COLUMN_NAME);

        VariantTable duplicate = new VariantTable(calls, colNames, types);

    }

    @Test
    public void emptyTableWorksCorrectly() {

        List<List<Object>> calls = new ArrayList<>();
        List<VariantPropertyType> types = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        List<String> colNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));

        VariantTable emptyTable = new VariantTable(calls, colNames, types);

        assertEquals(emptyTable.getNumberOfCalls(), 0);
        assertEquals(emptyTable.getRawCalls().size(), 0);
        assertEquals(emptyTable.getUnfilteredColumn(VariantTable.BP1_COLUMN_NAME).size(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingMandatoryFieldsThrowsException() {

        List<List<Object>> calls = new ArrayList<>();
        List<VariantPropertyType> types = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        List<String> colNames = new ArrayList<>();
        colNames.add(VariantTable.SAMPLE_COLUMN_NAME);
        colNames.add(VariantTable.BP1_COLUMN_NAME);

        VariantTable missingColumns = new VariantTable(calls, colNames, types);

    }

    private void testWrongColumnType(int i, Object wrongValue) {
        List<List<Object>> calls = new ArrayList<>();
        List<VariantPropertyType> types = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        types.add(VariantPropertyType.STRING_COLLECTION);
        types.add(VariantPropertyType.NUMERIC_COLLECTION);

        List<String> colNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        colNames.add("strColl");
        colNames.add("numColl");

        List<Object> call = new ArrayList<>();
        call.add("SAMPLE1");
        call.add("DELETION");
        call.add("17");
        call.add(12341234.0);
        call.add("18");
        call.add(12324565.0);
        call.add(Arrays.asList(new String[]{"blub", "Halhalo"}));
        call.add(Arrays.asList(new Double[]{13.0, 37.0}));

        call.set(i, wrongValue);
        calls.add(call);

        try {
            VariantTable noDataIntegrity = new VariantTable(calls, colNames, types);
            fail("data integrity was violated when creating variant table with false data types!");
        } catch (IllegalArgumentException ex) {
            // this is supposed to happen!
        }

    }

    @Test
    public void wrongColumnTypeYieldsError() {

        Collection<String> stringCollectionValue = new ArrayList<>();
        stringCollectionValue.add("blub");
        stringCollectionValue.add("blar");

        Collection<Double> numericCollectionValue = new ArrayList<>();
        numericCollectionValue.add(13.0);
        numericCollectionValue.add(37.0);

        int stringColumnIndex = Arrays.asList(VariantTable.MANDATORY_FIELDS).indexOf(VariantTable.SAMPLE_COLUMN_NAME);

        testWrongColumnType(stringColumnIndex, 0);
        testWrongColumnType(stringColumnIndex, 0.0);
        testWrongColumnType(stringColumnIndex, '1');
        testWrongColumnType(stringColumnIndex, stringCollectionValue);
        testWrongColumnType(stringColumnIndex, numericCollectionValue);

        int numericColumnIndex = Arrays.asList(VariantTable.MANDATORY_FIELDS).indexOf(VariantTable.BP1_COLUMN_NAME);

        testWrongColumnType(numericColumnIndex, 0);
        testWrongColumnType(numericColumnIndex, "0.0");
        testWrongColumnType(numericColumnIndex, '1');
        testWrongColumnType(numericColumnIndex, stringCollectionValue);
        testWrongColumnType(numericColumnIndex, numericCollectionValue);

        int stringCollectionColumnIndex = VariantTable.MANDATORY_FIELDS.length;

        testWrongColumnType(stringCollectionColumnIndex, 0);
        testWrongColumnType(stringCollectionColumnIndex, 0.0);
        testWrongColumnType(stringCollectionColumnIndex, "0.0");
        testWrongColumnType(stringCollectionColumnIndex, '1');
        testWrongColumnType(stringCollectionColumnIndex, numericCollectionValue);

        int numericCollectionColumnIndex = VariantTable.MANDATORY_FIELDS.length + 1;

        testWrongColumnType(numericCollectionColumnIndex, 0);
        testWrongColumnType(numericCollectionColumnIndex, 0.0);
        testWrongColumnType(numericCollectionColumnIndex, "0.0");
        testWrongColumnType(numericCollectionColumnIndex, '1');
        testWrongColumnType(numericCollectionColumnIndex, stringCollectionValue);

    }

    @Test
    public void breakpointOrderIsChangedIfNecessary() {

        List<List<Object>> calls = new ArrayList<>();
        List<VariantPropertyType> types = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        types.add(VariantPropertyType.STRING_COLLECTION);
        types.add(VariantPropertyType.NUMERIC_COLLECTION);

        List<String> colNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        colNames.add("strColl");
        colNames.add("numColl");

        List<Object> call = new ArrayList<>();
        call.add("SAMPLE1");
        call.add("DELETION");
        call.add("17");
        call.add(12524565.0);
        call.add("19");
        call.add(12341234.0);
        call.add(Arrays.asList(new String[]{"blub", "Halhalo"}));
        call.add(Arrays.asList(new Double[]{13.0, 37.0}));

        List<Object> call2 = new ArrayList<>();
        call2.add("SAMPLE2");
        call2.add("DELETION");
        call2.add("17");
        call2.add(12524565.5321);
        call2.add("18");
        call2.add(12341234.1234);
        call2.add(Arrays.asList(new String[]{}));
        call2.add(Arrays.asList(new Double[]{}));

        calls.add(call);
        calls.add(call2);

        VariantTable simpleTable = new VariantTable(calls, colNames, types);

        assertEquals(simpleTable.getUnfilteredColumn(VariantTable.BP1_COLUMN_NAME), Arrays.asList(new Double[]{12341234.0, 12341234.1234}));
        assertEquals(simpleTable.getUnfilteredColumn(VariantTable.BP2_COLUMN_NAME), Arrays.asList(new Double[]{12524565.0, 12524565.5321}));
    }

    private VariantTable createSimpleTable() {
        List<List<Object>> calls = new ArrayList<>();
        List<VariantPropertyType> types = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        types.add(VariantPropertyType.STRING_COLLECTION);
        types.add(VariantPropertyType.NUMERIC_COLLECTION);

        List<String> colNames = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        colNames.add("strColl");
        colNames.add("numColl");

        List<Object> call = new ArrayList<>();
        call.add("SAMPLE1");
        call.add("DELETION");
        call.add("17");
        call.add(12341234.0);
        call.add("19");
        call.add(12524565.0);
        call.add(Arrays.asList(new String[]{"blub", "Halhalo"}));
        call.add(Arrays.asList(new Double[]{13.0, 37.0}));

        List<Object> call2 = new ArrayList<>();
        call2.add("SAMPLE2");
        call2.add("DELETION");
        call2.add("17");
        call2.add(12341234.1234);
        call2.add("18");
        call2.add(12524565.5321);
        call2.add(Arrays.asList(new String[]{}));
        call2.add(Arrays.asList(new Double[]{}));

        calls.add(call);
        calls.add(call2);

        VariantTable simpleTable = new VariantTable(calls, colNames, types);

        return simpleTable;
    }

    private final static Map<String, Object> EXPECTED_CALL_MAP = new LinkedHashMap<>();

    static {
        EXPECTED_CALL_MAP.put(VariantTable.SAMPLE_COLUMN_NAME, "SAMPLE2");
        EXPECTED_CALL_MAP.put(VariantTable.TYPE_COLUMN_NAME, "DELETION");
        EXPECTED_CALL_MAP.put(VariantTable.CHR1_COLUMN_NAME, "17");
        EXPECTED_CALL_MAP.put(VariantTable.BP1_COLUMN_NAME, 12341234.1234);
        EXPECTED_CALL_MAP.put(VariantTable.CHR2_COLUMN_NAME, "18");
        EXPECTED_CALL_MAP.put(VariantTable.BP2_COLUMN_NAME, 12524565.5321);
        EXPECTED_CALL_MAP.put("strColl", Arrays.asList(new String[]{}));
        EXPECTED_CALL_MAP.put("numColl", Arrays.asList(new Double[]{}));
    }

    @Test
    public void correctTableIsCreated() {

        VariantTable simpleTable = createSimpleTable();

        assertEquals(simpleTable.getNumberOfCalls(), 2);
        assertEquals(simpleTable.getUnfilteredColumn(VariantTable.BP1_COLUMN_NAME), Arrays.asList(new Double[]{12341234.0, 12341234.1234}));
        assertEquals(simpleTable.getUnfilteredColumn(VariantTable.SAMPLE_COLUMN_NAME), Arrays.asList(new String[]{"SAMPLE1", "SAMPLE2"}));
        assertEquals(simpleTable.getCall(0).get("strColl"), Arrays.asList(new String[]{"blub", "Halhalo"}));
        assertEquals(simpleTable.getCall(1).get(VariantTable.CHR2_COLUMN_NAME), "18");

        simpleTable.setCallProperty(0, VariantTable.BP1_COLUMN_NAME, 37.0);
        assertEquals(simpleTable.getCall(0).get(VariantTable.BP1_COLUMN_NAME), 37.0);

        assertEquals(EXPECTED_CALL_MAP, simpleTable.getCall(1));
    }

    @Test
    public void softFiltersAreWorkingCorrectly() {

        Set<String> allowedStringValues = new HashSet<>();
        allowedStringValues.add("SAMPLE2");

        StringFilter filter = new StringFilter(VariantTable.SAMPLE_COLUMN_NAME);
        filter.setAllowedValues(allowedStringValues);
        VariantTable filteredTable = createSimpleTable();

        assertEquals(EXPECTED_CALL_MAP, filteredTable.getCall(1));

        filteredTable.filter(Arrays.asList(filter));

        assertEquals(EXPECTED_CALL_MAP, filteredTable.getCall(0));
        assertEquals(filteredTable.getNumberOfCalls(), 1);
        assertEquals(filteredTable.getUnfilteredColumn(VariantTable.BP1_COLUMN_NAME), Arrays.asList(new Double[]{12341234.0, 12341234.1234}));
        assertEquals(filteredTable.getUnfilteredColumn(VariantTable.SAMPLE_COLUMN_NAME), Arrays.asList(new String[]{"SAMPLE1", "SAMPLE2"}));
        assertEquals(filteredTable.getCall(0).get("strColl"), Arrays.asList(new String[]{}));
        assertEquals(filteredTable.getCall(0).get(VariantTable.CHR2_COLUMN_NAME), "18");

        filteredTable.setCallProperty(0, VariantTable.BP1_COLUMN_NAME, 37.0);
        assertEquals(filteredTable.getCall(0).get(VariantTable.BP1_COLUMN_NAME), 37.0);

        allowedStringValues.clear();
        filteredTable.filter(Arrays.asList(filter));

        assertEquals(filteredTable.getNumberOfCalls(), 2);
        assertEquals(filteredTable.getUnfilteredColumn(VariantTable.BP1_COLUMN_NAME), Arrays.asList(new Double[]{12341234.0, 37.0}));
        assertEquals(filteredTable.getUnfilteredColumn(VariantTable.SAMPLE_COLUMN_NAME), Arrays.asList(new String[]{"SAMPLE1", "SAMPLE2"}));
        assertEquals(filteredTable.getCallProperty(0, "strColl"), Arrays.asList(new String[]{"blub", "Halhalo"}));
        assertEquals(filteredTable.getCallProperty(1, VariantTable.CHR2_COLUMN_NAME), "18");
    }
}
