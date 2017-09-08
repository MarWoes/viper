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
import de.imi.marw.viper.util.Util;
import de.imi.marw.viper.variants.VariantTableCluster;
import de.imi.marw.viper.variants.table.VariantTable;
import de.imi.marw.viper.visualization.IGVVisualizer;
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

    public SnapshotRoutes(IGVVisualizer igv, VariantTableCluster cluster, Gson gson, ViperServerConfig config) {
        super(cluster, gson, config);
        this.igv = igv;
    }

    @Override
    public void addRoutes() {

        post("/take-snapshot", this::takeSnapshot);

        get("/is-available", this::isAvailable, gson::toJson);

        get("/:key", this::getSnapshotByKey);
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
