/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.ext.error;

import java.io.*;
import java.util.*;
import com.metaos.datamgt.*;
import com.metaos.util.*;
import com.metaos.engine.*;

/**
 * Interface with R engine to deal with errors.
 */
public class ErrorsStatistics {
    private final R rEngine;

    /**
     * Creates a new object wrapping <i>errors/errorsStatistics.r</i>
     * R file.
     */
    public ErrorsStatistics(final R rEngine) throws IOException {
        this.rEngine = rEngine;
        this.rEngine.evalFile("errors/errorsStatistics.r");
        this.rEngine.eval("errorsStatistics <- ErrorsStatistics()");
    }


    /**
     * Resets statistics.
     */
    public void reset() {
        this.rEngine.eval("errorsStatistics$reset()");
    }

    /**
     * Adds an error to statistics register.
     */
    public void addError(final double val) {
        this.rEngine.eval("errorsStatistics$addError(" + val + ")");
    }

    /**
     * Gets mean of erros.
     */
    public double mean() {
        this.rEngine.eval("x <- errorsStatistics$listAll()");
        return this.rEngine.evalDouble("mean(x)");
    }

    /**
     * Gets variance of errors.
     */
    public double var() {
        this.rEngine.eval("x <- errorsStatistics$listAll()");
        return this.rEngine.evalDouble("var(x)");
    }

    /**
     * Gets maximum value.
     */
    public double max() {
        this.rEngine.eval("x <- errorsStatistics$listAll()");
        return this.rEngine.evalDouble("max(x)");
    }

    /**
     * Gets minimum value.
     */
    public double min() {
        this.rEngine.eval("x <- errorsStatistics$listAll()");
        return this.rEngine.evalDouble("min(x)");
    }

}
