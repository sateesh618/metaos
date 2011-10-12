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
 * Set of predictors for volume profile daily forecasting.
 *
 * Six different predictors are used, one for each day of the week and one
 * more for third friday. 
 *
 * TODO: Generalize, since it's not implemented only for Volume Field.
 */
public class VolumeProfilePredictor implements PredictorListener {
    private final Predictor[][] predictors;
    private final CalUtils.InstantGenerator instantGenerator;
    private final Field field;


    public VolumeProfilePredictor(
            final CalUtils.InstantGenerator instantGenerator,
            final Field field) {
        this.field = field;
        this.instantGenerator = instantGenerator;
        this.predictors = new Predictor[6][];
        for(int i=0; i<6; i++) {
            this.predictors[i] = new Predictor[
                    this.instantGenerator.maxInstantValue()];
            for(int j=0; j<this.predictors[i].length; j++) {
                this.predictors[i][j] = new MovingAverage(5);
            }
        }
    }


    public void notify(final ParseResult parseResult) {
        final Calendar when = parseResult.getTimestamp();
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
        final int index = daySelector(when);
        final double prediction[] = new double[
                this.instantGenerator.maxInstantValue()];
        for(int i=0; i<predictors[index].length; i++) {
            prediction[i] = predictors[index][i].predict(when);
        }

        // Normalizes prediction to 1.0
        double sum = 0;
        for(int i=0; i<prediction.length; i++) sum += prediction[i];
        for(int i=0; i<prediction.length; i++) prediction[i] /= sum;
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
        final int i = daySelector(when);
        for(int j=0; j<this.predictors[i].length; j++) {
            this.predictors[i][j].learnValue(when, vals[j]);
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
        final int i = daySelector(when);
        for(int j=0; j<this.predictors[i].length; j++) {
            this.predictors[i][j].learnValue(when, vals.get(j));
        }
    }


    /**
     * Memorizes one single value.
     */
    public void learnValue(final Calendar when, final double val) {
        final int i = daySelector(when);
        final int j = this.instantGenerator.generate(when);
        predictors[i][j].learnValue(when, val);
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
    }


    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return "Daily Minute Volume Profile Predictor";
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
}
