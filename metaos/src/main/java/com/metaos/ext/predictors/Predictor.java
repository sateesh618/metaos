/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.ext.predictors;

import java.util.*;

/**
 * Most general interface for a single value predictor, 
 * able to learn and to be reset.
 */
public interface Predictor {
    public double predict();
    public void learn(final double[] vals);
    public void learn(final double val);
    public void reset();
}
