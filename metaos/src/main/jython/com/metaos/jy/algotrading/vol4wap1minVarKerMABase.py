from com.metaos.jy.algotrading.vol4wapBase import Vol4WapBase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

##
## No strategy: always same Moving Average.
##
class VariableSizeStrategy(DayOfWeekTypedPredictorMA.KernelStrategy):
    def __init__(self, maSize1, varLimit, maSize2):
        self.maSize1 = maSize1
        self.maSize2 = maSize2
        self.varLimit1 = varLimit1
        self.varLimit2 = varLimit2

    def injectKernel(self, predictor):
        core = predictor.getCore()
        if self.volatility(core)>self.varLimit:
        else:
        
    def kernelSize(self):
        return self.maSize2


class Vol4Wap1MinVarKernelMABase(Vol4WapBase):
    def createPredictor(self):
        kernelStrategy = FixedStrategy(5)
        return DayOfWeekTypedPredictorMA(fixedStrategy, \
                LocalTimeMinutes(), Field.VOLUME())

    def createSpreadTradesMgr(self):
            return TransparentSTMgr()

