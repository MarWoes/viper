package de.imi.marw.main;

import static spark.Spark.*;


public class ViperMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        port(8090);

        staticFiles.externalLocation("public");

        init();
    }
}
