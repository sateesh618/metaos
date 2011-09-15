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
    public Calendar getTimestamp() { return this.calendar; }


    /**
     * Creates a new calendar reset to time zero.
     */
    public void newTimestamp() {
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
     * Gets the list of parsed values for the given symbol
     */
    public Map<Field, Double> values(final String symbol) {
        return values.get(symbol);
    }


    /**
     * Gets the list of parsed values for the symbol at given position.
     */
    public Map<Field, Double> values(final int symbolPos) {
        return values.get(this.getSymbol(symbolPos));
    }


    /**
     * Adds new parsed symbol.
     */
    public void addSymbol(final String symbol) {
        this.symbols.add(symbol);
    }


    /**
     * Gets the list of parsed symbols.
     */
    public List<String> getSymbols() {
        return Collections.unmodifiableList(this.symbols);
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
