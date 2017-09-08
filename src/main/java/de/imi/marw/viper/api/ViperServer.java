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
import de.imi.marw.viper.api.routes.DecisionRoutes;
import de.imi.marw.viper.api.routes.ExportRoutes;
import de.imi.marw.viper.api.routes.FilterRoutes;
import de.imi.marw.viper.api.routes.SnapshotRoutes;
import de.imi.marw.viper.api.routes.TableRoutes;
import de.imi.marw.viper.variants.VariantClusterBuilder;
import de.imi.marw.viper.variants.VariantTableCluster;
import de.imi.marw.viper.variants.table.VariantTable;
import de.imi.marw.viper.visualization.IGVVisualizer;
import java.io.File;
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
    private VariantTableCluster variantTableCluster;

    private IGVVisualizer igv;

    public ViperServer(ViperServerConfig config) {

        this.config = config;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.clusterer = new VariantClusterBuilder(config.getBreakpointTolerance(), !config.isClusteringEnabled());
    }

    public void start() {

        this.igv = this.setupIGV();
        this.igv.start();

        this.variantTableCluster = this.loadVariants();

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

        path("/api", () -> {

            path("/variant-table", new TableRoutes(variantTableCluster, gson, config));

            path("/snapshots", new SnapshotRoutes(igv, variantTableCluster, gson, config));

            path("/filters", new FilterRoutes(variantTableCluster, gson, config));

            path("/exports", new ExportRoutes(variantTableCluster, gson, config));

            path("/decisions", new DecisionRoutes(variantTableCluster, gson, config));

        });

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
