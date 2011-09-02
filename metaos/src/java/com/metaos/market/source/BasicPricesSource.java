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
 * Basic implementation of prices source.
 */
public abstract class BasicPricesSource implements PricesSource {
    private final Set<MarketListener> markets = new HashSet<MarketListener>();
    private final Set<MarketObserver> otherObservers = 
            new HashSet<MarketObserver>();

    /**
     * Adds a marekt listener.
     */
    public void addMarketListener(final MarketListener market) {
        markets.add(market);
    }


    /**
     * Adds a general listener.
     */
    public void addListener(final MarketObserver observer) {
        this.otherObservers.add(observer);
    }


    public boolean test(final String sample, final int field, 
            final String value) {
        throw new UnsupportedOperationException("Method not implemented");
    }



    //
    // Utility methods ------------------------------
    //

    /**
     * Notifies to everyone is looking for good news...
     */
    protected void notifyListeners(final Calendar when, 
            final List<String> products) {
        for(final MarketObserver observer : otherObservers) {
            observer.update(products, when);
        }
    }

    /**
     * Reports new price for listening markets.
     */
    protected void sendPriceToMarkets(final Calendar when,
            final String product,final double price) {
        for(final MarketListener market : markets) {
            market.setPrice(when, product, price);
        }
    }

    /**
     * Reports new ask price for listening markets.
     */
    protected void sendAskToMarkets(final Calendar when,
            final String product,final double price) {
        for(final MarketListener market : markets) {
            market.setAsk(when, product, price);
        }
    }

    /**
     * Reports new bid price for listening markets.
     */
    protected void sendBidToMarkets(final Calendar when,
            final String product, final double price) {
        for(final MarketListener market : markets) {
            market.setBid(when, product, price);
        }
    }

    /**
     * Reports new price for listening markets.
     */
    protected void sendVolumeToMarkets(final Calendar when,
            final String product, final long how) {
        for(final MarketListener market : markets) {
            market.setVolume(when, product, how);
        }
    }


}
