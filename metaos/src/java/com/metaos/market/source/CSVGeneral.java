/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.market.source;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.metaos.market.source.csv.*;

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
     * Creates a Source of data from a CSV with only one symbol.
     * @param symbol symbol name, to send messages to markets.
     * @param filePath complete path to CSV file of prices.
     * @param dateFormat in format described by 
     *      <i>java.util.text.SimpleDateFormat</i>, date present in CSV.
     * @param linePattern REGEXP pattern describing groups of elements for
     *      each line. For example, to parse lines like
     *      <code>2011.07.01,10:15,1.234,4.567.7.890,10,111,234</code>
     *      pattern should look like <code>([0-9]{4}.[0-9]{2}.[0-9]{2},[0-9]{2}:[0-9]{2}),(.*),(.*),(.*),(.*),(.*)</code>.
     * @param fieldSet list of positions of elements into each line.
     *      In the previous example, <code>{DATE, OPEN, HIGH, LOW, CLOSE, 
     *      VOLUME}</code>.
     */
    public PricesSource simpleContinuousSingleSource(final String symbol,
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
                datePos, highPos, lowPos, openPos, closePos, volumePos,
                fieldSet.length);
    }


    /**
     * Creates a Source of data from a CSV with only one symbol for prices,
     * ask and bid prices and VWAP indicator.
     * @param symbol symbol name, to send messages to markets.
     * @param filePath complete path to CSV file of prices.
     * @param dateFormat in format described by 
     *      <i>java.util.text.SimpleDateFormat</i>, date present in CSV.
     * @param linePattern REGEXP pattern describing groups of elements for
     *      each line. 
     * @param fieldSetPrices list of positions of PRICE elements into each line.
     * @param fieldSetBid list of positions of BID elements into each line.
     * @param fieldSetAsk list of positions of ASK elements into each line.
     * @param vwapField position of VWAP element.
     */
    public PricesSource vwapContinuousSingleSource(final String symbol,
            final String filePath, final String dateFormat, 
            final String linePattern, final Fields fieldSetPrices[],
            final Fields fieldSetBid[], final Fields fieldSetAsk[],
            final int vwapFieldPosition) throws IOException {
        if(fieldSetBid.length != fieldSetAsk.length
                || fieldSetBid.length != fieldSetPrices.length
                || fieldSetAsk.length != fieldSetPrices.length) {
            throw new RuntimeException("Incorrect usage: \n"
                    + "fieldSetPrices, fieldSetBid, fieldSetAsk must have "
                    + "same sizes");
        }

        int highPos=0, lowPos=0, openPos=0, closePos=0, datePos=0, volumePos=0;
        int bidHighPos=0, bidLowPos=0, bidOpenPos=0, bidClosePos=0, 
                bidVolumePos=0;
        int askHighPos=0, askLowPos=0, askOpenPos=0, askClosePos=0, 
                askVolumePos=0;
        for(int i=0; i<fieldSetPrices.length; i++) {
            switch(fieldSetPrices[i]) {
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
                case IGNORE:
                    break;
                default:
                    throw new RuntimeException("Not understood field " 
                            + fieldSetPrices[i] + " in this kind of sources");
            }
        }
 
        for(int i=0; i<fieldSetBid.length; i++) {
            switch(fieldSetBid[i]) {
                case LOW:
                    bidLowPos = i;
                    break;
                case HIGH:
                    bidHighPos = i;
                    break;
                case OPEN:
                    bidOpenPos = i;
                    break;
                case CLOSE:
                    bidClosePos = i;
                    break;
                case VOLUME:
                    bidVolumePos = i;
                    break;
                case IGNORE:
                    break;
                default:
                    throw new RuntimeException("Not understood field " 
                            + fieldSetBid[i] + " in this kind of sources");
            }
        }
 
        for(int i=0; i<fieldSetAsk.length; i++) {
            switch(fieldSetAsk[i]) {
                case LOW:
                    askLowPos = i;
                    break;
                case HIGH:
                    askHighPos = i;
                    break;
                case OPEN:
                    askOpenPos = i;
                    break;
                case CLOSE:
                    askClosePos = i;
                    break;
                case VOLUME:
                    askVolumePos = i;
                    break;
                case IGNORE:
                    break;
                default:
                    throw new RuntimeException("Not understood field " 
                            + fieldSetAsk[i] + " in this kind of sources");
            }
        }
        return new CSVVWAPBidOfferSingleSource(symbol, filePath, 
                new SimpleDateFormat(dateFormat), 
                Pattern.compile(linePattern), datePos, 
                highPos, lowPos, openPos, closePos, volumePos,
                bidHighPos, bidLowPos, bidOpenPos, bidClosePos, bidVolumePos,
                askHighPos, askLowPos, askOpenPos, askClosePos, askVolumePos,
                vwapFieldPosition, fieldSetPrices.length);
    }
}
