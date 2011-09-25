/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.datamgt;

import java.util.*;

/**
 * The way to not accumulate anything: notifies to listener after every
 * line is accumulated.
 */
public class ZeroAccumulator implements LinesAccumulator {
    private final List<Listener> listeners;

    /**
     * Creates a zero capacity accumulator.
     */
    public ZeroAccumulator() {
        this.listeners = new ArrayList<Listener>();
    }

    /**
     * Memorizes the result and consider if "end of accumulation" event
     * should be notified.
     */
    public void accumulate(final ParseResult result) {
        for(final Listener listener : this.listeners) {
            listener.notify(result);
        }
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
    }

    /**
     * Subscribes a listener to "end of accumulation" events.
     */
    public void addListener(final Listener listener) {
        this.listeners.add(listener);
    }
}
