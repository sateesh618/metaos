/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.datamgt;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Line processor for CSV files with only one date and symbol per line.
 *
 * <b>Not thread safe</b>
 */
public class CSVLineParser implements LineParser {
    private final Format[] formatters;
    private final Field[] fieldNames;
    private final ParsePosition[] parsePositions;
    private final int symbolIndex, dateIndexes[];
    private final List<CacheWriteable> cacheListeners;
    private final List<Filter> pricesFilters;

    private String parsedLine;
    private ParseResult parsedData;
    private boolean parsingResult;
    private boolean isValid;
    
    /**
     * Creates a parser for CSV files.
     * @param formatters list of formatters to translate string into numbers,
     *      strings or dates.
     * @param fieldNames name of fields to notify listeners, null for fields 
     * that will be ignored.
     * @param symbolIndex index of the previous list of formatters for the
     *      symbol name (should be null in the previous list of fieldNames).
     * @param dateIndex index of the previous list of formatters for the
     *      date of the line (should be null in the previous list of 
     *      fieldNames).
     */
    public CSVLineParser(final Format formatters[],
            final Field[] fieldNames, final int symbolIndex, 
            final int dateIndexes[]) {
        assert (fieldNames.length == formatters.length);
        assert (symbolIndex < fieldNames.length);

        this.cacheListeners = new ArrayList<CacheWriteable>();
        this.pricesFilters = new ArrayList<Filter>();
        this.dateIndexes = dateIndexes;
        this.symbolIndex = symbolIndex;
        this.parsedLine = "";
        this.fieldNames = new Field[fieldNames.length];
        this.formatters = new Format[formatters.length];
        this.parsePositions = new ParsePosition[formatters.length];
        for(int i=0; i<parsePositions.length; i++) {
            this.formatters[i] = formatters[i];
            this.fieldNames[i] = fieldNames[i];
            this.parsePositions[i] = new ParsePosition(0);
        }
        this.parsedData = new ParseResult();
    }


    public boolean isValid(final String line) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
       }
        return this.isValid;
        
    }


    public ParseResult parse(final String line) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
        }

        final String symbol = this.parsedData.getSymbol(0);
        if(symbol!=null) {
            for(final CacheWriteable listener : this.cacheListeners) {
                for(final Map.Entry<Field, Double> entry
                        : this.parsedData.values(symbol).entrySet()) {
                    entry.getKey().notify(listener, 
                            this.parsedData.getTimestamp(),
                            symbol, entry.getValue());
                }
            }
        }
        return this.parsedData;
    }


    public LineParser addFilter(final Filter filter) {
        this.pricesFilters.add(filter);
        return this;
    }


    public LineParser addCacheWriteable(final CacheWriteable listener) {
        this.cacheListeners.add(listener);
        return this;
    }


    public String getSymbol(final String line, final int index) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
        }
        return this.parsedData.getSymbol(index);
    }


    public Calendar getTimestamp(final String line) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
        }
        return this.parsedData.getTimestamp();
    }

    public void reset() {
        this.pricesFilters.clear();
        this.cacheListeners.clear();
        this.parsedData = new ParseResult();
        this.parsedLine = "";
        this.isValid = false;
        this.parsingResult = false;
    }


    //
    // Private stuff ----------------------------------------------
    //

    /**
     * Modifies internal values trying to parse given line.
     */
    private void _parseLine(final String line) {
        this.parsedLine = line;
        this.parsedData.reset();
        this.parsingResult = false;

        final String parts[] = line.split(",");
        boolean anyValuePresent = false;
        for(int i=0; i<parts.length; i++) {
            if(this.formatters[i] != null) {
                try {
                    this.parsePositions[i].setIndex(0);
                    final Object obj = this.formatters[i]
                            .parseObject(parts[i], this.parsePositions[i]);
                    if(obj instanceof Object[]) {
                        if(i==this.symbolIndex) {
                            this.parsedData.addSymbol((String) 
                                    ((Object[])obj)[0]);
                        }
                    } else if(obj instanceof Number) {
                        boolean isFieldIndex = false;
                        double val = ((Number)obj).doubleValue();
                        for(int j=0; j<dateIndexes.length; j++) {
                            if(i==this.dateIndexes[j]) {
                                isFieldIndex = true;
                                if(this.parsedData.getTimestamp()==null) {
                                    this.parsedData.newTimestamp();
                                }
                                
                                if(val>0) {
                                    this.parsedData.getTimestamp()
                                            .setTimeZone(TimeZone.getTimeZone(
                                                    "GMT+"+(int)(val)));
                                } else if(val==0) {
                                    this.parsedData.getTimestamp()
                                            .setTimeZone(TimeZone.getTimeZone(
                                                    "GMT"));
                                } else {
                                    this.parsedData.getTimestamp()
                                            .setTimeZone(TimeZone.getTimeZone(
                                                    "GMT-"+(int)((-val))));
                                }
                            }
                        }
                        if(!isFieldIndex) {
                            this.parsedData.putValue(this.fieldNames[i],val);
                            anyValuePresent = true;
                        }
                    } else if(obj instanceof Date) {
                        for(int j=0; j<dateIndexes.length; j++) {
                            if(i==this.dateIndexes[j]) {
                                if(this.parsedData.getTimestamp()==null) {
                                    this.parsedData.newTimestamp();
                                }
                                this.parsedData.getTimestamp().setTimeInMillis(
                                        this.parsedData.getTimestamp()
                                                .getTimeInMillis() 
                                        + ((Date) obj).getTime());
                                break;
                            }
                        }
                    } else {
                        // Unknown type
                    }
                } catch(Exception e) {
                    // Ok, don't worry, nothing matters
                }
            }
        }
        this.parsingResult = anyValuePresent;


        this.isValid = this.parsedData.getSymbol(0) != null 
                    && this.parsedData.getTimestamp() != null
                    && this.parsingResult;
        if(this.isValid) {
            for(final Filter f : this.pricesFilters) {
                if( ! f.filter(this.parsedData.getTimestamp(),
                            this.parsedData.getSymbol(0), 
                            this.parsedData.values(0))) {
                    this.isValid = false;
                    break;
                }
            }
        }
    }
}
