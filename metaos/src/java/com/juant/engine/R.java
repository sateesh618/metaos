/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.juant.engine;

import java.io.*;
import java.util.*;
import org.rosuda.JRI.*;
import org.python.core.PyList;

/**
 * Adapter for JRI object.
 */
public class R {
    private final Rengine engine;

    /**
     * Creates a new no-verbose R engine adapter.
     * @param rFiles array of source files in R to load at startup time.
     */
    public R(final String rFiles[]) throws IOException {
        this(rFiles, false);
    }

    /**
     * Creates a new R engine adapter.
     * @param rFiles array of source files in R to load at startup time.
     * @param verbose true to enable verbosity, false to run the adapter 
     * silently.
     */
    public R(final String rFiles[], final boolean verbose) throws IOException {
        if(!Rengine.versionCheck()) {
            throw new RuntimeException("Mismatch R version");
        }
        this.engine = new Rengine(new String[] {"--vanilla"}, false, 
                verbose ? new VerboseLoopCallbacks() : new NullLoopCallbacks());
        if(!this.engine.waitForR()) {
            throw new RuntimeException("R cannot be loaded");
        }

        for(final String file : rFiles) {
            this.engine.eval("source(\"" + file + "\")");
        }

    }


    public R(final String rFile) throws IOException {
        this(new String[] {rFile});
    }

    public void end() {
        this.engine.stop();
    }

    public void set(final String name, final Object value) {
        if(value instanceof boolean[]) {
            this.engine.assign(name, (boolean[]) value);
        } else if(value instanceof double[]) {
            this.engine.assign(name, (double[]) value);
        } else if(value instanceof int[]) {
            this.engine.assign(name, (int[]) value);
        } else if(value instanceof String) {
            final REXP x = this.engine.eval(value.toString());
            this.engine.assign(name, x);
        } else {
            System.err.println("Ignoring unknown type " + value.getClass()
                    + " to assign");
        }
    }
    
    public void setDoubles(final String name, final PyList list) {
        final double[] values = new double[list.size()];
        for(int i = 0; i < values.length; i++) {
            values[i] = (Double) list.get(i);
        }
        this.engine.assign(name, values);
    }
 
    public REXP eval(final String what) {
        return this.engine.eval(what);
    }

    public boolean evalBool(final String what) {
        final REXP x = this.engine.eval(what);
        return x.asBool().isTRUE();
    }

    public String evalString(final String what) {
        final REXP x = this.engine.eval(what);
        return x.asString();
    }

    public double evalDouble(final String what) {
        final REXP x = this.engine.eval(what);
        return x.asDouble();
    }

    public double[] evalDoubleArray(final String what) {
        final REXP x = this.engine.eval(what);
        return x.asDoubleArray();
    }

    public double[][] evalDoubleMatrix(final String what) {
        final REXP x = this.engine.eval(what);
        return x.asDoubleMatrix();
    }





    //
    // Private stuff ----------------------------------
    //

    /**
     * Null implementation for R callbacks.
     */
    private final class NullLoopCallbacks implements RMainLoopCallbacks {
        public void rWriteConsole(Rengine re, String text, int oType) {
        }

        public void rBusy(Rengine re, int which) {
        }
   
        public String rReadConsole(final Rengine re, final String prompt, 
                final int addToHistory) {
            return null;
        }

        public void rShowMessage(Rengine re, String message) {
        }
                       
        public String rChooseFile(Rengine re, int newFile) {
            return null;
        }

        public void rFlushConsole(final Rengine re) {
        }
       
        public void rLoadHistory(final Rengine re, final String filename) {
        }
                  
        public void rSaveHistory(final Rengine re, final String filename) {
        }
    }

    /**
     * Verbose implementation for R callbacks.
     */
    private final class VerboseLoopCallbacks implements RMainLoopCallbacks {
        public void rWriteConsole(Rengine re, String text, int oType) {
            System.out.println(text);
        }

        public void rBusy(Rengine re, int which) {
            System.out.println("> Busy " + which);
        }
   
        public String rReadConsole(final Rengine re, final String prompt, 
                final int addToHistory) {
            return null;
        }

        public void rShowMessage(Rengine re, String message) {
            System.out.println(message);
        }
                       
        public String rChooseFile(Rengine re, int newFile) {
            return null;
        }

        public void rFlushConsole(final Rengine re) {
        }
       
        public void rLoadHistory(final Rengine re, final String filename) {
            System.out.println("> Loading history into " + filename);
        }
                  
        public void rSaveHistory(final Rengine re, final String filename) {
            System.out.println("> Saving history into " + filename);
        }
    }

}
