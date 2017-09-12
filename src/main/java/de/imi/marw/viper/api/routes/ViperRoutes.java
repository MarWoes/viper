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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import spark.RouteGroup;

/**
 *
 * @author marius
 */
public abstract class ViperRoutes implements RouteGroup {

    protected final VariantTableCluster variantTableCluster;
    protected final Gson gson;
    protected final ViperServerConfig config;

    public ViperRoutes(VariantTableCluster cluster, Gson gson, ViperServerConfig config) {
        this.variantTableCluster = cluster;
        this.gson = gson;
        this.config = config;
    }

    protected void serveFile(HttpServletResponse res, String fileName, String contentType) throws IOException {

        res.setContentType(contentType);

        File image = new File(fileName);

        try (
                OutputStream out = res.getOutputStream();
                FileInputStream in = new FileInputStream(image);) {

            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
        }
    }

}
