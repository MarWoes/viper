# Generate an IGV batch script from filtered sv calls
suppressPackageStartupMessages(library(Biostrings))
suppressPackageStartupMessages(library(R6))
suppressPackageStartupMessages(library(future))

plan(multiprocess)

viper.igv.RemoteIGV <-
  R6Class("viper.igv.RemoteIGV",

          public = list(

            initialize = function (fastaRef, igvJar, igvPort) {

              private$fastaRef <- fastaRef
              private$igvJar   <- igvJar
              private$igvPort  <- igvPort

            },

            start = function () {

              if (!is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has already been established"))

              command <- paste(
                "xvfb-run",
                "-a --server-args=\"-screen 0, 1280x1680x24\" java -jar",
                private$igvJar,
                "-p", private$igvPort,
                "-g", private$fastaRef,
                "-o", "igv/igv.properties")

              process %<-% {
                system(command)
              }

              private$establishSocketConnection()
            },

            stop = function () {

              if (is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has not been established."))

              writeLines("exit", con = private$igvSocket)
              private$igvSocket <- NULL

            },

            sendCommands = function(commands) {

              if (is.null(private$igvSocket)) return(warning("[WARNING] IGV socket connection has not been established."))

              writeLines(commands, con = private$igvSocket)

            }
          ),

          private = list(
            fastaRef  = NULL,
            igvJar    = NULL,
            igvPort   = NULL,
            igvSocket = NULL,

            openSocket = function () {

              socket <- tryCatch({

                suppressWarnings(socketConnection(port = private$igvPort))

              },
              error = function (cond) NULL)

              return(socket)
            },

            establishSocketConnection = function (attempts = 60) {

              attempt <- 0

              while (attempt < attempts) {

                socket <- private$openSocket()

                if (!is.null(socket)) {

                  private$igvSocket <- socket
                  return()
                } else {
                  Sys.sleep(1)
                  attempt <- attempt + 1
                }
              }
            }
          )

  )
