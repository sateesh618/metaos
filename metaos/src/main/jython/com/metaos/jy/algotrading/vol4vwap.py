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
        minute = minute + 60*result.values(0).get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        return int(minute)
    def maxInstantValue(self):
        return 60*24





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
backtester.run()


errorsStatistics.report()

