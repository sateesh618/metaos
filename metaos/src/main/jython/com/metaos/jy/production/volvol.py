##
## Volatility for volume.
##

from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

fileName = args[0]
symbol = args[1]



##
## Calculi helper for volatility on volume.
##
class VolatilityCalculator(Listener):
    def __init__(self):
        self.values = []
        self.minutesGenerator = LocalTimeMinutes()
        for i in range(0,self.minutesGenerator.maxInstantValue()+1):
            self.values.append([])

    def getValues(self):
        volatilities = []
        for i in range(1,len(self.values)):
            if len(self.values[i])<=1: continue

            total = 0
            for j in range(0, len(self.values[i])):
                total = total + self.values[i][j]
            mean = total / len(self.values[i])

            total = 0
            for j in range(0, len(self.values[i])):
                dif = mean - self.values[i][j]
                total = total + (dif*dif)
            variance = total / (len(self.values[i])-1)

            volatilities.append(variance)

        return volatilities


    def reset(self):
        self.values = []


    def notify(self, result):
        if result.values(0)!=None and \
                result.values(0).get(Field.VOLUME())!=None:
            minute = self.minutesGenerator.generate(result.getLocalTimestamp()) 
            self.values[minute].append(result.values(0).get(Field.VOLUME()))




lineParser = ReutersCSVLineParser(fileName)
noAccumulator = TransparentSTMgr()
source = SingleSymbolScanner(fileName, symbol, lineParser, noAccumulator)
volatilityCalculator = VolatilityCalculator()
noAccumulator.addListener(volatilityCalculator)




statistics = Statistics(interpreteR)
# Volatility for each minute and for each day of week
for dayOfWeek in [Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,\
                  Calendar.THURSDAY,Calendar.FRIDAY]:
    lineParser.addFilter(MercadoContinuoIsOpen())\
            .addFilter(DayOfWeek(dayOfWeek)).addFilter(OnlyThirdFriday(-1))\
            .addFilter(MainOutlier(0.75))
    source.run()
    vols = volatilityCalculator.getValues()
    for i in range(0, len(vols)): statistics.addValue(vols[i])

    volatilityCalculator.reset()
    source.reset()


# And then, third fridays
lineParser.addFilter(MercadoContinuoIsOpen()).addFilter(OnlyThirdFriday(1))\
            .addFilter(MainOutlier(0.75))
source.run()
vols = volatilityCalculator.getValues()
for i in range(0, len(vols)): statistics.addValue(vols[i])



print "Volume volatilities distribution: "
print str(statistics.quantiles(10))

