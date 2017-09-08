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
