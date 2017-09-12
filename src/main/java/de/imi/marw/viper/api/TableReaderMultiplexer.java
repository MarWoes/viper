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
