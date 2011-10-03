##
## Root code for volume predictions to calculate VWAP.
## 

from com.metaos.ext import *
import math

##
## Filters for open hours for M.C.
##
class MercadoContinuoIsOpen(Filter):
    def filter(self, when, symbol, values):
        minute = when.get(Calendar.HOUR_OF_DAY)*60 + when.get(Calendar.MINUTE)
        minute = minute + 60*values.get(\
                Field.EXTENDED(Field.Qualifier.NONE, "GMT"))
        minute = int(minute)
        return minute<=1056 and minute>=540

