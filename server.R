library(shiny)
library(datasets)

source("util/math.R")

# BP may either be 'bp1' or 'bp2'
viper.server.getBPImageFile <- function (serverValues, index, currentlySelectedSample, bp) {

  sample <- viper.server.getCurrentlySelectedSample(serverValues, index, currentlySelectedSample)
  svId <- serverValues$filteredData[index, "id"]
  imageFile <- paste(sample, "png", sep = ".")

  filename <- paste(viper.global.workDir, "visualization", svId , bp, imageFile, sep = "/")

  return(filename)
}

viper.server.getSingleCol <- function (serverValues, index, column) {

  relatedAnalysisCalls <- serverValues$filteredData[index, "relatedCalls"][[1]]

  value <- viper.global.analysisData[relatedAnalysisCalls, column][1]

  return(value)
}

viper.server.getAlignmentUrlString <- function (serverValues, index, currentlySelectedSample) {

  sample <- viper.server.getCurrentlySelectedSample(serverValues, index, currentlySelectedSample)
  alignmentUrlString <- paste("alignment/", sample, ".bam", sep = "")

  return(paste("\"",URLencode(alignmentUrlString), "\"", sep = ""))
}

viper.server.getLocusString <- function (serverValues, index) {

  chr <- viper.server.getSingleCol(serverValues, index, "chr1")
  from <- viper.server.getSingleCol(serverValues, index, "bp1") - 25
  to <- viper.server.getSingleCol(serverValues, index, "bp1") + 25

  locus <- paste("\"chr", chr, ":", from, "-", to, "\"", sep = "")

  return(locus)
}

viper.server.getGenomeReferencePath <- function () {
  basePath    <- paste("genomes", viper.global.fastaRefBase, sep = "/")
  escapedPath <- paste("\"", basePath, "\"", sep = "")

  return(escapedPath)
}

viper.server.getCurrentSVTable <- function (serverValues, index) {

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

viper.server.getRelatedCallTable <- function (serverValues, index) {

  relatedCalls <- unlist(serverValues$filteredData[index, "relatedCalls"])
  relatedCalls <- viper.global.analysisData[relatedCalls,]
  colnames(relatedCalls) <- util.trimString(colnames(relatedCalls), 5)

  return(relatedCalls)
}

viper.server.renderSampleChoice <- function (serverValues, index) {

  relatedCalls   <- unlist(serverValues$filteredData[index, "relatedCalls"])
  relatedSamples <- unique(viper.global.analysisData[relatedCalls, "sample"])

  sliderInput("currentSampleIndex", label = "Sample:", value = 1, min = 1, max = length(relatedSamples), step = 1)
}

viper.server.getCurrentlySelectedSample <- function (serverValues, index, currentlySelectedSample) {
  relatedCalls   <- unlist(serverValues$filteredData[index, "relatedCalls"])
  relatedSamples <- unique(viper.global.analysisData[relatedCalls, "sample"])

  return(relatedSamples[currentlySelectedSample])
}

viper.server.handleSVDecisionButtonClick <- function (serverValues, input, decision, session) {
  index <- input$svIndex

  selectedId <- serverValues$filteredData[index, "id"]
  serverValues$filteredData[index, "decision"] <- decision

  # This is ugly. Can we improve this?
  viper.global.clusteredData[viper.global.clusteredData$id == selectedId, "decision"] <<- decision

  serverValues$filteredData <- viper.server.applyFilters(input)

  updateNumericInput(session, "svIndex",
                     label = "SV number",
                     value = math.clamp(index + 1, 1, nrow(serverValues$filteredData)),
                     min = 1,
                     max = nrow(serverValues$filteredData))
}

viper.server.saveCallData <- function (unifiedData, fileName) {
  writtenData <- unifiedData
  writtenData$relatedCalls <- sapply(writtenData$relatedCalls, function (callIndices) paste(callIndices, collapse = ","))
  unification.write(writtenData, util.fileInDir(viper.global.workDir, fileName), header = TRUE)
}

viper.server.handleSVSaveButtonClick <- function (serverValues) {

  viper.server.saveCallData(serverValues$filteredData,  "all_filtered.csv")
  viper.server.saveCallData(viper.global.clusteredData, "all_clustered.csv")

  showModal(modalDialog(title = "Info", "Your file was saved. You may now safely close the browser."))
}

viper.server.getFilteredDataTable <- function (serverValues) {
  relevantFilteredData <- serverValues$filteredData
  relevantFilteredData$relatedCalls <- NULL
  datatable(relevantFilteredData, selection = "none", class = "compact") %>%
    formatRound(c("supporting", "cov1", "cov2", "qual1", "qual2", "maxBaseBalance", "control", "inconsistency"))
}

viper.server.applyFilter <- function (filterInput, filterNAInput, filterColumn, filterInfo) {
  includeNA <- !is.null(filterNAInput) && filterNAInput

  naFilter <- includeNA & is.na(filterColumn)

  return(filterInfo$filterFn(filterColumn,filterInput) | naFilter )
}

viper.server.applyFilters <- function (input) {

  unfilteredData <- viper.global.clusteredData

  # Create a matrix with each row representing a call from the unfiltered data.
  # Every column is the result of a single filter being applied to a specific column
  filterMatrix <- sapply(names(viper.global.filters), function (filterColumn) {

    filterInfo    <- viper.global.filters[[filterColumn]]
    filterInput   <- input[[paste(filterColumn, "Filter", sep = "")]]
    filterNAInput <- input[[paste(filterColumn, "NAFilter", sep = "")]]

    return(viper.server.applyFilter(filterInput, filterNAInput, unfilteredData[[filterColumn]], filterInfo))
  })

  filterResult <- apply(filterMatrix, 1, all)

  filteredData <- unfilteredData[filterResult,]

  return(filteredData)
}

viper.server.handleXLSXExportClick <- function (serverValues) {
  filteredFile <- util.fileInDir(viper.global.workDir, "all_filtered.csv")
  xlsxFile <- util.fileInDir(viper.global.workDir, "all_filtered.xlsx")

  viper.server.saveCallData(serverValues$filteredData, "all_filtered.csv")

  system(paste("python", "visualization/xlsx.py", filteredFile, viper.global.analysisDataFile, xlsxFile))
  showModal(modalDialog(title = "Info", "Your file was saved."))
}

# Define server logic required to summarize and view the selected
# dataset
shinyServer(function(input, output, session) {

  viper.global.igvWorker$start()
  viper.global.igvWorker$setupViewer()

  serverValues <- reactiveValues(
    filteredData  = viper.global.clusteredData
  )

  observe({
    serverValues$filteredData <- viper.server.applyFilters(input)
  })

  output$svChoice <- renderUI({
    numericInput("svIndex",
                 label = "SV number",
                 value = 1,
                 min = 1,
                 max = nrow(serverValues$filteredData))
  })

  output$bp1  <- renderImage({
    list(src = viper.server.getBPImageFile (serverValues, input$svIndex, input$currentSampleIndex, "BP1"),
         width = session$clientData$output_bp1_width)
  }, deleteFile = FALSE)

  output$bp2  <- renderImage({
    list(src = viper.server.getBPImageFile (serverValues, input$svIndex, input$currentSampleIndex, "BP2"),
         width = session$clientData$output_bp2_width)
  }, deleteFile = FALSE)

  # Generate a summary of the dataset
  output$svId <- renderText({
    serverValues$filteredData[input$svIndex, "id"]
  })

  output$igvBrowser <- renderUI({
    htmlTemplate("html/igv.tpl.html",
                                      sample    = viper.server.getAlignmentUrlString(serverValues, input$svIndex, input$currentSampleIndex),
                                      locus     = viper.server.getLocusString(serverValues, input$svIndex),
                                      reference = viper.server.getGenomeReferencePath())
    })

  output$progress <- renderText({
    paste("Progress:", input$svIndex, "/", nrow(serverValues$filteredData))
  })

  output$currentSVRow <- renderTable({
    viper.server.getCurrentSVTable(serverValues, input$svIndex)
  }, bordered = TRUE, striped = TRUE, align = "lr")

  output$relatedCalls <- renderTable({
    viper.server.getRelatedCallTable(serverValues, input$svIndex)
  }, html.table.attributes = "class=\"table table-sm table-striped table-bordered\" id=\"relatedAnalysisCallsTable\"")

  output$sampleChoice <- renderUI({
    viper.server.renderSampleChoice(serverValues, input$svIndex)
  })

  output$currentSample <- renderText({
    viper.server.getCurrentlySelectedSample(serverValues, input$svIndex, input$currentSampleIndex)
  })

  output$filteredDataDT <- DT::renderDataTable(viper.server.getFilteredDataTable(serverValues))

  observeEvent(input$declineSV, { viper.server.handleSVDecisionButtonClick(serverValues, input, "declined", session) })
  observeEvent(input$maybeSV,   { viper.server.handleSVDecisionButtonClick(serverValues, input, "maybe"   , session) })
  observeEvent(input$approveSV, { viper.server.handleSVDecisionButtonClick(serverValues, input, "approved", session) })
  observeEvent(input$saveSVs,   { viper.server.handleSVSaveButtonClick(serverValues) })
  observeEvent(input$saveXLSX,  { viper.server.handleXLSXExportClick(serverValues) })

  session$onSessionEnded(function (...) viper.global.igvWorker$stop())
})