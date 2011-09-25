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
 * Line processor.
 */
public interface LineParser {
    /**
     * Parses a line and remembers it.
     * @return true if the whole line has been processed, false if some
     * fields are invalid.
     */
    public ParseResult parse(final String line);

    /**
     * Tests if provided line is valid in some sense.
     */
    public boolean isValid(final String line);

    /**
     * Subscribe a listener to concluding line events that will receive the
     * list of pairs with symbol and prices.
     * The set of <i>Listener</i>s will be notified after the set of
     * <i>CacheWriteable</i> has been invoked.
     * @see #addListener
     * @return this object to chain adding several commands.
     */
    public LineParser addCacheWriteable(final CacheWriteable listener);

    /**
     * Analyzes the line getting the symbol, but not reporting to listeners
     * the result.
     * @param index 0 for the first symbol of the line, 1 for the second...
     * @return null if there is no such symbol.
     */
    public String getSymbol(final String line, final int index);

    /**
     * Analyzes the line getting the timestamp, but not reporting to listeners
     * the result.
     * @return null if there is no timestamp information
     */
    public Calendar getTimestamp(final String line);


    /**
     * Adds a filter to validate the line.
     * @return the same object to chain adding filters.
     */
    public LineParser addFilter(final Filter filter);
}
