# Generate an IGV batch script from filtered sv calls
suppressPackageStartupMessages(library(Biostrings))
suppressPackageStartupMessages(library(R6))
suppressPackageStartupMessages(library(future))
suppressPackageStartupMessages(library(uuid))

plan(multiprocess)

viper.igv.RemoteIGV <-
  R6Class("viper.igv.RemoteIGV",

          public = list(

            initialize = function (igvPort) {

              private$igvPort  <- igvPort

            },

            start = function () {

              if (!is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has already been established"))

              private$establishSocketConnection()
            },

            stop = function () {

              if (is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has not been established."))

              close(private$igvSocket)
              private$igvSocket <- NULL

            },

            checkCommandState = function () {

              if (length(private$pendingCommands) == 0) return(character(0))

              igvResponse <- readLines(con = private$igvSocket)
              commandsExecuted <- length(igvResponse)

              completedCommands <- character(0)

              while(commandsExecuted > 0) {

                commandsConsumed <- min(commandsExecuted, private$pendingCommands[[1]])
                private$pendingCommands[[1]] <- private$pendingCommands[[1]] - commandsConsumed
                print(private$pendingCommands)
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

            snapshot = function (bamFile, snapshotFileName, chr, pos, viewRange = 15) {

              self$sendCommands(c(
                      "new",
                paste("load", bamFile),
                      "collapse",
                paste("goto", sprintf("%s:%i-%i", chr, pos - viewRange, pos + viewRange)),
                paste("snapshot", snapshotFileName)
              ))

            }
          ),

          private = list(
            igvPort   = NULL,
            igvSocket = NULL,

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
            }
          )
  )

viper.igv.startWorker <- function (igvJar, igvPort, fastaRef) {

  command <- paste(
    "xvfb-run",
    "-a --server-args=\"-screen 0, 1280x1680x24\" java -jar",
    igvJar,
    "-p", igvPort,
    "-g", fastaRef,
    "-o", "igv/igv.properties")

  # This deferral to futures is necessary since xvfb-run (which needs xauth) does not work
  # from system calls within rstudio's console for some reason
  process %<-% {
    system(command)
  }
}