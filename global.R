library("DT")
library("data.table")
library("digest")

source("util/util.R")
source("staticHandlingHack.R")
source("clustering.R")
source("igv/igv.R")

options(scipen = 10)

if (!exists("VIPER_ARGS")) stop("No viper config found. Make sure you feed VIPER_ARGS with correct values.")

# Is there a better way to do things?
config <- VIPER_ARGS

viper.global.workDir          <- config$workDir
viper.global.alignmentDir     <- config$alignmentDir
viper.global.fastaRef         <- config$fastaRef
viper.global.analysisDataFile <- config$variantsFile

viper.global.fastaRefDir  <- dirname(viper.global.fastaRef)
viper.global.fastaRefBase <- basename(viper.global.fastaRef)

viper.global.analysisData  <- fread(viper.global.analysisDataFile, data.table = FALSE, stringsAsFactors = FALSE)
viper.global.clusteredData <- viper.clustering.clusterInput(viper.global.analysisData, 2)

viper.global.igvWorker     <- viper.igv.RemoteIGV$new(config$igvPort)

viper.global.loadingImagePath <- "www/images/loading.svg"
viper.global.analysisHash <- digest(viper.global.analysisData$bp1)

# Wait for server to start
viper.global.igvWorker$start()
viper.global.igvWorker$stop()

viper.global.filters <- list(
  tool = list(
    type      = "checkboxes",
    label     = "Tools:",
    filterFn  = function (column, selected) grepl(paste(selected, collapse = "|"), column),
    values    = unique(viper.global.analysisData$tool)
  ),
  svType = list(
    type      = "checkboxes",
    label     = "Types:",
    filterFn  = `%in%`
  ),
  decision = list(
    type      = "checkboxes",
    includeNA = TRUE,
    label     = "Decision:",
    values    = c("maybe", "approved", "declined"),
    filterFn  = `%in%`
  ),
  supporting = list(
    type      = "range",
    includeNA = TRUE,
    label     = "Supporting:",
    filterFn  = util.isInInterval
  ),
  maxBaseBalance = list(
    type      = "range",
    label     = "Max Base balance:",
    filterFn  = util.isInInterval
  ),
  numCalls = list(
    type      = "range",
    label     = "No. Samples:",
    filterFn  = util.isInInterval
  ),
  cov1 = list(
    type   = "range",
    label  = "Coverage (bp1):" ,
    filterFn = util.isInInterval
  ),
  cov2 = list(
    type   = "range",
    label  = "Coverage (bp2):" ,
    filterFn = util.isInInterval
  ),
  genes = list(
    type      = "selectize",
    label     = "Genes:",
    includeNA = TRUE,
    values    = unique(na.omit(unlist(strsplit(viper.global.analysisData$genes, ",")))),
    filterFn  = function (column, selected) is.null(selected) | grepl(paste(selected, collapse = "|"), column)
  )
)

addResourcePath("alignment", viper.global.alignmentDir)
addResourcePath("genomes", viper.global.fastaRefDir)