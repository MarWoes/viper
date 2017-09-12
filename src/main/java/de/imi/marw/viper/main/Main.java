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
package de.imi.marw.viper.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.imi.marw.viper.api.ViperServer;
import de.imi.marw.viper.api.ViperServerConfig;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String configFileName;

        if (args.length > 0) {
            configFileName = args[0];
        } else {
            configFileName = "config.json";
        }

        Gson gson = new GsonBuilder()
                .create();

        try (Reader reader = new FileReader(configFileName)) {

            ViperServerConfig config = gson.fromJson(reader, ViperServerConfig.class);

            ViperServer server = new ViperServer(config);
            server.start();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
