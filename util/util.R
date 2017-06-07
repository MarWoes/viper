# Takes a chr string like "chr12:1234" and returns list(chr="12", pos=1234)
# or takes a string like "chrX:1234-5678" and returns
# list(chr="X", from= 1234, to= 5678)
util.posStrToList <- function (posStr) {

  splitPos <- strsplit(posStr, ":")[[1]]
  chr <- splitPos[[1]]
  pos <- splitPos[[2]]

  if (grepl("chr", chr)) chr <- substr(chr,4, nchar(chr))

  if (grepl("-", pos)) {

    pos  <- strsplit(pos,"-")[[1]]
    from <- strtoi(pos[[1]])
    to   <- strtoi(pos[[2]])

    return(list(chr=chr, from=from, to=to))

  } else {

    pos <- strtoi(pos)

    return(list(chr=chr, pos=pos))
  }

}

util.emptyFrame <- function (colNames) {
  empty <- data.frame(matrix(nrow = 0, ncol = length(colNames)))
  colnames(empty) <- colNames
  return(empty)
}

util.fileInDir <- function(dirName, fileName) {
  return(paste(dirName, fileName, sep = "/"))
}

util.strBeginsWith <- function (string, start) {
  return(substr(string, 1, nchar(start)) == start)
}

util.strEndsWith <- function (string, end) {
  return(substr(string, nchar(string) - nchar(end) + 1, nchar(string)) == end)
}

util.trimString <- function (string, maxSize) {
  return(ifelse(!is.na(string) & nchar(string) >= maxSize + 2, paste(strtrim(string, maxSize), "..", sep = ""), string))
}

# See http://stackoverflow.com/questions/13612967/how-to-reverse-a-string-in-r
util.reverseString <- function(x) sapply(lapply(strsplit(x, NULL), rev), paste, collapse="")

util.removeSubstring <- function (string, start, end) {
  return(
    paste(
      substr(string, 0, start - 1),
      substr(string, end + 1, nchar(string) + 1),
      sep = ""
    )
  )
}

util.reverseSubstring <- function (string, start, end) {
  return(
    paste(
      substr(string, 0, start - 1),
      util.reverseString(substr(string, start, end)),
      substr(string, end + 1, nchar(string) + 1),
      sep = ""
    )
  )
}

util.duplicateSubstring <- function (string, start, end) {
  return(
    paste(
      substr(string, 0, start - 1),
      substr(string, start, end),
      substr(string, start, end),
      substr(string, end + 1, nchar(string) + 1),
      sep = ""
    )
  )
}

util.isInInterval <- function (values, interval) {
  return(!is.na(values) & values >= interval[1] & values <= interval[2])
}

# Reads a single config value from the configuration file "config.sh". This is somewhat hacky.
util.readFromConfig <- function(configKey) {

  configValue <- system(paste("bash -c 'source config.sh && echo $",configKey,"'", sep=""), intern=TRUE)

  if(nchar(configValue) == 0) configValue <- NA

  return(configValue)
}
