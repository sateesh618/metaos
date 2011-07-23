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
import java.util.logging.Level;
import java.util.logging.Logger;
import com.metaos.market.MarketListener;


/**
 * Joins into one source several sources.
 */
public class CompossedSource extends BasicPricesSource {
    private static final Logger log = Logger.getLogger(
            CompossedSource.class.getPackage().getClass().getName());

    private final Map<String, Double>[] lastBid;
    private final Map<String, Double>[] lastAsk;
    private final Map<String, Double>[] lastPrice;
    private final Map<String, Long>[] lastVolume;
    private final long nextMilliseconds[];
    private final PricesSource sources[];
    private boolean nomore;

    
    public CompossedSource(final PricesSource sources[]) {
        this.nomore = false;
        this.sources = sources;
        this.lastBid = new Map[sources.length];
        this.lastAsk = new Map[sources.length];
        this.lastPrice = new Map[sources.length];
        this.lastVolume = new Map[sources.length];
        this.nextMilliseconds = new long[sources.length];

        for(int i=0; i<sources.length; i++) {
            this.lastBid[i] = new HashMap<String, Double>();
            this.lastAsk[i] = new HashMap<String, Double>();
            this.lastPrice[i] = new HashMap<String, Double>();
            this.lastVolume[i] = new HashMap<String, Long>();
            final int j = i;
            final MarketListener listener = new MarketListener() {
                public void setPrice(final Calendar when, final String what,
                        final double how) {
                    final long m = when.getTimeInMillis();
                    if(nextMilliseconds[j]<m) {
                        nextMilliseconds[j] = m;
                        lastPrice[j].clear();
                        lastBid[j].clear();
                        lastAsk[j].clear();
                        lastVolume[j].clear();
                    }
                    lastPrice[j].put(what, how);
                }
                public void setBid(final Calendar when,final String what,
                        final double how) {
                    final long m = when.getTimeInMillis();
                    if(nextMilliseconds[j]<m) {
                        nextMilliseconds[j] = m;
                        lastPrice[j].clear();
                        lastBid[j].clear();
                        lastAsk[j].clear();
                        lastVolume[j].clear();
                    }
                    lastBid[j].put(what, how);
                }
                public void setAsk(final Calendar when,final String what,
                        final double how) {
                    final long m = when.getTimeInMillis();
                    if(nextMilliseconds[j]<m) {
                        nextMilliseconds[j] = m;
                        lastPrice[j].clear();
                        lastBid[j].clear();
                        lastAsk[j].clear();
                        lastVolume[j].clear();
                    }
                    lastAsk[j].put(what, how);
                }
                public void setVolume(final Calendar when, final String what,
                        final long how) {
                    final long m = when.getTimeInMillis();
                    if(nextMilliseconds[j]<m) {
                        nextMilliseconds[j] = m;
                        lastPrice[j].clear();
                        lastBid[j].clear();
                        lastAsk[j].clear();
                        lastVolume[j].clear();
                    }
                    lastVolume[j].put(what, how);
                }
            };
            sources[i].addMarketListener(listener);
        }

        for(int i=0; i<sources.length; i++) {
            sources[i].next();
        }
    }

    public void run() {
        while(this.next());
    }
    

    /**
     * Reads next line from sources and notifies markets and susbscribed 
     * observers.
     * @return true if more lines would be available, false if there was
     * no possible to read the line, since the EOF has been reached for all
     * sources.
     */
    public boolean next() {
        if(nomore) return false;
        try {
            final boolean inTheFuture[] = new boolean[sources.length];
            long minTime = nextMilliseconds[0];
            int i0;
            for(i0 = 0; i0<this.sources.length; i0++) {
                if(this.sources[i0]!=null) {
                    minTime = nextMilliseconds[i0];
                    break;
                }
            }
            for(int i=i0+1; i<this.sources.length; i++) {
                if(this.sources[i]==null) continue;
                if(this.nextMilliseconds[i]>minTime) inTheFuture[i] = true;
                else if(this.nextMilliseconds[i]<minTime) {
                    minTime = this.nextMilliseconds[i];
                    for(int j=0; j<i; j++) inTheFuture[j] = true;
                }
            }

            boolean somethingToReturn = false;
            final Set<String> processedSymbols = new HashSet<String>();
            final Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(minTime);
            for(int i=0; i<this.sources.length; i++) {
                if(this.sources[i]==null) continue;
                if(inTheFuture[i]) {
                    somethingToReturn = true;
                } else {
                    // Notify prices from the source.
                    for(final Map.Entry<String, Double> entry 
                            : this.lastBid[i].entrySet()) {
                        this.sendBidToMarkets(moment, entry.getKey(),
                                entry.getValue());
                        processedSymbols.add(entry.getKey());
                    }
                    for(final Map.Entry<String, Double> entry 
                            : this.lastAsk[i].entrySet()) {
                        this.sendAskToMarkets(moment, entry.getKey(),
                                entry.getValue());
                        processedSymbols.add(entry.getKey());
                    }
                    for(final Map.Entry<String, Double> entry 
                            : this.lastPrice[i].entrySet()) {
                        this.sendPriceToMarkets(moment, entry.getKey(),
                                entry.getValue());
                        processedSymbols.add(entry.getKey());
                    }
                    for(final Map.Entry<String, Long> entry 
                            : this.lastVolume[i].entrySet()) {
                        this.sendVolumeToMarkets(moment, entry.getKey(),
                                entry.getValue());
                        processedSymbols.add(entry.getKey());
                    }
                    
                    // Notify prices from the source.
                    final boolean moreForThisSource = this.sources[i].next();
                    if( ! moreForThisSource ) {
                        this.sources[i].close();
                        this.sources[i] = null;
                    } else {
                        somethingToReturn = true;
                    }
                }
            }


            if(!processedSymbols.isEmpty()) {
                this.notifyListeners(moment, new ArrayList<String>(
                        processedSymbols));
            }
            if( ! somethingToReturn ) nomore = true;

            return somethingToReturn;
        } catch(Exception e) {
            log.log(Level.SEVERE, "Exception dealing with source in "
                    + "compossed source reader", e);
            return false;
        }
    }


    public void close() {
        for(int i=0; i<this.sources.length; i++) {
            if(this.sources[i]!=null) {
                this.sources[i].close();
                this.sources[i] = null;
            }
        }
    }
}
