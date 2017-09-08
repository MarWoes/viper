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
import com.google.gson.JsonObject;
import de.imi.marw.viper.api.ViperServerConfig;
import de.imi.marw.viper.variants.VariantCallFilter;
import de.imi.marw.viper.variants.VariantPropertyType;
import de.imi.marw.viper.variants.VariantTableCluster;
import de.imi.marw.viper.variants.filters.FilterManager;
import de.imi.marw.viper.variants.filters.NumericCollectionFilter;
import de.imi.marw.viper.variants.filters.NumericFilter;
import de.imi.marw.viper.variants.filters.StringCollectionFilter;
import de.imi.marw.viper.variants.filters.StringFilter;
import java.util.ArrayList;
import java.util.List;
import spark.Request;
import spark.Response;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 *
 * @author marius
 */
public class FilterRoutes extends ViperRoutes {

    private final FilterManager filterManager;

    public FilterRoutes(VariantTableCluster cluster, Gson gson, ViperServerConfig config) {

        super(cluster, gson, config);

        this.filterManager = new FilterManager();
        this.filterManager.loadFromTable(cluster.getClusteredTable());

    }

    @Override
    public void addRoutes() {

        get("/current", this::getCurrentFilters, gson::toJson);

        post("/apply", this::applyFilters);

    }

    private Object applyFilters(Request req, Response res) {
        String body = req.body();

        List<VariantCallFilter> filters = parseFilters(body);
        filterManager.setFilters(filters);

        variantTableCluster.getClusteredTable().filter(filters);

        return "OK";
    }

    private Object getCurrentFilters(Request req, Response res) {
        return filterManager.getFilters();
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

}
