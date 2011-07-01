/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.juant.market.source;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;

import com.juant.market.*;

/**
 * Prices source.
 */
public interface PricesSource {
    /**
     * Adds a marekt listener.
     */
    public void addMarketListener(final MarketListener market);

    /**
     * Adds a general listener.
     */
    public void addListener(final MarketObserver observer);

    /**
     * Starts source to get all prices.
     */
    public void run();

    /**
     * Gets the next set of prices.
     */
    public boolean next();

    /**
     * Closes the source.
     * After calling this method, every invokation to <i>next()</i> will
     * return false.
     */
    public void close();
}
