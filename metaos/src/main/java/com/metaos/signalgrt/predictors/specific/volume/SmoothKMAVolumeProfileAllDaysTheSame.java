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
 * Moving averages for daily volume profile forecasting, considering
 * all days as the same day.
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
public final class SmoothKMAVolumeProfileAllDaysTheSame 
        extends OnePredictorPerBin {
    private final int ignoreElementsHead, ignoreElementsTail;

    /**
     * Creates a predictor for each bin in day using data from previous
     * days, considereing all days of the same type.
     *
     * @param predictorSelectionStrategy in the sense of Design Patterns,
     *      algorithm to select which predictor and which kernel will be
     *      used for each bin to predict next value.
     */
    public SmoothKMAVolumeProfileAllDaysTheSame(
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
     * Creates a predictor for each bin in day using data from previous
     * days, considereing all days of the same type and removing data
     * from the end and the begining of the day.
     *
     * @param predictorSelectionStrategy in the sense of Design Patterns,
     *      algorithm to select which predictor and which kernel will be
     *      used for each bin to predict next value.
     * @param ignoreElementsHead number of elements to ignore from the first
     * element with value (maybe opening auction).
     * @param ignoreElementsHead number of elements to ignore from the last 
     * element with value (maybe closing auction).
     */
    public SmoothKMAVolumeProfileAllDaysTheSame(
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
