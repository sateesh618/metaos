##
## Set of kernels for KernelMovingAverage predictors.
##
##
## See KernelMovingAverage.Kernel class for information.

from com.metaos.signalgrt.predictors import *


##
## K(x) = sum(xi,i=1,2,..,N) / N, With N=size
##
class ConstantMAKernel(KernelMovingAverage.Kernel):
    def __init__(self, size):
        self.size = size

    def getKernelSize(self):
        return self.size

    def eval(self, memory, headIndex):
        total = 0
        for i in range(0, self.size):
            index = int((i + headIndex) % len(memory))
            total = total + memory[index]
        return total / self.size


