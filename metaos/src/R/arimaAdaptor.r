#
# Predictor using ETS functions. 
#
arimaPredictor <- function(p,d,q) {
#    firstTime <- TRUE
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
        if(!learnClosed) {
            learnClosed <<- TRUE
            ar <- arima(x=yVals, order=pars)
            f <<- predict(ar)
#            if(firstTime) {
#                firstTime <<- FALSE
#                par(mfrow=c(2,1))
#                acf(ar,main="ACF")
#                pacf(ar,main="PACF")
#            }
        }

        return(f$pred)
    }

    return(list(forecast=forecast, learn=learn, clean=clean))
}
