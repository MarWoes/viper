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
import java.nio.file.Files;
import java.nio.file.Paths;
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

        if (Files.exists(Paths.get("public"))) {
            staticFiles.externalLocation("public");
        } else {
            staticFiles.location("public");
        }

        ipAddress("127.0.0.1");
        port(config.getViperPort());

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
