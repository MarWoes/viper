library("DT")
library("data.table")
library("digest")
library("subprocess")

source("util/util.R")
source("staticHandlingHack.R")
source("clustering.R")
source("igv/igv.R")

options(scipen = 10)

if (!exists("VIPER_ARGS")) stop("No viper config found. Make sure you feed VIPER_ARGS with correct values.")

# Is there a better way to do things?
config <- VIPER_ARGS

viper.global.workDir           <- config$workDir
viper.global.alignmentDir      <- config$alignmentDir
viper.global.fastaRef          <- config$fastaRef
viper.global.analysisDataFile  <- config$variantsFile
viper.global.maxScheduleSize   <- 200
viper.global.scheduleKeepIndex <- 100
viper.global.selectizeThreshhold <- 10

viper.global.fastaRefDir  <- dirname(viper.global.fastaRef)
viper.global.fastaRefBase <- basename(viper.global.fastaRef)

viper.global.xvfbWorker    <- spawn_process("/usr/bin/Xvfb", arguments = c(":4347", "-screen", "0,", "1280x1680x24"))
viper.global.igvWorker     <- viper.igv.RemoteIGV$new(config$igvJar, config$igvPort, config$fastaRef)
viper.global.igvWorker$startWorker(TRUE)

viper.global.analysisData  <- fread(viper.global.analysisDataFile, data.table = FALSE, stringsAsFactors = FALSE)
viper.global.clusteredData <- viper.clustering.clusterInput(viper.global.analysisData, 3)

viper.global.columnSep <- ","

viper.global.igvWorker$start()
viper.global.igvWorker$setupViewer()

viper.global.loadingImagePath <- "www/images/loading.svg"
viper.global.analysisHash <- digest(viper.global.analysisData$bp1)

viper.global.filters <- lapply(colnames(viper.global.analysisData), function (columnName) {

  columnValues <- viper.global.analysisData[[columnName]]

  if (all(is.na(columnValues))) return(NULL)

  containsNA <- any(is.na(columnValues))

  columnValues <- unique(na.omit(columnValues))

  label <- paste(columnName, ":", sep = "")

  filter <- list(
    includeNA = containsNA,
    label     = label
  )

  if (is.numeric(columnValues)) {

    filter$type     <- "range"
    filter$values   <- columnValues
    filter$filterFn <- util.isInInterval

  }

  if (is.character(columnValues)) {

    filter$values   <- unique(na.omit(unlist(strsplit(columnValues, viper.global.columnSep))))

    filter$type     <- ifelse(length(filter$values) > viper.global.selectizeThreshhold, "selectize", "checkboxes")

    filter$filterFn <- function (column, selected) {

      if (is.null(selected)) return(rep(TRUE, length(column)))

      splitValues <- strsplit(column, viper.global.columnSep)
      return(sapply(splitValues, function (vals) any(vals %in% selected)))
    }

  }

  return(filter)
})

names(viper.global.filters) <- colnames(viper.global.analysisData)
viper.global.filters$decision <- list(
  type      = "checkboxes",
  includeNA = TRUE,
  label     = "decision:",
  values    = c("maybe", "approved", "declined"),
  filterFn  = `%in%`
)
viper.global.filters <- viper.global.filters[!sapply(viper.global.filters, function (filter) is.null(filter))]

addResourcePath("alignment", viper.global.alignmentDir)
addResourcePath("genomes", viper.global.fastaRefDir)