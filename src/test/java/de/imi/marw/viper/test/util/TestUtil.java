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
package de.imi.marw.viper.test.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marius
 */
public class TestUtil {

    public static String getResourceFile(String fileName) {
        // see https://stackoverflow.com/questions/24499692/access-resources-in-unit-tests
        try {
            URI uri = TestUtil.class.getClassLoader().getResource(fileName).toURI();
            return uri.getRawPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(TestUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
