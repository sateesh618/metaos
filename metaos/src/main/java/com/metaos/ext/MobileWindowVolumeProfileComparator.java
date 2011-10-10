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
 *
// TODO
 */
public class MobileWindowVolumeProfileComparator implements ForecastingTest {
    private final ErrorsStatistics errorsCollector;

    public MobileWindowVolumeProfileComparator(final int windowSizes[],
            final ErrorsStatistics errorsCollector) {
        this.errorsCollector = errorsCollector;
    }


    public void notify(final ParseResult parseResult) {
        // index = minute in the day
        //this.dailyVolume[index] = parseResult.get(__volume__);
    }


    public void evaluate(final Calendar when, final Predictor predictor) {
        // Normaliza this.dailyVolume

        final double predictedProfile[] = predictor.predictVector(when);
        
        // Reportamos las diferencias a errorsCollector
    }
}
