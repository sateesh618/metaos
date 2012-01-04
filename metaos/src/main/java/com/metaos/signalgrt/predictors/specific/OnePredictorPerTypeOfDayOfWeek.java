/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors.specific;

import java.util.*;
import java.util.logging.Logger;
import com.metaos.signalgrt.predictors.*;
import com.metaos.signalgrt.predictors.simple.*;
import com.metaos.util.*;
import com.metaos.datamgt.*;

/**
 * Set of predictors for daily forecasting, one for each day of week and
 * one more for third friday in month.
 *
 * <br/>
 * To developers: Class designed following Decorator Pattern: combine
 * several predictors, one for each type of day of week wrapping them with
 * an object of this class.
 * <br/>
 */
public class OnePredictorPerTypeOfDayOfWeek implements PredictorListener {
    private static final Logger log = Logger.getLogger(
            OnePredictorPerTypeOfDayOfWeek.class.getPackage().getName());

    private final PredictorListener[] predictors;
    private final PredictorSelectionStrategy predictorSelectionStrategy;
    private final Field field;


    //
    // Public methods ---------------------------------------
    //


    /**
     * Creates a combined predictor: 5 days (labour days) + third friday,
     * time-series predictors with defined strategy to change predictors
     * and scaling learned and predicted values.
     *
     * @param predictorSelectionStrategy strategy in the sense of design 
     * patterns, to decide which predictor create and how to inject the core
     * before predicting. 
     * @deprecated Use constructor without <code>field</code> parameter.
     */
    public OnePredictorPerTypeOfDayOfWeek(
            final Predictor.PredictorSelectionStrategy 
                predictorSelectionStrategy, final Field field) {
        this.predictorSelectionStrategy = predictorSelectionStrategy;
        this.field = field;
        this.predictors = new PredictorListener[6];
        for(int i=0; i<6; i++) {
            predictors[i] = predictorSelectionStrategy.buildPredictor();
        }
        log.fine("Created set of 6 predictors, one for each type of day");
    }


    /**
     * Creates a combined predictor: 5 days (labour days) + third friday,
     * time-series predictors with defined strategy to change predictors
     * and scaling learned and predicted values.
     *
     * @param predictorSelectionStrategy strategy in the sense of design 
     * patterns, to decide which predictor create and how to inject the core
     * before predicting. Must create <code>PredictorListener</code>s, not
     * only <code>Predictor</code>s, since their <code>notify</code>
     * will be invoked.
     * @deprecated Use constructor without <code>field</code> parameter.
     */
    public OnePredictorPerTypeOfDayOfWeek(final Predictor
            .PredictorSelectionStrategy predictorSelectionStrategy) {
        this.predictorSelectionStrategy = predictorSelectionStrategy;
        this.field = null;
        this.predictors = new PredictorListener[6];
        for(int i=0; i<6; i++) {
            predictors[i] = predictorSelectionStrategy.buildPredictor();
        }
    }




    public void notify(final ParseResult parseResult) {
        final Calendar when = parseResult.getLocalTimestamp();

        final int index = daySelector(when);
        if(index==-1) return;
        if(this.predictors[index] instanceof PredictorListener) {
            ((PredictorListener) this.predictors[index]).notify(parseResult);
        } else {
            // Backcompatibility
            if(parseResult.values(0)!=null 
                    && parseResult.values(0).get(field)!=null) {
                this.learnValue(when, parseResult.values(0).get(field));
            }
        }
    }


    /**
     * Emits a single forecast based on learned values.
     */
    public double predict(final Calendar when) {
        final int index = daySelector(when);
        return predictors[index].predict(when);
    }


    /**
     * Emits a forecast based on learned values.
     */
    public double[] predictVector(final Calendar when) {
        final int index = daySelector(when);
        this.predictorSelectionStrategy.injectKernel(predictors[index]);
        return predictors[index].predictVector(when);
    }


    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final Calendar when, final double[] vals) {
        final int index = daySelector(when);
        predictors[index].learnVector(when, vals);
    }


    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final Calendar when, final List<Double> vals) {
        final int index = daySelector(when);
        predictors[index].learnVector(when, vals);
    }


    /**
     * Memorizes one single value.
     */
    public void learnValue(final Calendar when, final double val) {
        final int index = daySelector(when);
        predictors[index].learnValue(when, val);
    }


    /**
     * Empties memory and predictions restoring initial state.
     */
    public void reset() {
        for(int i=0; i<this.predictors.length; i++) {
            this.predictors[i].reset();
        }
    }


    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return "Weekly Predictor " + this.predictors[0].toString();
    }


    //
    // Private stuff -----------------------
    //

    /**
     * Returns 0 when moment is in Monday, 2 when it's in Tuesday and so on,
     * to select the suitable predictor.
     */
    private int daySelector(final Calendar when) {
        switch(when.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: 
                return when.get(Calendar.WEEK_OF_MONTH)==3 ? 5 : 4;
            default:
                log.info("Don't how to deal with SATURDAY or SUNDAYS");
                return -1;
        }
    }
}
