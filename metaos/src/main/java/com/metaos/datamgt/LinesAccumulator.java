/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.datamgt;

/**
 * 
 */
public interface LinesAccumulator {
    /**
     * Memorizes the result and consider if "end of accumulation" event
     * should be notified.
     */
    public void accumulate(final ParseResult result);

    /**
     * Ends forced accumulation process and notifies to listeners.
     */
    public void endAccumulation();

    /**
     * Subscribes a listener to "end of accumulation" events.
     */
    public void addListener(final Listener listener);


    /**
     * Empties internal memory.
     */
    public void reset();
}
