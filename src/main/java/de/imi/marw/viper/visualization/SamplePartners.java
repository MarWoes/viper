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
package de.imi.marw.viper.visualization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author marius
 */
public class SamplePartners {

    private static final List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

    private final Map<String, List<String>> samplePartnerMap;

    public SamplePartners() {
        this.samplePartnerMap = new HashMap<>();
    }

    private SamplePartners(Map<String, List<String>> samplePartnerMap) {

        this.samplePartnerMap = samplePartnerMap;
    }

    public Map<String, List<String>> getMap() {
        return Collections.unmodifiableMap(samplePartnerMap);
    }

    public List<String> getPartners(String sample) {

        if (!this.samplePartnerMap.containsKey(sample)) {
            return EMPTY_LIST;
        }

        return samplePartnerMap.get(sample);

    }

    private static Map<String, List<String>> getPartnersFromLines(List<List<String>> lines) {

        Map<String, List<String>> partnerMap = new HashMap<>();

        for (List<String> line : lines) {

            for (String sample : line) {

                if (!partnerMap.containsKey(sample)) {
                    partnerMap.put(sample, new ArrayList<>());
                }

                List<String> existingSamples = partnerMap.get(sample);

                for (String otherSample : line) {

                    if (!otherSample.equals(sample) && !existingSamples.contains(otherSample)) {
                        existingSamples.add(otherSample);
                    }

                }
            }

        }

        return partnerMap;
    }

    public static SamplePartners loadFromCsv(String fileName, char delimiter) {

        CSVFormat csvFormat = CSVFormat.RFC4180
                .withDelimiter(delimiter);

        try (Reader rd = new FileReader(new File(fileName))) {

            Iterable<CSVRecord> records = csvFormat.parse(rd);

            List<List<String>> lines = StreamSupport.stream(records.spliterator(), false)
                    .map(record -> StreamSupport.stream(record.spliterator(), false).collect(Collectors.toList()))
                    .collect(Collectors.toList());

            Map<String, List<String>> samplePartnerMap = getPartnersFromLines(lines);

            return new SamplePartners(samplePartnerMap);

        } catch (FileNotFoundException ex) {
            System.err.println("[WARNING] File " + fileName + " not found, partnering is ignored.");
            return new SamplePartners();
        } catch (IOException ex) {
            System.err.println("[ERROR] Error reading file " + fileName + ":");
            Logger.getLogger(SamplePartners.class.getName()).log(Level.SEVERE, null, ex);
            return new SamplePartners();
        }

    }
}
