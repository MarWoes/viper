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
