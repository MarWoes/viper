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

    public List<String> callToStringList(List<Object> call, List<VariantPropertyType> types) {

        List<String> callStrings = new ArrayList<>(types.size());

        for (int j = 0; j < types.size(); j++) {

            switch (types.get(j)) {
                case STRING:
                case NUMERIC:
                    callStrings.add("" + (call.get(j) == null ? "NA" : call.get(j).toString()));
                    break;
                case NUMERIC_COLLECTION:
                case STRING_COLLECTION:
                    String joinedValues = ((Collection) call.get(j)).stream()
                            .map(property -> property == null ? "NA" : property.toString())
                            .collect(Collectors.joining(collectionDelimiter + " ")).toString();
                    callStrings.add(joinedValues);
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
