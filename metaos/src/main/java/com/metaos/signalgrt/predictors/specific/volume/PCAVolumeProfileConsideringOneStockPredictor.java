/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors.specific.volume;

import java.util.*;
import java.util.logging.*;
import com.metaos.signalgrt.predictors.*;
import com.metaos.signalgrt.predictors.specific.*;
import com.metaos.util.*;
import com.metaos.datamgt.*;

/**
 * Uses PCA prediction technique to calculate typical volume profile
 * for a single stock using information on several trading days for
 * the stock.
 *
 * Specially designed for markets that have an open and closing times, but
 * no pauses during a trading session.
 * <br/>
 * Volume profiles are scaled to be no less than zero and to sum 100 for each
 * given day.
 * <br/>
 * Outliers are removed.
 * <br/>
 * When predicting, unknown data is represented as NaN.
 */
public final class PCAVolumeProfileConsideringOneStockPredictor 
        extends PCACombiningSeveralDays {
    private static final Logger log = Logger.getLogger(
            PCAVolumeProfileConsideringOneStockPredictor.class.getPackage()
                    .getName());

    private final int ignoreElementsHead, ignoreElementsTail;
    private final boolean cleanOutliers;


    //
    // Public methods ---------------------------------------
    //


    /**
     * Creates a day-by-day predictor which uses data from each day to
     * forecast data for next day.
     *
     * @param memory number of days to store into memory
     */
    public PCAVolumeProfileConsideringOneStockPredictor(
            final CalUtils.InstantGenerator instantGenerator,
            final double minimumVariance, final String symbol, 
            final int memory) {
        super(instantGenerator, new Field.VOLUME(), minimumVariance,
                100.0d, symbol, memory);
        this.ignoreElementsHead = 0;
        this.ignoreElementsTail = 0;
        this.cleanOutliers = false;
    }

    /**
     * Creates a day-by-day predictor which uses data from each day to
     * forecast data for next day ignoring some elements at the begining
     * and at the end of the day.
     *
     * @param memory number of days to store into memory
     * @param ignoreElementsHead number of elements to ignore from the first
     * element with value (maybe opening auction).
     * @param ignoreElementsHead number of elements to ignore from the last 
     * element with value (maybe closing auction).
     * @param cleanOutliers clean outliers when learning.
     */
    public PCAVolumeProfileConsideringOneStockPredictor(
            final CalUtils.InstantGenerator instantGenerator,
            final double minimumVariance, final String symbol, 
            final int memory, final int ignoreElementsHead, 
            final int ignoreElementsTail, final boolean cleanOutliers) {
        super(instantGenerator, new Field.VOLUME(), minimumVariance,
                100.0d, symbol, memory);
        this.ignoreElementsHead = ignoreElementsHead;
        this.ignoreElementsTail = ignoreElementsTail;
        this.cleanOutliers = cleanOutliers;
    }




    /**
     * Performs a cleaning of outliers and modifies data to get an acceptable
     * volume profile (all values greater or equals to zero and with a sum 
     * of 100).
     */
    public double[] predictVector(final Calendar when) {
        final double result[] = super.predictVector(when);

        // Remove negative values
        double sum = 0;
        for(int i=0; i<result.length; i++) {
            if(Double.isNaN(result[i])) continue;
            if(result[i]<0) {
                log.severe("Setting negative value for prediction to 0 at "
                        + "index " + i);
                result[i] = 0;
            }
            sum += result[i];
        }

        // Rescale
        for(int i=0; i<result.length; i++) {
            result[i] = 100 * result[i] / sum;
        }

        return result;
    }


    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return "Normalized to " + this.scale 
                + " PCA Volume Profile Combining Several Days Predictor"
                + " ignoring " + this.ignoreElementsHead + " from the begining"
                + " and " + this.ignoreElementsTail + " from the end";
    }


    //
    // Hook methods --------------------------
    //

    /**
     * Remove outliers: values greater than 50% of the last value 
     * (as supposed to be the closing auction value) are removed.
     * Then, elements from tail and head are removed too, according to
     * initialization parameters.
     */
    @Override protected void cleanData(final double vals[]) {
        if(this.cleanOutliers) {
            log.fine("Cleaning outliers data");
            RemoveVolumeData.cleanOutliers(vals);
        }
        RemoveVolumeData.cutHeadAndTail(vals, this.ignoreElementsHead,
                this.ignoreElementsTail);
    }
}
