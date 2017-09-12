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
package de.imi.marw.viper.api.routes;

import com.google.gson.Gson;
import de.imi.marw.viper.api.ViperServerConfig;
import de.imi.marw.viper.variants.VariantTableCluster;
import de.imi.marw.viper.variants.table.CsvTableWriter;
import de.imi.marw.viper.variants.table.XLSXWriter;
import java.util.function.BiConsumer;
import spark.Route;
import static spark.Spark.get;

/**
 *
 * @author marius
 */
public class ExportRoutes extends ViperRoutes {

    private final CsvTableWriter csvWriter;
    private final XLSXWriter xlsxWriter;

    public ExportRoutes(VariantTableCluster cluster, Gson gson, ViperServerConfig config) {
        super(cluster, gson, config);

        this.csvWriter = new CsvTableWriter(config.getCsvDelimiter(), config.getCollectionDelimiter());
        this.xlsxWriter = new XLSXWriter(config.getCollectionDelimiter(), config.getXslxExportWindowSize());
    }

    @Override
    public void addRoutes() {

        get("/clustered-csv", exportToFile("viper-all.csv", "text/csv", csvWriter::writeAllToCSV));
        get("/clustered-filtered-csv", exportToFile("viper-filtered.csv", "text/csv", csvWriter::writeFilteredToCSV));
        get("/clustered-xlsx", exportToFile("viper-all.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxWriter::writeAllToXSLX));
        get("/clustered-filtered-xlsx", exportToFile("viper-filtered.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxWriter::writeFilteredToXSLX));

    }

    private Route exportToFile(String fileName, String mimeType, BiConsumer<VariantTableCluster, String> exporter) {

        return (req, res) -> {
            String exportedFile = config.getWorkDir() + "/" + fileName;

            exporter.accept(this.variantTableCluster, exportedFile);

            res.raw().setHeader("Content-Disposition", "attachment; filename=" + fileName);

            serveFile(res.raw(), exportedFile, mimeType);

            return res.raw();
        };

    }

}
