##
## Root code for volume predictions to calculate VWAP.
## 

from com.metaos.ext import *
import math
from filters import MercadoContinuoIsOpen
from filters import DayOfWeek
from filters import OnlyThirdFriday

fileName = args[0]
symbol = args[1]


TimeZone.setDefault(TimeZone.getTimeZone("GMT"))

noAccumulator = ZeroAccumulator()
lineProcessor = ReutersCSVLineParser(fileName)
source = SingleSymbolScanner(fileName, symbol, lineProcessor, noAccumulator)
cache = RandomAccessCache(5000)
lineProcessor.addCacheWriteable(cache)

class LocalTimeMinutes(Transposer.InstantGenerator):
    def generate(self, result):
        when = result.getTimestamp()
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = minute + 60*result.values(0).get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        return int(minute)

##
## Filters for open hours for M.C.
##
class MercadoContinuoIsOpen(Filter):
    def filter(self, when, symbol, values):
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = minute + 60*values.get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        minute = int(minute)
        return minute<=1056 and minute>=540

##
## Filters only for the given day of week
##
class DayOfWeek(Filter):
    ##
    ## @param dayOfWeek according to Calendar.SUNDAY,... Calendar.SATURDAY
    ## constants, the day of week to filter.
    ##
    def __init__(self, dayOfWeek):
        self.dayOfWeek = dayOfWeek

    def filter(self, when, symbol, values):
        return when.get(Calendar.DAY_OF_WEEK) == self.dayOfWeek


##
## Filters only (or only not) third monthly friday.
##
class OnlyThirdFriday(Filter):
    ##
    ## @param positive is >0 to filter only for third friday in the month
    ## or <0 to filter for not third friday in the month.
    ##
    def __init__(self, positive):
        self.positive = positive

    def filter(self, when, symbol, values):
        isThirdFriday = when.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY \
                and when.get(Calendar.DAY_OF_MONTH)>14 \
                and when.get(Calendar.DAY_OF_MONTH)<22
        
        if self.positive>0: return isThirdFriday
        else: return not isThirdFriday





# Tests all predictors for each day of week.
for dayOfWeek in [Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,\
                  Calendar.THURSDAY,Calendar.FRIDAY]:
    lineProcessor.addFilter(MercadoContinuoIsOpen())\
            .addFilter(DayOfWeek(dayOfWeek)).addFilter(OnlyThirdFriday(-1))
    lineProcessor.addCacheWriteable(cache)
    t = Transposer(noAccumulator, LocalTimeMinutes())
    source.run()
    t.consolidateDay(None)
    t.normalizeDays(100)

    errors = []
    predictorFactory.reset()
    while (predictor = predictorFactory.next()) != None :
        totalQuadError = 0

        for i in range(0, t.numberOfInstants()):
            vals = t.getDayInstants(i)
            k = len(vals)
            if vals.get(k-1)!=None:
                learningVals = vals.subList(0, k-1)
                predictor.learn(learningVals)
                pvals = predictor.predictWith(learningVals)
                quadError = math.pow(vals.get(k-1)-pvals, 2)
                totalQuadError = totalQuadError + quadError

        erros.append(totalQuadError);

    source.reset()


#interpreteR.end()
