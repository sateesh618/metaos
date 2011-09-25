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
        return minute>=540 and minute<=1056

class DayOfWeek(Filter):
    ##
    ## @param dayOfWeek according to Calendar.SUNDAY,... Calendar.SATURDAY
    ## constants, the day of week to filter.
    ##
    def __init__(self, dayOfWeek):
        self.dayOfWeek = dayOfWeek

    def filter(self, when, symbol, values):
        return when.get(Calendar.DAY_OF_WEEK) == self.dayOfWeek


lineProcessor.addFilter(MercadoContinuoIsOpen())\
        .addFilter(DayOfWeek(Calendar.MONDAY))

t = Transposer(noAccumulator, LocalTimeMinutes())


# Collect data
source.run()
t.consolidateDay(None)

# Show some data
v = t.getInstantsDay(CalUtils.createDate(24,1,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'jan24=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(31,1,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'jan31=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(7,2,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'feb7=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(14,2,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'feb14=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(21,2,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'feb21=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(28,2,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'feb28=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(07,3,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'mar07=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(14,3,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'mar14=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(21,3,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'mar21=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(28,3,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'mar28=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(4,4,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'apr4=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(11,4,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'apr11=' + str(v) + ';'

v = t.getInstantsDay(CalUtils.createDate(18,4,2011))
for i in range(0, v.size()):
    if v.get(i)==None: v.set(i, 0)
print 'apr18=' + str(v) + ';'








interpreteR.end()

