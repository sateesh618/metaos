##
## Root code for volume predictions to calculate VWAP.
## 

import math
from com.metaos.ext import *
from com.metaos.jy.filters.MercadoContinuoIsOpen import MercadoContinuoIsOpen 
from com.metaos.jy.filters.OnlyThirdFriday import OnlyThirdFriday
from com.metaos.jy.filters.DayOfWeek import DayOfWeek
from com.metaos.jy.predictors.PredictorsFactory import PredictorsFactory

fileName = args[0]
symbol = args[1]


TimeZone.setDefault(TimeZone.getTimeZone("GMT"))

noAccumulator = ZeroAccumulator()
lineProcessor = ReutersCSVLineParser(fileName)
source = SingleSymbolScanner(fileName, symbol, lineProcessor, noAccumulator)
cache = RandomAccessCache(5000)
lineProcessor.addCacheWriteable(cache)

##
## Generator of "instants" for Transposer
##
class LocalTimeMinutes(Transposer.InstantGenerator):
    def generate(self, result):
        when = result.getTimestamp()
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = minute + 60*result.values(0).get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        return int(minute)





# Tests all predictors for each day of week.
interpreteR = R()
errorsStatistics = ErrorsStatistics(interpreteR)
predictorsFactory = PredictorsFactory()
for dayOfWeek in [Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,\
                  Calendar.THURSDAY,Calendar.FRIDAY]:
    lineProcessor.addFilter(MercadoContinuoIsOpen())\
            .addFilter(DayOfWeek(dayOfWeek)).addFilter(OnlyThirdFriday(-1))
    lineProcessor.addCacheWriteable(cache)

    t = Transposer(noAccumulator, LocalTimeMinutes())
    source.run()
    t.consolidateDay(None)
    t.normalizeDays(100)

    predictorsFactory.reset()
    predictor = predictorsFactory.next()
    while preddictor != None :
        errors = Errors()
        errorsStatistics.reset() 
        for i in range(0, t.numberOfInstants()):
            vals = t.getDayInstants(i)
            k = len(vals)
            if vals.get(k-1)!=None:
                learningVals = vals.subList(0, k-1)
                predictor.learnVector(learningVals)
                pvals = predictor.predictWith(learningVals)
                quadError = math.pow(vals.get(k-1)-pvals, 2)
                errors.addError(i, quadError);

        error.report(errorsStatistics)
        print 'Day: ' + str(dayOfWeek) + ', predictor: ' + predictor.toString()
        print '-----------------------------------------------------------'
        print 'Quad Error max ' + str(error.max())
        print 'Quad Error min ' + str(error.max())
        print 'Quad Error mean ' + str(error.mean())
        print 'Quad Error variance ' + str(error.var())
        print 
        predictor = predictorsFactory.next()

    source.reset()


interpreteR.end()
