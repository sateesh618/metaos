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





# Tests all predictors for each day of week.
errorsStatistics = ErrorsStatistics(interpreteR)
predictorsFactory = PredictorsFactory([MovingAverage(10)])

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

    predictorsFactory.reset()
    predictor = predictorsFactory.next()
    while predictor != None :
        errors = Errors()
        errorsStatistics.reset()         

        dailyPrediction = []
        dailyVol = []

        # Remember: range(0, N)=0,1,2,...,N-1
        for i in range(0, t.numberOfInstants()):
            vals = t.getValueAcrossDays(i)
            k = len(vals)
            if vals.get(k-1)!=None:
                learningVals = vals.subList(0, k-1)
                predictor.learnVector(learningVals)
                pval = predictor.predict()

                dailyPrediction.append(pval)
                dailyVol.append(vals.get(k-1))

                quadError = math.pow(vals.get(k-1)-pval, 2)
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
        print 'volpred=' + str(dailyPrediction)
        print 'vol=' + str(dailyVol)
        errorsStatistics.plot()
        print 
        predictor = predictorsFactory.next()

    source.reset()

