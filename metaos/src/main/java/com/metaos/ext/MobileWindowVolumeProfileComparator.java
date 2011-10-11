/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.ext;

import java.util.*;
import com.metaos.datamgt.*;
import com.metaos.util.*;
import com.metaos.signalgrt.predictors.*;
import com.metaos.ext.error.*;

/**
 * Tester of volume profile predictions using mobile windows.
 */
public class MobileWindowVolumeProfileComparator implements ForecastingTest {
    private static final Field _volume_ = new Field.VOLUME();

    private final Errors errorsCollector;
    private final int windowSize;
    private final double[] dailyVolume;
    private final CalUtils.InstantGenerator instantGenerator;

    public MobileWindowVolumeProfileComparator(final int windowSize,
            final Errors errorsCollector, 
            final CalUtils.InstantGenerator instantGenerator) {
        this.instantGenerator = instantGenerator;
        this.errorsCollector = errorsCollector;
        this.windowSize = windowSize;
        this.dailyVolume = new double[this.instantGenerator.maxInstantValue()];
    }


    public void notify(final ParseResult parseResult) {
        final int index = this.instantGenerator.generate(
                parseResult.getTimestamp());
        this.dailyVolume[index] = parseResult.values(0).get(_volume_);
    }


    public void evaluate(final Calendar when, final Predictor predictor) {
        // Normalizes dailyVolume
        /* Not necesary!!!
        double sum = 0;
        for(int i=0; i<this.dailyVolume; i++) sum += this.dailyVolume[i];
        for(int i=0; i<this.dailyVolume; i++) 100 * this.dailyVolume[i] /= sum;
        */

        final double predictedProfile[] = predictor.predictVector(when);
        final double errors[] = 
                contrast(predictedProfile, dailyVolume, windowSize);
        
        for(int i=0; i<errors.length; i++) {
            if(errors[i]>=0) this.errorsCollector.addError(i, errors[i]);
        }
        

        // Cleans dailyVolume
        for(int i=0; i<this.dailyVolume.length; i++) this.dailyVolume[i] = -1;
    }


    //
    // Private stuff -------------------------------
    //

    /**
     * Calculates quadratic differences between normalized windows of 
     * size 'windowSize' for vectors a and b and returns the maximum 
     * value for the each sample.
     * Values less than 0 are ignored and not considered for contrast.
     */
    private double[] contrast(final double a[], final double b[], 
            final int windowSize) {
        assert a.length==b.length;
        final double diffs[] = new double[a.length-windowSize];
        for(int i=0; i<a.length-windowSize; i++) {
            double sumA = 0;
            double sumB = 0;
            for(int j=i; j<i+windowSize; j++) {
                sumA += a[j];
                sumB += b[j];
            }

            double maxDiff = -1;
            for(int j=i; j<i+windowSize; j++) {
                if(a[j]>=0 && b[j]>=0) {
                    double e = (a[j] / sumA) - (b[j] / sumB);
                    e = e*e;
                    if(maxDiff < e) maxDiff=e;
                }
            }
            diffs[i] = maxDiff;
        }
        return diffs;
    }
}
