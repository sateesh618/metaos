/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors;

import java.util.*;
import com.metaos.util.*;
import com.metaos.datamgt.*;

/**
 * Uses a predictor to forecast each day based on lerant data from 
 * previous day.
 */
public class StaticDayByDayPredictor implements PredictorListener {
    protected final Predictor.PredictorSelectionStrategy 
            predictorSelectionStrategy;
    protected final Predictor predictor;
    protected final CalUtils.InstantGenerator instantGenerator;
    protected final Field field;
    protected final double scale;
    protected Calendar lastLearningTime = CalUtils.getZeroCalendar();


    //
    // Public methods ---------------------------------------
    //


    /**
     * Creates a day-by-day predictor which uses data from each day to
     * forecast data for next day.
     *
     * @param scale value to scale daily predictions, 
     *      0 or less if no scale is wanted.
     */
    public StaticDayByDayPredictor(final Predictor.PredictorSelectionStrategy 
                predictorSelectionStrategy,
            final CalUtils.InstantGenerator instantGenerator,
            final Field field, final double scale) {
        this.scale = scale;
        this.field = field;
        this.instantGenerator = instantGenerator;
        this.predictor = predictorSelectionStrategy.buildPredictor();
        this.predictorSelectionStrategy = predictorSelectionStrategy;
    }


    public void notify(final ParseResult parseResult) {
        final Calendar when = parseResult.getLocalTimestamp();
        if(parseResult.values(0) != null 
                && parseResult.values(0).get(field)!=null) {
            final double val = parseResult.values(0).get(field);
            this.learnValue(when, val);
        }
    }


    /**
     * Emits a single forecast based on learned values.
     */
    public double predict(final Calendar when) {
        return this.predict(when);
    }


    /**
     * Emits a forecast based on learned values.
     */
    public double[] predictVector(final Calendar when) {
        this.predictorSelectionStrategy.injectKernel(predictor);
        final double prediction[] = this.predictor.predictVector(when);

        // Normalizes prediction
        if(this.scale>0) {
            double sum = 0;
            for(int i=0; i<prediction.length; i++) sum += prediction[i];
            double s = scale/sum;
            for(int i=0; i<prediction.length; i++) prediction[i] *= s;
        }

        return prediction;
    }


    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final Calendar when, final double[] vals) {
        this.predictor.learnVector(when, vals);
    }


    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final Calendar when, final List<Double> vals) {
        this.predictor.learnVector(when, vals);
    }


    /**
     * Memorizes one single value.
     */
    public void learnValue(final Calendar when, final double val) {
        // Date Control: noitified date should not be before previous date
        assert( ! when.before(this.lastLearningTime) );
        this.predictor.learnValue(when, val);
    }


    /**
     * Empties memory and predictions restoring initial state.
     */
    public void reset() {
        this.predictor.reset();
    }


    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return this.scale>0 ? "Not Normalized Static DayByDay Predictor"
                : "Normalized to " + this.scale + " Static DayByDay Predictor";
    }
}
