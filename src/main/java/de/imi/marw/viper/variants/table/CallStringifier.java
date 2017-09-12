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
package de.imi.marw.viper.variants.table;

import de.imi.marw.viper.variants.VariantPropertyType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author marius
 */
public class CallStringifier {

    private final String collectionDelimiter;

    public CallStringifier(String collectionDelimiter) {
        this.collectionDelimiter = collectionDelimiter;
    }

    private String printDouble(Double d) {

        // d%1 == 0 iff d is integer
        if (d % 1 == 0) {
            return "" + d.intValue();
        }

        return d.toString();
    }

    private List<String> callToStringList(List<Object> call, List<VariantPropertyType> types) {

        List<String> callStrings = new ArrayList<>(types.size());

        for (int j = 0; j < types.size(); j++) {

            switch (types.get(j)) {
                case STRING:
                    callStrings.add(call.get(j).toString());
                    break;
                case NUMERIC:
                    callStrings.add("" + (call.get(j) == null ? "NA" : printDouble((double) call.get(j))));
                    break;
                case NUMERIC_COLLECTION:
                    String joinedNumericValues = ((Collection) call.get(j)).stream()
                            .map(property -> property == null ? "NA" : printDouble((double) property))
                            .collect(Collectors.joining(collectionDelimiter + " ")).toString();
                    callStrings.add(joinedNumericValues);
                    break;
                case STRING_COLLECTION:
                    String joinedStringValues = ((Collection) call.get(j)).stream()
                            .map(property -> property.toString())
                            .collect(Collectors.joining(collectionDelimiter + " ")).toString();
                    callStrings.add(joinedStringValues);
            }

        }

        return callStrings;
    }

    public List<List<String>> callsToStringLists(VariantTable table, int[] indices) {

        List<List<String>> values = new ArrayList<>();
        List<VariantPropertyType> types = table.getTypes();

        values.add(table.getColumnNames());

        for (int i = 0; i < indices.length; i++) {

            List<Object> call = table.getRawCalls().get(indices[i]);
            List<String> callStrings = this.callToStringList(call, types);

            values.add(callStrings);

        }

        return values;
    }

}
