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
package de.imi.marw.viper.variants;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author marius
 */
public class VariantCall {

    public static final String TYPE_COLUMN_NAME = "svType";
    public static final String SAMPLE_COLUMN_NAME = "sample";
    public static final String CHR1_COLUMN_NAME = "chr1";
    public static final String CHR2_COLUMN_NAME = "chr2";
    public static final String BP1_COLUMN_NAME = "bp1";
    public static final String BP2_COLUMN_NAME = "bp2";

    private static final String[] MANDATORY_FIELDS = {
        TYPE_COLUMN_NAME,
        SAMPLE_COLUMN_NAME,
        CHR1_COLUMN_NAME,
        CHR2_COLUMN_NAME,
        BP1_COLUMN_NAME,
        BP2_COLUMN_NAME
    };

    private final Map<String, VariantProperty> properties;

    public VariantCall(Map<String, VariantProperty> properties) {
        this.properties = properties;

        checkMandatoryFields();
        ensureDataIntegrity();
    }

    public Map<String, VariantProperty> getProperties() {
        return properties;
    }

    public VariantProperty getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    public boolean isPassingFilters(Collection<VariantCallFilter> filters) {
        return filters
                .stream()
                .allMatch((filter) -> filter.isPassing(this));
    }

    private void checkMandatoryFields() {
        boolean allFieldsFound = Arrays.stream(MANDATORY_FIELDS)
                .allMatch((field) -> this.properties.containsKey(field));

        if (!allFieldsFound) {
            throw new IllegalArgumentException("Error creating VariantCall, not all mandatory fields were found.");
        }
    }

    private void ensureDataIntegrity() {
        Double bp1 = (Double) this.properties.get(BP1_COLUMN_NAME).getValue();
        Double bp2 = (Double) this.properties.get(BP2_COLUMN_NAME).getValue();

        if (bp1 > bp2) {
            Object chr1 = this.properties.get(CHR1_COLUMN_NAME).getValue();
            Object chr2 = this.properties.get(CHR2_COLUMN_NAME).getValue();

            this.properties.get(BP1_COLUMN_NAME).setValue(bp2);
            this.properties.get(BP2_COLUMN_NAME).setValue(bp1);

            this.properties.get(CHR1_COLUMN_NAME).setValue(chr2);
            this.properties.get(CHR2_COLUMN_NAME).setValue(chr1);
        }
    }

    @Override
    public String toString() {
        return properties.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.properties);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VariantCall other = (VariantCall) obj;
        return Objects.equals(this.properties, other.properties);
    }
}
