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

import com.metaos.market.source.*;

/**
 * Simple processor: only open-close-low-high-volume.
 */
public class CSVSingleSource extends BasicPricesSource {
    private boolean isClosed = false;
    private boolean nomore = false;
    private final BufferedReader fileReader;
    private final int datePosition, highPosition, lowPosition,
            openPosition, closePosition, volumePosition;
    private final DateFormat dateFormat;
    private final List<String> symbolAsList;
    private final Pattern pattern;
    private final int maxParts;
    protected final String symbol;


    /**
     * To be used by extending classes.
     */
    public CSVSingleSource(final String symbol, final String filePath, 
            final DateFormat dateFormat, final Pattern linePattern,
            final int datePosition, final int highPosition, 
            final int lowPosition, final int openPosition, 
            final int closePosition, final int volumePosition, 
            final int maxParts) throws IOException {
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
        this.maxParts = maxParts;
    }


    public boolean test(final String sample, final int field, 
            final String value) {
        final Matcher matcher = this.pattern.matcher(sample);
        if(matcher.matches()) {
            return value.equals(matcher.group(field+1));
        } else {
            throw new RuntimeException("Line does not match pattern");
        }
    }


    public final void run() {
        while(this.next());
    }


    public final boolean next() {
        if(this.isClosed) return false;
        if(this.nomore) return false;
        try {
            final String line = fileReader.readLine();
            if(line==null) {
                this.nomore = true;
                return false;
            }

            final String[] parts = new String[this.maxParts];
            final Matcher matcher = this.pattern.matcher(line);
            if(matcher.matches()) {
                for(int i=0; i<this.maxParts; i++) {
                    parts[i] = matcher.group(i+1);
                }
            } else {
                final boolean tmp = this.next();
                if(!tmp) this.nomore = true;
                return tmp;
            }

            final Date date = this.dateFormat.parse(parts[this.datePosition]);
            final Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(date.getTime());

            try {
                this.processLine(parts, moment);
                this.notifyListeners(moment, symbolAsList);
            } catch(NumberFormatException nfe) {
                // Ok, no problem, ignore the line.
            }

            return true;
        } catch(Exception e) {
e.printStackTrace();
            this.nomore = true;
            return false;
        }
    }


    public final void close() {
        this.isClosed = true;
        try {
            this.fileReader.close();
        } catch(IOException ioe) {
            // Maybe nothing should happen in this case.... don't worry.
        }
    }

    //
    // HOOK Area.-----------------------------
    //

    /**
     * Hook method for each subclass.
     * ATENTION: remember to call super.processLine()
     */
    protected void processLine(final String parts[], final Calendar moment) {
        final double high =
                Double.parseDouble(parts[this.highPosition]);
        final double low = Double.parseDouble(parts[this.lowPosition]);
        final double open =
                Double.parseDouble(parts[this.openPosition]);
        final double close =
                Double.parseDouble(parts[this.closePosition]);
        final long volume = Long.parseLong(parts[this.volumePosition]);

        this.sendPriceToMarkets(moment, symbol + "-HIGH", high); 
        this.sendPriceToMarkets(moment, symbol + "-LOW", low); 
        this.sendPriceToMarkets(moment, symbol + "-OPEN", open); 
        this.sendPriceToMarkets(moment, symbol + "-CLOSE", close); 
        this.sendVolumeToMarkets(moment, symbol, volume); 
    }
}
