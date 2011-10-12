/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors;

import java.util.*;

/**
 * Most general interface for a predictor, able to learn and to be reset.
 */
public interface Predictor {
    /**
     * Emits a single forecast based on learned values.
     */
    public double predict(final Calendar when);

    /**
     * Emits a forecast based on learned values.
     */
    public double[] predictVector(final Calendar when);

    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final Calendar when, final double[] vals);

    /**
     * Memorizes several values at the same time.
     */
    public void learnVector(final Calendar when, final List<Double> vals);


    /**
     * Memorizes one single value.
     */
    public void learnValue(final Calendar when, final double val);

    /**
     * Empties memory and predictions restoring initial state.
     */
    public void reset();

    /**
     * Returns the human name of the predictor.
     */
    public String toString();
}
