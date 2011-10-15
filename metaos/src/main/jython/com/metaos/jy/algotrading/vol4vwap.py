##
## Root code for volume predictions to calculate VWAP.
## 
from com.metaos.jy.util.LocalTimeMinutes import *

##
##
##
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
        return minute==1056


fileName = args[0]
symbol = args[1]

# Is it really useful??
TimeZone.setDefault(TimeZone.getTimeZone("GMT"))

lineParser = ReutersCSVLineParser(fileName)
noAccumulator = TransparentSTMgr()
source = SingleSymbolScanner(fileName, symbol, lineParser, noAccumulator)

lineParser.addFilter(MercadoContinuoIsOpen())
#          .addFilter(MainOutliers())
predictor = VolumeProfilePredictor(LocalTimeMinutes(), Field.VOLUME())
mobileWindowVolumeProfileComparator = MobileWindowVolumeProfileComparator(\
            5, LocalTimeMinutes(), Field.VOLUME())
backtester = BacktesterAgent(source, predictor, OneDayAvoidingWeekEnds(), \
            mobileWindowVolumeProfileComparator)

noAccumulator.addListener(backtester)
source.run()

minuteErrors = mobileWindowVolumeProfileComparator.getMinuteErrors();
dayErrors = mobileWindowVolumeProfileComparator.getDayErrors();

print "Day\tMaxError\t\MeanError\tErrorVariance"
for day in dayErrors.indexes():
    dayStatistics = Statistics(interpreteR)
    dayErrors.report(day, dayStatistics)
    print day + "\t" + str(dayStatistics.max()) + "\t"  \
            + str(dayStatistics.mean()) + "\t"
            + str(dayStatistics.var())




