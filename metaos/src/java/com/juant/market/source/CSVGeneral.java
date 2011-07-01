/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.juant.market.source;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Prices source factory for CSV files with a field to store date, five
 * fields for high, low, open and close and volume.
 */
public final class CSVGeneral {
    private static final Logger log = Logger.getLogger(
            CSVGeneral.class.getPackage().getClass().getName());

    private static final CSVGeneral instance = new CSVGeneral();
    
    public static CSVGeneral getInstance() { return instance; }

    private CSVGeneral() {
    }

    /**
     * Creates a Source of data from a CSV with only one symbol
     */
    public PricesSource continuousSingleSource(final String symbol,
            final String filePath, final String dateFormat, 
            final String linePattern, final Fields fieldSet[]) 
            throws IOException {
        int highPos=0, lowPos=0, openPos=0, closePos=0, datePos=0, volumePos=0;
        for(int i=0; i<fieldSet.length; i++) {
            switch(fieldSet[i]) {
                case LOW:
                    lowPos = i;
                    break;
                case HIGH:
                    highPos = i;
                    break;
                case OPEN:
                    openPos = i;
                    break;
                case CLOSE:
                    closePos = i;
                    break;
                case VOLUME:
                    volumePos = i;
                    break;
                case DATE:
                    datePos = i;
                    break;
                default:
                    throw new RuntimeException("Not understood field " 
                            + fieldSet[i] + " in this kind of sources");
            }
        }
        return new CSVSingleSource(symbol, filePath, new SimpleDateFormat(
                dateFormat), Pattern.compile(linePattern),
                datePos, highPos, lowPos, openPos, closePos,
                volumePos);
    }



    //
    // Private inner classes.
    //
    private static final class CSVSingleSource extends BasicPricesSource {
        private boolean isClosed = false;
        private boolean nomore = false;
        private final BufferedReader fileReader;
        private final String symbol;
        private final int datePosition, highPosition, lowPosition,
                openPosition, closePosition, volumePosition;
        private final DateFormat dateFormat;
        private final List<String> symbolAsList;
        private final Pattern pattern;


        public CSVSingleSource(final String symbol, final String filePath, 
                final DateFormat dateFormat, final Pattern linePattern,
                final int datePosition, final int highPosition, 
                final int lowPosition, final int openPosition, 
                final int closePosition, final int volumePosition) 
                throws IOException {
            this.fileReader = new BufferedReader(new FileReader(filePath));
            this.symbol = symbol;
            this.datePosition = datePosition;
            this.highPosition = highPosition;
            this.lowPosition = lowPosition;
            this.openPosition = openPosition;
            this.closePosition = closePosition;
            this.volumePosition = volumePosition;
            this.dateFormat = dateFormat;
            this.symbolAsList = Arrays.asList(symbol);
            this.pattern = linePattern;
        }


        public void run() {
            while(this.next());
        }


        public boolean next() {
            if(this.isClosed) return false;
            if(this.nomore) return false;
            try {
                final String line = fileReader.readLine();
                if(line==null) {
                    this.nomore = true;
                    return false;
                }

                final String[] parts = new String[6];
                final Matcher matcher = pattern.matcher(line);
                if(matcher.matches()) {
                    for(int i=0; i<6; i++) {
                        parts[i] = matcher.group(i+1);
                    }
                } else {
                    final boolean tmp = this.next();
                    if(!tmp) this.nomore = true;
                    return tmp;
                }
                final Date date =
                        this.dateFormat.parse(parts[this.datePosition]);
                final double high =
                        Double.parseDouble(parts[this.highPosition]);
                final double low = Double.parseDouble(parts[this.lowPosition]);
                final double open =
                        Double.parseDouble(parts[this.openPosition]);
                final double close =
                        Double.parseDouble(parts[this.closePosition]);
                final long volume = Long.parseLong(parts[this.volumePosition]);

                final Calendar moment = Calendar.getInstance();
                moment.setTimeInMillis(date.getTime());
                this.sendPriceToMarkets(moment, symbol + "-HIGH", high); 
                this.sendPriceToMarkets(moment, symbol + "-LOW", low); 
                this.sendPriceToMarkets(moment, symbol + "-OPEN", open); 
                this.sendPriceToMarkets(moment, symbol + "-CLOSE", close); 
                this.sendVolumeToMarkets(moment, symbol, volume); 
                this.notifyListeners(moment, symbolAsList);
                return true;
            } catch(Exception e) {
                this.nomore = true;
                return false;
            }
        }


        public void close() {
            this.isClosed = true;
            try {
                this.fileReader.close();
            } catch(IOException ioe) {
                // Maybe nothing should happen in this case.... don't worry.
            }
        }
    }
}
