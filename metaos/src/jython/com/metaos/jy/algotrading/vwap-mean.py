
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
        self.days = HashMap()

    def notify(self, parseResult):
        moment = parseResult.getTimestamp()
        minute = moment.get(Calendar.HOUR_OF_DAY)*60 \
                + moment.get(Calendar.MINUTE)

        minute = minute + 60*cache.get(moment, \
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"), symbol)
        if minute>=540and minute<1049:
            if self.data.get(minute)==None: 
                self.data.put(minute, [])
                self.days.put(minute, [])

            try:
                self.data.get(minute).append(cache.get(moment, \
                        Field.VOLUME(), symbol))
                self.days.get(minute).append(str(\
                        moment.get(Calendar.DAY_OF_MONTH)) \
                        + "-" + str(moment.get(Calendar.MONTH)+1))
            except:
                print str(moment.get(Calendar.DAY_OF_MONTH)) \
                        + "-" + str(moment.get(Calendar.MONTH)+1) \
                        + " " + str(minute)
                None

        # The same as:
        #data[minute].push(parseResult.values(symbol).get(VOLUME()))


    def calculatePercents(self):
        length = 0
        for v in self.data.values():
            if len(v)!=0: 
                length = len(v)
            else:
                # Remove this element...
                None

        for i in range(0,length-1):
            dailyVol = 0
            for v in self.data.values():
                if len(v)>0: dailyVol = dailyVol + v[i]

            for v in self.data.values():
                if len(v)>0: v[i] = v[i] / dailyVol
                


traversalCutter = TraversalCutter()
noAccumulator.addListener(traversalCutter)

print "Go!"

source.run()

pos = 0
for k in traversalCutter.data.keySet():
    v = traversalCutter.data[k]
    if len(v)>0: 
        if pos==0 or pos==2:
            print len(v)
            print k
            print traversalCutter.days[k]
        pos = pos + 1

#traversalCutter.calculatePercents()

#for v in traversalCutter.data.values():
#    print v
