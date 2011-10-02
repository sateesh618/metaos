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
        this.rEngine.evalFile("errors/errorsStatistics.r");
        this.rEngine = rEngine;
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
}
