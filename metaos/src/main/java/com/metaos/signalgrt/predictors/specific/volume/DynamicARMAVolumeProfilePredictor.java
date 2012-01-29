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
import com.metaos.signalgrt.predictors.simple.*;
import com.metaos.util.*;
import com.metaos.datamgt.*;

/**
 * Calculates DYNAMICALLY daily volume for the given stock in the given market.
 *
 * From previous days, it calculates market a profile and then applies
 * ARMA to adapt predicted profile to running day.
 * <br/>
 * Prepared to be used for markets with opening and closing times, but not
 * with pauses during the session.
 * <br/>
 * Outliers are optionally removed.
 * <br/>
 * When predicting, unknown data is represented as NaN.
 */
public class DynamicARMAVolumeProfilePredictor implements PredictorListener {
    private static final Logger log = Logger.getLogger(
            DynamicARMAVolumeProfilePredictor.class.getPackage()
                    .getName());
    private static final Field VOLUME = new Field.VOLUME();

    private final PredictorListener baseProfilePredictor;
    private final CalUtils.InstantGenerator instantGenerator;
    private final String consideredSymbol;

    private int lastLearntDayOfMonth, lastLearntMonth, lastNotifiedInstant;
    private double[] basePrediction;
    private double[] notifiedValues;
    private final double scale;

    //
    // Public methods ---------------------------------------
    //

    /**
     * Creates a dynamic ARMA predictor which uses data from each day to
     * forecast data for next day.
     *
     * @param consideredSymbol symbol to calculate real volume based on 
     * dynamic ARMA
     * @param scale -1 to not scale anything, greater than zero to scale,
     * zero is invalid.
     */
    public DynamicARMAVolumeProfilePredictor(
            final PredictorListener baseProfilePredictor,
            final CalUtils.InstantGenerator instantGenerator,
            final String consideredSymbol, final double scale) {
        assert scale == -1 || scale > 0;
        this.baseProfilePredictor = baseProfilePredictor;
        this.lastLearntDayOfMonth = -1;
        this.lastLearntMonth = -1;
        this.consideredSymbol = consideredSymbol;
        this.instantGenerator = instantGenerator;
        this.lastNotifiedInstant = -1;
        this.basePrediction = new double[instantGenerator.maxInstantValue()];
        this.notifiedValues = new double[instantGenerator.maxInstantValue()];
        this.scale = scale;
        this.reset();
    }


    public double predict(final Calendar when) {
        final double[] prediction = this.predictVector(when);
        return prediction[this.instantGenerator.generate(when)];
    }

    
    /**
     * Performs a cleaning of outliers and modifies data to get an acceptable
     * volume (all values greater or equals to zero)
     */
    public double[] predictVector(final Calendar when) {
        final double normalizedValues[];
        if(this.scale>0) {
            normalizedValues = new double[this.notifiedValues.length];
            double sum = 0;
            for(int i=0; i<this.notifiedValues.length; i++) {
                sum += Double.isNaN(this.notifiedValues[i]) ? 0 
                           : this.notifiedValues[i];
            }
            for(int i=0; i<this.notifiedValues.length; i++) {
                normalizedValues[i] = this.scale * this.notifiedValues[i] / sum;
            }
        } else {
            normalizedValues = this.notifiedValues;    
        }

        // basePrediction is based on yesterday data and it's normalize 
        final double differences[] = new double[normalizedValues.length];
        for(int i=0; i<differences.length; i++) {
            differences[i] = normalizedValues[i] - this.basePrediction[i];
        }
        final double prediction[] = new double[differences.length];

        for(int i=0; i<differences.length; i++) {
            final ARIMA arima = new ARIMA(1, 0, 1);
            if(Double.isNaN(differences[i])) {
                prediction[i] = Double.NaN;
            } else {
                for(int j=0; j<i; j++) {
                    arima.learnValue(when, differences[j]);
                }
                final double arimaResult = arima.predict(when);
                if(Double.isNaN(arimaResult)) {
                    prediction[i] = this.basePrediction[i];
                } else {
                    prediction[i] = arima.predict(when)+this.basePrediction[i];
                }
            }
        }

        // Remove negative values
        for(int i=0; i<prediction.length; i++) {
            if(Double.isNaN(prediction[i])) continue;
            if(prediction[i]<0) {
                log.severe("Setting negative value for prediction to 0 at "
                        + "index " + i);
                prediction[i] = 0;
            }
        }

        // Normalize
        if(this.scale>0) {
            double sum = 0;
            for(int i=0; i<prediction.length; i++) {
                sum += Double.isNaN(prediction[i]) ? 0 : prediction[i];
            }
            for(int i=0; i<prediction.length; i++) {
                prediction[i] = this.scale * prediction[i] / sum;
            }
        }
        return prediction;
    }



    public void notify(final ParseResult data) {
        final Calendar when = data.getLocalTimestamp(consideredSymbol);
        // If the day begins..
        if(when.get(Calendar.DAY_OF_MONTH) != this.lastLearntDayOfMonth
                    || when.get(Calendar.MONTH) != this.lastLearntMonth) {
            // Predict Dynamic profile using learnt data from previous day
            this.basePrediction = this.baseProfilePredictor.predictVector(when);
            this.lastLearntDayOfMonth = when.get(Calendar.DAY_OF_MONTH);
            this.lastLearntMonth = when.get(Calendar.MONTH);
            this.lastNotifiedInstant = -1;
            // Reset notified values
            for(int i=0; i<this.notifiedValues.length; i++) {
                this.notifiedValues[i] = Double.NaN;
            }
        }
        this.baseProfilePredictor.notify(data);

        if(this.basePrediction==null || data.values(consideredSymbol)==null
                || data.values(consideredSymbol).get(VOLUME)==null) {
            return;
        }
        final int binIndex = this.instantGenerator.generate(when);
        this.notifiedValues[binIndex]=data.values(consideredSymbol).get(VOLUME);
    }


    public void learnVector(final Calendar when, final double[] vals) {
        throw new UnsupportedOperationException(
                "Dynamic-ARMA cannot be used as simple predictor, since "
                + "it needs market information");
    }


    public void learnVector(final Calendar when, final List<Double> vals) {
        throw new UnsupportedOperationException(
                "Dynamic-ARMA cannot be used as simple predictor, since "
                + "it needs market information");
    }


    public void learnValue(final Calendar when, final double val) {
        throw new UnsupportedOperationException(
                "Dynamic-ARMA cannot be used as simple predictor, since it "
                + "needs market information");
    }


    public void reset() {
        this.baseProfilePredictor.reset();

        for(int i=0; i<this.notifiedValues.length; i++) {
            this.notifiedValues[i] = Double.NaN;
            this.basePrediction[i] = Double.NaN;
        }
    }





    /**
     * Returns the human name of the predictor.
     */
    public String toString() {
        return "Normalized to DynamicARMA Volume Profile Considering "
                + "all market values Predictor";
    }
}
