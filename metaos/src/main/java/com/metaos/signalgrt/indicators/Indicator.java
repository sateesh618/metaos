/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.indicators;

import java.util.*;

/**
 * Interface of a techincal indicator.
 */
public interface Indicator {
    public double calculate();
    public void addValues(final double[] vals);
    public void addValues(final List<Double> vals);
    public void addValue(final double val);
    public void reset();
    public String toString();
}
