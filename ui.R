library(shiny)
library(shinyBS)

shiny.ui.checkboxGroupInput <- function (filterCol, caption, columnValues) {

  choices <- as.list(columnValues)
  choices <- setNames(choices, choices)

  uiElement <- checkboxGroupInput(paste(filterCol, "Filter", sep = ""),
           label = caption,
           choices  = choices,
           selected = choices,
           inline = TRUE)

  return(uiElement)
}

shiny.ui.rangeInput <- function (filterCol, caption, columnValues) {

  lower <- floor(min(columnValues))
  upper <- ceiling(max(columnValues))

  uiElement <- sliderInput(paste(filterCol, "Filter", sep = ""),
                           min = lower,
                           max = upper,
                           label = caption,
                           value = c(lower, upper))

  return(uiElement)
}

shiny.ui.selectizeInput <- function (filterCol, caption, columnValues) {

  uiElement <- selectizeInput(paste(filterCol, "Filter", sep = ""),
                              caption,
                              choices = columnValues,
                              multiple = TRUE,
                              options = list(
                                "plugins"          = list("remove_button"),
                                "openOnFocus"      = FALSE,
                                "closeAfterSelect" = TRUE
                              ))

  return(uiElement)
}

shiny.ui.filteringCriteriaInputs <- function () {

  filterUIElements <- lapply(names(shiny.global.filters), function (filterColumn) {

    filterInfo <- shiny.global.filters[[filterColumn]]

    if (is.null(filterInfo$values)) {
      columnValues <- na.omit(unique(shiny.global.clusteredData[[filterColumn]]))
    } else {
      columnValues <- filterInfo$values
    }

    uiElements <- switch(filterInfo$type,
                        checkboxes = shiny.ui.checkboxGroupInput(filterColumn, filterInfo$label, columnValues),
                        range      = shiny.ui.rangeInput(filterColumn, filterInfo$label, columnValues),
                        selectize  = shiny.ui.selectizeInput(filterColumn, filterInfo$label, columnValues))

    if (!is.null(filterInfo$includeNA) && filterInfo$includeNA) {
      naFilterInput <- checkboxInput(paste(filterColumn, "NAFilter", sep = ""), value = TRUE, label = em("Allow NA"))
      uiElements <- list(uiElements, naFilterInput)
    }

    return(uiElements)
  })

  return(filterUIElements)
}

# Define UI for dataset viewer application
shinyUI(navbarPage("IMI SV pipeline",
  ## INSPECTOR
  tabPanel("Inspector",
    sidebarLayout(
      sidebarPanel(
        tags$head(
          tags$link(rel = "stylesheet", type = "text/css", href = "libs/jquery-ui.css"),
          tags$link(rel = "stylesheet", type = "text/css", href = "libs/font-awesome.min.css"),
          tags$link(rel = "stylesheet", type = "text/css", href = "libs/igv-1.0.7.css"),
          tags$link(rel = "stylesheet", type = "text/css", href = "css/svBrowser.css"),
          tags$script(type = "text/javascript", src = "libs/es6-shim.js"),
          #tags$script(type = "text/javascript", src = "libs/jquery.min.js"),
          tags$script(type = "text/javascript", src = "libs/jquery-ui.min.js"),
          tags$script(type = "text/javascript", src = "libs/igv-1.0.7.min.js")
        ),
        uiOutput("svChoice"),

        fluidRow(
          column(4, bsButton("declineSV", label = "Decline", style = "danger",  block = TRUE)),
          column(4, bsButton("maybeSV", label = "Undecided", style = "warning", block = TRUE)),
          column(4, bsButton("approveSV", label = "Approve", style = "success", block = TRUE))
        ),

        textOutput("progress"),

        bsButton("saveSVs", label = "Save"),

        hr(),

        strong("Viewing sample: "), textOutput("currentSample", inline = TRUE),
        uiOutput("sampleChoice"),

        hr(),
        h5("SV details:"),
        tableOutput("currentSVRow")

      , width = 3),

      mainPanel(

        tabsetPanel(

          tabPanel("IGV BPs", fluidRow(

            column(6,
              h3("First breakpoint:"),
              div(imageOutput("bp1"), style = "width: 100%")
            ),
            column(6,
              h3("Second breakpoint:"),
              div(imageOutput("bp2"), style = "width: 100%")
            )
          )),

          tabPanel("Inconsistency Score",
                   plotOutput("inconsistency"),
                   fluidRow(
                     column(4, sliderInput("inconsistencySmoothRange", label = "Smoothing Range:", min = 1, max = 10, value = 2)),
                     column(4, selectInput("inconsistencySmoothFunction", label = "Smoothing Function:",
                                 choices = list("median", "mean"),
                                 selected = 1))
                   )
          ),

          tabPanel("Related Analysis Calls", tableOutput("relatedCalls")),

          tabPanel("IGV Browser", htmlOutput("igvBrowser"))
        )
      , width = 9)
    )
  ),
  tabPanel("Filtering",
           h3("Filtering Criteria:"),
           hr(),

           shiny.ui.filteringCriteriaInputs(),

           hr(),

           DT::dataTableOutput("filteredDataDT"),

           hr(),

           h4("Export as .xlsx file:"),

           bsButton("saveXLSX", label = "Save as XLSX", style = "success")
  )

))
