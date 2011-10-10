/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.ext;

import java.util.*;
import com.metaos.datamgt.*;
import com.metaos.util.*;
import com.metaos.signalgrt.predictors.*;

/**
 * Agent to perform a backtesting for a predictor.
 *
 * Usage: add as listener to SpreadTradesManager. Be careful to avoid:
 *  <ol>
 *    <li>add the production listeners to SpreadTradesManager attached
 *          directly to the LineScanner</li>
 *  </ol>
 *
 * To create this class, two different type of events are considered:
 * <ul>
 *      <li><i>present events</i></li>
 *      <li><i>future events</i></li>
 * </ul>
 * The source is asked for more elements, getting to events. These events
 * are taken thanks to <i>notify</i> callback function.<br/>
 * Events are, by default, <i>future events</i>. <i>Future events</i> are
 * memorized by <code>VolumeProfileBacktester</code>. <br/>
 * When several <i>future events</i> have been memorized (the mean of 'several'
 * depends on the authority of <code>ForecastingTime</code> object)
 * they are used to test the predictors and then notified to subscribed 
 * listener as <i>present events</i>.
 *
 */
public class BacktesterAgent implements Listener {
    private final PredictorListener predictor;
    private final LineScanner source;
    private final ForecastingTime forecastingTime;
    private final ForecastingTest forecastingTest;
    private final List<Listener> listeners;
    private final List<ParseResult> futureParseResults;

    /**
     * Creates a backtesting for volume.
     */
    public BacktesterAgent(final LineScanner source, 
            final PredictorListener predictor, 
            final ForecastingTime forecastingTime,
            final ForecastingTest forecastingTest) {
        this.listeners = new ArrayList<Listener>();
        this.futureParseResults = new ArrayList<ParseResult>();

        this.source = source;
        this.predictor = predictor;
        this.forecastingTest = forecastingTest;
        this.forecastingTime = forecastingTime;

        this.addListener(predictor);
    }


    /**
     * Adds listener to <i>present events</i>.
     */
    private void addListener(final Listener listener) {
        this.listeners.add(listener);
    }


    /**
     * Receives notification signals as <i>future events</i>.
     */
    public void notify(final ParseResult parseResult) {
        final Calendar currentDay = CalUtils.clone(parseResult.getTimestamp());

        if(this.forecastingTime.shouldContinue(currentDay)) {
            this.futureParseResults.add(parseResult);
            this.forecastingTest.notify(parseResult);
        } else {
            this.forecastingTest.evaluate(currentDay, this.predictor);
            for(final ParseResult pr : this.futureParseResults) {
                for(final Listener listener : this.listeners) {
                    listener.notify(pr);
                }
            }
            this.futureParseResults.clear();
            this.forecastingTime.setPresentTime(currentDay);
        }
    }
}
