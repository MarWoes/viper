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
package de.imi.marw.viper.visualization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marius
 */
public class IGVVisualizer extends Thread {

    private final int viewRange;
    private final int xvfbDisplay;
    private final int xvfbWidth;
    private final int xvfbHeight;

    private final Map<String, Boolean> visualizationProgressMap;
    private final PriorityBlockingQueue<IGVCommand> commandQueue;
    private final int port;
    private final String fastaRef;
    private final String igvJar;
    private final String workDir;
    private final String bamDir;
    private Process igvProcess;
    private Process xvfbServer;
    private Socket client;

    public IGVVisualizer(String igvJar, String fastaRef, int port, String workDir, String bamDir, int viewRange, int xvfbDisplay, int xvfbWidth, int xvfbHeight) {
        this.port = port;
        this.fastaRef = fastaRef;
        this.igvJar = igvJar;
        this.commandQueue = new PriorityBlockingQueue<>(20, Comparator.reverseOrder());
        this.workDir = workDir;
        this.visualizationProgressMap = new ConcurrentHashMap<>();
        this.bamDir = bamDir;
        this.viewRange = viewRange;
        this.xvfbDisplay = xvfbDisplay;
        this.xvfbWidth = xvfbWidth;
        this.xvfbHeight = xvfbHeight;
    }

    @Override
    public void run() {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            startIGVProcess();
            this.client = connectToIGV();
            this.setupViewer();

            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            while (this.igvProcess != null && this.igvProcess.isAlive()) {

                IGVCommand nextCommand = this.commandQueue.poll(1, TimeUnit.SECONDS);

                if (nextCommand == null) {
                    continue;
                }

                Arrays.stream(nextCommand.getSubCommands()).forEach((String subCommand) -> {
                    out.println(subCommand);
                    try {
                        String response = in.readLine();
                    } catch (IOException ex) {
                        Logger.getLogger(IGVVisualizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });

                nextCommand.getFinishedCallback().run();
            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(IGVVisualizer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            shutdown();
        }
    }

    private void startIGVProcess() throws IOException {
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", this.igvJar,
                "-p", "" + port,
                "-g", this.fastaRef,
                "-o", "igv.properties")
                .inheritIO();

        if (isXvfbInstalled()) {
            ProcessBuilder xvfbBuilder = new ProcessBuilder("Xvfb",
                    ":" + xvfbDisplay,
                    "-screen", "0,", xvfbWidth + "x" + xvfbHeight + "x24")
                    .inheritIO();

            this.xvfbServer = xvfbBuilder.start();

            Map<String, String> igvEnv = builder.environment();
            igvEnv.put("DISPLAY", ":" + xvfbDisplay);
        }

        this.igvProcess = builder.start();

    }

    private Socket connectToIGV() {

        while (true) {
            try {
                Socket client = new Socket("127.0.0.1", port);
                return client;
            } catch (IOException ex) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(IGVVisualizer.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
    }

    public void awaitStartup() {
        while (this.client == null || !this.client.isConnected()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(IGVVisualizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean isSnapshotDone(String key) {
        return this.visualizationProgressMap.getOrDefault(key, false);
    }

    public void scheduleSnapshot(String sample, String chr, int bp, boolean isUrgent) {

        String key = sample + "-" + chr + "-" + bp;

        if (this.visualizationProgressMap.containsKey(key) && (!isUrgent || this.visualizationProgressMap.get(key))) {
            return;
        }

        this.visualizationProgressMap.put(key, false);

        Path workdir = Paths.get(this.workDir);
        String imageFileName = workdir.resolve(key + ".png").toString();

        Path bamDir = Paths.get(this.bamDir);
        String bamName = bamDir.resolve(sample + ".bam").toString();

        String[] subCommands = new String[]{
            "new",
            "load " + bamName,
            "collapse",
            "goto " + chr + ":" + (bp - viewRange) + "-" + (bp + viewRange),
            "snapshot " + imageFileName
        };

        IGVCommand command = new IGVCommand(key, subCommands, isUrgent, () -> this.visualizationProgressMap.put(key, true));

        if (isUrgent) {
            this.commandQueue.remove(command);
        }

        this.enqueueCommand(command);
    }

    public void enqueueCommand(IGVCommand command) {
        this.commandQueue.offer(command);
    }

    private void setupViewer() {
        this.enqueueCommand(new IGVCommand("setup", new String[]{"setSleepInterval 0"}, true, () -> {
        }));
    }

    public synchronized void shutdown() {
        if (this.client != null) {
            try {
                this.client.close();
                this.client = null;
            } catch (IOException ex) {
                Logger.getLogger(IGVVisualizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (this.igvProcess != null) {
            this.igvProcess.destroy();
            this.igvProcess = null;
        }

        if (this.xvfbServer != null) {
            this.xvfbServer.destroy();
            this.xvfbServer = null;
        }
    }

    private boolean isXvfbInstalled() {
        Runtime rt = Runtime.getRuntime();
        Process proc;
        try {
            proc = rt.exec("Xvfb -help");
            proc.waitFor();
            int exitVal = proc.exitValue();

            return exitVal == 0;
        } catch (IOException | InterruptedException ex) {
            return false;
        }
    }
}
