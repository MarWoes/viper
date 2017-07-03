library(shiny)
library(datasets)
library(uuid)

source("util/math.R")

viper.server.getAlignmentUrlString <- function (serverValues, currentlySelectedSample) {

  sample <- serverValues$currentAnalysisCalls$sample[currentlySelectedSample]
  alignmentUrlString <- paste("alignment/", sample, ".bam", sep = "")

  return(paste("\"",URLencode(alignmentUrlString), "\"", sep = ""))
}

viper.server.getLocusString <- function (serverValues) {

  viewRange <- 25

  chr  <- serverValues$currentFilteredCall$chr1
  pos  <- serverValues$currentFilteredCall$bp1
  from <- pos - viewRange
  to   <- pos + viewRange

  locus <- paste("\"chr", chr, ":", from, "-", to, "\"", sep = "")

  return(locus)
}

viper.server.getGenomeReferencePath <- function () {
  basePath    <- paste("genomes", viper.global.fastaRefBase, sep = "/")
  escapedPath <- paste("\"", basePath, "\"", sep = "")

  return(escapedPath)
}

viper.server.getCurrentVariantTable <- function (serverValues) {

  values <- serverValues$currentFilteredCall
  numericValues <- sapply(as.list(values), function (v) is.numeric(v))

  values[numericValues] <- round(values[numericValues], 3)
  values$relatedCalls <- paste(unlist(values$relatedCalls), collapse = ", ")

  rowTable <- data.frame(
    key = colnames(serverValues$filteredData),
    value = t(values))
  colnames(rowTable) <- c("key", "value")

  return(rowTable)
}

viper.server.getRelatedCallTable <- function (serverValues) {

  relatedCalls <- serverValues$currentAnalysisCalls
  colnames(relatedCalls) <- util.trimString(colnames(relatedCalls), 5)

  return(relatedCalls)
}

viper.server.renderSampleChoice <- function (serverValues) {

  relatedSamples <- unique(serverValues$currentAnalysisCalls$sample)

  sliderInput("sampleIndex", label = "Sample:", value = 1, min = 1, max = length(relatedSamples), step = 1)
}

viper.server.handleVariantDecisionButtonClick <- function (serverValues, input, decision, session) {

  index <- input$svIndex

  selectedId <- serverValues$currentFilteredCall$id

  # This is ugly. Can we improve this?
  serverValues$filteredData[index, "decision"] <- decision
  viper.global.clusteredData[viper.global.clusteredData$id == selectedId, "decision"] <<- decision

  preFilterSize <- nrow(serverValues$filteredData)

  serverValues$filteredData <- viper.server.applyFilters(input, viper.global.filters["decision"])

  postFilterSize <- nrow(serverValues$filteredData)

  svIndexShift <- ifelse(preFilterSize == postFilterSize, 1, 0)

  updateNumericInput(session, "svIndex",
                     label = "Variant number",
                     value = math.clamp(index + svIndexShift, 1, postFilterSize),
                     min = 1,
                     max = postFilterSize)
}

viper.server.saveCallData <- function (unifiedData, fileName) {
  writtenData <- unifiedData
  writtenData$relatedCalls <- sapply(writtenData$relatedCalls, function (callIndices) paste(callIndices, collapse = ","))
  write.table(writtenData, util.fileInDir(viper.global.workDir, fileName), row.names = FALSE)
}

viper.server.loadExistingDecisions <- function () {

  existingFile <- util.fileInDir(viper.global.workDir, paste("clustered", viper.global.analysisHash, "csv", sep = "."))

  if (!file.exists(existingFile)) return()

  decisions <- fread(existingFile)$decision

  viper.global.clusteredData$decision <<- decisions

  showModal(modalDialog(
    title = "Welcome back!",
    paste(sum(!is.na(decisions)), "decision(s) loaded from previous ratings.")
  ))
}

viper.server.handleVariantSaveButtonClick <- function (serverValues) {

  viper.server.saveCallData(serverValues$filteredData,  paste("filtered", viper.global.analysisHash, "csv", sep = "."))
  viper.server.saveCallData(viper.global.clusteredData, paste("clustered", viper.global.analysisHash, "csv", sep = "."))

  showModal(modalDialog(title = "Info", paste("Your files were saved. You may now safely close the browser.")))
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

viper.server.applyFilters <- function (input, filters = viper.global.filters) {

  unfilteredData <- viper.global.clusteredData

  # Create a matrix with each row representing a call from the unfiltered data.
  # Every column is the result of a single filter being applied to a specific column
  filterMatrix <- sapply(names(filters), function (filterColumn) {

    filterInfo    <- filters[[filterColumn]]
    filterInput   <- input[[paste(filterColumn, "Filter", sep = "")]]
    filterNAInput <- input[[paste(filterColumn, "NAFilter", sep = "")]]

    return(viper.server.applyFilter(filterInput, filterNAInput, unfilteredData[[filterColumn]], filterInfo))
  })

  filterResult <- apply(filterMatrix, 1, all)

  filteredData <- unfilteredData[filterResult,]

  return(filteredData)
}

# viper.server.handleXLSXExportClick <- function (serverValues) {
#   filteredFile <- util.fileInDir(viper.global.workDir, "all_filtered.csv")
#   xlsxFile <- util.fileInDir(viper.global.workDir, "all_filtered.xlsx")
#
#   viper.server.saveCallData(serverValues$filteredData, "all_filtered.csv")
#
#   system(paste("python", "visualization/xlsx.py", filteredFile, viper.global.analysisDataFile, xlsxFile))
#   showModal(modalDialog(title = "Info", "Your file was saved."))
# }

viper.server.getSnapshotKey <- function (id, sample, breakpointIndex) {

  snapshotKey <- paste(id, sample, breakpointIndex, sep = "-")

  return(snapshotKey)
}

viper.server.scheduleSnapshot <- function (serverValues, svIndex, sampleIndex, breakpointIndex) {

  if (is.null(svIndex)) return()

  id           <- serverValues$filteredData[svIndex,"id"]

  relatedCallsIndices <- unlist(serverValues$filteredData[svIndex, "relatedCalls"])
  relatedCalls <- viper.global.analysisData[relatedCallsIndices,]

  sample <- unique(relatedCalls$sample)[sampleIndex]

  # When changing current sv without changing the sample index first, a sample index is chosen that leads to an NA sample.
  # ih this case, no snapshot is scheduled.
  if (is.na(sample)) return()

  snapshotKey <- viper.server.getSnapshotKey(id, sample, breakpointIndex)

  if (snapshotKey %in% names(serverValues$schedule)) return()

  chr         <- relatedCalls[1, sprintf("chr%i", breakpointIndex)]
  pos         <- relatedCalls[1, sprintf("bp%i", breakpointIndex)]
  imageFile   <- sprintf("/tmp/%s.png", snapshotKey)

  serverValues$schedule[[snapshotKey]] <- list(
    file      = imageFile,
    complete  = FALSE
  )

  viper.global.igvWorker$snapshot(sprintf("%s/%s.bam", viper.global.alignmentDir, sample), imageFile, chr, pos, snapshotKey)

}

viper.server.scheduleSnapshots <- function (serverValues, svIndex, sampleIndex) {

  if (is.null(svIndex)) return()

  # TODO: replace by configuration
  snapshotsAhead <- 10
  startIndex <- math.clamp(svIndex, 1, nrow(serverValues$filteredData))
  endIndex <- math.clamp(startIndex + snapshotsAhead, startIndex, nrow(serverValues$filteredData))

  for (i in seq(startIndex, endIndex)) {
    # Only precompute images for the first sample index
    adjustedSampleIndex <- ifelse(i == startIndex, sampleIndex, 1)

    viper.server.scheduleSnapshot(serverValues, i, adjustedSampleIndex, 1)
    viper.server.scheduleSnapshot(serverValues, i, adjustedSampleIndex, 2)
  }
}

viper.server.getBreakpointImageFile <- function (serverValues, sampleIndex, breakpointIndex) {

  id <- serverValues$currentFilteredCall$id
  sample <- unique(serverValues$currentAnalysisCalls$sample)[sampleIndex]

  snapshotKey <- viper.server.getSnapshotKey(id, sample, breakpointIndex)
  scheduledSnapshot <- serverValues$schedule[[snapshotKey]]

  if (!is.null(scheduledSnapshot) && scheduledSnapshot$complete) {
    return(scheduledSnapshot$file)
  } else {
    return("www/images/loading.svg")
  }
}

viper.server.updateSnapshotStatus <- function (serverValues) {

  snapshotKeys <- viper.global.igvWorker$updatePendingCommands()

  for (snapshotKey in snapshotKeys) {

    if (!snapshotKey %in% names(serverValues$schedule)) next

    serverValues$schedule[[snapshotKey]]$complete <- TRUE
  }

  serverValues$igvIdle <- viper.global.igvWorker$isIdle()
}

viper.server.updateBreakpointImageFile <- function (serverValues, sampleIndex) {

  imageFile1 <- viper.server.getBreakpointImageFile(serverValues, sampleIndex, 1)
  imageFile2 <- viper.server.getBreakpointImageFile(serverValues, sampleIndex, 2)

  if (serverValues$breakpointImageFile1 != imageFile1) {
    serverValues$breakpointImageFile1 <- imageFile1
  }

  if (serverValues$breakpointImageFile2 != imageFile2) {
    serverValues$breakpointImageFile2 <- imageFile2
  }
}

viper.server.updateCurrentVariantSelection <- function (serverValues, svIndex) {
  if (is.null(svIndex)) return()

  serverValues$currentFilteredCall  <- serverValues$filteredData[svIndex,]
  serverValues$currentAnalysisCalls <- viper.global.analysisData[unlist(serverValues$currentFilteredCall$relatedCalls),]
}

viper.server.cleanup <- function (serverValues) {

  # close igv socket
  viper.global.igvWorker$stop()

  # remove all temporary image files
  system("rm /tmp/VAR*.png")
}

# Define server logic required to summarize and view the selected
# dataset
shinyServer(function(input, output, session) {

  viper.global.igvWorker$start()
  viper.global.igvWorker$setupViewer()

  viper.server.loadExistingDecisions()

  serverValues <- reactiveValues(

    filteredData       = viper.global.clusteredData,
    schedule           = list(),

    currentFilteredCall    = viper.global.clusteredData[1,],
    currentAnalysisCalls   = viper.global.analysisData[unlist(viper.global.clusteredData[1,"relatedCalls"]),],

    breakpointImageFile1 = viper.global.loadingImagePath,
    breakpointImageFile2 = viper.global.loadingImagePath,

    igvIdle = FALSE
  )

  output$svChoice <- renderUI({
    numericInput("svIndex",
                 label = "Variant number",
                 value = 1,
                 min = 1,
                 max = nrow(serverValues$filteredData))
  })

  output$bp1  <- renderImage({
    list(src = serverValues$breakpointImageFile1,
       width = session$clientData$output_bp1_width)
  }, deleteFile = FALSE)

  output$bp2  <- renderImage({
    list(src = serverValues$breakpointImageFile2,
         width = session$clientData$output_bp2_width)
  }, deleteFile = FALSE)

  # Generate a summary of the dataset

  output$igvBrowser <- renderUI({
    htmlTemplate("html/igv.tpl.html",
                                      sample    = viper.server.getAlignmentUrlString(serverValues, input$sampleIndex),
                                      locus     = viper.server.getLocusString(serverValues),
                                      reference = viper.server.getGenomeReferencePath())
    })

  output$progress <- renderText({ paste("Progress:", input$svIndex, "/", nrow(serverValues$filteredData)) })

  output$currentVariantRow <- renderTable({
    viper.server.getCurrentVariantTable(serverValues)
  }, bordered = TRUE, striped = TRUE, align = "lr")

  output$relatedCalls <- renderTable({
    viper.server.getRelatedCallTable(serverValues)
  }, html.table.attributes = "class=\"table table-sm table-striped table-bordered\" id=\"relatedAnalysisCallsTable\"")

  output$sampleChoice <- renderUI({ viper.server.renderSampleChoice(serverValues) })

  output$currentSample <- renderText({ unique(serverValues$currentAnalysisCalls$sample)[input$sampleIndex] })

  output$filteredDataDT <- DT::renderDataTable(viper.server.getFilteredDataTable(serverValues))
  output$igvIdleState <- renderUI({
    if (serverValues$igvIdle) {
      return(icon("ok", lib = "glyphicon"))
    } else {
      return(icon("refresh", lib = "glyphicon"))
    }
  })


  observeEvent(input$declineVariant, { viper.server.handleVariantDecisionButtonClick(serverValues, input, "declined", session) })
  observeEvent(input$maybeVariant,   { viper.server.handleVariantDecisionButtonClick(serverValues, input, "maybe"   , session) })
  observeEvent(input$approveVariant, { viper.server.handleVariantDecisionButtonClick(serverValues, input, "approved", session) })
  observeEvent(input$saveVariants,   { viper.server.handleVariantSaveButtonClick(serverValues) })
  # observeEvent(input$saveXLSX,  { viper.server.handleXLSXExportClick(serverValues) })

  observe({ serverValues$filteredData <- viper.server.applyFilters(input) })
  observe({ viper.server.updateCurrentVariantSelection(serverValues, input$svIndex)})
  observe({ viper.server.updateBreakpointImageFile(serverValues, input$sampleIndex) })
  observe({ viper.server.updateSnapshotStatus(serverValues); invalidateLater(1000)},          priority = -1)
  observe({ viper.server.scheduleSnapshots(serverValues, input$svIndex, input$sampleIndex) }, priority = -2)


  session$onSessionEnded(function (...) { viper.server.cleanup(serverValues) })

})
