from com.metaos.jy.lab.algotrading.vol4wapBase import Vol4WapBase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes

class Vol4Wap1MinMA5(Vol4Wap1MinMA5Base):
    def createProfileComparator(self):
        return MobileWindowVolumeProfileComparator(\
                60, LocalTimeMinutes(), Field.VOLUME())


Vol4Wap1MinMA5().run(args, interpreteR)
