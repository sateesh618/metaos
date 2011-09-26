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

##
## Filters for open hours for M.C.
##
class MercadoContinuoIsOpen(Filter):
    def filter(self, when, symbol, values):
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = minute + 60*values.get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        minute = int(minute)
        return minute>=540 and minute<=1056

##
## Filters only for the given day of week
##
class DayOfWeek(Filter):
    ##
    ## @param dayOfWeek according to Calendar.SUNDAY,... Calendar.SATURDAY
    ## constants, the day of week to filter.
    ##
    def __init__(self, dayOfWeek):
        self.dayOfWeek = dayOfWeek

    def filter(self, when, symbol, values):
        return when.get(Calendar.DAY_OF_WEEK) == self.dayOfWeek


##
## Filters only (or only not) third monthly friday.
##
class OnlyThirdFriday(Filter):
    ##
    ## @param positive is >0 to filter only for third friday in the month
    ## or <0 to filter for not third friday in the month.
    ##
    def __init__(self, positive):
        self.positive = positive

    def filter(self, when, symbol, values):
        isThirdFriday = when.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY \
                and when.get(Calendar.DAY_OF_MONTH)>14 \
                and when.get(Calendar.DAY_OF_MONTH)<22
        
        if self.positive>0: return isThirdFriday
        else: return not isThirdFriday




for dayOfWeek in [Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,\
                  Calendar.THURSDAY,Calendar.FRIDAY]:
    lineProcessor.addFilter(MercadoContinuoIsOpen())\
            .addFilter(DayOfWeek(dayOfWeek)).addFilter(OnlyThirdFriday(-1))
    lineProcessor.addCacheWriteable(cache)
    t = Transposer(noAccumulator, LocalTimeMinutes())
    source.run()
    t.consolidateDay(None)

    if dayOfWeek==Calendar.WEDNESDAY:
        print 'prenorm=' + str(t.getInstantsDay(CalUtils.createDate(26,1,2011)))
        t.normalizeDays(100)
        print 'postnorm='+str(t.getInstantsDay(CalUtils.createDate(26,1,2011)))

    source.reset()


interpreteR.end()

