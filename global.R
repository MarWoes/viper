library("DT")

source("util/util.R")
source("staticHandlingHack.R")

if (!exists("SHINY_INPUT")) stop("No shiny config found. Make sure you define SHINY_INPUT with correct values.")

# Is there a better way to do things?
config <- SHINY_INPUT

shiny.global.workDir   <- config$workDir
shiny.global.alignmentDir <- config$alignmentDir
shiny.global.fastaRef     <- config$fastaRef
shiny.global.analysisDataFile <- util.fileInDir(shiny.global.workDir, "all_analysis.csv")

shiny.global.fastaRefDir  <- dirname(shiny.global.fastaRef)
shiny.global.fastaRefBase <- basename(shiny.global.fastaRef)

shiny.global.analysisData  <- read.csv(shiny.global.analysisDataFile, sep = ";", stringsAsFactors = FALSE)
shiny.global.clusteredData <- read.csv(util.fileInDir(shiny.global.workDir, "all_clustered.csv"), sep = ";", stringsAsFactors = FALSE)
shiny.global.clusteredData$relatedCalls <- sapply(strsplit(as.character(shiny.global.clusteredData$relatedCalls), ","), as.integer)

shiny.global.filters <- list(
  tools = list(
    type      = "checkboxes",
    label     = "Tools:",
    filterFn  = function (column, selected) grepl(paste(selected, collapse = "|"), column),
    values    = unique(shiny.global.analysisData$tool)
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
  samples = list(
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
    values    = unique(na.omit(unlist(strsplit(shiny.global.analysisData$genes, ",")))),
    filterFn  = function (column, selected) is.null(selected) | grepl(paste(selected, collapse = "|"), column)
  )
)

addResourcePath("alignment", shiny.global.alignmentDir)
addResourcePath("genomes", shiny.global.fastaRefDir)