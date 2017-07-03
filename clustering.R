# Provides functionality to summarize analysis calls

library(GenomicRanges)
library(stringr)


viper.clustering.reduceToTransitivity <- function (overlapIndices) {

  adjustedIndices <- overlapIndices

  # break upon transitivity completion
  repeat {

    lengthSeq <- seq_len(length(adjustedIndices))

    transitivityIndices <- lapply(lengthSeq, function (...) integer(0))

    for (i in lengthSeq) {

      indices <- adjustedIndices[[i]]

      transitivityIndices[indices] <- lapply(transitivityIndices[indices], c, i)
    }

    nonTransitiveIndices <- sapply(transitivityIndices, length) > 1

    if (!any(nonTransitiveIndices)) break

    expansionIndices <- transitivityIndices[nonTransitiveIndices]

    for (indices in expansionIndices) {

      mergedIndices <- sort(unique(unlist(adjustedIndices[indices])))
      adjustedIndices[indices] <- lapply(length(indices), function (...) mergedIndices)

    }

    adjustedIndices <- unique(adjustedIndices)
  }


  return(adjustedIndices)
}

# Analysis data is an arbitrary data frame that has at least the columns 'sample', 'chr1', 'bp1', 'chr2', 'bp2'
viper.clustering.findOverlapIndices <- function(analysisData, maxGap) {

  swapIndices <- analysisData$bp1 > analysisData$bp2
  analysisData[swapIndices, c("chr1", "chr2", "bp1", "bp2")] <- analysisData[swapIndices, c("chr2", "chr1", "bp2", "bp1")]

  query <- GRanges(
    seqnames = paste(analysisData$svType, analysisData$chr1, analysisData$chr2, sep = "-"),
    ranges   = IRanges(
      start = analysisData$bp1,
      end   = analysisData$bp2
    )
  )

  overlapIndices <- as.list(findOverlaps(query, query, type = "equal", maxgap = maxGap))

  # overlapIndices contains duplicates, we remove them by sorting indices and leaving only unique calls
  overlapIndices <- lapply(overlapIndices, function (indices) sort(indices))
  overlapIndices <- unique(overlapIndices)

  overlapIndices <- viper.clustering.reduceToTransitivity(overlapIndices)

  return(overlapIndices)
}

viper.clustering.generateIDs <- function (numberOfSvs) {
  if (numberOfSvs == 0) return(c())

  svIndices     <- as.character(seq_len(numberOfSvs))
  padWidth      <- max(nchar(svIndices))
  paddedIndices <- str_pad(svIndices, padWidth, pad = "0")
  finalIDs      <- paste("VAR", paddedIndices, sep = "")

  return(finalIDs)
}

viper.clustering.summarizeVariantCalls <- function (analysisData, clusteringIndices) {

  clusteredData <- data.frame(
    id = viper.clustering.generateIDs(length(clusteringIndices)),
    numCalls = sapply(clusteringIndices, function (indices) length(unique(analysisData[indices, "sample"])))
  )

  for (column in colnames(analysisData)) {

    summarizeFunction <- function (values) {

      if (all(is.na(values))) return(NA)

      return(paste(na.omit(unique(values)), collapse = ", "))
    }

    if (util.strBeginsWith(column, "chr") || util.strBeginsWith(column, "bp")) {

      summarizeFunction <- function (values) values[1]

    } else if (is.numeric(analysisData[[column]])) {

      summarizeFunction <- function (values) median(values, na.rm = TRUE)

    }

    clusteredData[[column]] <- sapply(clusteringIndices, function (indices) summarizeFunction(analysisData[indices, column]))
  }

  clusteredData$relatedCalls <- clusteringIndices
  clusteredData$decision     <- NA

  return(clusteredData)
}

viper.clustering.clusterInput <- function (analysisData, maxGap) {

  clusteringIndices <- viper.clustering.findOverlapIndices(analysisData, maxGap)

  clusteredData <- viper.clustering.summarizeVariantCalls(analysisData, clusteringIndices)

  return(clusteredData)
}