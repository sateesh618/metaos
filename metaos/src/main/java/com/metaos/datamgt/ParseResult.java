/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.datamgt;

import java.util.*;
import java.util.logging.*;
import com.metaos.util.*;

/**
 * Easy way to store and retreive parsed data from a line.
 */
public class ParseResult {
    private static final Logger log = Logger.getLogger(ParseResult.class
            .getPackage().getName());

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

        // Consistency check.
        for(int i=0; i<this.symbols.size(); i++) {
            if(this.symbols.get(i).equals(symbol)) return;
        }
        log.severe("Danger: inconsistent usage of ParseResult object. "
                + "'putValue' function should be called AFTER 'addSymbol'. "
                + "Read documentation");
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


    /**
     * Gets given data into this object.
     */
    public void merge(final ParseResult other) {
        if(this.getLocalTimestamp()==null) {
            this.calendar = (Calendar) other.calendar.clone();
        }
        if(!other.getLocalTimestamp().equals(this.getLocalTimestamp())) {
            throw new IllegalArgumentException("Cannot merge result data in "
//            System.out.println("Cannot merge result data in "
                   + "different times (" + other.getLocalTimestamp() 
                   + " is not as expected " + this.getLocalTimestamp() + ")\n"
                   + "Original result is related to " + this.symbols + " and "
                   + "unexpected result is related to " + other.symbols);
        }

        for(Map.Entry<String,Map<Field,Double>> symbolVals : 
                other.values.entrySet()) {
            boolean symbolFound = false;
            for(int i=0; i<this.symbols.size(); i++) {
                if(this.symbols.get(i).equals(symbolVals.getKey())) {
                    symbolFound = true;
                    break;
                }
            }
            if(!symbolFound) this.addSymbol(symbolVals.getKey());

            for(Map.Entry<Field, Double> fieldValue : 
                    symbolVals.getValue().entrySet()) {
                this.putValue(symbolVals.getKey(), fieldValue.getKey(), 
                        fieldValue.getValue());
            }
        }
    }


    /**
     * Clones result COPYING all data into new variables.
     */
    public Object clone() {
        final ParseResult cloned = new ParseResult();
        cloned.calendar = (Calendar) this.calendar.clone();

        for(int i=0; i<this.symbols.size(); i++) {
            cloned.symbols.add(this.symbols.get(i));
        }

        for(final Map.Entry<String, Map<Field, Double>> entry1 : 
                this.values.entrySet()) {
            final Map<Field, Double> vals = new HashMap<Field, Double>();
            for(final Map.Entry<Field,Double> entry2 :
                    entry1.getValue().entrySet()) {
                vals.put(entry2.getKey(), entry2.getValue());
            }
            cloned.values.put(entry1.getKey(), vals);
        }

        return cloned;
    }


    /**
     * Logging purposes.
     */
    public String toString() {
        return "ParseResult:[moment=" + this.calendar + ", values="
                + this.values + "]";
    }
}
