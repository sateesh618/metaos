/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.market.source.csv;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Extension to CSVSingleSource to deal with bid and asks prices.
 */
public class CSVVWAPBidOfferSingleSource extends CSVBidOfferSingleSource {
    private final int vwapPosition;


    public CSVVWAPBidOfferSingleSource(final String symbol, 
            final String filePath, 
            final DateFormat dateFormat, final Pattern linePattern,
            final int datePosition, final int highPosition, 
            final int lowPosition, final int openPosition, 
            final int closePosition, final int volumePosition, 
            final int bidHighPosition, final int bidLowPosition,
            final int bidOpenPosition, final int bidClosePosition,
            final int bidVolumePosition,
            final int askHighPosition, final int askLowPosition,
            final int askOpenPosition, final int askClosePosition,
            final int askVolumePosition, final int vwapPosition) 
            throws IOException {
        super(symbol, filePath, dateFormat, linePattern, datePosition, 
                highPosition, lowPosition, openPosition, closePosition, 
                volumePosition,
                bidHighPosition, bidLowPosition, bidOpenPosition, 
                bidClosePosition, bidVolumePosition, 
                askHighPosition, askLowPosition, askOpenPosition, 
                askClosePosition, askVolumePosition, 17);
        this.vwapPosition = vwapPosition;
    }


    /**
     * Hook method for each subclass.
     * ATENTION: remember to call super.processLine()
     */
    protected void processLine(final String parts[], final Calendar moment) {
        super.processLine(parts, moment);

        final double vwap = Double.parseDouble(parts[this.vwapPosition]);
        this.sendPriceToMarkets(moment, symbol + "-VWAP", vwap);
    }
}
