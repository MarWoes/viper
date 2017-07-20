# Generate an IGV batch script from filtered sv calls
suppressPackageStartupMessages(library(Biostrings))
suppressPackageStartupMessages(library(R6))
suppressPackageStartupMessages(library(uuid))
suppressPackageStartupMessages(library(subprocess))
suppressPackageStartupMessages(library(svSocket))

viper.igv.RemoteIGV <-
  R6Class("viper.igv.RemoteIGV",

          public = list(

            initialize = function (igvJar, igvPort, fastaRef, xvfbDisplay = 4347) {

              private$igvJar   <- igvJar
              private$igvPort  <- igvPort
              private$fastaRef <- fastaRef
              private$xvfbDisplay <- xvfbDisplay

            },

            start = function () {

              if (!is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has already been established"))

              private$establishSocketConnection()
              private$running <- TRUE
            },

            stop = function () {

              if (is.null(private$igvSocket)) {

                warning("[WARNING] IGV socket connection has not been established.")

              } else {

                close(private$igvSocket)
                private$igvSocket <- NULL

              }

              private$running <- FALSE
            },

            isIdle = function () {
              return(all(sapply(private$commandQueue, function (pendingCommand) pendingCommand$finished)))
            },

            isRunning = function () {
              return(private$running)
            },

            sendCommands = function(commands, commandToken = UUIDgenerate()) {

              if (is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has not been established."))

              private$commandQueue[[commandToken]] <- list(

                finished = FALSE,
                commands = commands

              )

              return(commandToken)
            },

            processCommands = function () {

              if (is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has not been established."))
              if (self$isIdle()) return()

              isPending <- sapply(private$commandQueue, function (element) !element$finished)
              pendingKeys <- names(private$commandQueue)[isPending]

              for (key in pendingKeys) {

                commands <- private$commandQueue[[key]]$commands

                for (command in commands) {

                  if (!private$running) return()
                  writeLines(command, con = private$igvSocket)

                  while (private$running && length(readLines(con = private$igvSocket, n = 1)) == 0) {
                    Sys.sleep(0.01)
                  }
                }

                private$commandQueue[[key]]$finished <- TRUE
              }
            },

            updateCommandQueue = function () {
              if (length(private$commandQueue) == 0) return(character(0))

              isFinished <- sapply(private$commandQueue, function (element) element$finished)
              finishedCommandKeys <- names(private$commandQueue)[isFinished]

              for (key in finishedCommandKeys) {
                private$commandQueue[[key]] <- NULL
              }

              return(finishedCommandKeys)
            },

            setupViewer = function ()
              self$sendCommands(
                "setSleepInterval 0"
              ),

            snapshot = function (bamFile, snapshotFileName, chr, pos, snapshotKey = UUIDgenerate(), viewRange = 25) {

              self$sendCommands(c(
                      "new",
                paste("load", bamFile),
                      "collapse",
                paste("goto", sprintf("%s:%i-%i", chr, pos - viewRange, pos + viewRange)),
                paste("snapshot", snapshotFileName)
              ), snapshotKey)

            },

            startWorker = function (ignoreOutput = FALSE) {

              system(
                paste("export", sprintf("DISPLAY=:%i", private$xvfbDisplay),";",
                      "java", "-jar", private$igvJar,
                      "-p",   private$igvPort,
                      "-g",   private$fastaRef,
                      "-o",   "igv/igv.properties"),
                wait = FALSE
              )
            }
          ),

          private = list(
            igvPort   = NULL,
            igvSocket = NULL,
            igvJar    = NULL,
            fastaRef  = NULL,
            xvfbDisplay = NULL,
            running   = FALSE,

            commandQueue = list(),

            openSocket = function () {

              socket <- tryCatch({

                suppressWarnings(socketConnection(port = private$igvPort))

              },
              error = function (cond) NULL)

              return(socket)
            },

            establishSocketConnection = function (attempts = 120) {

              attempt <- 0

              while (attempt < attempts) {

                socket <- private$openSocket()

                if (!is.null(socket)) {

                  private$igvSocket <- socket
                  return(TRUE)

                } else {

                  Sys.sleep(1)
                  attempt <- attempt + 1

                }
              }

              return(FALSE)
            },

            numLinesWrittenByCommands = function (commands) {

              firstExitCommand <- which("exit" == commands)

              if (length(firstExitCommand) == 0) return(length(commands))

              return(firstExitCommand - 1)
            }
          )
  )

viper.igv.configDumpPath <- "/tmp/background.config.R"

viper.igv.startInBackground <- function () {

  # loads a config object
  source(viper.igv.configDumpPath)

  viper.igv.backgroundWorker <<- viper.igv.RemoteIGV$new(config$igvJar, config$igvPort, config$fastaRef)
  viper.igv.backgroundWorker$startWorker()
  viper.igv.backgroundWorker$start()
  viper.igv.backgroundWorker$setupViewer()

  startSocketServer(port = config$igvWorkerPort, local = TRUE)
  while(viper.igv.backgroundWorker$isRunning()) {
    viper.igv.backgroundWorker$processCommands()
    Sys.sleep(0.25)
  }
  q(save = "no")
}

# workaround because R's threading is non-existent ¯\_(ツ)_/¯
viper.igv.startAsBackgroundServer <- function(config, maxTries = 30) {

  dump("config", file = viper.igv.configDumpPath)

  system("Rscript -e \" source('igv/igv.R') ; viper.igv.startInBackground() \"", wait = FALSE)

  client <- NULL

  try <- 1
  while(is.null(client) && try <= maxTries) {
    client <- tryCatch({

      suppressWarnings(socketConnection(port = config$igvWorkerPort))

    },
    error = function (cond) NULL)

    try <- try + 1

    Sys.sleep(1)
  }

  return(client)
}