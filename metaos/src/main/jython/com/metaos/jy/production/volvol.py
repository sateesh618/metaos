##
## Volatility for volume.
##

from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

fileName = args[0]
symbol = args[1]
memorySize = Integer.parseInt(args[2])



##
## Calculi helper for volatility on volume.
##
class VolatilityCalculator:
    def __init__(self, volumeViews, memorySize):
        self.volumeViews = volumeViews
        self.memorySize = memorySize


    def getValues(self):
        volatilities = []
        for instant in range(0, self.volumeViews.numberOfInstantsInADay()):
            instantValues = self.volumeViews.getValueAcrossDays(instant)

            mean = 0
            N = 0
            j = self.volumeViews.getValueAcrossDays(instant).size() - 1
            while j>0 and N<self.memorySize:
                if instantValues[j] != None:
                    N = N + 1
                    mean = mean + instantValues[j]
                j = j-1

            if N>1:
                mean = mean / N
                N = 0
                volatility = 0
                j = self.volumeViews.getValueAcrossDays(instant).size() - 1
                while j>0 and N<self.memorySize:
                    if instantValues[j] != None:
                        dif = mean - instantValues[j]
                        volatility = volatility + (dif * dif)
                        N = N + 1
                    j = j - 1 

                volatility = volatility / (N-1)
                volatilities.append(volatility)

        return volatilities
         

    def reset(self):
        self.volumeViews.reset()



#
# Let's go...
#
lineParser = ReutersCSVLineParser(fileName)
noAccumulator = TransparentSTMgr()
source = SingleSymbolScanner(fileName, symbol, lineParser, noAccumulator)
volumeViews = VolumeViews(LocalTimeMinutes())
volatilityCalculator = VolatilityCalculator(volumeViews, memorySize)
statistics = Statistics(interpreteR)


# Volatility for each minute and for each day of week
for dayOfWeek in [Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,\
                  Calendar.THURSDAY,Calendar.FRIDAY]:
    lineParser.addFilter(MercadoContinuoIsOpen())\
            .addFilter(DayOfWeek(dayOfWeek)).addFilter(OnlyThirdFriday(-1))\
            .addFilter(MainOutliers(0.75))
    noAccumulator.addListener(volumeViews)
    source.run()
    volumeViews.consolidateDay(None)
    volumeViews.normalizeDays(1.0)
    vols = volatilityCalculator.getValues()
    for i in range(0, len(vols)): statistics.addValue(vols[i])

    volatilityCalculator.reset()
    source.reset()


# And then, third fridays
lineParser.addFilter(MercadoContinuoIsOpen()).addFilter(OnlyThirdFriday(1))\
            .addFilter(MainOutliers(0.75))
noAccumulator.addListener(volumeViews)
source.run()
volumeViews.consolidateDay(None)
volumeViews.normalizeDays(1.0)
vols = volatilityCalculator.getValues()
for i in range(0, len(vols)): 
    print vols[i]
    statistics.addValue(vols[i])



print "Volume volatilities distribution: "
print str(statistics.quantiles(10))

