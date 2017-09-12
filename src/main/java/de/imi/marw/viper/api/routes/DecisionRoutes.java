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
