##
## Root code for volume predictions to calculate VWAP.
## 

##
## Generator of "instants" for VolumeViews
##
## TODO: move to another place....
##
class LocalTimeMinutes(CalUtils.InstantGenerator):
    def generate(self, when):
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        # minute = minute + 60*result.values(0).get(\
        #        Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        return int(minute)
    def maxInstantValue(self):
        return 60*24


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
errors = Errors()
source = SingleSymbolScanner(fileName, symbol, lineParser, noAccumulator)

lineParser.addFilter(MercadoContinuoIsOpen()) \
          .addFilter(MainOutliers())

predictor = VolumeProfilePredictor(LocalTimeMinutes(), Field.VOLUME())
backtester = BacktesterAgent(source, predictor, OneDayAvoidingWeekEnds(), \
            MobileWindowVolumeProfileComparator(5, errors, LocalTimeMinutes(), \
                Field.VOLUME()))

noAccumulator.addListener(backtester)
source.run()

errorsStatistics = ErrorsStatistics(interpreteR)
errors.report(errorsStatistics)

