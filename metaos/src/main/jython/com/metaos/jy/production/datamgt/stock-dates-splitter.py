##
## Splits a CSV Reuters file into several ones, each of them with only
## one stock information and covering one trading day (in local time).
## 

fileName = args[0]

#symbols = [ 'ABE.MC','ABG.MC','ACS.MC','ACX.MC','ISPA.AS','TEF.MC','TL5.MC',\
#            'TRE.MC' ]
symbols = [ 'TEF.MC' ]
symbol = 'TEF.MC'
# TODO: implement MultipleSymbols 

lineParser = ReutersCSVLineParser(fileName)
accumulator = TransparentSTMgr()
source = SingleSymbolScanner(fileName,symbol,lineParser,accumulator)

accumulator.addListener(FileSplitting.CSVReutersSplitter())

source.run()
