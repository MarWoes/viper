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
package de.imi.marw.viper.test.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.imi.marw.viper.api.ViperServer;
import de.imi.marw.viper.api.ViperServerConfig;
import de.imi.marw.viper.test.util.TestUtil;
import de.imi.marw.viper.variants.table.VariantTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author marius
 */
public class APITest {

    private static ViperServer testServer;
    private static Gson gson;

    private static final String URL_BASE = "http://localhost:13337";

    private String readFromFile(String testResourceFileName) throws IOException {

        // see https://stackoverflow.com/questions/3402735/what-is-simplest-way-to-read-a-file-into-string
        String content = new Scanner(new File(TestUtil.getResourceFile(testResourceFileName))).useDelimiter("\\Z").next();

        // ignore newlines for readability
        return content.replaceAll("\n", "");

    }

    @BeforeClass
    public static void setUp() {

        ViperServerConfig conf = new ViperServerConfig(TestUtil.getResourceFile("examples.csv"));
        conf.setViperPort(13337);
        conf.setIgvPort(13338);
        conf.setFastaRef(TestUtil.getResourceFile("api/mock-ref.fa"));

        testServer = new ViperServer(conf);
        testServer.start();

        gson = new GsonBuilder().create();

    }

    @AfterClass
    public static void tearDown() {
        testServer.stop();
    }

    @Test
    public void testPublicFolderAvailability() throws UnirestException {
        String content = Unirest.get(URL_BASE + "/index.html")
                .asString()
                .getBody();

        assertTrue(content.startsWith("<!DOCTYPE html>"));
    }

    @Test
    public void loadsCorrectNumberOfVariants() throws UnirestException {

        String response = Unirest.get(URL_BASE + "/api/variant-table/size")
                .asString()
                .getBody();

        assertEquals("90", response);

    }

    @Test
    public void correctlyLoadsRow() throws UnirestException, IOException {
        String response = Unirest.get(URL_BASE + "/api/variant-table/row")
                .queryString("index", "0")
                .asString()
                .getBody();

        String expectedJson = readFromFile("api/expected-first-row.json");

        assertEquals(expectedJson, response);
    }

    @Test
    public void correctlyLoadsRows() throws UnirestException, IOException {
        String response = Unirest.get(URL_BASE + "/api/variant-table/rows")
                .queryString("from", "1")
                .queryString("to", "4")
                .asString()
                .getBody();

        String expectedJson = readFromFile("api/expected-rows.json");

        assertEquals(expectedJson, response);
    }

    @Test
    public void correctlyLoadsColumnNames() throws UnirestException, IOException {

        String response = Unirest.get(URL_BASE + "/api/variant-table/column-names")
                .asString()
                .getBody();

        String expectedJson = readFromFile("api/expected-column-names.json");

        assertEquals(expectedJson, response);

    }

    private List<Map<String, String>> getCalls() throws UnirestException {
        String response = Unirest.get(URL_BASE + "/api/variant-table/rows")
                .queryString("from", "0")
                .queryString("to", "90")
                .asString()
                .getBody();

        List<Map<String, String>> calls = gson.fromJson(response, List.class);

        return calls;
    }

    private String findSaveFile() throws IOException {

        File workDir = new File(testServer.getConfig().getWorkDir());

        assertTrue(workDir.isDirectory());

        for (File listFile : workDir.listFiles()) {
            if (listFile.getName().endsWith(".txt")) {
                return listFile.getAbsolutePath();
            }
        }

        throw new IllegalStateException("no decision file found!");

    }

    @Test
    public void correctlyHandlesDecisionMaking() throws UnirestException, IOException {

        assertEquals("OK", Unirest.put(URL_BASE + "/api/decisions/change")
                .queryString("index", "69")
                .queryString("decision", "maybe")
                .asString()
                .getBody());

        List<Map<String, String>> calls = getCalls();

        assertEquals("OK", Unirest.post(URL_BASE + "/api/decisions/save")
                .asString()
                .getBody()
        );

        String saveFileName = findSaveFile();

        File saveFile = new File(saveFileName);

        List<String> savedDecisions = new ArrayList<>();

        Scanner sc = new Scanner(saveFile);

        while (sc.hasNextLine()) {
            savedDecisions.add(sc.nextLine());
        }

        saveFile.deleteOnExit();

        assertEquals(90, savedDecisions.size());

        for (int i = 0; i < 90; i++) {

            String savedDecision = savedDecisions.get(i);
            String expectedDecision = i == 69 ? "maybe" : "NA";
            String actualDecision = calls.get(i).get(VariantTable.DECISION_COLUMN_NAME);

            assertEquals(expectedDecision, actualDecision);
            assertEquals(expectedDecision, savedDecision);
        }

        assertEquals("OK", Unirest.put(URL_BASE + "/api/decisions/all")
                .queryString("decision", "approved")
                .asString()
                .getBody()
        );

        calls = getCalls();
        calls.forEach(call -> assertEquals("approved", call.get(VariantTable.DECISION_COLUMN_NAME)));

        assertEquals("OK", Unirest.put(URL_BASE + "/api/decisions/all")
                .queryString("decision", "NA")
                .asString()
                .getBody()
        );

        calls = getCalls();
        calls.forEach(call -> assertEquals("NA", call.get(VariantTable.DECISION_COLUMN_NAME)));
    }

    @Test
    public void correctlyLoadingRelatedCalls() throws IOException, UnirestException {

        String expectedRelatedColumnNames = readFromFile("api/expected-related-column-names.json");

        assertEquals(expectedRelatedColumnNames, Unirest.get(URL_BASE + "/api/variant-table/related-calls/column-names")
                .asString()
                .getBody()
        );

        String expectedRelatedCalls = readFromFile("api/expected-related-calls.json");

        String actualRelatedCalls = Unirest.get(URL_BASE + "/api/variant-table/related-calls")
                .queryString("index", "3")
                .asString()
                .getBody();

        assertEquals(expectedRelatedCalls, actualRelatedCalls);

    }

    private void waitUntilSnapshotFinished(String key) throws InterruptedException, UnirestException {
        while ("false".equals(Unirest.get(URL_BASE + "/api/snapshots/is-available")
                .queryString("key", key)
                .asString()
                .getBody())) {
            Thread.sleep(250);
        }
    }

    @Test(timeout = 30000)
    public void snapshotCorrectlyCreated() throws IOException, UnirestException, InterruptedException {

        String expectedKey = "SIM1-2-25459763";

        assertEquals("false", Unirest.get(URL_BASE + "/api/snapshots/is-available")
                .queryString("key", expectedKey)
                .asString()
                .getBody()
        );

        assertEquals("OK", Unirest.post(URL_BASE + "/api/snapshots/take-snapshot")
                .field("index", "2")
                .field("relatedCallIndex", "2")
                .asString()
                .getBody()
        );

        assertEquals("OK", Unirest.post(URL_BASE + "/api/snapshots/take-snapshot")
                .field("index", "3")
                .field("relatedCallIndex", "0")
                .asString()
                .getBody()
        );

        waitUntilSnapshotFinished(expectedKey);
    }

    @Test
    public void filtersAreAppliedCorrectly() throws IOException, UnirestException {

        String startFilters = Unirest.get(URL_BASE + "/api/filters/current")
                .asString()
                .getBody();

        String exampleDecisionFilter = readFromFile("api/example-filters.json");

        assertEquals("OK", Unirest.post(URL_BASE + "/api/filters/apply")
                .body(exampleDecisionFilter)
                .asString()
                .getBody()
        );

        assertEquals("1", Unirest.get(URL_BASE + "/api/variant-table/size")
                .asString()
                .getBody()
        );

        assertEquals("90", Unirest.get(URL_BASE + "/api/variant-table/unfiltered-size")
                .asString()
                .getBody()
        );

        assertEquals("OK", Unirest.post(URL_BASE + "/api/filters/apply")
                .body(startFilters)
                .asString()
                .getBody()
        );

        assertEquals("90", Unirest.get(URL_BASE + "/api/variant-table/size")
                .asString()
                .getBody()
        );

        assertEquals("90", Unirest.get(URL_BASE + "/api/variant-table/unfiltered-size")
                .asString()
                .getBody()
        );
    }

    @Test
    public void columnStringSearchWorkingCorrectly() throws IOException, UnirestException {

        String expectedColumnValues = readFromFile("api/expected-column-string-search.json");

        String actualColumnValues = Unirest.get(URL_BASE + "/api/variant-table/string-column-search")
                .queryString("limit", "3")
                .queryString("columnName", VariantTable.ID_COLUMN_NAME)
                .queryString("search", "VAR1")
                .asString()
                .getBody();

        assertEquals(expectedColumnValues, actualColumnValues);
    }
}
