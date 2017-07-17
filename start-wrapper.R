library(shiny)

viper.startWrapper.startViper <- function () {

  VIPER_ARGS <<- list(
    workDir = "/mnt/home2/marw/results/",
    alignmentDir = "/mnt/home2/share/Analyses/Nijmegen_MDS_sequencing/MDS-Triage/Netherlands_illumina_1/alignment2/alignment/",
    fastaRef = "/mnt/home2/share/Genomes/Homo_sapiens.GRCh37.67/Homo_sapiens.GRCh37.67.dna.chromosome.all.fasta",
    igvJar = "/mnt/home2/marw/igv/igv.jar",
    variantsFile = "/mnt/home2/marw/results/all_analysis.csv",
    igvPort = 9090
  )

  on.exit({
    # remove all temporary image files
    system("rm /tmp/VAR*.png")

    # stop igv
    if (exists("viper.global.igvWorker")) viper.global.igvWorker$stop()

    # stop xvfb
    if (exists("viper.global.xvfbWorker")) process_terminate(viper.global.xvfbWorker)
  })
  runApp(port = 8099, launch.browser = FALSE)
}