class PythonMA:
    def __init__(self, history):
        self.history = history

    def predictWith(self, vector):
        subvector = vector[-self.history:]
        return math.fsum(subvector) / self.history
