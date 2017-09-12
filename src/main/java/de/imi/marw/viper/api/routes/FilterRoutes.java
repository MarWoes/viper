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
