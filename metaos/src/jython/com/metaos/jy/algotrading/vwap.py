from com.metaos.ext import *

fileName = args[0]
symbol = args[1]

#interpreteR = R('arimaAdaptor.r')
interpreteR = R('maAdaptor.r')

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


class MercadoContinuoIsOpen(Filter):
    def filter(self, when, symbol, values):
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = minute + 60*values.get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        minute = int(minute)
        return minute>=540 and minute <=1056


t = Transposer(noAccumulator, LocalTimeMinutes())


# Collect data
source.run()

# Show some data
print t.getInstantsDay(CalUtils.createDate(18,4,2011))

interpreteR.end()

