from com.metaos.jy.production.algotrading.vol4wap1minVarKerMABase \
    import Vol4Wap1MinVarKernelMABase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

class Vol4Wap1MinVarKernelMA(Vol4Wap1MinVarKernelMABase):
    def createProfileComparator(self):
        return MobileWindowVolumeProfileComparator(\
                60, LocalTimeMinutes(), Field.VOLUME())


Vol4Wap1MinVarKernelMA().run(args, interpreteR)
