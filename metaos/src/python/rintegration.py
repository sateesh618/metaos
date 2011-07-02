from java.lang import Long
from java.lang import Double



# TO DO: get symbols from file source
symbols = [ '1288.HK', '3988.HK', '0883.HK', '0939.HK', '2628.HK', '3968.HK', '0941.HK', '0688.HK', '0386.HK', '1088.HK', '0728.HK', '0762.HK', '1398.HK', '0857.HK', '2318.HK', '0700.HK', 'GAZPq.L', 'LKOHyq.L', 'NKELyq.L', 'NVTKq.L', 'RELIq.L', 'ROSNq.L', 'SNGSyq.L', 'TATNxq.L', 'BSBR.N', 'BBD.N', 'ABV.N', 'CIG.N', 'SID.N', 'GGB.N', 'HDB.N', 'IBN.N', 'ITUB.N', 'MBT.N', 'PBR.N', 'TNE.N', 'VALE.N', 'VIP.N', 'BIDU.OQ', 'INFY.OQ']


source = CSVUnorderedData.getInstance().reuters('BRIC40_1min.csv', symbols)

# R code: create predictor object
interpreteR = R(["lsPredictor.r"])
interpreteR.eval("predictor <- lsPredictor()")

# Bind R predictor to source events through an observer
class MyObserver(MarketObserver):
    def update(self, ss, when):
        if '1288.HK' in ss and 'GAZPq.L' in ss:
            interpreteR.eval('predictor$learn(' \
                + str(market.getLastPrice(0, '1288.HK-CLOSE')) + ','
                + str(market.getLastPrice(0, 'GAZPq.L-CLOSE')) + ')')

            strLine = Long.toString(when.getTimeInMillis()).encode('utf-8')
            strLine = strLine + ',' \
                    + Double.toString(market.getLastPrice(0,'1288.HK-OPEN'))\
                            .encode('utf-8') + ','\
                    + Double.toString(market.getLastPrice(0,'1288.HK-HIGH'))\
                            .encode('utf-8') + ',' \
                    + Double.toString(market.getLastPrice(0,'1288.HK-LOW'))\
                            .encode('utf-8') + ','\
                    + Double.toString(market.getLastPrice(0,'1288.HK-CLOSE'))\
                            .encode('utf-8') + ','\
                    + Long.toString(market.getLastVolume(0,'1288.HK'))\
                            .encode('utf-8')
            strLine = strLine + ',' \
                    + Double.toString(market.getLastPrice(0,'GAZPq.L-OPEN'))\
                            .encode('utf-8') + ','\
                    + Double.toString(market.getLastPrice(0,'GAZPq.L-HIGH'))\
                            .encode('utf-8') + ',' \
                    + Double.toString(market.getLastPrice(0,'GAZPq.L-LOW'))\
                            .encode('utf-8') + ','\
                    + Double.toString(market.getLastPrice(0,'GAZPq.L-CLOSE'))\
                            .encode('utf-8') + ','\
                    + Long.toString(market.getLastVolume(0,'GAZPq.L'))\
                            .encode('utf-8')
            #print strLine

        else if '1288.HK' in ss and 'GAZPq.L' not in ss:
            interpreteR.eval('predictor$predict(' \
                + str(market.getLastPrice(0, '1288.HK-CLOSE')) + ')')

            strLine = Long.toString(when.getTimeInMillis()).encode('utf-8')
            strLine = strLine + ',' \
                    + Double.toString(market.getLastPrice(0,'1288.HK-OPEN'))\
                            .encode('utf-8') + ','\
                    + Double.toString(market.getLastPrice(0,'1288.HK-HIGH'))\
                            .encode('utf-8') + ',' \
                    + Double.toString(market.getLastPrice(0,'1288.HK-LOW'))\
                            .encode('utf-8') + ','\
                    + Double.toString(market.getLastPrice(0,'1288.HK-CLOSE'))\
                            .encode('utf-8') + ','\
                    + Long.toString(market.getLastVolume(0,'1288.HK'))\
                            .encode('utf-8')
            strLine = strLine + ',-,-,-,-,-'
            #print strLine




# Join everything together
market = SequentialAccessMarket(0.0, 5000)
source.addMarketListener(market)
source.addListener(MyObserver())

# Ready, steady, go
source.run()

# Land down
interpreteR.end()
