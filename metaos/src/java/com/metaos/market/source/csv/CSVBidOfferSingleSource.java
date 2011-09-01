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
public class CSVBidOfferSingleSource extends CSVSingleSource {
    private final int bidHighPosition, bidLowPosition,
            bidOpenPosition, bidClosePosition, bidVolumePosition;
    private final int askHighPosition, askLowPosition,
            askOpenPosition, askClosePosition, askVolumePosition;


    public CSVBidOfferSingleSource(final String symbol, 
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
            final int askVolumePosition) throws IOException {
        this(symbol, filePath, dateFormat, linePattern, datePosition, 
                highPosition, lowPosition, openPosition, closePosition, 
                volumePosition, bidHighPosition, bidLowPosition,
                bidOpenPosition, bidClosePosition, bidVolumePosition,
                askHighPosition, askLowPosition, askOpenPosition, 
                askClosePosition, askVolumePosition, 16);
    }


    /**
     * To be used by extending classes.
     */
    protected CSVBidOfferSingleSource(final String symbol, 
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
            final int askVolumePosition, final int maxPos) throws IOException {
        super(symbol, filePath, dateFormat, linePattern, datePosition, 
                highPosition, lowPosition, openPosition, closePosition, 
                volumePosition, maxPos);
        this.bidHighPosition = bidHighPosition;
        this.bidLowPosition = bidLowPosition;
        this.bidOpenPosition = bidOpenPosition;
        this.bidClosePosition = bidClosePosition;
        this.bidVolumePosition = bidVolumePosition;
        this.askHighPosition = askHighPosition;
        this.askLowPosition = askLowPosition;
        this.askOpenPosition = askOpenPosition;
        this.askClosePosition = askClosePosition;
        this.askVolumePosition = askVolumePosition;

    }
 

    /**
     * Hook method for each subclass.
     * ATENTION: remember to call super.processLine()
     */
    protected void processLine(final String parts[], final Calendar moment) {
        super.processLine(parts, moment);

        final double bidHigh =
                Double.parseDouble(parts[this.bidHighPosition]);
        final double bidLow = Double.parseDouble(parts[this.bidLowPosition]);
        final double bidOpen =
                Double.parseDouble(parts[this.bidOpenPosition]);
        final double bidClose =
                Double.parseDouble(parts[this.bidClosePosition]);
        final long bidVolume = Long.parseLong(parts[this.bidVolumePosition]);

        this.sendBidToMarkets(moment, symbol + "-HIGH", bidHigh);
        this.sendBidToMarkets(moment, symbol + "-LOW", bidLow);
        this.sendBidToMarkets(moment, symbol + "-OPEN", bidOpen);
        this.sendBidToMarkets(moment, symbol + "-CLOSE", bidClose);

        final double askHigh =
                Double.parseDouble(parts[this.askHighPosition]);
        final double askLow = Double.parseDouble(parts[this.askLowPosition]);
        final double askOpen =
                Double.parseDouble(parts[this.askOpenPosition]);
        final double askClose =
                Double.parseDouble(parts[this.askClosePosition]);
        final long askVolume = Long.parseLong(parts[this.askVolumePosition]);

        this.sendAskToMarkets(moment, symbol + "-HIGH", askHigh);
        this.sendAskToMarkets(moment, symbol + "-LOW", askLow);
        this.sendAskToMarkets(moment, symbol + "-OPEN", askOpen);
        this.sendAskToMarkets(moment, symbol + "-CLOSE", askClose);
    }
}
