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
package de.imi.marw.viper.test.visualization;

import de.imi.marw.viper.test.util.TestUtil;
import de.imi.marw.viper.visualization.SamplePartners;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author marius
 */
public class SamplePartnerTest {

    @Test
    public void emptyPartnersTest() throws IOException {

        SamplePartners partners = SamplePartners.loadFromCsv(TestUtil.getResourceFile("partners/empty.csv"), '\t');

        Assert.assertEquals(0, partners.getPartners("BATMAN").size());

    }

    @Test
    public void nonExistentFileTest() throws IOException {

        SamplePartners partners = SamplePartners.loadFromCsv("this-file-better-be-non-existent.csv", ',');

        assertEquals(0, partners.getPartners("Grinch").size());
    }

    @Test
    public void simplePartnerTest() throws IOException {

        SamplePartners partners = SamplePartners.loadFromCsv(TestUtil.getResourceFile("partners/some-simple-partners.csv"), ',');

        assertEquals(Arrays.asList("Robin"), partners.getPartners("Batman"));
        assertEquals(Arrays.asList("Iron Man"), partners.getPartners("Money"));
        assertEquals(Arrays.asList("Superman"), partners.getPartners("Kryptonite"));
        assertEquals(Arrays.asList("Elliot Alderson"), partners.getPartners("kali"));

        assertEquals(0, partners.getPartners("Grinch").size());
    }

    @Test
    public void multiplePartnersTest() throws IOException {

        SamplePartners partners = SamplePartners.loadFromCsv(TestUtil.getResourceFile("partners/multiple-partners.csv"), '\t');

        assertEquals(Arrays.asList("Donatello", "Michelangelo", "Raphael"), partners.getPartners("Leonardo"));
        assertEquals(Arrays.asList("Leonardo", "Michelangelo", "Raphael"), partners.getPartners("Donatello"));
        assertEquals(Arrays.asList("Leonardo", "Donatello", "Raphael"), partners.getPartners("Michelangelo"));
        assertEquals(Arrays.asList("Leonardo", "Donatello", "Michelangelo"), partners.getPartners("Raphael"));

        assertEquals(Arrays.asList("Dewey", "Louie"), partners.getPartners("Huey"));
        assertEquals(Arrays.asList("Huey", "Louie"), partners.getPartners("Dewey"));
        assertEquals(Arrays.asList("Huey", "Dewey"), partners.getPartners("Louie"));

        assertEquals(Arrays.asList("Cooper"), partners.getPartners("MUUUUUURPH"));
        assertEquals(Arrays.asList("MUUUUUURPH"), partners.getPartners("Cooper"));

        assertEquals(0, partners.getPartners("Batman").size());

        assertEquals(0, partners.getPartners("Grinch").size());

    }

}
