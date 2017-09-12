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
package de.imi.marw.viper.variants.table;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author marius
 */
public class DecisionManager {

    private final Path workDir;

    public DecisionManager(String workDir) {
        this.workDir = Paths.get(workDir);
    }

    public boolean saveDecisions(VariantTable table) {

        List<String> decisions = table.getUnfilteredColumn(VariantTable.DECISION_COLUMN_NAME).stream()
                .map(decision -> (String) decision)
                .collect(Collectors.toList());

        Path saveFilePath = getSavePath(table);

        try {
            Files.write(saveFilePath, decisions, Charset.forName("UTF-8"));
            return true;
        } catch (IOException ex) {
            Logger.getLogger(DecisionManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void loadDecisions(VariantTable table) {

        Path saveFilePath = getSavePath(table);

        if (!saveFilePath.toFile().exists()) {
            return;
        }

        try {
            List<String> decisions = Files.readAllLines(saveFilePath, Charset.forName("UTF-8"));

            for (int i = 0; i < decisions.size(); i++) {
                table.setCallProperty(i, VariantTable.DECISION_COLUMN_NAME, decisions.get(i));
            }

        } catch (IOException ex) {
            Logger.getLogger(DecisionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Path getSavePath(VariantTable table) {

        String[] bp1 = table.getUnfilteredColumn(VariantTable.BP1_COLUMN_NAME)
                .stream()
                .map(bp -> bp.toString())
                .toArray(String[]::new);

        int analysisHash = Arrays.hashCode(bp1);
        int size = table.getRawCalls().size();

        String fileName = "progress." + analysisHash + "." + size + ".txt";
        return workDir.resolve(fileName);
    }
}
