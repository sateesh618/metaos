#
# See com.metaos.ext.error.ErrosStatistics for more information.
#
# Represents a collector of data to show error statistics.
#
ErrorsStatistics <- function() {
    eVals <- c()

    reset <- function() {
        eVals <<- c()
    }

    addError <- function(x) {
        eVals <<- append(eVals, x)
    }

    listAll <- function() {
        return(eVals)
    }

    return(list(reset=reset, addError=addError, listAll=listAll))
}
