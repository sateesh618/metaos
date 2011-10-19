/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.ext.filters;

import com.metaos.datamgt.Field;
import com.metaos.datamgt.Filter;
import java.util.Calendar;
import java.util.Map;

/**
 *
 */
public class MainOutliers implements Filter {
    private double scaledYesterdayCloseVol;
    private final double proportion;
    private static final Field _volume_ = new Field.VOLUME();
    
    /**
     * Creates a filter to supress volumes greater than given proportion 
     * of the previous day volume at closing time.
     */
    public MainOutliers(final double proportion) {
        this.proportion = proportion;
        this.scaledYesterdayCloseVol = -1;
    }

    /**
     * Tests if given set of prices for the symbol is valid.
     * @return true if set of prices is valid, false if should be ignored.
     */
    public boolean filter(final Calendar when, final String symbol,
            final Map<Field, Double> values) {
        if(values==null || values.get(_volume_)==null) return true;
        if(when.get(Calendar.HOUR_OF_DAY)==17 
                && when.get(Calendar.MINUTE)==35) {
            scaledYesterdayCloseVol = proportion * values.get(_volume_);
            return true;
        } else if (scaledYesterdayCloseVol == -1) {
            return true;
        } else {
            return values.get(_volume_) < scaledYesterdayCloseVol;
        }
    }
}
