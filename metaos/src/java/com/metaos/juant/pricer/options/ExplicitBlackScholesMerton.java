/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.pricer.options;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;
import com.metaos.deriva.options.*;
import com.metaos.deriva.options.vanilla.*;

/**
 * Explicit method black scholes for call and put options.
 */
public class ExplicitBlackScholesMerton implements PriceCalculator {
    /*
     * Implementation trends:
     *  - to avoid the intensive use of if/else if/else if ... sequences
     *    think about a pluggable strategy patterns: from the class name
     *    of the instrument, use a specific and name-related
     *    Black-Scholes-Merton class to price.
     */
    public double calculate(final Option option, final double underlyingPrice,
            final Calendar when) {
        if(option instanceof EuropeanCall) {
            return 0;
        } else if(option instanceof EuropeanPut) {
            return 0;
        } else {
            throw new IllegalArgumentException("Explicit Black-Scholes-Merton "
                    + "are only valid for European Options");
        }
    }
}
