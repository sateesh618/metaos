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
    private double[] lastForecast;

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
        this.addListener(forecastingTest);
    }


    /**
     * Adds listener.
     */
    private void addListener(final Listener listener) {
        this.listeners.add(listener);
    }


    /**
     * Receives notification signals as time goes on.
     */
    public void notify(final ParseResult parseResult) {
        for(final Listener l : this.listeners) l.notify(parseResult);

        final Calendar currentDay = parseResult.getTimestamp();

        // Is it the moment to test previous forecasting?
        if(this.forecastingTime.shouldEvaluatePrediction(currentDay)) {
            if(this.lastForecast != null) {
                this.forecastingTest.evaluate(currentDay, this.lastForecast);
            }
        }

        // Is it the moment to generate a new prediction?
        if(this.forecastingTime.shouldPredict(currentDay)) {
            this.lastForecast = (double[]) this.predictor
                    .predictVector(currentDay).clone();
        }
    }
}
