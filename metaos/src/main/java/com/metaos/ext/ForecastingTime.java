/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.ext;

import java.util.*;
import com.metaos.datamgt.*;

/**
 * Strategy to decide the length in time needed to backtests.
 */
public interface ForecastingTime extends Listener {
    /**
     * Tests if it's moment to evaluate a previous prediction.
     */
    public boolean shouldEvaluatePrediction(final Calendar when);

    /**
     * Tests if it's moment to predict.
     */
    public boolean shouldPredict(final Calendar when);

}
