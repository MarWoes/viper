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
import spark.Request;
import spark.Response;
import static spark.Spark.get;

/**
 *
 * @author marius
 */
public class TableRoutes extends ViperRoutes {

    public TableRoutes(VariantTableCluster cluster, Gson gson, ViperServerConfig config) {
        super(cluster, gson, config);
    }

    @Override
    public void addRoutes() {

        get("/size", this::getClusteredSize, gson::toJson);

        get("/unfiltered-size", this::getUnfilteredSize, gson::toJson);

        get("/row", this::getRow, gson::toJson);

        get("/rows", this::getRows, gson::toJson);

        get("/column-names", this::getColumnNames, gson::toJson);

        get("/related-calls", this::getRelatedCalls, gson::toJson);

        get("/related-calls/column-names", this::getRelatedColumnNames, gson::toJson);

        get("/string-column-search", this::searchColumns, gson::toJson);
    }

    private Object getClusteredSize(Request req, Response res) {
        return variantTableCluster.getClusteredTable().getNumberOfCalls();
    }

    private Object getUnfilteredSize(Request req, Response res) {
        return variantTableCluster.getClusteredTable().getRawCalls().size();
    }

    private Object getRow(Request req, Response res) {
        int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);

        return variantTableCluster.getClusteredTable().getCall(queryIndex);
    }

    private Object getRows(Request req, Response res) {
        int from = gson.fromJson(req.queryParams("from"), Integer.class);
        int to = gson.fromJson(req.queryParams("to"), Integer.class);

        return variantTableCluster.getClusteredTable().getCallRange(from, to);
    }

    private Object getColumnNames(Request req, Response res) {
        return variantTableCluster.getClusteredTable().getColumnNames();
    }

    private Object getRelatedCalls(Request req, Response res) {
        int queryIndex = gson.fromJson(req.queryParams("index"), Integer.class);

        return variantTableCluster.getUnclusteredCalls(queryIndex);
    }

    private Object getRelatedColumnNames(Request req, Response res) {
        return variantTableCluster.getUnclusteredTable().getColumnNames();
    }

    private Object searchColumns(Request req, Response res) {

        int limit = gson.fromJson(req.queryParams("limit"), Integer.class);
        String columnName = req.queryParams("columnName");
        String search = req.queryParams("search");

        return variantTableCluster.getClusteredTable().searchStringColumn(columnName, search, limit);

    }
}
