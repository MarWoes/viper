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

import de.imi.marw.viper.variants.VariantCall;
import de.imi.marw.viper.variants.VariantCallFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author marius
 */
public class VariantTable {

    private final List<VariantCall> calls;
    private final List<String> columnNames;

    public VariantTable(Collection<VariantCall> calls, List<String> columnNames) {
        this.calls = new ArrayList<>();
        this.columnNames = columnNames;
        this.calls.addAll(calls);
    }

    public synchronized VariantTable filter(Collection<VariantCallFilter> filters) {

        Collection<VariantCall> callsAfterFiltering = this.calls.stream()
                .filter((call) -> call.isPassingFilters(filters))
                .collect(Collectors.toList());

        return new VariantTable(callsAfterFiltering, columnNames);
    }

    public synchronized VariantCall getCall(int rowIndex) {
        return this.calls.get(rowIndex);
    }

    public synchronized List<VariantCall> getCallRange(int lower, int upper) {
        return IntStream
                .range(lower, upper)
                .boxed()
                .map((index) -> calls.get(index))
                .collect(Collectors.toList());
    }

    public synchronized int getNumberOfCalls() {
        return this.calls.size();
    }

    public synchronized List<String> getColumnNames() {
        return columnNames;
    }
}
