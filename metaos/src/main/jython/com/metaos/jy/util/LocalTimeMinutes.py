##
## Generator of "instants" for VolumeViews
##
class LocalTimeMinutes(CalUtils.InstantGenerator):
    def generate(self, when):
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        return int(minute)
    def maxInstantValue(self):
        return 60*24
