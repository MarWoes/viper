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
import de.imi.marw.viper.variants.table.DecisionManager;
import de.imi.marw.viper.variants.table.VariantTable;
import java.util.stream.IntStream;
import spark.Request;
import spark.Response;
import static spark.Spark.post;
import static spark.Spark.put;

/**
 *
 * @author marius
 */
public class DecisionRoutes extends ViperRoutes {

    private final DecisionManager decisionManager;

    public DecisionRoutes(VariantTableCluster cluster, Gson gson, ViperServerConfig config) {
        super(cluster, gson, config);

        this.decisionManager = new DecisionManager(config.getWorkDir());
        this.decisionManager.loadDecisions(cluster.getClusteredTable());
    }

    @Override
    public void addRoutes() {

        post("/save", this::saveDecisions);

        put("/all", this::decideForAll);

        put("/change", this::makeDecision);

    }

    private Object makeDecision(Request req, Response res) {
        int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);
        String decision = req.queryParams("decision");

        variantTableCluster.getClusteredTable().setCallProperty(queryIndex, VariantTable.DECISION_COLUMN_NAME, decision);

        return "OK";
    }

    private Object decideForAll(Request req, Response res) {
        String decision = req.queryParams("decision");

        IntStream.range(0, variantTableCluster.getClusteredTable().getNumberOfCalls())
                .forEach(i -> variantTableCluster.getClusteredTable().setCallProperty(i, VariantTable.DECISION_COLUMN_NAME, decision));

        return "OK";
    }

    private Object saveDecisions(Request req, Response res) {
        boolean success = decisionManager.saveDecisions(variantTableCluster.getClusteredTable());

        if (success) {
            return "OK";
        } else {
            res.status(500);
            return "ERROR";
        }
    }

}
