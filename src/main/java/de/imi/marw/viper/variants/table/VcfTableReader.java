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
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCompoundHeaderLine;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author marius
 */
public class VcfTableReader implements TableReader {

    private final String fileName;
    private List<String> columns;
    private List<VariantPropertyType> types;
    private List<Boolean> valuesDefinedPerSample;

    private static final String VCF_ID_FIELD = "ID";
    private static final String VCF_REF_FIELD = "REF";
    private static final String VCF_ALT_FIELD = "ALT";
    private static final String VCF_QUAL_FIELD = "QUAL";
    private static final String VCF_FILTER_FIELD = "FILTER";

    private static final String[] VCF_REQUIRED_FIELDS = {
        VCF_ID_FIELD,
        VCF_REF_FIELD,
        VCF_ALT_FIELD,
        VCF_QUAL_FIELD,
        VCF_FILTER_FIELD
    };

    public VcfTableReader(String fileName) {
        this.fileName = fileName;
    }

    private boolean isCollectionType(VCFCompoundHeaderLine info) {

        VCFHeaderLineCount count = info.getCountType();

        return count == VCFHeaderLineCount.A
                || count == VCFHeaderLineCount.R
                || count == VCFHeaderLineCount.UNBOUNDED
                || (count == VCFHeaderLineCount.INTEGER && info.getCount() > 1);
    }

    private VariantPropertyType convertVcfToVariantType(VCFCompoundHeaderLine info) {

        VCFHeaderLineType type = info.getType();

        boolean isCollection = isCollectionType(info);

        switch (type) {
            case Flag:
            case Character:
            case String:
                return isCollection ? VariantPropertyType.STRING_COLLECTION : VariantPropertyType.STRING;

            case Float:
            case Integer:
                return isCollection ? VariantPropertyType.NUMERIC_COLLECTION : VariantPropertyType.NUMERIC;

            default:
                throw new IllegalStateException("unexpected vcf type " + type);

        }
    }

    private void extractColumnAndType(VCFCompoundHeaderLine info) {
        columns.add(info.getID());
        types.add(convertVcfToVariantType(info));
        valuesDefinedPerSample.add(info.getCountType() == VCFHeaderLineCount.G);
    }

    private void extractColumnsAndTypes(VCFHeader header) {

        columns.add(VCF_ID_FIELD);
        types.add(VariantPropertyType.STRING);

        columns.add(VCF_REF_FIELD);
        types.add(VariantPropertyType.STRING);

        columns.add(VCF_ALT_FIELD);
        types.add(VariantPropertyType.STRING_COLLECTION);

        columns.add(VCF_QUAL_FIELD);
        types.add(VariantPropertyType.NUMERIC);

        columns.add(VCF_FILTER_FIELD);
        types.add(VariantPropertyType.STRING_COLLECTION);

        for (VCFInfoHeaderLine infoHeaderLine : header.getInfoHeaderLines()) {
            extractColumnAndType(infoHeaderLine);
        }

        for (VCFFormatHeaderLine formatHeaderLine : header.getFormatHeaderLines()) {
            extractColumnAndType(formatHeaderLine);
        }

    }

    private List<Object> extractMandatoryFields(VariantContext context, String sample) {
        List<Object> mandatoryCallValues = new ArrayList<>();

        mandatoryCallValues.add(sample);
        mandatoryCallValues.add(context.getType().toString());
        mandatoryCallValues.add(context.getContig());

        double start = context.getStart();
        mandatoryCallValues.add(start);
        //TODO: this is chr2, how is this expressed in vcf files?
        mandatoryCallValues.add(context.getContig());

        double end = context.getEnd();
        mandatoryCallValues.add(end);

        return mandatoryCallValues;
    }

    private List<Object> extractRequiredVCFFields(VariantContext context, String sample) {

        List<Object> vcfFieldValues = new ArrayList<>();

        vcfFieldValues.add(context.getID());
        vcfFieldValues.add(context.getReference().toString());

        List<String> altAlleles = context.getAlleles().stream()
                .map(allele -> allele.toString())
                .collect(Collectors.toList());
        vcfFieldValues.add(altAlleles);

        vcfFieldValues.add(context.getPhredScaledQual());
        vcfFieldValues.add(context.getFilters());

        return vcfFieldValues;
    }

    private Object extractAttributeValues(VariantContext context, String sample, String attribute, VariantPropertyType type, boolean isDefinedPerSample) {

        int sampleIndex = context.getSampleNamesOrderedByName().indexOf(sample);
        List<String> values = context.getAttributeAsStringList(attribute, null);

        switch (type) {

            case NUMERIC: {
                if (isDefinedPerSample) {
                    return Double.parseDouble(values.get(sampleIndex));
                } else {
                    return values.isEmpty() ? null : Double.parseDouble(values.get(0));
                }
            }
            case NUMERIC_COLLECTION: {
                return values.stream().map(Double::parseDouble).collect(Collectors.toList());
            }
            case STRING: {
                if (isDefinedPerSample) {
                    return values.get(sampleIndex);
                } else {
                    return values.isEmpty() ? "NA" : values.get(0);
                }
            }
            case STRING_COLLECTION: {
                return values;
            }

            default:
                throw new IllegalStateException("unexpected type " + type + " when extracting vcf attribute values");
        }

    }

    private Object extractGenotypeValues(Object genotypeAttribute, VariantPropertyType type) {

        Object defaultValue = null;

        switch (type) {
            case NUMERIC_COLLECTION:
            case STRING_COLLECTION:
                defaultValue = new ArrayList<>();
                break;
            case STRING:
                defaultValue = "NA";
        }

        if (genotypeAttribute == null) {
            return defaultValue;
        }

        if (type == VariantPropertyType.STRING) {
            return genotypeAttribute.toString();
        }

        if (type == VariantPropertyType.NUMERIC) {
            String value = genotypeAttribute.toString();
            if (".".equals(value)) {
                return null;
            }
            return Double.parseDouble(value);
        }

        String[] splitValues = ((String) genotypeAttribute).split(",|;");

        if (type == VariantPropertyType.STRING_COLLECTION) {
            return new ArrayList<>(Arrays.asList(splitValues));
        }

        if (type == VariantPropertyType.NUMERIC_COLLECTION) {
            List<Double> parsed = Arrays.stream(splitValues).map((str) -> ".".equals(str) ? null : Double.parseDouble(str))
                    .collect(Collectors.toList());
            return parsed;
        }

        throw new IllegalStateException("unexpected type " + type + "when extracting genotype values");
    }

    private List<Object> extractCall(VariantContext context, String sample, List<String> columns, List<VariantPropertyType> types) {

        int numAttributes = context.getCommonInfo().getNumAttributes();

        List<Object> call = new ArrayList<>(columns.size());

        call.addAll(extractMandatoryFields(context, sample));
        call.addAll(extractRequiredVCFFields(context, sample));

        int attributeLower = VariantTable.MANDATORY_FIELDS.length + VCF_REQUIRED_FIELDS.length;
        int attributeUpper = attributeLower + numAttributes;

        for (int i = attributeLower; i < attributeUpper; i++) {

            String attribute = columns.get(i);
            VariantPropertyType type = types.get(i);

            call.add(extractAttributeValues(context, sample, attribute, type, valuesDefinedPerSample.get(i)));

        }

        for (int i = attributeUpper; i < columns.size(); i++) {

            Genotype genotype = context.getGenotype(sample);
            Object genotypeAttribute = genotype.getAnyAttribute(columns.get(i));

            call.add(extractGenotypeValues(genotypeAttribute, types.get(i)));

        }

        return call;
    }

    @Override
    public VariantTable readTable() {
        VCFFileReader reader = new VCFFileReader(new File(fileName), false);
        VCFHeader header = reader.getFileHeader();

        List<String> newColumns = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        List<VariantPropertyType> newTypes = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        List<Boolean> newValuesDefinedPerSample = IntStream.range(0, VariantTable.MANDATORY_FIELDS.length)
                .boxed()
                .map(i -> false)
                .collect(Collectors.toList());

        this.columns = newColumns;
        this.types = newTypes;
        this.valuesDefinedPerSample = newValuesDefinedPerSample;

        extractColumnsAndTypes(header);

        List<List<Object>> calls = new ArrayList<>();

        for (VariantContext context : reader) {

            for (String sample : context.getSampleNamesOrderedByName()) {

                calls.add(extractCall(context, sample, columns, types));

            }

        }

        return new VariantTable(calls, columns, types);

    }

}
