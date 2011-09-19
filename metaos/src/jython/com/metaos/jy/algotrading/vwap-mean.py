
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
    MIN_MC_MINUTE = 540
    MAX_MC_MINUTE = 1049

    def __init__(self):
        self.data = HashMap()   # minute of day -> list of values
        self.days = HashMap()   # minute of day -> list of available dates
        self.seqOfDays = []     # list of days


    def notify(self, parseResult):
        moment = parseResult.getTimestamp()
        minute = moment.get(Calendar.HOUR_OF_DAY)*60 \
                + moment.get(Calendar.MINUTE)

        minute = minute + 60*cache.get(moment, \
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"), symbol)
        if minute>=MIN_MC_MINUTE and minute<MAX_MC_MINUTE:
            if self.data.get(minute)==None: 
                self.data.put(minute, [])
                self.days.put(minute, [])

            dayAndMonth = str(Calendar.DAY_OF_MONTH)) \
                        + "-" + str(moment.get(Calendar.MONTH)+1))
            try:
                self.data.get(minute).append(cache.get(moment, \
                        Field.VOLUME(), symbol))
                self.days.get(minute).append(moment.get(dayAndMonth))
                if self.seqOfDays[-1]!=dayAndMonth:
                    self.seqOfDays.append(dayAndMonth)

            except:
                #print str(moment.get(Calendar.DAY_OF_MONTH)) \
                #        + "-" + str(moment.get(Calendar.MONTH)+1) \
                #        + " " + str(minute)
                None

        # The same as:
        #data[minute].push(parseResult.values(symbol).get(VOLUME()))


    def calculatePercents(self):
        for day in seqOfDays:
            dailyVol = 0
            for m in range(MIN_MC_MINUTE, MAX_MC_MINUTE):
                for i in range(0, len(self.days.get(m)):
                    if self.days.get(m)[i]==day:
                        dailyVol = dailyVol + self.data.get(m)[i]
                        break

            for m in range(MIN_MC_MINUTE, MAX_MC_MINUTE):
                for i in range(0, len(self.days.get(m)):
                    if self.days.get(m)[i]==day:
                        self.data.get(m)[i] = self.data(m)[i] / dailyVol
                        break
                


traversalCutter = TraversalCutter()
noAccumulator.addListener(traversalCutter)

print "Go!"

source.run()

# Buscando aberraciones en los datos...
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
