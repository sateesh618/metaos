/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.util.backtesting;

import java.util.*;
import com.metaos.datamgt.*;
import com.metaos.util.*;
import com.metaos.signalgrt.predictors.*;

/**
 * Tester of daily predictions.
 */
public abstract class AbstractDailyDataProfileComparator 
        implements ForecastingTest {
    private final Calendar minimumDay;
    private final Field field;
    private final Errors<Integer> minuteErrors;
    private final Errors<String> dayErrors;
    private final double[] dailyData;
    protected final CalUtils.InstantGenerator instantGenerator;

    /**
     * Compares forecasts with real intrday daily data.
     * @param instantGenerator definition of 'instant', coherent with one
     * used in predictors.
     * @param field the field to compare profile.
     * @param minimumDay minimum day to compare forecasts.
     */
    public AbstractDailyDataProfileComparator(
            final CalUtils.InstantGenerator instantGenerator,
            final Field field, final Calendar minimumDay) {
        this.instantGenerator = instantGenerator;
        this.minuteErrors = new Errors<Integer>();
        this.dayErrors = new Errors<String>();
        this.field = field;
        this.dailyData = new double[this.instantGenerator.maxInstantValue()];
        for(int i=0; i<this.dailyData.length; i++) this.dailyData[i] = -1;
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
        if(parseResult.values(0) != null &&
                parseResult.values(0).get(field) != null) {
            this.dailyData[index] = parseResult.values(0).get(field);
        }
    }


    /**
     * Evaluates errors if the moment is after or equals the limit date.
     */
    public void evaluate(final Calendar when, final double[] predictedValues) {
        if(this.minimumDay.after(when)) return;
        final double errors[] = 
                contrast(predictedValues, this.dailyData);
/*
System.out.println("Evaluating for day : " + when.get(Calendar.DAY_OF_MONTH) + "/" + (when.get(Calendar.MONTH)+1));
for(int i=0; i<predictedValues.length; i++) {
    System.out.print(predictedValues[i]);
    System.out.print(" ");
}
System.out.println();
for(int i=0; i<this.dailyData.length; i++) {
    System.out.print(this.dailyData[i]);
    System.out.print(" ");
}
System.out.println();
*/

        final String dayAsString = when.get(Calendar.YEAR) + "-"
                + (when.get(Calendar.MONTH)+1) + "-" 
                + when.get(Calendar.DAY_OF_MONTH);
        for(int i=0; i<errors.length; i++) {
            if(errors[i]>=0) {
                this.minuteErrors.addError(i, errors[i]);
                this.dayErrors.addError(dayAsString, errors[i]);
            }
        }

        // Cleans dailyData, to avoid contamination
        for(int i=0; i<this.dailyData.length; i++) this.dailyData[i] = -1;
    }


    //
    // Protected stuff -------------------------------
    //

    /**
     * Hook method to calculate quadratic differences between normalized 
     * vectors a and b and returns the maximum value for the each sample.
     * Values less than 0 are ignored and not considered for contrast.
     */
    protected abstract double[] contrast(final double a[], final double b[]);
}
