## HACK HACK HACK HACK HACK
# shiny does not support range byte request, so we hack the static file handling function to support range byte requests.
# see https://github.com/rstudio/shiny/pull/747/files

hackedStaticHandling <- function(root) {
  force(root)
  return(function(req) {
    if (!identical(req$REQUEST_METHOD, 'GET'))
      return(NULL)

    path <- URLdecode(req$PATH_INFO)

    if (is.null(path))
      return(shiny:::httpResponse(400, content="<h1>Bad Request</h1>"))

    if (path == '/')
      path <- '/index.html'

    abs.path <- shiny:::resolve(root, path)
    if (is.null(abs.path))
      return(NULL)

    content.type <- shiny:::getContentType(abs.path)

    if(length(req$HTTP_RANGE) && grepl('^bytes=.+', req$HTTP_RANGE) && (util.strEndsWith(abs.path, ".bam") || util.strEndsWith(abs.path, ".fasta") || util.strEndsWith(abs.path, ".fa"))) {
      rng <- as.numeric(
        strsplit(gsub('^bytes=', '', req$HTTP_RANGE), '-')[[1]]
      )
      file.connection <- file(abs.path, "rb")
      seek(file.connection, where = rng[1], origin = "start")
      response.content <- readBin(
        file.connection, 'raw', n=length(rng[1]:rng[2])
      )
      close(file.connection)
      return(shiny:::httpResponse(206, content.type, response.content))
    } else {
      response.content <- readBin(abs.path, 'raw', n=file.info(abs.path)$size)
      return(shiny:::httpResponse(200, content.type, response.content))
    }
  })
}
assignInNamespace("staticHandler", hackedStaticHandling, "shiny")
## HACK HACK HACK HACK HACK
