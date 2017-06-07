
math.clamp <- function (x, min, max) {
  if (x < min) return(min)
  if (x > max) return(max)
  return(x)
}

math.smooth <- function (values, windowSize = 2, smoothFn = median) {

  if (is.null(values)) return(NULL)

  ranges <- sapply(seq_len(length(values)), function (v) math.clamp(v - windowSize, 1, v - windowSize):math.clamp(v + windowSize,v + windowSize,length(values)))
  smoothedValues <- sapply(ranges, function (indices) smoothFn(values[indices]))

  return(smoothedValues)
}
