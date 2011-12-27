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
 * Moving average. 
 *
 * ATTENTION: Not thread safe.
 */
public class MovingAverage implements Indicator {
    private final double memory[];
    private int head;

    public MovingAverage(final int size) {
        this.memory = new double[size];
        this.head = 0;
    }


    public double calculate() {
        double total = 0;
        for(int i=0;i<this.memory.length; i++) {
            total = total + this.memory[i];       
        }
        return total / this.memory.length;
    }



    public void addValues(final double[] vals) {
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


    public void addValues(final List<Double> vals) {
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


    public void addValue(final double val) {
        this.memory[this.head] = val;
        this.head = this.head + 1;
        if(this.head >= this.memory.length) {
            this.head = 0;
        }
    }


    public void reset() {
        for(int i=0; i<this.memory.length; i++) {
            this.memory[i] = 0;
        }
        this.head = 0;
    }


    public String toString() {
        return "MA(" + this.memory.length + ")";
    }
}
