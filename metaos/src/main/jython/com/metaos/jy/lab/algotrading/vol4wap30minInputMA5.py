from com.metaos.jy.algotrading.vol4wapBase import Vol4WapBase
from com.metaos.jy.util.LocalTimeMinutes import LocalTimeMinutes


class Vol4Wap30MinInputMA5(Vol4WapBase):
    def createPredictor(self):
        return VolumeProfilePredictor(LocalTimeMinutes(), Field.VOLUME())

    def createProfileComparator(self):
        return MobileWindowVolumeProfileComparator(\
                5, LocalTimeMinutes(30), Field.VOLUME())

    def createSpreadTradesMgr(self):
        return BlocksOfMinutesSTMgr(30)


Vol4Wap30MinInputMA5().run(args, interpreteR)
