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

import com.google.gson.Gson;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;

import static spark.Spark.*;

/**
 *
 * @author marius
 */
public class ViperServer {

    private final ViperServerConfig config;
    private final Gson gson;
    private VariantTable variantTable;

    public ViperServer(ViperServerConfig config) {

        this.config = config;
        this.gson = new Gson();

    }

    public void start() {

        this.variantTable = this.loadVariants();

        this.setupRoutes();
    }

    private VariantTable loadVariants() {
        CsvTableReader reader = new CsvTableReader(config.getAnalysisCsvFile(), config.getCsvDelimiter(), config.getPropertyCollectionDelimiter());

        return (reader.readTable());
    }

    private void setupRoutes() {
        ipAddress("127.0.0.1");
        port(config.getPortNumber());

        staticFiles.externalLocation("public");

        setupTableApi();

        init();
    }

    private void setupTableApi() {

        get("/api/variant-table/size", (req, res) -> variantTable.getNumberOfCalls(), gson::toJson);

        get("/api/variant-table/row", (req, res) -> {

            int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);

            return variantTable.getCall(queryIndex);

        }, gson::toJson);

        get("/api/variant-table/rows", (req, res) -> {

            int from = gson.fromJson(req.queryParams("from"), Integer.class);
            int to = gson.fromJson(req.queryParams("to"), Integer.class);

            return variantTable.getCallRange(from, to);

        }, gson::toJson);

        get("/api/variant-table/column-names", (req, res) -> variantTable.getColumnNames(), gson::toJson);
    }

}
