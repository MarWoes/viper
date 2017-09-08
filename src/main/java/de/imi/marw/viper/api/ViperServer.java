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
import com.google.gson.GsonBuilder;
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
import de.imi.marw.viper.variants.table.CsvTableWriter;
import de.imi.marw.viper.variants.table.VariantTable;
import de.imi.marw.viper.variants.table.DecisionManager;
import de.imi.marw.viper.variants.table.XLSXWriter;
import de.imi.marw.viper.visualization.IGVVisualizer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import javax.servlet.http.HttpServletResponse;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import static spark.Spark.*;

/**
 *
 * @author marius
 */
public class ViperServer {

    private final ViperServerConfig config;
    private final Gson gson;
    private final VariantClusterBuilder clusterer;
    private final DecisionManager progressManager;
    private final FilterManager filterManager;
    private VariantTableCluster variantTableCluster;
    private final CsvTableWriter csvWriter;
    private final XLSXWriter xlsxWriter;

    private IGVVisualizer igv;

    public ViperServer(ViperServerConfig config) {

        this.config = config;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.clusterer = new VariantClusterBuilder(config.getBreakpointTolerance(), !config.isClusteringEnabled());
        this.progressManager = new DecisionManager(config.getWorkDir());
        this.filterManager = new FilterManager();
        this.csvWriter = new CsvTableWriter(config.getCsvDelimiter(), config.getCollectionDelimiter());
        this.xlsxWriter = new XLSXWriter(config.getCollectionDelimiter(), config.getXslxExportWindowSize());
    }

    public void start() {

        this.igv = this.setupIGV();
        this.igv.start();

        this.variantTableCluster = this.loadVariants();
        progressManager.loadDecisions(this.variantTableCluster.getClusteredTable());
        filterManager.loadFromTable(this.variantTableCluster.getClusteredTable());

        this.igv.awaitStartup();

        this.setupRoutes();

        awaitInitialization();
    }

    public void stop() {
        this.igv.shutdown();
        Spark.stop();
    }

    public ViperServerConfig getConfig() {
        return this.config;
    }

    private VariantTableCluster loadVariants() {
        TableReaderMultiplexer reader = new TableReaderMultiplexer(config);

        VariantTable unclusteredTable = reader.readTable(config.getAnalysisFile());
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
        get("/api/variant-table/unfiltered-size", (req, res) -> variantTableCluster.getClusteredTable().getRawCalls().size(), gson::toJson);

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
            String fileName = this.config.getWorkDir() + "/" + key + ".png";

            serveFile(res.raw(), fileName, "image/png");

            return res.raw();

        });

        post("/api/variant-table/save", (req, res) -> {

            boolean success = progressManager.saveDecisions(variantTableCluster.getClusteredTable());

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

        get("/api/variant-table/export-clustered-csv", exportToFile("viper-all.csv", "text/csv", csvWriter::writeAllToCSV));
        get("/api/variant-table/export-clustered-filtered-csv", exportToFile("viper-filtered.csv", "text/csv", csvWriter::writeFilteredToCSV));
        get("/api/variant-table/export-clustered-xlsx", exportToFile("viper-all.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxWriter::writeAllToXSLX));
        get("/api/variant-table/export-clustered-filtered-xlsx", exportToFile("viper-filtered.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxWriter::writeFilteredToXSLX));
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

        int precomputeRange = config.getNumPrecomputedSnapshots();
        int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);
        int selectedRelatedCall = gson.fromJson(req.queryParams("relatedCallIndex"), Integer.class);
        int clusteredPrecomputeHigh = Util.clamp(queryIndex + precomputeRange, 0, variantTableCluster.getClusteredTable().getNumberOfCalls());

        for (int i = queryIndex; queryIndex < clusteredPrecomputeHigh; queryIndex++) {

            List<Map<String, Object>> relatedCalls = variantTableCluster.getUnclusteredCalls(queryIndex);

            int relatedPrecomputeHigh = Util.clamp(selectedRelatedCall + precomputeRange, 0, relatedCalls.size());
            for (int j = selectedRelatedCall; j < relatedPrecomputeHigh; j++) {

                Map<String, Object> relatedCall = relatedCalls.get(j);

                String sample = relatedCall.get(VariantTable.SAMPLE_COLUMN_NAME).toString();

                String chr1 = relatedCall.get(VariantTable.CHR1_COLUMN_NAME).toString();
                String chr2 = relatedCall.get(VariantTable.CHR2_COLUMN_NAME).toString();

                int bp1 = ((Double) relatedCall.get(VariantTable.BP1_COLUMN_NAME)).intValue();
                int bp2 = ((Double) relatedCall.get(VariantTable.BP2_COLUMN_NAME)).intValue();

                this.igv.scheduleSnapshot(sample, chr1, bp1, i == queryIndex && j == selectedRelatedCall);
                this.igv.scheduleSnapshot(sample, chr2, bp2, i == queryIndex && j == selectedRelatedCall);

            }
        }

        return "OK";
    }

    private void serveFile(HttpServletResponse res, String fileName, String contentType) throws IOException {

        res.setContentType(contentType);

        File image = new File(fileName);

        try (
                OutputStream out = res.getOutputStream();
                FileInputStream in = new FileInputStream(image);) {

            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
        }
    }

    private IGVVisualizer setupIGV() {
        new File(this.config.getWorkDir()).mkdirs();

        return new IGVVisualizer(this.config.getIgvJar(),
                this.config.getFastaRef(),
                this.config.getIgvPort(),
                this.config.getWorkDir(),
                this.config.getBamDir(),
                this.config.getViewRange(),
                this.config.getXvfbDisplay(),
                this.config.getXvfbWidth(),
                this.config.getXvfbHeight(),
                this.config.getIgvMaxMemory());
    }
}
