/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.juant.maths.pricer.options;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;
import com.juant.deriva.options.*;

/**
 * Explicit method black scholes for call and put options.
 */
public class ExplicitBlackScholesMerton implements PriceCalculator {
    public double calculate(final Option option, final double underlyingPrice,
            final Calendar when) {
        if(option instanceof EuropeanCall) {
        } else if(option instanceof EuropeanPut) {
        } else {
            throw new IllegalArgumentException("Explicit Black-Scholes-Merton "
                    + "are only valid for European Options");
        }
    }
}
