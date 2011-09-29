import Predictor

class PythonMA(Predictor):
    def __init__(self, history):
        self.history = history

    def predictWith(self, vector):
        vector = vector.toArray()
        subvector = vector[-1:]
        sum = 0
        for x in subvector: 
            if x!=None : sum = sum + x
        return sum / self.history
