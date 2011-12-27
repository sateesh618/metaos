/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors.specific.volume;

import java.util.*;
import com.metaos.signalgrt.predictors.*;
import com.metaos.signalgrt.predictors.specific.*;
import com.metaos.util.*;
import com.metaos.datamgt.*;

/**
 * Set of moving averages for daily volume profile forecasting, 
 * one for each day of week and one more for third friday in month.
 *
 * Smooth Kernel Moving Average is the predictor for each bin.
 * <br/>
 * Which kernel select, according to trading data, is performed by a
 * given algorithm (called <i>strategy</i>, in the sense of Design Patterns).
 * <br/>
 * Outliers are removed.
 * <br/>
 * Volume profile is scaled to 100, summing 100.
 */
public final class SmoothKMAVolumeProfileDifferentEachDayOfWeek 
        extends OnePredictorPerBinAndTypeOfDayOfWeek {
    private final int ignoreElementsHead, ignoreElementsTail;

    /**
     * Creates a combined predictor: 5 days (labour days) + third friday,
     * time-series predictors with defined strategy to change predictors
     * and scaling learned and predicted values.
     *
     * @param predictorSelectionStrategy in the sense of Design Patterns,
     *      algorithm to select which predictor and which kernel will be
     *      used for each bin to predict next value.
     */
    public SmoothKMAVolumeProfileDifferentEachDayOfWeek(
            final Predictor.PredictorSelectionStrategy 
                    predictorSelectionStrategy,
            final CalUtils.InstantGenerator instantGenerator, 
            final String symbol) {
        super(predictorSelectionStrategy, instantGenerator, symbol,
                new Field.VOLUME(), 100.0d);
        this.ignoreElementsHead = 0;
        this.ignoreElementsTail = 0;
    }


    /**
     * Creates a combined predictor: 5 days (labour days) + third friday,
     * time-series predictors with defined strategy to change predictors
     * and scaling learned and predicted values ignoring elements at the
     * end and ath the begining of the day.
     *
     * @param scale value to scale daily predictions, 
     *      0 or less if no scale is wanted.
     * @param predictorSelectionStrategy in the sense of Design Patterns,
     *      algorithm to select which predictor and which kernel will be
     *      used for each bin to predict next value.
     * @param ignoreElementsHead number of elements to ignore from the first
     * element with value (maybe opening auction).
     * @param ignoreElementsHead number of elements to ignore from the last 
     * element with value (maybe closing auction).
     */
    public SmoothKMAVolumeProfileDifferentEachDayOfWeek(
            final Predictor.PredictorSelectionStrategy 
                    predictorSelectionStrategy,
            final CalUtils.InstantGenerator instantGenerator, 
            final String symbol, final int ignoreElementsHead, 
            final int ignoreElementsTail) {
        super(predictorSelectionStrategy, instantGenerator, symbol,
                new Field.VOLUME(), 100.0d);
        this.ignoreElementsHead = ignoreElementsHead;
        this.ignoreElementsTail = ignoreElementsTail;
    }




    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return "Smooth Kernel Moving Average normalized to 100 Volume "
                + "Profile Predictor";
    }


    //
    // Hook methods ---------------------------------------------
    //
    /**
     * Cleans data before learning.
     */
    @Override protected void cleanData(final double[] vals) {
        RemoveVolumeData.cleanOutliers(vals);
        RemoveVolumeData.cutHeadAndTail(vals, this.ignoreElementsHead,
                this.ignoreElementsTail);
    }
}
