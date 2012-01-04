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
 * MACD Line indicator.
 * See http://en.wikipedia.org/wiki/MACD
 *
 * ATTENTION: Not thread safe.
 */
public class MACDLine implements Indicator {
    private final double[] memory;
    private int head;
    private final int faster, slower;
    private final double kFaster, kSlower;

    /**
     * Builds MACD Line according to given parameters.
     * Remember that MACD Line = EMA(faster) - EMA(slower)
     */
    public MACDLine(final int faster, final int slower) {
        this.faster = faster;
        this.slower = slower;
        this.memory = new double[(int) Math.max(faster, slower)];
        this.head = 0;
        this.kFaster = 2 / (faster+1);
        this.kSlower = 2 / (slower+1);
    }


    /**
     * Returns the MACD line of notified values.
     * If not enough values have been received, moving averages will consider
     * them as zero.
     */
    public double calculate() {
        double emaFaster = 0;
        for(int i=0, pointer=head-1; i<this.faster; i++, pointer--) {
            if(pointer<0) pointer = memory.length - 1;
            emaFaster += kFaster * (memory[pointer] - emaFaster);
        }

        double emaSlower = 0;
        for(int i=0, pointer=head-1; i<this.slower; i++, pointer--) {
            if(pointer<0) pointer = memory.length - 1;
            emaSlower += kSlower * (memory[pointer] - emaSlower);
        }

        return emaFaster - emaSlower;
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
        return "MACDLine(" + this.faster + "," + this.slower + ")";
    }
}
