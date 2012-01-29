/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.indicators.traditional;

import com.metaos.signalgrt.indicators.*;
import java.util.*;

/**
 * Moving median.
 *
 * ATTENTION: Not thread safe.
 */
public class MovingMedian implements Indicator {
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
    public double calculate() {
        final int memoryLength;
        final double[] tmpMemory;
        if( ! this.trainedEnough ) {
            memoryLength = this.head;
            tmpMemory = this.workingMemory;
        } else {
            memoryLength = memory.length;
            tmpMemory = this.workingMemory;
        }
        System.arraycopy(this.memory, 0, tmpMemory, 0, memoryLength);
        Arrays.sort(tmpMemory);
        final double value;
        if(tmpMemory.length % 2 == 1) {
            value = tmpMemory[(tmpMemory.length+1)/2-1];
        } else {
            final double lower = tmpMemory[tmpMemory.length/2-1];
            final double upper = tmpMemory[tmpMemory.length/2];
            value = (lower + upper) / 2.0;
        }
        return value;
    }


    public void addValues(final double[] vals) {
        if(vals.length>memory.length) {
            for(int i=vals.length-memory.length-1;i<vals.length; i++) {
                this.addValue(vals[i]);
            }
        } else {
            for(int i=0; i<vals.length; i++) {
                this.addValue(vals[i]);
            }
        }
    }


    public void addValues(final List<Double> vals) {
        if(vals.size()>memory.length) {
            for(int i=vals.size()-memory.length-1;i<vals.size(); i++) {
                if(vals.get(i)!=null) this.addValue(vals.get(i));
            }
        } else {
            for(int i=0; i<vals.size(); i++) {
                if(vals.get(i)!=null) this.addValue(vals.get(i));
            }
        }
    }


    public void addValue(final double val) {
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
