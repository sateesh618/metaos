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
import java.util.logging.Logger;

import com.metaos.market.*;

/**
 * Line processor for CSV files.
 *
 * <b>Not thread safe</b>
 */
public class CSVSourceLineProcessor implements SourceLineProcessor {
    private final Format[] formatters;
    private final String[] fieldNames;
    private final ParsePosition[] parsePositions;
    private final int symbolIndex, dateIndex;
    private final Map<String, Double> parsedValues;

    private String parsedLine;
    private String parsedSymbol;
    private Calendar parsedCalendar;
    private boolean parsingResult;
    
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
    public CSVSourceLineProcessor(final Format formatters[],
            final String[] fieldNames, final int symbolIndex, 
            final int dateIndex) {
        assert (fieldNames.length == formatters.length);
        assert (symbolIndex != dateIndex);
        assert (symbolIndex < fieldNames.length);
        assert (dateIndex < fieldNames.length);

        this.parsedValues = new HashMap<String, Double>();
        this.dateIndex = dateIndex;
        this.symbolIndex = symbolIndex;
        this.fieldNames = new String[fieldNames.length];
        this.formatters = new Format[formatters.length];
        this.parsePositions = new ParsePosition[formatters.length];
        for(int i=0; i<parsePositions.length; i++) {
            this.formatters[i] = formatters[i];
            this.fieldNames[i] = fieldNames[i];
            this.parsePositions[i] = new ParsePosition(0);
        }
    }


    public boolean isValid(final String line) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
        }
        return this.parsedSymbol!=null && this.parsedCalendar!=null
                && this.parsingResult;
    }


    public void process(final String line) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
        }

        if(this.parsedSymbol!=null && this.parsedCalendar!=null) {
            for(final Map.Entry<String, Double> entry
                    : this.parsedValues.entrySet()) {
                // NOTIFIES TO LISTENERS this.parsedSymbol+"-"+entry.getKey()
            }
        }
    }


    public void addListener(final PricesObserver listener, 
            final boolean highPriority) {
    }


    public void concludeLineSet() {
        // NOTIFIES TO LISTENERS THE END OF THE SET LINE
    }


    public String getSymbol(final String line) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
        }
        return this.parsedSymbol;
    }


    public Calendar getDate(final String line) {
        if( ! line.equals(this.parsedLine) ) {
            _parseLine(line);
        }
        return this.parsedCalendar;
    }


    //
    // Private stuff ----------------------------------------------
    //

    /**
     * Modifies internal values trying to parse given line.
     */
    private void _parseLine(final String line) {
        this.parsedValues.clear();
        this.parsedLine = line;
        this.parsedCalendar = null;
        this.parsedSymbol = null;
        this.parsingResult = false;

        final String parts[] = line.split(",");
        boolean allLineProcessed = true;
        for(int i=0; i<parts.length; i++) {
            if(this.fieldNames[i] != null) {
                final Object obj = this.formatters[i].parseObject(parts[i],
                        this.parsePositions[i]);
                if(obj instanceof String) {
                    if(i==this.symbolIndex) {
                        this.parsedSymbol = (String) obj;
                    }
                } else if(obj instanceof Double) {
                   this.parsedValues.put(this.fieldNames[i], (Double) obj); 
                } else if(obj instanceof Date) {
                    if(i==this.dateIndex) {
                        this.parsedCalendar = Calendar.getInstance();
                        this.parsedCalendar.setTimeInMillis(
                                ((Date) obj).getTime());
                    }
                } else {
                    // Unknown type
                    allLineProcessed = false;
                }
            }
        }

        this.parsingResult = allLineProcessed;
    }
}
