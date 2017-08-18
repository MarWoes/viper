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
import com.google.gson.JsonObject;
import de.imi.marw.viper.util.Util;
import de.imi.marw.viper.variants.VariantCallFilter;
import de.imi.marw.viper.variants.VariantClusterBuilder;
import de.imi.marw.viper.variants.VariantPropertyType;
import de.imi.marw.viper.variants.VariantTableCluster;
import de.imi.marw.viper.variants.filters.FilterManager;
import de.imi.marw.viper.variants.filters.NumericCollectionFilter;
import de.imi.marw.viper.variants.filters.NumericFilter;
import de.imi.marw.viper.variants.filters.StringCollectionFilter;
import de.imi.marw.viper.variants.filters.StringFilter;
import de.imi.marw.viper.variants.table.CsvTableReader;
import de.imi.marw.viper.variants.table.VariantTable;
import de.imi.marw.viper.variants.table.ProgressManager;
import de.imi.marw.viper.visualization.IGVVisualizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

/**
 *
 * @author marius
 */
public class ViperServer {

    private final ViperServerConfig config;
    private final Gson gson;
    private final VariantClusterBuilder clusterer;
    private final ProgressManager progressManager;
    private final FilterManager filterManager;
    private VariantTableCluster variantTableCluster;

    private IGVVisualizer igv;

    public ViperServer(ViperServerConfig config) {

        this.config = config;
        this.gson = new Gson();
        this.clusterer = new VariantClusterBuilder();
        this.progressManager = new ProgressManager(config.getWorkDir());
        this.filterManager = new FilterManager();
    }

    public void start() {

        this.igv = this.setupIGV();
        this.igv.start();

        this.variantTableCluster = this.loadVariants();
        progressManager.loadProgress(this.variantTableCluster);
        filterManager.loadFromTable(this.variantTableCluster.getClusteredTable());

        this.igv.awaitStartup();

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
        port(config.getViperPort());

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

        put("/api/variant-table/decision/all", (req, res) -> {

            String decision = req.queryParams("decision");

            IntStream.range(0, variantTableCluster.getClusteredTable().getNumberOfCalls())
                    .forEach(i -> variantTableCluster.getClusteredTable().setCallProperty(i, VariantTable.DECISION_COLUMN_NAME, decision));

            return "OK";

        });

        put("/api/variant-table/decision", (req, res) -> {

            int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);
            String decision = req.queryParams("decision");

            variantTableCluster.getClusteredTable().setCallProperty(queryIndex, VariantTable.DECISION_COLUMN_NAME, decision);

            return "OK";
        });

        get("/api/variant-table/related-calls", (req, res) -> {

            int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);

            return variantTableCluster.getUnclusteredCalls(queryIndex);

        }, gson::toJson);

        get("/api/variant-table/related-calls/column-names", (req, res) -> variantTableCluster.getUnclusteredTable().getColumnNames(), gson::toJson);

        post("/api/variant-table/snapshot", this::takeSnapshot);
        get("/api/variant-table/is-snapshot-available", (req, res) -> {

            String key = req.queryParams("key");

            return igv.isSnapshotDone(key);

        }, gson::toJson);

        get("/api/variant-table/snapshot/:key", (req, res) -> {

            String key = req.params("key");

            res.raw().setContentType("image/png");

            File image = new File(this.config.getWorkDir() + "/" + key + ".png");

            try (
                    OutputStream out = res.raw().getOutputStream();
                    FileInputStream in = new FileInputStream(image);) {

                byte[] buf = new byte[1024];
                int count = 0;
                while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                }
            }

            return res.raw();

        });

        post("/api/variant-table/save", (req, res) -> {

            boolean success = progressManager.saveProgress(variantTableCluster);

            if (success) {
                return "OK";
            } else {
                res.status(500);
                return "ERROR";
            }
        });

        get("/api/variant-table/current-filters", (req, res) -> filterManager.getFilters(), gson::toJson);

        post("/api/variant-table/apply-filters", (req, res) -> {

            String body = req.body();

            List<VariantCallFilter> filters = parseFilters(body);
            filterManager.setFilters(filters);

            variantTableCluster.getClusteredTable().filter(filters);

            return "OK";
        });

        get("/api/variant-table/string-column-search", (req, res) -> {

            int limit = gson.fromJson(req.queryParams("limit"), Integer.class);
            String columnName = req.queryParams("columnName");
            String search = req.queryParams("search");

            return this.variantTableCluster.getClusteredTable().searchStringColumn(columnName, search, limit);

        }, gson::toJson);
    }

    private List<VariantCallFilter> parseFilters(String jsonFilterArray) {

        JsonObject[] rawObjects = gson.fromJson(jsonFilterArray, JsonObject[].class);

        List<VariantCallFilter> filters = new ArrayList<>();

        for (JsonObject rawObject : rawObjects) {

            VariantPropertyType type = VariantPropertyType.valueOf(rawObject.get("columnType").getAsString());

            VariantCallFilter filter = parseFilter(rawObject, type);

            filters.add(filter);
        }

        return filters;
    }

    private VariantCallFilter parseFilter(JsonObject rawObject, VariantPropertyType type) {

        switch (type) {
            case NUMERIC:
                return gson.fromJson(rawObject, NumericFilter.class);
            case NUMERIC_COLLECTION:
                return gson.fromJson(rawObject, NumericCollectionFilter.class);
            case STRING:
                return gson.fromJson(rawObject, StringFilter.class);
            case STRING_COLLECTION:
                return gson.fromJson(rawObject, StringCollectionFilter.class);
            default:
                throw new IllegalStateException("unrecognized filter type " + type);
        }
    }

    private Object takeSnapshot(Request req, Response res) {
        int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);
        int maxIndex = Util.clamp(queryIndex + 10, 0, variantTableCluster.getClusteredTable().getNumberOfCalls());

        for (int i = queryIndex; queryIndex < maxIndex; queryIndex++) {

            List<Map<String, Object>> relatedCalls = variantTableCluster.getUnclusteredCalls(queryIndex);

            Map<String, Object> firstCall = relatedCalls.get(0);

            String sample = firstCall.get(VariantTable.SAMPLE_COLUMN_NAME).toString();

            String chr1 = firstCall.get(VariantTable.CHR1_COLUMN_NAME).toString();
            String chr2 = firstCall.get(VariantTable.CHR2_COLUMN_NAME).toString();

            int bp1 = ((Double) firstCall.get(VariantTable.BP1_COLUMN_NAME)).intValue();
            int bp2 = ((Double) firstCall.get(VariantTable.BP2_COLUMN_NAME)).intValue();

            this.igv.scheduleSnapshot(sample, chr1, bp1);
            this.igv.scheduleSnapshot(sample, chr2, bp2);
        }

        return "OK";
    }

    private IGVVisualizer setupIGV() {
        new File(this.config.getWorkDir()).mkdirs();

        return new IGVVisualizer(this.config.getIgvJar(),
                this.config.getFastaRef(),
                this.config.getIgvPort(),
                this.config.getWorkDir(),
                this.config.getBamDir());
    }
}
