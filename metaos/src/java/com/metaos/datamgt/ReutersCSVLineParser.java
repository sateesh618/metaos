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
 * Line processor for Reuters CSV files with only one date and symbol per line.
 *
 * <b>Not thread safe</b>
 */
public class ReutersCSVLineParser extends CSVLineParser {
    /**
     * Creates a parser for a CSV file according to its header line.
     * @param filePath complete or relative path to file to parse.
     */
    public ReutersCSVLineParser(final String filePath) throws IOException {
        super(this.getFormatters(filePath), this.getFieldNames(filePath), 
                this.getSymbolIndex(filePath), this.getDateIndexes(filePath));
    }

    

    //
    // Private stuff ------------------ only useful for construction
    //
    private String filePath;
    private Format[] formatters;
    private Field[] fieldNames;
    private int symbolIndex;
    private List<Integer> dateIndexes;

    private Format[] getFormatters(final String filePath) 
            throws IOException {
        if( ! filePath.equals(this.filePath)) {
            parseHeader(filePath);
        }
        return this.formatters;
    }

    private Filed[] getFieldNames(final String filePath) 
            throws IOException {
        if( ! filePath.equals(this.filePath)) {
            parseHeader(filePath);
        }
        return this.fieldNames;
    }

    private int getSymbolIndex(final String filePath) 
            throws IOException {
        if( ! filePath.equals(this.filePath)) {
            parseHeader(filePath);
        }
        return this.symbolIndex;
    }

    private int[] getDateIndexes(final String filePath) 
            throws IOException {
        if( ! filePath.equals(this.filePath)) {
            parseHeader(filePath);
        }
        return this.dateIndexes.toArray();
    }


    private void parseHeader(final String filePath) throws IOException {
        final BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        final String firstLine = reader.readLine();
        final String[] parts = firstLine.split(",");
        this.formatters = new Format[parts.length];
        this.fields = new Field[parts.length];
        this.dateIndexs = new ArrayList<Integer>();
        for(int i=0; i<parts.length; i++) {
            parts[i] = // Quitar la #
            if(parts[i].equals("RIC")) {
                this.symbolIndex = i;
            } else if(parts[i].equals("Date[G]")) {
                this.dateIndexes.add(i);
            } else if(parts[i].equals("Time[G]")) {
                this.dateIndexes.add(i);
            }
            formatters[i] = formattersMap.get(parts[i]);
            if(formatters[i]==null) {
                formatters[i] = doubleFormat;
                fields[i] = EXTENDED(NONE, parts[i]);
            } else {
                fields[i] = fieldsMap.get(parts[i]);
            }
        }
        reader.close();
    }


    private static final Map<String, Format> formattersMap = 
            new HashMap<String, Format>();
    private static final Map<String, Field> fieldsMap = 
            new HashMap<String, Field>();

    static {
        fieldsMap.put("Ask Price", CLOSE(ASK));
        fieldsMap.put("Ask Size", VOLUME(ASK));
        fieldsMap.put("Ask Size", VOLUME(BID));
        fieldsMap.put("Ave.Price", EXTENDED(NONE,"Ave.Price"));
        fieldsMap.put("Bid Price", CLOSE(BID));
        fieldsMap.put("Close Ask", CLOSE(ASK));
        fieldsMap.put("Close Bid", CLOSE(BID));
        fieldsMap.put("Close", CLOSE());
        fieldsMap.put("High Ask", HIGH(ASK));
        fieldsMap.put("High Bid", HIGH(BID));
        fieldsMap.put("High", HIGH());
        fieldsMap.put("Last", CLOSE());
        fieldsMap.put("Low Ask", LOW(ASK));
        fieldsMap.put("Low Bid", LOW(BID));
        fieldsMap.put("Low", Low());
        fieldsMap.put("No. Asks", EXTENDED(ASK, "No."));
        fieldsMap.put("No. Trades", EXTENDED(BID, "No."));
        fieldsMap.put("No. Trades", EXTENDED(NONE,"No."));
        fieldsMap.put("Open Ask", OPEN(ASK));
        fieldsMap.put("Open Bid", OPEN(BID));
        fieldsMap.put("Open", OPEN());
        fieldsMap.put("Price", CLOSE());
        fieldsMap.put("VWAP", EXTENDED(NONE,"VWAP"));
        fieldsMap.put("Volume", VOLUME());
        formattersMap.put("Ask Price", doubleFormat);
        formattersMap.put("Ask Size", doubleFormat);
        formattersMap.put("Ave.Price", doubleFormat);
        formattersMap.put("Bid Price", doubleFormat);
        formattersMap.put("Bid Size", doubleFormat);
        formattersMap.put("Close Ask", doubleFormat);
        formattersMap.put("Close Bid", doubleFormat);
        formattersMap.put("Close", doubleFormat);
        formattersMap.put("Date[G]", dateFormat);
        formattersMap.put("High Ask", doubleFormat);
        formattersMap.put("High Bid", doubleFormat);
        formattersMap.put("High", doubleFormat);
        formattersMap.put("Last", doubleFormat);
        formattersMap.put("Low Ask", doubleFormat);
        formattersMap.put("Low Bid", doubleFormat);
        formattersMap.put("Low", doubleFormat);
        formattersMap.put("No. Asks", doubleFormat);
        formattersMap.put("No. Bids", doubleFormat);
        formattersMap.put("No. Trades", doubleFormat);
        formattersMap.put("Open Ask", doubleFormat);
        formattersMap.put("Open Bid", doubleFormat);
        formattersMap.put("Open", doubleFormat);
        formattersMap.put("Price", doubleFormat);
        formattersMap.put("RIC", textFormat);
        formattersMap.put("Time[G]", timeFormat);
        formattersMap.put("Type", textFormat);
        formattersMap.put("VWAP", doubleFormat);
        formattersMap.put("Volume", doubleFormat);
    }
}
