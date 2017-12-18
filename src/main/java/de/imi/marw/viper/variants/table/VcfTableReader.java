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
import htsjdk.variant.variantcontext.Allele;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author marius
 */
public class VcfTableReader implements TableReader {

    private List<String> columns;
    private List<String> prefixedColumns;
    private List<VariantPropertyType> types;
    private boolean simple;
    private boolean excludeReferenceCalls;

    public VcfTableReader(boolean simple, boolean excludeReferenceCalls) {
        this.simple = simple;
        this.excludeReferenceCalls = excludeReferenceCalls;
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
            extractColumnAndType(formatHeaderLine, "GT");
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

        if (altAlleles.isEmpty()) {
            vcfFieldValues.add(new ArrayList<>(Arrays.asList("NA")));
        } else {
            vcfFieldValues.add(altAlleles);
        }

        vcfFieldValues.add(context.getPhredScaledQual());

        Collection<String> filters = new ArrayList<>(context.getFilters());
        if (filters.isEmpty()) {
            filters = new ArrayList<>(Arrays.asList("PASS"));
        }

        vcfFieldValues.add(filters);

        return vcfFieldValues;
    }

    private Object extractAttributeValues(VariantContext context, String attribute, VariantPropertyType type) {

        List<String> values = context.getAttributeAsStringList(attribute, null);

        switch (type) {

            case NUMERIC: {
                return values.isEmpty() ? null : Double.parseDouble(values.get(0));
            }
            case NUMERIC_COLLECTION: {
                Collection<Double> coll = values.stream()
                        .map(Double::parseDouble)
                        .distinct()
                        .collect(Collectors.toList());

                return coll.isEmpty() ? new ArrayList<>(Arrays.asList((Object) null)) : coll;
            }
            case STRING: {
                return values.isEmpty() ? "NA" : values.get(0);
            }
            case STRING_COLLECTION: {
                return values == null || values.isEmpty() ? new ArrayList<>(Arrays.asList("NA")) : values.stream()
                        .distinct()
                        .collect(Collectors.toList());
            }

            default:
                throw new IllegalStateException("unexpected type " + type + " when extracting vcf attribute values");
        }

    }

    private Object extractGenotypeValues(Object genotypeAttribute, VariantPropertyType type) {

        Object defaultValue = null;

        switch (type) {
            case NUMERIC_COLLECTION:
                defaultValue = new ArrayList<>(Arrays.asList((Object) null));
                break;
            case STRING_COLLECTION:
                defaultValue = new ArrayList<>(Arrays.asList("NA"));
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
                    .map((str) -> (".".equals(str.toString()) || str.toString().isEmpty()) ? null : Double.parseDouble(str.toString()))
                    .distinct()
                    .collect(Collectors.toList());

            return parsed.isEmpty() ? new ArrayList<>(Arrays.asList((Object) null)) : parsed;
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

            call.add(extractAttributeValues(context, attribute, type));

        }

        for (int i = attributeUpper; i < columns.size(); i++) {

            String columnName = columns.get(i);
            Genotype genotype = context.getGenotype(sample);

            Object genotypeAttribute = genotype.getAnyAttribute(columnName);
            call.add(extractGenotypeValues(genotypeAttribute, types.get(i)));

        }

        return call;
    }

    @Override
    public VariantTable readTable(String fileName) {
        VCFFileReader reader = new VCFFileReader(new File(fileName), false);
        VCFHeader header = reader.getFileHeader();

        List<String> newColumns = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS));
        List<VariantPropertyType> newTypes = new ArrayList<>(Arrays.asList(VariantTable.MANDATORY_FIELDS_TYPES));

        this.columns = newColumns;
        this.prefixedColumns = new ArrayList<>();
        this.types = newTypes;

        extractColumnsAndTypes(header);

        List<List<Object>> calls = new ArrayList<>();

        for (VariantContext context : reader) {

            for (String sample : context.getSampleNamesOrderedByName()) {

                List<Allele> alleles = context.getGenotype(sample).getAlleles();

                boolean hasSampleGenotype = alleles.stream().anyMatch(allele -> allele.isCalled());
                boolean hasNonReferenceAlleles = alleles.stream().anyMatch(allele -> allele.isNonReference());

                if (!hasSampleGenotype || (!hasNonReferenceAlleles && isExcludingReferenceCalls())) {
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

    public boolean isExcludingReferenceCalls() {
        return excludeReferenceCalls;
    }

    public void setExcludeReferenceCalls(boolean excludeReferenceCalls) {
        this.excludeReferenceCalls = excludeReferenceCalls;
    }

}
