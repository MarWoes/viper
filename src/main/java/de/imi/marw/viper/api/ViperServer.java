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
import java.io.FileNotFoundException;
import java.io.IOException;
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
    private boolean sparkInitDone;

    private IGVVisualizer igv;

    public ViperServer(ViperServerConfig config) {

        this.config = config;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.clusterer = new VariantClusterBuilder(config.getBreakpointTolerance(), !config.isClusteringEnabled());
        this.sparkInitDone = false;
    }

    public void start() throws FileNotFoundException, IOException {

        System.out.println("[INFO] Loading variants...");

        this.variantTableCluster = this.loadVariants();

        System.out.println("[INFO] Loaded " + this.variantTableCluster.getUnclusteredTable().getNumberOfCalls() + " calls.");

        System.out.println("[INFO] Starting IGV on port " + this.config.getIgvPort() + ", logging to " + this.config.getIgvLog());
        this.igv = this.setupIGV();
        this.igv.start();

        this.igv.awaitStartup();
        System.out.println("[INFO] IGV started.");
        this.setupRoutes();

        awaitInitialization();
        System.out.println("[INFO] VIPER started, listening in port " + this.config.getViperPort());
    }

    public void stop() {

        if (this.igv != null) {
            this.igv.shutdown();
        }

        if (this.sparkInitDone) {
            Spark.stop();
        }

    }

    public ViperServerConfig getConfig() {
        return this.config;
    }

    private VariantTableCluster loadVariants() throws FileNotFoundException, IOException {
        TableReaderMultiplexer reader = new TableReaderMultiplexer(config);

        VariantTable unclusteredTable = reader.readTable(config.getAnalysisFile());
        VariantTableCluster cluster = clusterer.clusterVariantTable(unclusteredTable);

        return cluster;
    }

    private void setupRoutes() {

        // Setting Jetty logger implementation and level (DEBUG | INFO | WARN | IGNORE)
        System.setProperty("org.eclipse.jetty.util.log.class",
                "org.eclipse.jetty.util.log.JavaUtilLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "WARN");

        if (Files.exists(Paths.get("public"))) {
            staticFiles.externalLocation("public");
        } else {
            staticFiles.location("public");
        }

        ipAddress("127.0.0.1");
        port(config.getViperPort());

        setupTableApi();

        init();

        this.sparkInitDone = true;
    }

    private void setupTableApi() {

        path("/api", () -> {

            get("/version", (req, res) -> getViperVersion());

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
                this.config.getIgvLog(),
                this.config.getXvfbDisplay(),
                this.config.getXvfbWidth(),
                this.config.getXvfbHeight(),
                this.config.getIgvMaxMemory(),
                this.config.getSleepInterval()
        );
    }

    private String getViperVersion() {

        String version = getClass().getPackage().getImplementationVersion();

        return version == null ? "DEV" : version;
    }
}
