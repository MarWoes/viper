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
package de.imi.marw.viper.variants.table;

import de.imi.marw.viper.variants.VariantTableCluster;
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
public class ProgressManager {

    private final Path workDir;

    public ProgressManager(String workDir) {
        this.workDir = Paths.get(workDir);
    }

    public boolean saveProgress(VariantTableCluster cluster) {

        List<String> decisions = cluster.getClusteredTable().getUnfilteredColumn(VariantTable.DECISION_COLUMN_NAME).stream()
                .map(decision -> (String) decision)
                .collect(Collectors.toList());

        Path saveFilePath = getSavePath(cluster);

        try {
            Files.write(saveFilePath, decisions, Charset.forName("UTF-8"));
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ProgressManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void loadProgress(VariantTableCluster cluster) {

        Path saveFilePath = getSavePath(cluster);

        if (!saveFilePath.toFile().exists()) {
            return;
        }

        try {
            List<String> decisions = Files.readAllLines(saveFilePath, Charset.forName("UTF-8"));

            for (int i = 0; i < decisions.size(); i++) {
                cluster.getClusteredTable().setCallProperty(i, VariantTable.DECISION_COLUMN_NAME, decisions.get(i));
            }

        } catch (IOException ex) {
            Logger.getLogger(ProgressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Path getSavePath(VariantTableCluster cluster) {

        String[] bp1 = cluster.getClusteredTable().getUnfilteredColumn(VariantTable.BP1_COLUMN_NAME)
                .stream()
                .map(bp -> bp.toString())
                .toArray(String[]::new);

        int analysisHash = Arrays.hashCode(bp1);
        int size = cluster.getClusteredTable().getRawCalls().size();

        String fileName = "progress." + analysisHash + "." + size + ".txt";
        return workDir.resolve(fileName);
    }
}
