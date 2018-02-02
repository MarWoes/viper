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
import de.imi.marw.viper.util.Util;
import de.imi.marw.viper.variants.VariantTableCluster;
import de.imi.marw.viper.variants.table.VariantTable;
import de.imi.marw.viper.visualization.IGVVisualizer;
import de.imi.marw.viper.visualization.SamplePartners;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 *
 * @author marius
 */
public class SnapshotRoutes extends ViperRoutes {

    private final IGVVisualizer igv;
    private final SamplePartners partners;

    public SnapshotRoutes(IGVVisualizer igv, VariantTableCluster cluster, Gson gson, ViperServerConfig config) {
        super(cluster, gson, config);
        this.igv = igv;
        this.partners = config.getPartnerFile() == null ? new SamplePartners() : SamplePartners.loadFromCsv(config.getPartnerFile(), config.getPartnerDelimiter());
    }

    @Override
    public void addRoutes() {

        post("/take-snapshot", this::takeSnapshot);

        get("/is-available", this::isAvailable, gson::toJson);

        get("/configuration", this::getIGVConfiguration, gson::toJson);

        get("/configuration-hash", this::getIGVConfigurationHash);

        get("/partners", this::getPartners, gson::toJson);

        post("/configuration", this::setIGVConfigurationValue);

        get("/:key", this::getSnapshotByKey);
    }

    private Object getPartners(Request req, Response res) {
        return partners.getMap();
    }

    private Object getIGVConfigurationHash(Request req, Response res) {
        return igv.getConfigurationHash();
    }

    private Object setIGVConfigurationValue(Request req, Response res) {

        String key = req.queryParams("key");
        Object value = gson.fromJson(req.queryParams("value"), Object.class);

        if (value instanceof Double) {
            value = ((Double) value).intValue();
        }

        return igv.setConfigurationValue(key, value);
    }

    private Object getIGVConfiguration(Request req, Response res) {
        return igv.getConfiguration();
    }

    private Object isAvailable(Request req, Response res) {
        String key = req.queryParams("key");

        return igv.isSnapshotDone(key);
    }

    private Object getSnapshotByKey(Request req, Response res) throws IOException {
        String key = req.params("key");
        String fileName = this.config.getWorkDir() + "/" + key + ".png";

        serveFile(res.raw(), fileName, "image/png");

        return res.raw();
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
}
