/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.datamgt;

import java.util.*;
import com.metaos.util.*;

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
     * Gets local time calendar (with adjusted time zone) for parsed date.
     * @return eturns null if calendar is reset and has not been set.
     */
    public Calendar getLocalTimestamp() { return this.calendar; }


    /**
     * Returns A COPY of the local timestamp set to GMT+0
     */
    public Calendar getUTCTimestampCopy() { 
        final Calendar cloned = (Calendar) this.calendar.clone();
        cloned.setTimeZone(TimeZone.getTimeZone("GMT"));
        return cloned;
    }


    /**
     * Creates a new calendar reset to time zero.
     */
    public void newTimestamp() {
        this.calendar = CalUtils.getZeroCalendar();
    }


    /** 
     * Stores the value for the given field associated to 
     * last inserted symbol.
     * If no symbol has been inserted yet, an exception occurs.
     */
    public void putValue(final Field field, final double val) {
        final String symbol = symbols.get(symbols.size()-1);
        Map<Field, Double> vs = values.get(symbol);
        if(vs==null) vs = new HashMap<Field, Double>();
        vs.put(field, val);
        this.values.put(symbol, vs);
    }


    /** 
     * Stores the value for the given field associated to 
     * given symbol.
     * Be careful: If symbol has not been previously inserted, object will
     * become incoherent.
     */
    public void putValue(final String symbol, final Field field, 
            final double val) {
        Map<Field, Double> vs = values.get(symbol);
        if(vs==null) vs = new HashMap<Field, Double>();
        vs.put(field, val);
        this.values.put(symbol, vs);
    }


    /**
     * Gets the list of parsed values for the given symbol
     * @return null if object has no news about given symbol or an empty
     * map if symbol has been inserted but no elements have been associated.
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
        this.values.put(symbol, new HashMap<Field, Double>());
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
