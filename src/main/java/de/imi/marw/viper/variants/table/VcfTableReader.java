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

    private List<String> columns;
    private List<String> prefixedColumns;
    private List<VariantPropertyType> types;
    private List<Boolean> valuesDefinedPerSample;
    private boolean simple;
    private boolean excludeNonReferenceCalls;

    public VcfTableReader(boolean simple, boolean excludeNonReferenceCalls) {
        this.simple = simple;
        this.excludeNonReferenceCalls = excludeNonReferenceCalls;
    }

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

    private boolean isCollectionType(VCFCompoundHeaderLine info) {

        VCFHeaderLineCount count = info.getCountType();

        return count == VCFHeaderLineCount.A
                || count == VCFHeaderLineCount.G
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

    private void extractColumnAndType(VCFCompoundHeaderLine info, String prefix) {
        columns.add(info.getID());
        prefixedColumns.add(prefix + ":" + info.getID());
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

        prefixedColumns.addAll(columns);

        if (isSimple()) {
            return;
        }

        for (VCFInfoHeaderLine infoHeaderLine : header.getInfoHeaderLines()) {
            extractColumnAndType(infoHeaderLine, "INFO");
        }

        for (VCFFormatHeaderLine formatHeaderLine : header.getFormatHeaderLines()) {
            extractColumnAndType(formatHeaderLine, "FORMAT");
        }

    }

    private List<Object> extractMandatoryFields(VariantContext context, String sample) {
        List<Object> mandatoryCallValues = new ArrayList<>();

        mandatoryCallValues.add(sample);

        String svType;
        String chr2;
        if (context.isSymbolicOrSV()) {
            svType = context.getStructuralVariantType().toString();
            chr2 = context.getAttribute("CHR2").toString();
        } else {
            svType = context.getType().toString();
            chr2 = context.getContig();
        }
        mandatoryCallValues.add(svType);

        mandatoryCallValues.add(context.getContig());

        double start = context.getStart();
        mandatoryCallValues.add(start);

        //TODO: this is chr2, how is this expressed in vcf files?
        mandatoryCallValues.add(chr2);

        double end = context.getEnd();
        mandatoryCallValues.add(end);

        return mandatoryCallValues;
    }

    private List<Object> extractRequiredVCFFields(VariantContext context, String sample) {

        List<Object> vcfFieldValues = new ArrayList<>();

        String id = ".".equals(context.getID()) ? "NA" : context.getID();

        vcfFieldValues.add(id);
        vcfFieldValues.add(context.getReference().getDisplayString());

        List<String> altAlleles = context.getAlternateAlleles().stream()
                .map(allele -> allele.getDisplayString())
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

        List splitValues = genotypeAttribute instanceof List ? (List) genotypeAttribute : Arrays.asList(genotypeAttribute.toString().split(",|;"));

        if (type == VariantPropertyType.STRING_COLLECTION) {
            return new ArrayList<>(splitValues);
        }

        if (type == VariantPropertyType.NUMERIC_COLLECTION) {
            List<Double> parsed = (List<Double>) splitValues.stream()
                    .map((str) -> ".".equals(str.toString()) ? null : Double.parseDouble(str.toString()))
                    .collect(Collectors.toList());

            return parsed;
        }

        throw new IllegalStateException("unexpected type " + type + " when extracting genotype values");
    }

    private List<Object> extractCall(VariantContext context, String sample, List<String> columns, List<VariantPropertyType> types) {

        int numAttributes = context.getCommonInfo().getNumAttributes();

        List<Object> call = new ArrayList<>(columns.size());

        call.addAll(extractMandatoryFields(context, sample));
        call.addAll(extractRequiredVCFFields(context, sample));

        if (isSimple()) {
            return call;
        }

        int attributeLower = VariantTable.MANDATORY_FIELDS.length + VCF_REQUIRED_FIELDS.length;
        int attributeUpper = attributeLower + numAttributes;

        for (int i = attributeLower; i < attributeUpper; i++) {

            String attribute = columns.get(i);
            VariantPropertyType type = types.get(i);

            call.add(extractAttributeValues(context, sample, attribute, type, valuesDefinedPerSample.get(i)));

        }

        for (int i = attributeUpper; i < columns.size(); i++) {

            String columnName = columns.get(i);
            Genotype genotype = context.getGenotype(sample);

            if (columnName.equals("GT")) {

                String delimiter = genotype.isPhased() ? "|" : "/";

                String genotypeString = genotype.getAlleles().stream()
                        .map(allele -> allele.getDisplayString())
                        .collect(Collectors.joining(delimiter));

                call.add(genotypeString);

            } else {

                Object genotypeAttribute = genotype.getAnyAttribute(columnName);
                call.add(extractGenotypeValues(genotypeAttribute, types.get(i)));

            }

        }

        return call;
    }

    @Override
    public VariantTable readTable(String fileName) {
        VCFFileReader reader = new VCFFileReader(new File(fileName), false);
        VCFHeader header = reader.getFileHeader();

        List<String> newColumns = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        List<VariantPropertyType> newTypes = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));
        List<Boolean> newValuesDefinedPerSample = IntStream.range(0, VariantTable.MANDATORY_FIELDS.length)
                .boxed()
                .map(i -> false)
                .collect(Collectors.toList());

        this.columns = newColumns;
        this.prefixedColumns = new ArrayList<>();
        this.types = newTypes;
        this.valuesDefinedPerSample = newValuesDefinedPerSample;

        extractColumnsAndTypes(header);

        List<List<Object>> calls = new ArrayList<>();

        for (VariantContext context : reader) {

            for (String sample : context.getSampleNamesOrderedByName()) {

                boolean hasNonReferenceAlleles = context.getGenotype(sample).getAlleles().stream().anyMatch(allele -> allele.isNonReference());

                if (!hasNonReferenceAlleles && isExcludingNonReferenceCalls()) {
                    continue;
                }

                calls.add(extractCall(context, sample, columns, types));

            }

        }

        return new VariantTable(calls, prefixedColumns, types);

    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    public boolean isExcludingNonReferenceCalls() {
        return excludeNonReferenceCalls;
    }

    public void setExcludeNonReferenceCalls(boolean excludeNonReferenceCalls) {
        this.excludeNonReferenceCalls = excludeNonReferenceCalls;
    }

}
