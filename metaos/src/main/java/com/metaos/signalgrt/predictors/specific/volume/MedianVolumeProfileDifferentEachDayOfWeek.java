/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors.specific.volume;

import java.util.*;
import com.metaos.signalgrt.predictors.*;
import com.metaos.signalgrt.predictors.simple.*;
import com.metaos.signalgrt.predictors.specific.*;
import com.metaos.util.*;
import com.metaos.datamgt.*;

/**
 * Uses a predictor to forecast each day based on lerant data from the previous
 * same days of week.
 * <br/>
 * Predictions are scaled in order to sum 100.
 * <br/>
 * Outliers are removed.
 */
public class MedianVolumeProfileDifferentEachDayOfWeek 
        extends OnePredictorPerBinAndTypeOfDayOfWeek{
    private final int ignoreElementsHead, ignoreElementsTail;
    private final boolean cleanOutliers;

    /**
     * Creates a day-by-day predictor which uses median of N previous days,
     * considering in a separated way each day of week,
     * to forecast volume profile value in each bin for next day.
     *
     * @param memorySize number of days back to consider.
     * @param predictorSelectionStrategy in the sense of Design Patterns,
     *      algorithm to select which predictor and which kernel will be
     *      used for each bin to predict next value.
     */
    public MedianVolumeProfileDifferentEachDayOfWeek(final int memorySize,
            final CalUtils.InstantGenerator instantGenerator, 
            final String symbol) {
        this(memorySize, instantGenerator, symbol, 0, 0, true);
    }


    /**
     * Creates a day-by-day predictor which uses median of N previous days 
     * to forecast volume profile value in each bin for next day ignoring
     * bins at the begining and at the end (even in prediction and learning).
     *
     * @param memorySize number of days back to consider.
     * @param ignoreElementsHead number of elements to ignore from the first
     *      element with value (maybe opening auction).
     * @param ignoreElementsHead number of elements to ignore from the last 
     *      element with value (maybe closing auction).
     */
    public MedianVolumeProfileDifferentEachDayOfWeek(final int memorySize,
            final CalUtils.InstantGenerator instantGenerator,
            final String symbol, final int ignoreElementsHead, 
            final int ignoreElementsTail, final boolean cleanOutliers) {
        super(new Predictor.PredictorSelectionStrategy() {
                    public void injectKernel(final Predictor predictor) {
                        // Do nothing.
                    }

                    public int kernelSize() { return memorySize; }

                    public PredictorListener buildPredictor() {
                        return new PredictorListener.SingleSymbolField(
                                new MovingMedian(memorySize), symbol, 
                                new Field.VOLUME());
                    }
                },
                instantGenerator, symbol, new Field.VOLUME(), 100.0d);
        this.ignoreElementsHead = ignoreElementsHead;
        this.ignoreElementsTail = ignoreElementsTail;
	this.cleanOutliers = cleanOutliers;
    }



    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return "Moving Median normalized to 100 Volume Profile Predictor, "
                + "using different predictors for each day of week "
                + (this.cleanOutliers ? "cleaning" : "not cleaning")
                + " outliers";
    }


    //
    // Hook methods ---------------------------------------------
    //
    /**
     * Cleans data before learning.
     */
    @Override protected void cleanData(final double[] vals) {
        if(this.cleanOutliers) {
	    RemoveVolumeData.cleanOutliers(vals);
        }
        RemoveVolumeData.cutHeadAndTail(vals, this.ignoreElementsHead,
                this.ignoreElementsTail);
    }
}
