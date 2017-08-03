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

import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author marius
 */
public final class VariantProperty {

    private final VariantPropertyType type;
    private Object propertyValue;

    public VariantProperty(VariantPropertyType type, Object propertyValue) {
        this.type = type;

        setValue(propertyValue);
    }

    VariantPropertyType getType() {
        return this.type;
    }

    public Object getValue() {
        return propertyValue;
    }

    private void checkCorrectCollectionType(Object value, Class someClass) {
        if (value instanceof Collection) {
            ((Collection) value).stream().forEach((o) -> checkCorrectType(o, someClass));
        } else {
            throw new IllegalPropertyValueException(type, value, Collection.class);
        }
    }

    private void checkCorrectType(Object value, Class someClass) {
        if (value != null && !someClass.isInstance(value)) {
            throw new IllegalPropertyValueException(type, value, someClass);
        }
    }

    public void setValue(Object newValue) {
        switch (type) {
            case STRING:
                checkCorrectType(newValue, String.class);
                break;
            case STRING_COLLECTION:
                checkCorrectCollectionType(newValue, String.class);
                break;
            case NUMERIC:
                checkCorrectType(newValue, Double.class);
                break;
            case NUMERIC_COLLECTION:
                checkCorrectCollectionType(newValue, Double.class);
                break;
            default:
                throw new IllegalStateException("Unexpected type " + type + "when setting variant value");
        }

        this.propertyValue = newValue;
    }

    @Override
    public String toString() {
        return this.propertyValue == null ? "null" : propertyValue.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.type);
        hash = 47 * hash + Objects.hashCode(this.propertyValue);
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
        final VariantProperty other = (VariantProperty) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.propertyValue, other.propertyValue)) {
            return false;
        }
        return true;
    }
}
