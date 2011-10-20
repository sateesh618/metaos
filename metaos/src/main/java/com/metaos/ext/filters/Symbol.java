/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.ext.filters;

import com.metaos.datamgt.Field;
import com.metaos.datamgt.Filter;
import java.util.Calendar;
import java.util.Map;

/**
 * Filter lines containing information on a given symbol.
 */
public class Symbol implements Filter {
    private final String symbol;
    
    /**
     * Creates the filtor for given symbol.
     */
    public Symbol(final String symbol) {
        this.symbol = symbol;
    }

    /**
     * Tests if given set of prices for the symbol is valid.
     * @return true if set of prices is valid, false if should be ignored.
     */
    public boolean filter(final Calendar when, final String symbol,
            final Map<Field, Double> values) {
        return this.symbol.equals(symbol);
    }
}
