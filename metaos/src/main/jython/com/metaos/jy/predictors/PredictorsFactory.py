from com.metaos.ext.predictors import *

##
## Factory of predictors.
##
class PredictorsFactory:
    def __init__(self, predictors):
        self.predictors = predictors
        self.index = 0


    #
    # Gets the next predictor or None if no more predictors are to be built.
    def next(self):
        if self.index>=len(self.predictors): 
            return None
        else:
            self.index = self.index + 1
            p = self.predictors[self.index-1]
            p.reset()
            return p


    #
    # Resets predictors building system.
    #
    def reset(self):
        self.index = 0
