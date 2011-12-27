/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors.simple;

import com.metaos.signalgrt.predictors.*;
import java.util.*;
import java.util.logging.*;

/**
 * Moving median as predictor for the next value.
 * ATTENTION: Not thread safe.
 */
public class MovingMedian implements Predictor {
    protected static final Logger log = Logger.getLogger(
                MovingMedian.class.getPackage().getName());
    private final double memory[];
    private final double workingMemory[];
    private int head;
    private boolean trainedEnough;

    public MovingMedian(final int memorySize) {
        this.memory = new double[memorySize];
        this.workingMemory = new double[memorySize];
        this.head = 0;
        this.trainedEnough = false;
    }


    /**
     * Returns the median of learnt values, of NaN if not enough values have
     * been learnt.
     */
    public double predict(final Calendar ignored) {
        if( ! this.trainedEnough ) return Double.NaN;
        System.arraycopy(this.memory, 0, this.workingMemory, 0, memory.length);
        Arrays.sort(workingMemory);
        final double value;
        if(memory.length % 2 == 1) {
            value = workingMemory[(memory.length+1)/2-1];
        } else {
            final double lower = workingMemory[memory.length/2-1];
            final double upper = workingMemory[memory.length/2];
            value = (lower + upper) / 2.0;
        }
        return value;
    }


    public double[] predictVector(final Calendar ignored) {
        return new double[] { predict(ignored) };
    }


    public void learnVector(final Calendar ignored, final double[] vals) {
        if(vals.length>memory.length) {
            for(int i=vals.length-memory.length-1;i<vals.length; i++) {
                this.learnValue(null, vals[i]);
            }
        } else {
            for(int i=0; i<vals.length; i++) {
                this.learnValue(null, vals[i]);
            }
        }
    }


    public void learnVector(final Calendar ignored, final List<Double> vals) {
        if(vals.size()>memory.length) {
            for(int i=vals.size()-memory.length-1;i<vals.size(); i++) {
                if(vals.get(i)!=null) this.learnValue(null, vals.get(i));
            }
        } else {
            for(int i=0; i<vals.size(); i++) {
                if(vals.get(i)!=null) this.learnValue(null, vals.get(i));
            }
        }
    }


    public void learnValue(final Calendar ignored, final double val) {
        if(Double.isNaN(val)) return;
        this.memory[this.head] = val;
        this.head = this.head + 1;
        if(this.head >= this.memory.length) {
            this.trainedEnough = true;
            this.head = 0;
        }
    }


    public void reset() {
        for(int i=0; i<this.memory.length; i++) {
            this.memory[i] = 0;
        }
        this.head = 0;
        this.trainedEnough = false;
    }


    public String toString() {
        return "Median(" + this.memory.length + ")";
    }
}
