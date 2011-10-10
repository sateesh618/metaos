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
     * Tests if backtesting agent should continue collecting data "in the
     * future" or should evalute forecasting.
     */
    public boolean shouldContinue(final Calendar when);

    /**
     * Notifies the moment considered "present" in backtesting routine.
     */
    public void setPresentTime(final Calendar when);

}
