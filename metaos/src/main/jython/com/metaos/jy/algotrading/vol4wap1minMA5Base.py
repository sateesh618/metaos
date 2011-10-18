from com.metaos.jy.algotrading.vol4wapBase import Vol4WapBase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

##
## No strategy: always same Moving Average.
##
class FixedStrategy(DayOfWeekTypedPredictorMA.KernelStrategy):
    def __init__(self, maSize):
        self.maSize = maSize

    def injectKernel(self, predictor):
        core = predictor.getCore()
        total = 0
        for i in range(0, self.maSize): total = total + core[i]
        total = total / self.maSize
        return total
        
    def kernelSize(self):
        return self.maSize


class Vol4Wap1MinMA5Base(Vol4WapBase):
    def createPredictor(self):
        kernelStrategy = FixedStrategy(5)
        return DayOfWeekTypedPredictorMA(fixedStrategy, \
                LocalTimeMinutes(), Field.VOLUME())

    def createSpreadTradesMgr(self):
            return TransparentSTMgr()

