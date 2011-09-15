/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.datamgt;

import java.util.*;

/**
 * Easy way to store and retreive parsed data from a line.
 */
public class ParseResult {
    private final Map<String, Map<Field, Double>> values;
    private final List<String> symbols;
    private Calendar calendar;

    /**
     * Creates an empty result set.
     */
    public ParseResult() {
        this.values = new HashMap<String,Map<Field, Double>>();
        this.symbols = new ArrayList<String>();
        this.calendar = null;
    }

    /**
     * Returns null if there is no symbol at given position.
     */
    public String getSymbol(final int pos) { 
        if(pos>this.symbols.size() || pos<0) return null;
        else return this.symbols.get(pos);
    }

    /**
     * Returns null if calendar is reset and has not been set.
     */
    public Calendar getCalendar() { return this.calendar; }

    /**
     * Creates a new calendar reset to time zero.
     */
    public void newCalendar() {
        this.calendar = Calendar.getInstance();
        this.calendar.setTimeInMillis(0);
    }

    /** 
     * Stores the value for the given field associated to 
     * last inserted symbol symbol.
     * If no symbol has been inserted yet, an exception occurs.
     */
    public void putValue(final Field field, final double val) {
        final String symbol = symbols.get(symbols.size());
        Map<Field, Double> vs = values.get(symbol);
        if(vs==null) vs = new HashMap<Field, Double>();
        vs.put(field, val);
        this.values.put(symbol, vs);
    }


    /**
     * Adds new parsed symbol.
     */
    public void addSymbol(final String symbol) {
        this.symbols.add(symbol);
    }

    /**
     * Empties all values.
     */
    public void reset() {
        this.values.clear();
        this.calendar = null;
        this.symbols.clear();
    }
}
