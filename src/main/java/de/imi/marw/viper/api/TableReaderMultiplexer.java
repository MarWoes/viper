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
package de.imi.marw.viper.api;

import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.TableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import de.imi.marw.viper.variants.table.VcfTableReader;

/**
 *
 * @author marius
 */
public class TableReaderMultiplexer implements TableReader {

    private final CsvTableReader csvReader;
    private final VcfTableReader vcfReader;

    public TableReaderMultiplexer(ViperServerConfig config) {
        this.csvReader = new CsvTableReader(config.getCsvDelimiter(), config.getCollectionDelimiter());
        this.vcfReader = new VcfTableReader(config.isKeepingVcfSimple(), config.isExcludingNonRefVcfCalls());
    }

    @Override
    public VariantTable readTable(String fileName) {

        if (fileName.endsWith(".vcf")) {
            return vcfReader.readTable(fileName);
        }

        return csvReader.readTable(fileName);

    }

}
