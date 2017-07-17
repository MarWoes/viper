# Generate an IGV batch script from filtered sv calls
suppressPackageStartupMessages(library(Biostrings))
suppressPackageStartupMessages(library(R6))
suppressPackageStartupMessages(library(uuid))
suppressPackageStartupMessages(library(subprocess))

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

              private$startWorker()
              private$establishSocketConnection()
            },

            stop = function () {

              if (is.null(private$igvSocket)) {

                warning("[WARNING] IGV socket connection has not been established.")

              } else {

                close(private$igvSocket)
                private$igvSocket <- NULL

              }

              if (is.null(private$igvHandle)) {

                warning("[WARNING] IGV process has not been started.")

              } else {

                process_terminate(private$igvHandle)
                private$igvHandle <- NULL

              }
            },

            isIdle = function () {
              return (length(private$pendingCommands) == 0)
            },

            updatePendingCommands = function () {

              if (self$isIdle()) return(character(0))

              cat(process_read(private$igvHandle)$stderr)

              igvResponse <- readLines(con = private$igvSocket)
              commandsExecuted <- length(igvResponse)

              completedCommands <- character(0)

              while(commandsExecuted > 0) {

                commandsConsumed <- min(commandsExecuted, private$pendingCommands[[1]])
                private$pendingCommands[[1]] <- private$pendingCommands[[1]] - commandsConsumed

                commandsExecuted <- commandsExecuted - commandsConsumed

                if (private$pendingCommands[[1]] == 0) {

                  completedCommands <- c(completedCommands, names(private$pendingCommands)[1])
                  private$pendingCommands[[1]] <- NULL

                }
              }

              return(completedCommands)
            },

            sendCommands = function(commands, commandToken = UUIDgenerate()) {

              if (is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has not been established."))

              writeLines(commands, con = private$igvSocket)

              private$pendingCommands[[commandToken]] <- private$numLinesWrittenByCommands(commands)

              return(commandToken)
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

            }
          ),

          private = list(
            igvPort   = NULL,
            igvHandle = NULL,
            igvSocket = NULL,
            igvJar    = NULL,
            fastaRef  = NULL,
            xvfbDisplay = NULL,

            pendingCommands = list(),

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
            },

            startWorker = function (ignoreOutput = FALSE) {

              private$igvHandle <- spawn_process("/usr/bin/java", arguments = c(
                "-jar", private$igvJar,
                "-p",   private$igvPort,
                "-g",   private$fastaRef,
                "-o",   "igv/igv.properties"
              ), workdir = getwd(), environment = sprintf("DISPLAY=:%i", private$xvfbDisplay))
            }
          )
  )