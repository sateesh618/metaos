/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.juant.deriva.options;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;
import com.juant.deriva.Derivative;
import com.juant.Instrument;
import com.juant.pricer.options.PriceCalculator;
import com.juant.util.*;

/**
 * Option base commons.
 */
public abstract class Option implements Derivative {
    protected final Instrument underlying;
    protected final double strikePrice;
    protected final double prime;
    protected final Calendar strike;
    protected final int size;
    protected final PriceCalculator pricer;

    public Option(final double prime, final double strikePrice,
            final Calendar strike, final Instrument underlying, final int size,
            final PriceCalculator pricer) {
        this.underlying = underlying;
        this.strikePrice = strikePrice;
        this.prime = prime;
        this.strike = strike;
        this.size = size;
        this.pricer = pricer;
    }

    public double getAcquisitionPrice() {
        return this.prime * this.size;
    }

    public double getReleaseCosts(final Calendar date) {
        throw new NoSuchMethodError("Not implemented yet");
    }

    public Instrument getUnderlying() {
        return this.underlying;
    }

    public double getDelta(final Calendar when) {
        final double dS = 0.0001;
        final double S0 = this.underlying.getPrice(when);

        final double a = this.pricer.calculate(this, S0-dS, when);
        final double b = this.pricer.calculate(this, S0+dS, when);
        return (a-b)/(2*dS);
    }


    public double getTheta(final Calendar when) {
        int dT = 1;
        double S = this.getUnderlying().getPrice(when);
        final Calendar t1 = CalUtils.clone(when);
        final Calendar t2 = CalUtils.clone(when);
        t1.add(Calendar.DAY_OF_MONTH, -dT);
        t2.add(Calendar.DAY_OF_MONTH, dT);
        return -(getPrice(t2, S) - getPrice(t1, S))/(2*dT);
    }


    public double getGamma(final Calendar when) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");    
    }

    public double getSpeed(final Calendar when) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");    
    }


    //
    // Abstract methods
    //
    public abstract double getRho(final Calendar when);
    public abstract double getVega(final Calendar when);

    //
    // Protected hook methods
    //

    /**
     * Internal shortcut to calculate prices.
     */
    protected abstract double getPrice(final Calendar when,
            final double underlyingPrice,final PriceCalculator priceCalculator);
}
