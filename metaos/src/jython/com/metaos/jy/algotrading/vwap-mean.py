
fileName = args[0]
symbol = args[1]

TimeZone.setDefault(TimeZone.getTimeZone("GMT+10"))

noAccumulator = ZeroAccumulator()
lineProcessor = ReutersCSVLineParser(fileName)
source = SingleSymbolScanner(fileName, symbol, lineProcessor, noAccumulator)
cache = RandomAccessCache(5000)
lineProcessor.addCacheWriteable(cache)

#
# Stores for each instrument and minute in day, the list of %volumes/dailyVol 
# for each minute.
#
class TraversalCutter(Listener):
    def __init__(self):
        self.data = HashMap()

    def notify(self, parseResult):
        moment = parseResult.getTimestamp()
        minute = moment.get(Calendar.HOUR_OF_DAY)*60 \
                + moment.get(Calendar.MINUTE)

        if self.data.get(minute)==None: self.data.put(minute, [])

        try:
            self.data.get(minute).append(cache.get(moment, \
                    Field.VOLUME(), symbol))
        except:
            None

        # The same as:
        #data[minute].push(parseResult.values(symbol).get(VOLUME()))


traversalCutter = TraversalCutter()
noAccumulator.addListener(traversalCutter)

print "Go!"

source.run()

for v in traversalCutter.data.values():
    print v
