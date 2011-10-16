/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.datamgt;

import java.util.*;
import com.metaos.datamgt.Field.*;

/**
 * Acumulates sequential trades to create N-minutes trades.
 * Results are typical Open-Close-Hifg-Low-TotalVolume information for
 * a block of minutes (usuarlly 1,5,10,30,60 minutes).
 */
public class BlocksOfMinutesSTMgr implements SpreadTradesMgr {
    private final List<Listener> listeners;
    private final List<ParseResult> memory;
    private long lastTimestamp;
    private final long accumulationWindow;

    /**
     * Creates accumulator of windows of given size in minutes.
     * @param minutesWindowSize size of window in minutes. 
     */
    public BlocksOfMinutesSTMgr(final int minutesWindowSize) {
        this.listeners = new ArrayList<Listener>();
        this.memory = new ArrayList<ParseResult>();
        this.lastTimestamp = -1;
        this.accumulationWindow = minutesWindowSize * 60 * 1000;
    }

    /**
     * Memorizes the result and consider if "end of accumulation" event
     * should be notified.
     */
    public void accumulate(final ParseResult result) {
        if(this.lastTimestamp == -1) {
            this.lastTimestamp = result.getLocalTimestamp().getTimeInMillis();
        }

        if(result.getLocalTimestamp().getTimeInMillis()-this.lastTimestamp 
                > this.accumulationWindow) {
            this.endAccumulation();
            this.lastTimestamp = result.getLocalTimestamp().getTimeInMillis();
        }
        this.memory.add(result);
    }


    /**
     * Resets accumulator.
     */
    public void reset() {
        this.listeners.clear();
    }


    /**
     * Ends forced accumulation process and notifies to listeners.
     */
    public void endAccumulation() {
        final ParseResult accumResult = new ParseResult();
   
        for(final ParseResult result : memory) {
            for(final String symbol : result.getSymbols()) {
                if(accumResult.values(symbol)==null) {
                    accumResult.addSymbol(symbol);
                }

                for(final Map.Entry<Field,Double> val : 
                        result.values(symbol).entrySet()) {
                    if(val.getKey() instanceof LOW) {
                        final Double d = accumResult.values(symbol)
                                .get(val.getKey());
                        if(d!=null && d.doubleValue()>val.getValue()) {
                            accumResult.putValue(symbol,val.getKey(), 
                                    val.getValue());
                        }
                    } else if(val.getKey() instanceof HIGH) {
                        final Double d = accumResult.values(symbol)
                                .get(val.getKey());
                        if(d!=null && d.doubleValue()<val.getValue()) {
                            accumResult.putValue(symbol,val.getKey(), 
                                    val.getValue());
                        }
                    } else if(val.getKey() instanceof OPEN) {
                        if(accumResult.values(symbol).get(val.getKey())==null) {
                            accumResult.putValue(symbol,val.getKey(), 
                                    val.getValue());
                        }
                    } else if(val.getKey() instanceof CLOSE) {
                        accumResult.putValue(symbol,val.getKey(),
                                val.getValue());
                    } else if(val.getKey() instanceof VOLUME) {
                        final Double d = accumResult.values(symbol)
                                .get(val.getKey());
                        if(d!=null) {
                            accumResult.putValue(symbol,val.getKey(), 
                                    val.getValue() + d.doubleValue());
                        } else {
                            accumResult.putValue(symbol,val.getKey(), 
                                    val.getValue());
                        }
                    } else {
                        // Don't know what to do...
                        accumResult.putValue(symbol, val.getKey(), 
                                val.getValue());
                    }
                }
            }
        }


        for(final Listener listener : this.listeners) {
            listener.notify(accumResult);
        }
        this.memory.clear();
    }


    /**
     * Subscribes a listener to "end of accumulation" events.
     */
    public void addListener(final Listener listener) {
        this.listeners.add(listener);
    }
}
