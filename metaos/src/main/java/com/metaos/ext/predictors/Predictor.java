/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.ext.predictors;

import java.util.List;

/**
 * Most general interface for a single value predictor, 
 * able to learn and to be reset.
 */
public interface Predictor {
    /**
     * Emits a forecast based on learned values.
     */
    public double predict();

    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final double[] vals);

    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final List<Double> vals);


    /**
     * Memorizes one single value.
     */
    public void learnValue(final double val);

    /**
     * Empties memory and predictions restoring initial state.
     */
    public void reset();

    /**
     * Returns the human name of the predictor.
     */
    public String toString();
}
