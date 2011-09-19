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
        if(!learnClosed) {
            learnClosed <<- TRUE
            #ar <- arima(x=yVals, order=pars)
            #f <<- predict(ar)
            f <<- sum(yVals)/length(yVals)
        }
        # Existe el caso: un solo punto training => no hay ARIMA que valga
        # ¿que hacemos?
        return(f)
    }

    return(list(forecast=forecast, learn=learn, clean=clean))
}
