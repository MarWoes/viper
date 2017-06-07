
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
