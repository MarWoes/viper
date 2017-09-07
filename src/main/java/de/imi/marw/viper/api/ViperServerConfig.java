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

    private String analysisFile;
    private int viperPort = 8090;
    private int igvPort = 9090;
    private char csvDelimiter = ';';
    private String collectionDelimiter = ",";
    private boolean enableClustering = true;
    private int breakpointTolerance = 3;
    private String workDir = "/tmp/viper";
    private String fastaRef = "/tmp/ref.fa";
    private String igvJar = "../igv/igv.jar";
    private String bamDir = "/tmp/bam";
    private boolean keepVcfSimple = true;
    private boolean excludeRefVcfCalls = true;
    private int numPrecomputedSnapshots = 10;
    private int xslxExportWindowSize = 1000;
    private int viewRange = 25;
    private int xvfbDisplay = 4499;
    private int xvfbWidth = 1280;
    private int xvfbHeight = 1680;
    private int igvMaxMemory = 1200;

    public ViperServerConfig(String csvFile) {
        this.analysisFile = csvFile;
    }

    public char getCsvDelimiter() {
        return csvDelimiter;
    }

    public void setCsvDelimiter(char csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }

    public String getCollectionDelimiter() {
        return collectionDelimiter;
    }

    public void setCollectionDelimiter(String collectionDelimiter) {
        this.collectionDelimiter = collectionDelimiter;
    }

    public int getViperPort() {
        return viperPort;
    }

    public void setViperPort(int viperPort) {
        this.viperPort = viperPort;
    }

    public String getAnalysisFile() {
        return analysisFile;
    }

    public void setAnalysisFile(String analysisFile) {
        this.analysisFile = analysisFile;
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
        return excludeRefVcfCalls;
    }

    public void setExcludeRefVcfCalls(boolean excludeRefVcfCalls) {
        this.excludeRefVcfCalls = excludeRefVcfCalls;
    }

    public int getNumPrecomputedSnapshots() {
        return numPrecomputedSnapshots;
    }

    public void setNumPrecomputedSnapshots(int numPrecomputedSnapshots) {
        this.numPrecomputedSnapshots = numPrecomputedSnapshots;
    }

    public int getXslxExportWindowSize() {
        return xslxExportWindowSize;
    }

    public void setXslxExportWindowSize(int xslxExportWindowSize) {
        this.xslxExportWindowSize = xslxExportWindowSize;
    }

    public int getViewRange() {
        return viewRange;
    }

    public void setViewRange(int viewRange) {
        this.viewRange = viewRange;
    }

    public int getXvfbDisplay() {
        return xvfbDisplay;
    }

    public void setXvfbDisplay(int xvfbDisplay) {
        this.xvfbDisplay = xvfbDisplay;
    }

    public int getXvfbWidth() {
        return xvfbWidth;
    }

    public void setXvfbWidth(int xvfbWidth) {
        this.xvfbWidth = xvfbWidth;
    }

    public int getXvfbHeight() {
        return xvfbHeight;
    }

    public void setXvfbHeight(int xvfbHeight) {
        this.xvfbHeight = xvfbHeight;
    }

    public int getBreakpointTolerance() {
        return breakpointTolerance;
    }

    public void setBreakpointTolerance(int breakpointTolerance) {
        this.breakpointTolerance = breakpointTolerance;
    }

    public int getIgvMaxMemory() {
        return this.igvMaxMemory;
    }

    public void setIgvMaxMemory(int igvMaxMemory) {
        this.igvMaxMemory = igvMaxMemory;
    }

    public boolean isClusteringEnabled() {
        return enableClustering;
    }

    public void setEnableClustering(boolean enableClustering) {
        this.enableClustering = enableClustering;
    }

}
