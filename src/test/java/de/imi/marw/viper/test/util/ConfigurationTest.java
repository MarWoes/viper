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

import de.imi.marw.viper.api.ViperServerConfig;
import de.imi.marw.viper.main.Main;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class ConfigurationTest {

    @Test
    public void correctlyLoadsConfig() throws IOException {

        ViperServerConfig defaultValues = new ViperServerConfig();
        ViperServerConfig testConfig = Main.loadConfig(TestUtil.getResourceFile("test-config.json"));

        // values that are present in .json file
        assertEquals("/my/spacy/tmp/dir", testConfig.getWorkDir());
        assertEquals(true, testConfig.isClusteringEnabled());
        assertEquals(3, testConfig.getBreakpointTolerance());

        // default values
        assertEquals(defaultValues.getAnalysisFile(), testConfig.getAnalysisFile());
        assertEquals(defaultValues.getNumPrecomputedSnapshots(), testConfig.getNumPrecomputedSnapshots());
        assertEquals(defaultValues.isKeepingVcfSimple(), testConfig.isKeepingVcfSimple());

    }

}
