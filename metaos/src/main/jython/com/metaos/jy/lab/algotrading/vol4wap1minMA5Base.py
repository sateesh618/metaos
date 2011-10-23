from com.metaos.jy.lab.algotrading.vol4wapBase import Vol4WapBase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

from com.metaos.jy.predictors.MAKernels import ConstantMAKernel


##
## No strategy: always same Moving Average.
##
class FixedStrategy(DayOfWeekTypedPredictorMA.KernelStrategy):
    def __init__(self, maSize):
        self.maSize = maSize
        self.constantKernel = ConstantMAKernel(maSize)

    def injectKernel(self, predictor):
        predictor.setKernel(self.constantKernel)
        
    def kernelSize(self):
        return self.maSize


class Vol4Wap1MinMA5Base(Vol4WapBase):
    def createPredictor(self):
        fixedStrategy = FixedStrategy(5)
        return DayOfWeekTypedPredictorMA(fixedStrategy, LocalTimeMinutes(), \
                Field.VOLUME(), 1.0)

    def createSpreadTradesMgr(self):
            return TransparentSTMgr()

