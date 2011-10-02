/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.ext.predictors;

import java.util.*;

/**
 * Moving avergae as predictor for the next value.
 * ATTENTION: Not thread safe.
 */
public class MovingAverage implements Predictor {
    private final double memory[];
    private int head;

    public MovingAverage(final int memorySize) {
        this.memory = new double[memorySize];
        this.head = 0;
    }


    public double predict() {
        double total = 0;
        for(int i=0;i<this.memory.length; i++) {
            total = total + this.memory[i];       
        }
        return total / this.memory.length;
    }


    public void learn(final double[] vals) {
        if(vals.length>memory.length) {
            for(int i=vals.length-memory.length-1;i<vals.length; i++) {
                this.learn(vals[i]);
            }
        } else {
            for(int i=0; i<vals.length; i++) {
                this.learn(vals[i]);
            }
        }
    }


    public void learn(final double val) {
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
}
