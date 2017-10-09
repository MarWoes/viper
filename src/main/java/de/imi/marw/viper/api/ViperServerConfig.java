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
package de.imi.marw.viper.api;

/**
 *
 * @author marius
 */
public class ViperServerConfig {

    private String analysisFile = "/tmp/analysis/file";
    private int viperPort = 8090;
    private int igvPort = 9090;
    private char csvDelimiter = ';';
    private String collectionDelimiter = ",";
    private boolean enableGrouping = true;
    private int breakpointTolerance = 3;
    private String workDir = "/tmp/viper";
    private String fastaRef = "hg19";
    private String igvJar = "igv.jar";
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

    public ViperServerConfig() {
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
        return enableGrouping;
    }

    public void setEnableGrouping(boolean enableGrouping) {
        this.enableGrouping = enableGrouping;
    }

    @Override
    public String toString() {
        return "ViperServerConfig{" + "analysisFile=" + analysisFile + ", viperPort=" + viperPort + ", igvPort=" + igvPort + ", csvDelimiter=" + csvDelimiter + ", collectionDelimiter=" + collectionDelimiter + ", enableClustering=" + enableGrouping + ", breakpointTolerance=" + breakpointTolerance + ", workDir=" + workDir + ", fastaRef=" + fastaRef + ", igvJar=" + igvJar + ", bamDir=" + bamDir + ", keepVcfSimple=" + keepVcfSimple + ", excludeRefVcfCalls=" + excludeRefVcfCalls + ", numPrecomputedSnapshots=" + numPrecomputedSnapshots + ", xslxExportWindowSize=" + xslxExportWindowSize + ", viewRange=" + viewRange + ", xvfbDisplay=" + xvfbDisplay + ", xvfbWidth=" + xvfbWidth + ", xvfbHeight=" + xvfbHeight + ", igvMaxMemory=" + igvMaxMemory + '}';
    }

}
