##
## Root code for volume predictions to calculate VWAP.
## 
from com.metaos import *
from com.metaos.datamgt import *
from com.metaos.engine import *
from com.metaos.util import *
from com.metaos.ext import *
from com.metaos.ext.filters import *
from com.metaos.ext.error import *
from com.metaos.signalgrt import *
from com.metaos.signalgrt.predictors import *
from java.util import *

from com.metaos.jy.filters.MercadoContinuoIsOpen import MercadoContinuoIsOpen


##
## Time signaling for prediction moment.
##
class OneDayAvoidingWeekEnds(ForecastingTime):
    def shouldEvaluatePrediction(self, when):
        return self.isNotWeekend(when) and self.isLastMinuteInDay(when)

    def shouldPredict(self, when):
        return self.isNotWeekend(when) and self.isLastMinuteInDay(when)

    def isNotWeekend(self, when):
        return when.get(Calendar.DAY_OF_WEEK)!=Calendar.SATURDAY \
            and when.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY

    def isLastMinuteInDay(self, when):
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = int(minute)
        return minute>=1056


class NotifiedDay(Listener):
    def notify(self, parseResult):
        self.when = CalUtils.normalizedClone(\
            parseResult.getLocalTimestamp())

    def nextDay(self):
        self.when.add(Calendar.DAY_OF_MONTH, 1)
        while self.when.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY \
                or self.when.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY:
            self.when.add(Calendar.DAY_OF_MONTH, 1)
        return self.when



##
## Base clas for Vol4Wap production.
##
class Vol4WapBase(object):
    def run(self, args, interpreteR):
        fileName = args[0]
        symbol = args[1]

        # Is it really useful??
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"))

        lineParser = ReutersCSVLineParser(fileName)
        accumulator = self.createSpreadTradesMgr()
        source = SingleSymbolScanner(fileName,symbol,lineParser,accumulator)

        lineParser.addFilter(MercadoContinuoIsOpen())\
                .addFilter(MainOutliers(0.75))

        ## 
        ## Functions 'createPredictor()' and 'createProfileComparator()' must 
        ## be defined previously.
        ##
        predictor = self.createPredictor()
        profileComparator = self.createProfileComparator()
        notifiedDay = NotifiedDay()
        
        accumulator.addListener(predictor)
        accumulator.addListener(notifiedDay)
        source.run()
        
        for i in range(0,5):
            nextDay = notifiedDay.nextDay()
            forecasting = predictor.predictVector(nextDay)

            file = open(symbol + "-" + str(nextDay.get(Calendar.DAY_OF_MONTH)) \
                    + "-" + str(nextDay.get(Calendar.MONTH)+1) \
                    + "-" + str(nextDay.get(Calendar.YEAR)) \
                    + ".forecasting.txt", "w")

            for j in range(0,len(forecasting)):
                file.write(str(forecasting[j]))
                file.write('\n')
            file.close()

