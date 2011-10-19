
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



##
## Base clas for Vol4Wap prediction testing.
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

        lineParser.addFilter(MercadoContinuoIsOpen())
        #        .addFilter(MainOutliers(0.75))
        print "Please, activate MainOutliers"

        ## 
        ## Functions 'createPredictor()' and 'createProfileComparator()' must 
        ## be defined previously.
        ##
        predictor = self.createPredictor()
        profileComparator = self.createProfileComparator()
        
        backtester = BacktesterAgent(source, predictor, \
                    OneDayAvoidingWeekEnds(), profileComparator)
        
        accumulator.addListener(backtester)
        source.run()
        
        minuteErrors = profileComparator.getMinuteErrors();
        dayErrors = profileComparator.getDayErrors();
        
        print "Pred.Date\tMaxError\tMeanError\tErrorVariance\tQ80%\tQ90%"
        for day in dayErrors.indexes():
            dayStatistics = Statistics(interpreteR)
            dayErrors.report(day, dayStatistics)
            print str(day) + "\t" \
                    + str(dayStatistics.max()) + "\t"  \
                    + str(dayStatistics.mean()) + "\t" \
                    + str(dayStatistics.var()) + "\t" \
                    + str(dayStatistics.quantiles(10)[7]) + "\t" \
                    + str(dayStatistics.quantiles(10)[8])
        
        
        print
        print "Pred.Minute\tMaxError\tMeanError\tErrorVariance\tQ80%\tQ90%"
        for minute in minuteErrors.indexes():
            minuteStatistics = Statistics(interpreteR)
            minuteErrors.report(minute, minuteStatistics)
            print str(minute) + "\t" \
                    + str(minuteStatistics.max()) + "\t" \
                    + str(minuteStatistics.mean()) + "\t" \
                    + str(minuteStatistics.var()) + "\t" \
                    + str(minuteStatistics.quantiles(10)[7]) + "\t" \
                    + str(minuteStatistics.quantiles(10)[8])
