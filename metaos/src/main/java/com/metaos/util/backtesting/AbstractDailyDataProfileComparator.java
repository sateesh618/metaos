/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.util.backtesting;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.text.*;
import com.metaos.datamgt.*;
import com.metaos.engine.*;
import com.metaos.util.*;
import com.metaos.signalgrt.predictors.*;
import com.metaos.signalgrt.predictors.specific.volume.*;

/**
 * Tester of daily predictions.
 * If <code>dump results</code> functionallity is enabled, predicted and real
 * data are dumped into files named <code>yyyy-MM-dd-prediction.csv</code>
 * and <code>yyyy-MM-dd-real.csv</code> and, at the same time,
 * <code>backtest.lastPrediction</code> and <code>backtest.lastReal</code>
 * variables are defined into the R engine with predicted and real values.
 */
public abstract class AbstractDailyDataProfileComparator 
        implements ForecastingTest {
    private static final Logger log = Logger.getLogger(
            AbstractDailyDataProfileComparator.class.getPackage().getName());
    private static final SimpleDateFormat dateFormat = 
            new SimpleDateFormat("yyyy-MM-dd");
    private final Calendar minimumDay;
    private final Field field;
    private final Errors<Integer> minuteErrors;
    private final Errors<String> dayErrors;
    private final double[] dailyData;
    private final boolean dumpResults = true;
    private final String symbol;
    protected final CalUtils.InstantGenerator instantGenerator;

    /**
     * Compares forecasts with real intrday daily data.
     * @param instantGenerator definition of 'instant', coherent with one
     * used in predictors.
     * @param field the field to compare profile.
     * @param minimumDay minimum day to compare forecasts.
     * @param symbol to make comparisons.
     */
    public AbstractDailyDataProfileComparator(
            final CalUtils.InstantGenerator instantGenerator,
            final String symbol, final Field field, final Calendar minimumDay) {
        this.instantGenerator = instantGenerator;
        this.minuteErrors = new Errors<Integer>();
        this.dayErrors = new Errors<String>();
        this.field = field;
        this.symbol = symbol;
        this.dailyData = new double[this.instantGenerator.maxInstantValue()];
        for(int i=0; i<this.dailyData.length; i++) {
            this.dailyData[i] = Double.NaN;
        }
        this.minimumDay = minimumDay==null ? Calendar.getInstance() : 
                CalUtils.normalizedClone(minimumDay);
        if(minimumDay==null) {
            this.minimumDay.set(Calendar.YEAR, 1970);
            this.minimumDay.set(Calendar.MONTH, 0);
            this.minimumDay.set(Calendar.DAY_OF_MONTH, 1);
            this.minimumDay.set(Calendar.HOUR_OF_DAY, 0);
            this.minimumDay.set(Calendar.MINUTE, 0);
            this.minimumDay.set(Calendar.SECOND, 0);
        }
    }


    /**
     * Gets collector for daily (intra-day) error vectors.
     */
    public Errors<String> getDayErrors() {
        return this.dayErrors;
    }


    /**
     * Gets collector for minute by minute (inter-day) error vectors.
     */
    public Errors<Integer> getMinuteErrors() {
        return this.minuteErrors;
    }



    public void notify(final ParseResult parseResult) {
        final int index = this.instantGenerator.generate(
                parseResult.getLocalTimestamp());
        // Effect:only takes memory of the each minute for the last received day
        if(parseResult.values(this.symbol) != null &&
                parseResult.values(this.symbol).get(field) != null) {
            this.dailyData[index] = parseResult.values(this.symbol).get(field);
        }
    }


    /**
     * Evaluates errors if the moment is after or equals the limit date.
     */
    public void evaluate(final Calendar when, final double[] predictedValues) {
        if(this.minimumDay.after(when)) return;
        final String dayStr = dateFormat.format(when.getTime());

        // Clean values, removing outliers
        RemoveVolumeData.cleanOutliers(this.dailyData);

        final double errors[] = 
                contrast(predictedValues, this.dailyData);

        boolean anyErrorPresent = false;
        for(int i=0; i<errors.length; i++) {
            if(!Double.isNaN(errors[i]) && errors[i]>=0) {
                anyErrorPresent = true;
                this.minuteErrors.addError(i, errors[i]);
                this.dayErrors.addError(dayStr, errors[i]);
            }
        }
        // Dumps data.
        if(this.dumpResults && anyErrorPresent) {
            try {
                final PrintWriter predictedFile = new PrintWriter(
                    new FileWriter(dayStr +"-prediction.csv"));
                final PrintWriter realFile = new PrintWriter(
                    new FileWriter(dayStr +"-real.csv"));
            
                for(int i=0; i<predictedValues.length; i++) {
                    predictedFile.println(predictedValues[i]);
                }
                for(int i=0; i<this.dailyData.length; i++) {
                    realFile.println(this.dailyData[i]);
                }

                predictedFile.flush();
                predictedFile.close();
                realFile.flush();
                realFile.close();

                if(System.getProperty("RCONSOLE")!=null) {
                    final StringBuffer predictedValsStr = new StringBuffer()
                            .append(predictedValues[0]);
                    final StringBuffer realValsStr = new StringBuffer()
                            .append(this.dailyData[0]);
                    for(int i=1; i<predictedValues.length; i++) {
                        predictedValsStr.append(",").append(predictedValues[i]);
                    }
                    for(int i=1; i<this.dailyData.length; i++) {
                        realValsStr.append(",").append(this.dailyData[i]);
                    }
                    final R r = Engine.getR();
                    r.eval("backtest.lastPrediction<-c("+predictedValsStr+")");
                    r.eval("backtest.lastReal<-c(" + realValsStr + ")");
                }
            } catch(IOException ioe) {
                log.log(Level.SEVERE, "Error dumping results to file", ioe);
            }
        } else {
            log.finest("No prediction to evaluate (all positions are NaN)");
        }



        // Cleans dailyData, to avoid contamination
        for(int i=0; i<this.dailyData.length; i++) {
            this.dailyData[i] = Double.NaN;
        }
    }


    //
    // Protected stuff -------------------------------
    //

    /**
     * Hook method to calculate differences between normalized 
     * vectors a and b and returns the maximum value for the each sample.
     * Values less than 0 are ignored and not considered for contrast.
     */
    protected abstract double[] contrast(final double a[], final double b[]);
}
