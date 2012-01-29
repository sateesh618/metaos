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
 * Calculates daily volume real for the given stock in the given market.
 *
 * Thus, after receiving all daily traded volumes for each bin for every stock
 * in the market, this predictor calculates the principal component for the
 * "typical" stock in the market the next day and applies it to the desired
 * stock.
 * <br/>
 * Prepared to be used for markets with opening and closing times, but not
 * with pauses during the session.
 * <br/>
 * Outliers are optionally removed.
 * <br/>
 * When predicting, unknown data is represented as NaN.
 */
public final class PCAVolumeRealConsideringMarketPredictor 
        extends PCADayByDayPredictor {
    private static final Logger log = Logger.getLogger(
            PCAVolumeRealConsideringMarketPredictor.class.getPackage()
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
     * @param symbols list of market symbols to consider when calculating PCA
     * @param calculatedSymbol symbol to calculate real volume based on PCA
     * @param minimumVariance minimum explained variance by model.
     */
    public PCAVolumeRealConsideringMarketPredictor(
            final CalUtils.InstantGenerator instantGenerator,
            final double minimumVariance, final String[] symbols, 
	    final String calculatedSymbol) {
        super(instantGenerator, new Field.VOLUME(), minimumVariance, 0.0d,
                symbols, calculatedSymbol);
        this.ignoreElementsHead = 0;
        this.ignoreElementsTail = 0;
        this.cleanOutliers = false;
    }


    /**
     * Creates a day-by-day predictor which uses data from each day to
     * forecast data for next day ignoring elements at the begining and at
     * the end of the trading day.
     *
     * @param minimumVariance minimum explained variance by model.
     * @param ignoreElementsHead number of elements to ignore from the first
     * element with value (maybe opening auction).
     * @param ignoreElementsHead number of elements to ignore from the last 
     * element with value (maybe closing auction).
     * @param cleanOutliers true to clean otuliers when learning.
     * @param calculatedSymbol symbol to calculate real volume based on PCA
     */
    public PCAVolumeRealConsideringMarketPredictor(
            final CalUtils.InstantGenerator instantGenerator,
            final double minimumVariance, final String[] symbols, 
	    final String calculatedSymbol, final int ignoreElementsHead, 
	    final int ignoreElementsTail, final boolean cleanOutliers) {
        super(instantGenerator, new Field.VOLUME(), minimumVariance, 0.0d,
                symbols, calculatedSymbol);
        this.ignoreElementsHead = ignoreElementsHead;
        this.ignoreElementsTail = ignoreElementsTail;
        this.cleanOutliers = cleanOutliers;
    }


    
    /**
     * Performs a cleaning of outliers and modifies data to get an acceptable
     * volume (all values greater or equals to zero)
     */
    public double[] predictVector(final Calendar when) {
        final double result[] = super.predictVector(when);

        // Move all values to not get negative values.
        for(int i=0; i<result.length; i++) {
            if(Double.isNaN(result[i])) continue;
            if(result[i]<0) {
                for(int j=0; j<result.length; j++) {
                    result[j] = result[j] - result[i];
                }
            }
        }

        return result;
    }


    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return "Not Normalized PCA Volume Real Considering "
                + "all market values Predictor "
                + (this.cleanOutliers ? "cleaning" : "not cleaning")
                + " outliers";
    }


    //
    // Hook methods ---------------------------------------------
    //


    /**
     * Remove outliers: values greater than 50% of the last value 
     * (as supposed to be the closing auction value) are removed.
     * Then, elements from tail and head are removed too, according to
     * initialization parameters.
     */
    @Override protected void cleanData(final double vals[]) {
        if(this.cleanOutliers) {
            log.fine("Cleaning outliers before predicting");
            RemoveVolumeData.cleanOutliers(vals);
        }
        RemoveVolumeData.cutHeadAndTail(vals, this.ignoreElementsHead,
                this.ignoreElementsTail);
    }
}
