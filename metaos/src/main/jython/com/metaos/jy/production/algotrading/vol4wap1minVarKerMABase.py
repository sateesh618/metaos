from com.metaos.signalgrt.predictors import *
from com.metaos.datamgt import *
from com.metaos.jy.production.algotrading.vol4wapBase import Vol4WapBase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes
from com.metaos.jy.predictors.MAKernels import ConstantMAKernel

##
## Variable strategy: according to volatility, MA(x) or MA(y) is isued.
##
class VariableSizeStrategy(DayOfWeekTypedPredictorMA.KernelStrategy):
    def __init__(self, maSize1, varLimit, maSize2):
        if maSize2<maSize1: raise "maSize2 should be greater than maSize1"
        self.maSize1 = maSize1
        self.maSize2 = maSize2
        self.varLimit = varLimit
        self.smallKernel = ConstantMAKernel(maSize1)
        self.largeKernel = ConstantMAKernel(maSize2)

    def injectKernel(self, predictor):
        core = predictor.getCore()
        if self.variance(core)>self.varLimit: 
            predictor.setKernel(self.largeKernel)
        else: predictor.setKernel(self.smallKernel)
        
    def kernelSize(self):
        return self.maSize2


    ## Calculates vector standard deviation.
    def variance(self, vector):
        total = 0
        for i in range(0, len(vector)): total = total + vector[i]
        mean = total / len(vector)

        total = 0
        for i in range(0, len(vector)): 
            dif = mean - vector[i]
            total = total + (dif*dif)
        return total / (len(vector)-1)





##
## Base for variable kernels.
##
class Vol4Wap1MinVarKernelMABase(Vol4WapBase):
    def createPredictor(self):
        kernelStrategy = VariableSizeStrategy(5, 0.00000572, 10)
        return DayOfWeekTypedPredictorMA(kernelStrategy, LocalTimeMinutes(), \
                Field.VOLUME(), 1.0)

    def createSpreadTradesMgr(self):
            return TransparentSTMgr()


