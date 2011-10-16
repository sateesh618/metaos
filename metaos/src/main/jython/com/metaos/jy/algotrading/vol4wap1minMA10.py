from com.metaos.jy.algotrading.vol4wapBase import Vol4WapBase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

class Vol4Wap1MinMA5(Vol4WapBase):
    def createPredictor(self):
        return VolumeProfilePredictor(LocalTimeMinutes(), Field.VOLUME())

    def createProfileComparator(self):
        return MobileWindowVolumeProfileComparator(\
                10, LocalTimeMinutes(), Field.VOLUME())


Vol4Wap1MinMA5().run(args, interpreteR)
