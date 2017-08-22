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
package de.imi.marw.viper.api;

/**
 *
 * @author marius
 */
public class ViperServerConfig {

    private String analysisCsvFile;
    private int viperPort = 8090;
    private int igvPort = 9090;
    private char csvDelimiter = ';';
    private String propertyCollectionDelimiter = ",";
    private String workDir = "/tmp/viper";
    private String fastaRef = "/tmp/ref.fa";
    private String igvJar = "../igv/igv.jar";
    private String bamDir = "/tmp/bam";
    private boolean keepVcfSimple = true;
    private boolean excludeNonRefVcfCalls = true;

    public ViperServerConfig(String csvFile) {
        this.analysisCsvFile = csvFile;
    }

    public char getCsvDelimiter() {
        return csvDelimiter;
    }

    public void setCsvDelimiter(char csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }

    public String getPropertyCollectionDelimiter() {
        return propertyCollectionDelimiter;
    }

    public void setPropertyCollectionDelimiter(String propertyCollectionDelimiter) {
        this.propertyCollectionDelimiter = propertyCollectionDelimiter;
    }

    public int getViperPort() {
        return viperPort;
    }

    public void setViperPort(int viperPort) {
        this.viperPort = viperPort;
    }

    public String getAnalysisCsvFile() {
        return analysisCsvFile;
    }

    public void setAnalysisCsvFile(String analysisCsvFile) {
        this.analysisCsvFile = analysisCsvFile;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getFastaRef() {
        return fastaRef;
    }

    public void setFastaRef(String fastaRef) {
        this.fastaRef = fastaRef;
    }

    public String getIgvJar() {
        return igvJar;
    }

    public void setIgvJar(String igvJar) {
        this.igvJar = igvJar;
    }

    public int getIgvPort() {
        return igvPort;
    }

    public void setIgvPort(int igvPort) {
        this.igvPort = igvPort;
    }

    public String getBamDir() {
        return bamDir;
    }

    public void setBamDir(String bamDir) {
        this.bamDir = bamDir;
    }

    public boolean isKeepingVcfSimple() {
        return keepVcfSimple;
    }

    public void setKeepVcfSimple(boolean keepVcfSimple) {
        this.keepVcfSimple = keepVcfSimple;
    }

    public boolean isExcludingNonRefVcfCalls() {
        return excludeNonRefVcfCalls;
    }

    public void setExcludeNonRefVcfCalls(boolean excludeNonRefVcfCalls) {
        this.excludeNonRefVcfCalls = excludeNonRefVcfCalls;
    }

}
