##
## Root code for volume predictions to calculate VWAP.
## 


fileName = args[0]
symbol = args[1]

# Is it really useful??
TimeZone.setDefault(TimeZone.getTimeZone("GMT"))

lineParser = ReutersCSVLineParser(fileName)
noAccumulator = TransparentSTMgr()
errorsStatistics = ErrorsStatistics()
source = SingleSymbolScanner(fileName, symbol, lineParser, noAccumulator)

lineParser.addFilter(MercadoContinuoIsOpen()) \
          .addFilter(MainOutliers())

predictor = VolumeProfilePredictor(LocalTimeMinutes(), MA(5))
backtester = BacktesterAgent(source, predictor, OneDayAvoidingWeekEnds(),
            MobileWindow([5,10,15]))

noAccumulator.addListener(backtester)
backtester.run()


errorsStatistics.report()

