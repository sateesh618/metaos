##
## Root code for volume predictions to calculate VWAP.
## 

import math
from com.metaos.ext import *
from com.metaos.jy.filters.MercadoContinuoIsOpen import MercadoContinuoIsOpen 
from com.metaos.jy.filters.OnlyThirdFriday import OnlyThirdFriday
from com.metaos.jy.filters.DayOfWeek import DayOfWeek
from com.metaos.jy.predictors.PredictorsFactory import PredictorsFactory
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes



fileName = args[0]
symbol = args[1]


TimeZone.setDefault(TimeZone.getTimeZone("GMT"))


lineParser = ReutersCSVLineParser(fileName)
noAccummulator = TransparentSTMgr()
source = SingleSymbolScanner(fileName, symbol, lineParser, noAccummulator)



# Tests all predictors for each day of week.
interpreteR = R()
errorsStatistics = ErrorsStatistics(interpreteR)
predictorsFactory = PredictorsFactory([MovingAverage(4),MovingAverage(5),MovingAverage(6)])
for dayOfWeek in [Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,\
                  Calendar.THURSDAY,Calendar.FRIDAY]:
    lineParser.addFilter(MercadoContinuoIsOpen())\
            .addFilter(DayOfWeek(dayOfWeek)).addFilter(OnlyThirdFriday(1))

    t = VolumeViews(noAccumulator, LocalTimeMinutes())
    source.run()
    t.consolidateDay(None)
    t.normalizeDays(100)

    predictorsFactory.reset()
    predictor = predictorsFactory.next()
    while predictor != None :
        errors = Errors()
        errorsStatistics.reset() 
        # Remember: range(0, N)=0,1,2,...,N-1
        for i in range(0, t.numberOfInstants()):
            vals = t.getDayInstants(i)
            k = len(vals)
            if vals.get(k-1)!=None:
                learningVals = vals.subList(0, k-1)
                predictor.learnVector(learningVals)
                pvals = predictor.predict()
                quadError = math.pow(vals.get(k-1)-pvals, 2)
                errors.addError(i, quadError);

        errors.report(errorsStatistics)
        print 'Day: ' + str(dayOfWeek) + ', predictor: ' \
            + predictor.toString().encode('utf-8')
        print '-----------------------------------------------------------'
        print 'Quad Error max ' + str(errorsStatistics.max())
        print 'Quad Error min ' + str(errorsStatistics.min())
        print 'Quad Error mean ' + str(errorsStatistics.mean())
        print 'Quad Error variance ' + str(errorsStatistics.var())
        print 'Quad Error deciles ' + str(errorsStatistics.quantiles(10))
        print 
        predictor = predictorsFactory.next()

    source.reset()


interpreteR.end()
