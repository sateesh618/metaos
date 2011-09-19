#
# Predictor using ETS functions. 
#
arimaPredictor <- function(p,d,q) {
    yVals <- c()
    learnClosed <- FALSE
    f <- NULL
    pars <- c(p,d,q)

    
    clean <- function() {
        learnClosed <<- FALSE
        yVals <<- c()
        f <<- NULL
    }


    learn <- function(y) {
        yVals <<- append(yVals, y)
    }


    forecast <- function() {
        return(3)
#        if(!learnClosed) {
#            learnClosed <<- TRUE
#            ar <- arima(x=yVals, order=pars)
#            f <<- predict(ar)
#        }
#        return(f)
    }

    return(list(forecast=forecast, learn=learn, clean=clean))
}
