library(shiny)
library(datasets)

source("util/math.R")

# BP may either be 'bp1' or 'bp2'
shiny.server.getBPImageFile <- function (serverValues, index, currentlySelectedSample, bp) {

  sample <- shiny.server.getCurrentlySelectedSample(serverValues, index, currentlySelectedSample)
  svId <- serverValues$filteredData[index, "id"]
  imageFile <- paste(sample, "png", sep = ".")

  filename <- paste(shiny.global.resultsDir, "visualization", svId , bp, imageFile, sep = "/")

  return(filename)
}

shiny.server.plotInconsistency <- function (serverValues, index, currentlySelectedSample, smoothingRange, smoothingFnName) {

  sample      <- shiny.server.getCurrentlySelectedSample(serverValues, index, currentlySelectedSample)
  mergeRegion <- shiny.server.getSingleCol(serverValues, index, "mergeRegion")
  bp1         <- shiny.server.getSingleCol(serverValues, index, "bp1")
  bp2         <- shiny.server.getSingleCol(serverValues, index, "bp2")

  txtFile <- paste(mergeRegion, "txt", sep = ".")

  filename <- paste(shiny.global.resultsDir, "read-depth", sample, txtFile, sep = "/")

  smoothingFn <- switch(smoothingFnName,
    "mean"   = mean,
    "median" = median
  )
  mergeRegionCoverage <- read.csv(filename, sep = ";", stringsAsFactors = FALSE)
  mergeRegionCoverage$cov <- math.smooth(mergeRegionCoverage$cov, smoothingRange, smoothingFn)

  visualization.plotCoverageInconsistency(mergeRegionCoverage, breakpoints = c(bp1, bp2))
}

shiny.server.getSingleCol <- function (serverValues, index, column) {

  relatedAnalysisCalls <- serverValues$filteredData[index, "relatedCalls"][[1]]

  value <- shiny.global.analysisData[relatedAnalysisCalls, column][1]

  return(value)
}

shiny.server.getAlignmentUrlString <- function (serverValues, index, currentlySelectedSample) {

  sample <- shiny.server.getCurrentlySelectedSample(serverValues, index, currentlySelectedSample)
  alignmentUrlString <- paste("alignment/", sample, ".bam", sep = "")

  return(paste("\"",URLencode(alignmentUrlString), "\"", sep = ""))
}

shiny.server.getLocusString <- function (serverValues, index) {

  chr <- shiny.server.getSingleCol(serverValues, index, "chr1")
  from <- shiny.server.getSingleCol(serverValues, index, "bp1") - 25
  to <- shiny.server.getSingleCol(serverValues, index, "bp1") + 25

  locus <- paste("\"chr", chr, ":", from, "-", to, "\"", sep = "")

  return(locus)
}

shiny.server.getGenomeReferencePath <- function () {
  basePath    <- paste("genomes", shiny.global.fastaRefBase, sep = "/")
  escapedPath <- paste("\"", basePath, "\"", sep = "")

  return(escapedPath)
}

shiny.server.getCurrentSVTable <- function (serverValues, index) {

  # We render the id choice with renderUI, so initially id might be NULL
  if (is.null(index)) return(util.emptyFrame(c("key", "value")))

  values <- serverValues$filteredData[index,]
  numericValues <- sapply(as.list(values), function (v) is.numeric(v))

  values[numericValues] <- round(values[numericValues], 3)
  values$relatedCalls <- paste(unlist(values$relatedCalls), collapse = ", ")

  rowTable <- data.frame(
    key = colnames(serverValues$filteredData),
    value = t(values))
  colnames(rowTable) <- c("key", "value")

  return(rowTable)
}

shiny.server.getRelatedCallTable <- function (serverValues, index) {

  relatedCalls <- unlist(serverValues$filteredData[index, "relatedCalls"])
  relatedCalls <- shiny.global.analysisData[relatedCalls,]
  colnames(relatedCalls) <- util.trimString(colnames(relatedCalls), 5)

  return(relatedCalls)
}

shiny.server.renderSampleChoice <- function (serverValues, index) {

  relatedCalls   <- unlist(serverValues$filteredData[index, "relatedCalls"])
  relatedSamples <- unique(shiny.global.analysisData[relatedCalls, "sample"])

  sliderInput("currentSampleIndex", label = "Sample:", value = 1, min = 1, max = length(relatedSamples), step = 1)
}

shiny.server.getCurrentlySelectedSample <- function (serverValues, index, currentlySelectedSample) {
  relatedCalls   <- unlist(serverValues$filteredData[index, "relatedCalls"])
  relatedSamples <- unique(shiny.global.analysisData[relatedCalls, "sample"])

  return(relatedSamples[currentlySelectedSample])
}

shiny.server.handleSVDecisionButtonClick <- function (serverValues, input, decision, session) {
  index <- input$svIndex

  selectedId <- serverValues$filteredData[index, "id"]
  serverValues$filteredData[index, "decision"] <- decision

  # This is ugly. Can we improve this?
  shiny.global.clusteredData[shiny.global.clusteredData$id == selectedId, "decision"] <<- decision

  serverValues$filteredData <- shiny.server.applyFilters(input)

  updateNumericInput(session, "svIndex",
                     label = "SV number",
                     value = math.clamp(index + 1, 1, nrow(serverValues$filteredData)),
                     min = 1,
                     max = nrow(serverValues$filteredData))
}

shiny.server.saveCallData <- function (unifiedData, fileName) {
  writtenData <- unifiedData
  writtenData$relatedCalls <- sapply(writtenData$relatedCalls, function (callIndices) paste(callIndices, collapse = ","))
  unification.write(writtenData, util.fileInDir(shiny.global.resultsDir, fileName), header = TRUE)
}

shiny.server.handleSVSaveButtonClick <- function (serverValues) {

  shiny.server.saveCallData(serverValues$filteredData,  "all_filtered.csv")
  shiny.server.saveCallData(shiny.global.clusteredData, "all_clustered.csv")

  showModal(modalDialog(title = "Info", "Your file was saved. You may now safely close the browser."))
}

shiny.server.getFilteredDataTable <- function (serverValues) {
  relevantFilteredData <- serverValues$filteredData
  relevantFilteredData$relatedCalls <- NULL
  datatable(relevantFilteredData, selection = "none", class = "compact") %>%
    formatRound(c("supporting", "cov1", "cov2", "qual1", "qual2", "maxBaseBalance", "control", "inconsistency"))
}

shiny.server.applyFilter <- function (filterInput, filterNAInput, filterColumn, filterInfo) {
  includeNA <- !is.null(filterNAInput) && filterNAInput

  naFilter <- includeNA & is.na(filterColumn)

  return(filterInfo$filterFn(filterColumn,filterInput) | naFilter )
}

shiny.server.applyFilters <- function (input) {

  unfilteredData <- shiny.global.clusteredData

  # Create a matrix with each row representing a call from the unfiltered data.
  # Every column is the result of a single filter being applied to a specific column
  filterMatrix <- sapply(names(shiny.global.filters), function (filterColumn) {

    filterInfo    <- shiny.global.filters[[filterColumn]]
    filterInput   <- input[[paste(filterColumn, "Filter", sep = "")]]
    filterNAInput <- input[[paste(filterColumn, "NAFilter", sep = "")]]

    return(shiny.server.applyFilter(filterInput, filterNAInput, unfilteredData[[filterColumn]], filterInfo))
  })

  filterResult <- apply(filterMatrix, 1, all)

  filteredData <- unfilteredData[filterResult,]

  return(filteredData)
}

shiny.server.handleXLSXExportClick <- function (serverValues) {
  filteredFile <- util.fileInDir(shiny.global.resultsDir, "all_filtered.csv")
  xlsxFile <- util.fileInDir(shiny.global.resultsDir, "all_filtered.xlsx")

  shiny.server.saveCallData(serverValues$filteredData, "all_filtered.csv")

  system(paste("python", "visualization/xlsx.py", filteredFile, shiny.global.analysisDataFile, xlsxFile))
  showModal(modalDialog(title = "Info", "Your file was saved."))
}

# Define server logic required to summarize and view the selected
# dataset
shinyServer(function(input, output, session) {

  serverValues <- reactiveValues(
    filteredData  = shiny.global.clusteredData
  )

  observe({
    serverValues$filteredData <- shiny.server.applyFilters(input)
  })

  output$svChoice <- renderUI({
    numericInput("svIndex",
                 label = "SV number",
                 value = 1,
                 min = 1,
                 max = nrow(serverValues$filteredData))
  })

  output$bp1  <- renderImage({
    list(src = shiny.server.getBPImageFile (serverValues, input$svIndex, input$currentSampleIndex, "BP1"),
         width = session$clientData$output_bp1_width)
  }, deleteFile = FALSE)

  output$bp2  <- renderImage({
    list(src = shiny.server.getBPImageFile (serverValues, input$svIndex, input$currentSampleIndex, "BP2"),
         width = session$clientData$output_bp2_width)
  }, deleteFile = FALSE)

  output$inconsistency <- renderPlot({
    shiny.server.plotInconsistency(serverValues, input$svIndex, input$currentSampleIndex, input$inconsistencySmoothRange, input$inconsistencySmoothFunction)
  })

  # Generate a summary of the dataset
  output$svId <- renderText({
    serverValues$filteredData[input$svIndex, "id"]
  })

  output$igvBrowser <- renderUI({
    htmlTemplate("html/igv.tpl.html",
                                      sample    = shiny.server.getAlignmentUrlString(serverValues, input$svIndex, input$currentSampleIndex),
                                      locus     = shiny.server.getLocusString(serverValues, input$svIndex),
                                      reference = shiny.server.getGenomeReferencePath())
    })

  output$progress <- renderText({
    paste("Progress:", input$svIndex, "/", nrow(serverValues$filteredData))
  })

  output$currentSVRow <- renderTable({
    shiny.server.getCurrentSVTable(serverValues, input$svIndex)
  }, bordered = TRUE, striped = TRUE, align = "lr")

  output$relatedCalls <- renderTable({
    shiny.server.getRelatedCallTable(serverValues, input$svIndex)
  }, html.table.attributes = "class=\"table table-sm table-striped table-bordered\" id=\"relatedAnalysisCallsTable\"")

  output$sampleChoice <- renderUI({
    shiny.server.renderSampleChoice(serverValues, input$svIndex)
  })

  output$currentSample <- renderText({
    shiny.server.getCurrentlySelectedSample(serverValues, input$svIndex, input$currentSampleIndex)
  })

  output$filteredDataDT <- DT::renderDataTable(shiny.server.getFilteredDataTable(serverValues))

  observeEvent(input$declineSV, { shiny.server.handleSVDecisionButtonClick(serverValues, input, "declined", session) })
  observeEvent(input$maybeSV,   { shiny.server.handleSVDecisionButtonClick(serverValues, input, "maybe"   , session) })
  observeEvent(input$approveSV, { shiny.server.handleSVDecisionButtonClick(serverValues, input, "approved", session) })
  observeEvent(input$saveSVs,   { shiny.server.handleSVSaveButtonClick(serverValues) })
  observeEvent(input$saveXLSX,  { shiny.server.handleXLSXExportClick(serverValues) })
})
