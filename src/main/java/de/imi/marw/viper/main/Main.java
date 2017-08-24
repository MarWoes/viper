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
package de.imi.marw.viper.main;

import de.imi.marw.viper.api.ViperServer;
import de.imi.marw.viper.api.ViperServerConfig;
import java.io.IOException;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        ViperServerConfig config = new ViperServerConfig("../results-france1/all_analysis.csv");
        config.setWorkDir("/home/marius/wd");
        config.setFastaRef("/home/marius/workspace/sftp-share/Genomes/Homo_sapiens.GRCh37.67/Homo_sapiens.GRCh37.67.dna.chromosome.all.fasta");
        config.setBamDir("/home/marius/workspace/sftp-share/Analyses/Nijmegen_MDS_sequencing/MDS-Triage/Netherlands_illumina_1/alignment2/alignment");

        ViperServer server = new ViperServer(config);
        server.start();

    }
}
