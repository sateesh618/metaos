##
## Maximum variations with given realized probability 
## for the given filename of CSV data.
##
## Parameters:
##       symbol name;
##       csv file complete path;
##       size of period to evaluate variations;
##
##
## Expected data format is:
##   yyyy.MM.dd,hh:mm,open,high,low,close,volume
##

symbol = args[0]
fileName = args[1]
periods = Integer.parseInt(args[2])

source = CSVGeneral.getInstance().continuousSingleSource(symbol, \
    fileName, 'yyyy.MM.dd,HH:mm', \
    '([0-9]{4}.[0-9]{2}.[0-9]{2},[0-9]{2}:[0-9]{2}),(.*),(.*),(.*),(.*),(.*)', \
    [Fields.DATE,Fields.OPEN,Fields.HIGH,Fields.LOW,Fields.CLOSE,Fields.VOLUME])



# Bind 
class VariancesObserver(MarketObserver):
    def __init__(self):
        self.numOfSignals = 0
        self.listHighVariancesPeriod = []
        self.listLowVariancesPeriod = []
        self.listLowVariancesIntraPeriod = []
        self.listHighVariancesIntraPeriod = []

    def update(self, ss, when):
        print when
        self.numOfSignals = self.numOfSignals + 1
        print '.'

        if self.numOfSignals >= periods :
            refPrice = market.getLastPrice(period, symbol + '-OPEN')

            # Look for extreme values into period
            lowVarianceIntraPeriod = 0;
            highVarianceIntraPeriod = 0;
            for i in range(0,period):
                m = market.getLastPrice(i, symbol + '-LOW')    
                M = market.getLastPrice(i, symbol + '-HIGH')    
                if m<refPrice :
                    lowVarianceIntraPeriod = max(lowVarianceIntraPeriod, \
                            refPrice - m)
                if M>refPrice :
                    highVarianceIntraPeriod = max(highVarianceIntraPeriod, \
                            M - refPrice)

            # Calculate variance in the limits of period
            highVariancePeriod = max(0, \
                    market.getLastPrice(0, symbol + '-HIGH') - refPrice)
            lowVariancePeriod = max(0, \
                    refPrice - market.getLastPrice(0, symbol + '-LOW')) 

            self.listHighVariancesPeriod.append(highVariancePeriod)
            self.listLowVariancesPeriod.append(lowVariancePeriod)
            self.listLowVariancesIntraPeriod.append(lowVarianceIntraPeriod)
            self.listHighVariancesIntraPeriod.append(highVarianceIntraPeriod)


# Join everything together
market = SequentialAccessMarket(0.0, 5000)
variances = VariancesObserver()
source.addMarketListener(market)
source.addListener(variances)

# Ready, steady, go
source.run()

print variances.listHighVariancesPeriod


