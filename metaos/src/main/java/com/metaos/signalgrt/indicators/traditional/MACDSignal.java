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
 * MACD Signal indicator.
 * See http://en.wikipedia.org/wiki/MACD
 *
 * ATTENTION: Not thread safe.
 */
public class MACDSignal implements Indicator {
    private final double[] memory;
    private final MACDLine macd;
    private int head;
    private final int faster, slower;
    private final double k;

    /**
     * Builds MACD Signal according to given parameters.
     * Remember that MACDSignal = EMA(MACDLine(faster,slower), signal)
     */
    public MACDSignal(final int faster, final int slower, final int signal) {
        this.memory = new double[signal];
        this.head = 0;
        this.macd = new MACDLine(faster, slower);
        this.faster = faster;
        this.slower = slower;
        this.k = 2 / (signal + 1);
    }


    /**
     * Returns the MACD signal of notified values.
     * If not enough values have been received, moving averages will consider
     * them as zero.
     */
    public double calculate() {
        double ema = 0;
        for(int i=0, pointer=head-1; i<this.memory.length; i++, pointer--) {
            if(pointer<0) pointer = this.memory.length - 1;
            ema += k * (this.memory[pointer] - ema);
        }

        return ema;
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

        this.macd.addValue(val);
        this.memory[this.head] = this.macd.calculate();
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
        return "MACDSignal(" + this.faster + "," + this.slower + ","
                + this.memory.length + ")";
    }
}
