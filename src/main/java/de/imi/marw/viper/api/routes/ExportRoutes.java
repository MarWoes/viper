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
