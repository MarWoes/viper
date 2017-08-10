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
import de.imi.marw.viper.variants.VariantClusterBuilder;
import de.imi.marw.viper.variants.VariantTableCluster;
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
    private final VariantClusterBuilder clusterer;
    private VariantTableCluster variantTableCluster;

    public ViperServer(ViperServerConfig config) {

        this.config = config;
        this.gson = new Gson();
        this.clusterer = new VariantClusterBuilder();
    }

    public void start() {

        this.variantTableCluster = this.loadVariants();

        this.setupRoutes();
    }

    private VariantTableCluster loadVariants() {
        CsvTableReader reader = new CsvTableReader(config.getAnalysisCsvFile(), config.getCsvDelimiter(), config.getPropertyCollectionDelimiter());

        VariantTable unclusteredTable = reader.readTable();
        VariantTableCluster cluster = clusterer.clusterVariantTable(unclusteredTable);

        return cluster;
    }

    private void setupRoutes() {
        ipAddress("127.0.0.1");
        port(config.getPortNumber());

        staticFiles.externalLocation("public");

        setupTableApi();

        init();
    }

    private void setupTableApi() {

        get("/api/variant-table/size", (req, res) -> variantTableCluster.getClusteredTable().getNumberOfCalls(), gson::toJson);

        get("/api/variant-table/row", (req, res) -> {

            int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);

            return variantTableCluster.getClusteredTable().getCall(queryIndex);

        }, gson::toJson);

        get("/api/variant-table/rows", (req, res) -> {

            int from = gson.fromJson(req.queryParams("from"), Integer.class);
            int to = gson.fromJson(req.queryParams("to"), Integer.class);

            return variantTableCluster.getClusteredTable().getCallRange(from, to);

        }, gson::toJson);

        get("/api/variant-table/column-names", (req, res) -> variantTableCluster.getClusteredTable().getColumnNames(), gson::toJson);

        get("/api/variant-table/related-calls", (req, res) -> {

            int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);

            return variantTableCluster.getUnclusteredCalls(queryIndex);

        }, gson::toJson);

        get("/api/variant-table/related-calls/column-names", (req, res) -> variantTableCluster.getUnclusteredTable().getColumnNames(), gson::toJson);
    }
}
