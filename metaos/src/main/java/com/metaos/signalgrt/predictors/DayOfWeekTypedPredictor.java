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
 * Set of moving averages for daily forecasting, one for each day of week and
 * one more for third friday in month.
 */
public class DayOfWeekTypedPredictor implements PredictorListener {
    private final Predictor[][] predictors;
    private final CalUtils.InstantGenerator instantGenerator;
    private final Field field;
    private final PredictorSelectionStrategy predictorSelectionStrategy;
    private final List<Pair> learnedValues;
    private final double scale;
    private Calendar lastLearningTime = CalUtils.getZeroCalendar();


    //
    // Public methods ---------------------------------------
    //


    /**
     * Creates a combined predictor: 5 days (labour days) + third friday,
     * time-series predictors with defined strategy to change predictors
     * and scaling learned and predicted values.
     *
     * @param scale value to scale daily predictions, 
     *      0 or less if no scale is wanted.
     */
    public DayOfWeekTypedPredictor(final Predictor.PredictorSelectionStrategy 
                predictorSelectionStrategy,
            final CalUtils.InstantGenerator instantGenerator,
            final Field field, final double scale) {
        this.scale = scale;
        this.predictorSelectionStrategy = predictorSelectionStrategy;
        this.field = field;
        this.instantGenerator = instantGenerator;
        this.predictors = new Predictor[6][];
        for(int i=0; i<6; i++) {
            predictors[i] = new Predictor[instantGenerator.maxInstantValue()];
            for(int j=0; j<predictors[i].length; j++) {
                predictors[i][j] = predictorSelectionStrategy.buildPredictor();
            }
        }

        this.learnedValues = new ArrayList<Pair>();
        for(int i=0; i<this.instantGenerator.maxInstantValue(); i++) {
            this.learnedValues.add(null);
        }

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
        throw new UnsupportedOperationException("Predictor only "
                + "generates a vector of values");
    }


    /**
     * Emits a forecast based on learned values.
     */
    public double[] predictVector(final Calendar when) {
        this.learnInsidePredictors();

        final int index = daySelector(when);
        final double prediction[] = new double[
                this.instantGenerator.maxInstantValue()];
        for(int i=0; i<predictors[index].length; i++) {
            this.predictorSelectionStrategy.injectKernel(predictors[index][i]);
            prediction[i] = predictors[index][i].predict(when);
        }

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
        if(this.instantGenerator.maxInstantValue()!=vals.length) {
            throw new IllegalArgumentException("Size of vector to learn "
                    + "must be equals to maximum number of instants for each "
                    + "training period (" 
                    + this.instantGenerator.maxInstantValue() 
                    + " in this case, and not " + vals.length + ")");
        }
        for(int j=0; j<vals.length; j++) {
            this.learnValue(when, vals[j]);
        }
    }


    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final Calendar when, final List<Double> vals) {
        if(this.instantGenerator.maxInstantValue()!=vals.size()) {
            throw new IllegalArgumentException("Size of vector to learn "
                    + "must be equals to maximum number of instants for each "
                    + "training period (" 
                    + this.instantGenerator.maxInstantValue() 
                    + " in this case, and not " + vals.size() + ")");
        }
        for(int j=0; j<vals.size(); j++) {
            this.learnValue(when, vals.get(j));
        }
    }


    /**
     * Memorizes one single value.
     */
    public void learnValue(final Calendar when, final double val) {
        // Date Control: noitified date should not be before previous date
        assert( ! when.before(this.lastLearningTime) );

        if(when.get(Calendar.DAY_OF_YEAR) !=
                        this.lastLearningTime.get(Calendar.DAY_OF_YEAR) 
                || when.get(Calendar.YEAR) !=
                        this.lastLearningTime.get(Calendar.YEAR)) {
            learnInsidePredictors();
        }

        this.lastLearningTime = when;

        final int j = this.instantGenerator.generate(when);
        this.learnedValues.set(j, new Pair(when, val));
    }


    /**
     * Empties memory and predictions restoring initial state.
     */
    public void reset() {
        for(int i=0; i<this.predictors.length; i++) {
            for(int j=0; j<this.predictors[i].length; j++) {
                this.predictors[i][j].reset();
            }
        }
        this.learnedValues.clear();
        for(int i=0; i<this.instantGenerator.maxInstantValue(); i++) {
            this.learnedValues.add(null);
        }
    }


    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return this.scale>0 ? "Not Normalized Weekly Predictor"
                : "Normalized to " + this.scale + " Weekly Predictor";
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
                throw new IllegalArgumentException(
                        "Don't how to deal with SATURDAY or SUNDAYS");
        }
    }



    /**
     * Learns inside predictors with memorized values for day, normalizing
     * values (if it's needed) and forgeting values for next learning cycle.
     */
    private void learnInsidePredictors() {
        // Normalize
        if(this.scale>0) {
            double total = 0;
            for(int moment=0; moment<this.learnedValues.size(); moment++) {
                final Pair p = this.learnedValues.get(moment);
                if(p!=null) {
                    total += p.val;
                }
            }
            for(int moment=0; moment<this.learnedValues.size(); moment++) {
                final Pair p = this.learnedValues.get(moment);
                if(p!=null) {
                    this.learnedValues.set(moment, 
                            new Pair(p.when, this.scale * p.val / total));
                }
            }
        }

        // Learn
        final int dayOfWeek = daySelector(this.lastLearningTime);
        for(int moment=0; moment<this.learnedValues.size(); moment++) {
            final Pair p = this.learnedValues.get(moment);
            if(p!=null) {
                this.predictors[dayOfWeek][moment].learnValue(p.when, p.val);
            }
        }

        // Reset memorized values
        this.learnedValues.clear();
        for(int i=0; i<this.instantGenerator.maxInstantValue() ;i++) {
            this.learnedValues.add(null);
        }
    }



    /**
     * Struct-C emulation.
     */
    private class Pair {
        private final double val;
        private final Calendar when;
        private Pair(final Calendar when, final double val) {
            this.when = (Calendar) when.clone();
            this.val = val;
        }
    }
}
