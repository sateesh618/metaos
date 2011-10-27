from com.metaos.jy.production.algotrading.vol4wap1minVarKerMABase \
    import Vol4Wap1MinVarKernelMABase
from com.metaos.jy.production.algotrading.vol4wapBase import NotifiedDay
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes
from java.util import Calendar



inputFile = args[0]
repositoryPath = args[1]
forecastingPath = args[2]

# IBEX35
symbols = [ 'TEF.MC' ]
endDay = None
initDay = None
notifiedDay = NotifiedDay()

# Feed repository
repository = FileSplitting(repositoryPath, '1min')
for symbol in symbols:
    lineParser = ReutersCSVLineParser(inputFile)
    accumulator = TransparentSTMgr()
    
    accumulator.addListener(FileSplitting.CSVReutersSplitter(repository))
    accumulator.addListener(notifiedDay)
    source = SingleSymbolScanner(inputFile,symbol,lineParser,accumulator)
    source.run()

    if endDay==None or notifiedDay.lastDay().after(endDay):
        endDay = notifiedDay.lastDay()

initDay = endDay.clone()
initDay.add(Calendar.DAY_OF_MONTH, -25*7)

# Forecast
engine = Vol4Wap1MinVarKernelMABase()
for symbol in symbols:
    engine.run(repository, forecastingPath, interpreteR, initDay, \
            endDay, symbol)

