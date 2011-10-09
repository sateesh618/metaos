##
## Root code for volume predictions to calculate VWAP.
## 

import math
from com.metaos.ext import *

fileName = args[0]
symbol = args[1]


TimeZone.setDefault(TimeZone.getTimeZone("GMT"))


lineParser = ReutersCSVLineParser(fileName)
noAccumulator = TransparentSTMgr()
source = SingleSymbolScanner(fileName, symbol, lineParser, noAccumulator)


##
## Generator of "instants" for VolumeViews
##
class LocalTimeMinutes(VolumeViews.InstantGenerator):
    def generate(self, result):
        when = result.getTimestamp()
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = minute + 60*result.values(0).get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        return int(minute)


##
## Function to calculate forecast predictions for a day.
## 
## param forecast: vector of forecasted volumes, normalized to N
## param real: vector of real volumes, normalized to N, len(real)=len(forecast)
## param windowSize: size of moving window to test forecasts 
## param norm: value of 1-norm for normalized vectors
## return: vector of size len(forecast)-windowSize with maximum absolute error
##          for each window position.
##
## Description: tests forecast and real values moving the window continuously
##          from the begining to the end of the real and forecast vectors.
##      
def calculateErrors(forecast, real, windowSize, norm):
    L = len(forecast)
    if len(forecast)!=len(real): 
        raise "Error, 'real' and 'forecast' must have the same size"

    errorVector = []
    for i in range(0, L - windowSize):
        forecastWindow = forecast[i:i+windowSize]
        realWindow = real[i:i+windowSize]

        # Normalizes
        total = 0
        for j in range(0, len(forecastWindow)): total = total+forecastWindow[j]
        if total==0: 
            errorVector.append(-1)
            continue

        for j in range(0, len(forecastWindow)): 
            forecastWindow[j] = norm * forecastWindow[j] / total
            
        total = 0
        for j in range(0, len(realWindow)): 
            if realWindow[j] != None : total = total + realWindow[j]
        for j in range(0, len(realWindow)): 
            if realWindow[j] != None : 
                realWindow[j] = norm * realWindow[j] / total

        
        # Checks errors
        maxError = 0
        for j in range(0, len(realWindow)):
            if realWindow[j] != None:
                err = realWindow[j] - forecastWindow[j]
                err = err * err
                if err > maxError: maxError = err
        
        if maxError!=0: errorVector.append(maxError)
        else: errorVector.append(-1)

    return errorVector




# Tests all predictors for each day of week.
errorsStatistics = ErrorsStatistics(interpreteR)
predictorsFactory = PredictorsFactory([MovingAverage(5)])

for dayOfWeek in [Calendar.TUESDAY]: 
    #[Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,\
    #              Calendar.THURSDAY,Calendar.FRIDAY]:
    lineParser.addFilter(MercadoContinuoIsOpen())\
            .addFilter(DayOfWeek(dayOfWeek)).addFilter(OnlyThirdFriday(-1))

    t = VolumeViews(noAccumulator, LocalTimeMinutes())
    source.run()
    if t.isEmpty() : continue
    t.consolidateDay(None)
    t.normalizeDays(100)

    maxDays = len(t.getValueAcrossDays(0))

    predictorsFactory.reset()
    predictor = predictorsFactory.next()
    while predictor != None :
        # 
        # TODO: create a backtesting specific class...
        #
        errorsInDay = Errors()
        errorsStatistics.reset()         

        for k in range(5, maxDays):
            dailyPrediction = []
            dailyVol = []

            # Remember: range(0, N)=0,1,2,...,N-1
            for i in range(0, t.numberOfInstantsInADay()):
                predictor.reset()       # Empties predictor memory
                vals = t.getValueAcrossDays(i)
                learningVals = vals.subList(0, k-1)
                predictor.learnVector(learningVals)
                pval = predictor.predict()

                dailyPrediction.append(pval)
                dailyVol.append(vals.get(k-1))

            # Normalize prediction
            total = 0
            for i in range(0, len(dailyPrediction)): 
                total = total + dailyPrediction[i]
            for i in range(0, len(dailyPrediction)): 
                dailyPrediction[i] = 100 * dailyPrediction[i] / total 

            windowSize = 10
            dailyErrors = calculateErrors(dailyPrediction, dailyVol, \
                    windowSize, 100)

            errorsInDay.addErrors(k, dailyErrors)

        errorsInDay.report(errorsStatistics)



        print 'Day: ' + str(dayOfWeek) + ', predictor: ' \
                + predictor.toString().encode('utf-8')
        print '-----------------------------------------------------------'
        print 'Quad Error max ' + str(errorsStatistics.max())
        print 'Quad Error min ' + str(errorsStatistics.min())
        print 'Quad Error mean ' + str(errorsStatistics.mean())
        print 'Quad Error variance ' + str(errorsStatistics.var())
        print 'Quad Error deciles ' + str(errorsStatistics.quantiles(10))
        print 'volpred=' + str(dailyPrediction)
        print 'vol=' + str(dailyVol)
        errorsStatistics.plot()
        print 
        predictor = predictorsFactory.next()

    source.reset()

