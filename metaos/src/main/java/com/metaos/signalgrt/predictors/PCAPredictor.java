/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.signalgrt.predictors;

import com.metaos.engine.*;
import com.metaos.util.*;
import java.util.*;

/**
 * ATTENTION: Not thread safe.
 */
public class PCAPredictor implements Predictor {
    private final double matrix[][];
    private final CalUtils.InstantGenerator instantGenerator;
    private final double minimumExplainedVariance;
    private int minInstant, maxInstant;
    private int lastInstant = -1;


    /**
     * Creates an PCAPredictor predictor with given parameters.
     *
     * @param minimumExplainedVariance quantity of variance that should be
     * explained by eigenvectors.
     */
    public PCAPredictor(final CalUtils.InstantGenerator instantGenerator,
            final double minimumExplainedVariance) {
        this.matrix = new double[instantGenerator.maxInstantValue()][];
        this.instantGenerator = instantGenerator;
        this.minInstant = 0;
        this.maxInstant = instantGenerator.maxInstantValue();
        this.minimumExplainedVariance = minimumExplainedVariance;
    }


    /**
     * Creates an PCAPredictor predictor with given parameters.
     * Only first eigenvector will be used to explain variance.
     */
    public PCAPredictor(final CalUtils.InstantGenerator instantGenerator) {
        this(instantGenerator, 0.0d);
    }


    public double predict(final Calendar ignored) {
        throw new UnsupportedOperationException(
                "Cannot predict only one value");
    }


    /**
     * Gets profile for the principal component analysis.
     */
    public double[] predictVector(final Calendar ignored) {
        this.lastInstant = -1;

        final R r = Engine.getR();
        final int n = matrix.length;
        final int m = matrix[0].length;

        // Search for zeros at the begining and at the end
        int maxIndexOfZerosAtTheBegining = -1;
        outter: for(int i=0; i<n; i++) {
            for(int j=0; j<m; j++) {
                if(matrix[i]!=null && matrix[i][j]!=0) break outter;
            }
            maxIndexOfZerosAtTheBegining++;
        }
        int minIndexOfZerosAtTheEnd = n;
        outter: for(int i=n-1; i>maxIndexOfZerosAtTheBegining; i--) {
            for(int j=0; j<m; j++) {
                if(matrix[i]!=null && matrix[i][j]!=0) break outter;
            }
            minIndexOfZerosAtTheEnd--;
        }


        // First: pass elements to R
        final int n2 = minIndexOfZerosAtTheEnd-maxIndexOfZerosAtTheBegining-1;
        r.eval("vols<-array(dim=c(" + n2 + "," + m + "))");
        for(int i=0; i<n2; i++) {
            if(matrix[i]==null) {
                matrix[i] = new double[m];
            }
            int i2 = i + maxIndexOfZerosAtTheBegining + 1;
            for(int j=0; j<m; j++) {
                r.eval("vols[" + (i+1) + "," + (j+1) + "]<-" + matrix[i2][j]);
            }
        }


        // Second: normalize volume to get volume profiles
        for(int j=0; j<m; j++) {
            r.eval("vols[," + (j+1) + "]<-100*vols[," + (j+1) 
                    + "]/sum(vols[," + (j+1) + "])");
        }

        // Third: perform PCA
        r.eval("pca.vols<-prcomp(vols, xret=TRUE, scale=TRUE)");
        
        // Fourth: calculate how many eigenvectors should be used to 
        r.eval("index<-1");
        r.eval("explainedVar<-0");
        r.eval("totalVar<-sum(pca.vols$sdev^2)");
        r.eval("while(explainedVar<" + this.minimumExplainedVariance + ") {\n" +
               "    explainedVar<-sum(pca.vols$sdev[1:index]^2)/totalVar\n" +
               "    index <- index + 1\n");
        r.eval("}");

        // Fifth: use eigenvectors in a linear combination
        r.eval("pred.vols<-array(0,dim=c(length(pca.vols$x[,1]),1))");
        r.eval("for(i in 1:index) {\n" +
               "    pred.vols<-pred.vols + pca.vols$x[,i]\n" +
               "}");

        final double core[] = r.evalDoubleArray("pred.vols"); 
        final double characteristic[] = new double[this.matrix.length];
        for(int i=0; i<n2; i++) {
            characteristic[i+maxIndexOfZerosAtTheBegining] = core[i];
        }
        
        return characteristic;
    }


    public void learnVector(final Calendar when, final double[] vals) {
        final int currentInstant = this.instantGenerator.generate(when);
        if(currentInstant>=this.matrix.length) return;

        for(int i=this.lastInstant+1; i<currentInstant; i++) {
            this.matrix[i] = new double[vals.length];
        }
        this.matrix[currentInstant] = new double[vals.length];

        for(int i=0; i<vals.length; i++) {
            if(Double.isNaN(vals[i])) {
                if(currentInstant>0) {
                    this.matrix[currentInstant][i] = 
                            this.matrix[currentInstant-1][i];
                } else {
                    this.matrix[currentInstant][i] = 0.0d;
                }
            } else {
                this.matrix[currentInstant][i] = vals[i];
            }
        }

        this.lastInstant = currentInstant;
    }


    public void learnVector(final Calendar when, final List<Double> vals) {
        final int currentInstant = this.instantGenerator.generate(when);
        assert currentInstant>this.lastInstant;

        this.lastInstant++;
        do {
            this.matrix[this.lastInstant] = new double[vals.size()];
            this.lastInstant++;
        } while(this.lastInstant<=currentInstant);

        for(int i=0; i<vals.size(); i++) {
            if(Double.isNaN(vals.get(i))) {
                if(currentInstant>0) {
                    this.matrix[currentInstant][i] = 
                            this.matrix[currentInstant-1][i];
                } else {
                    this.matrix[currentInstant][i] = 0.0d;
                }
            } else {
                this.matrix[currentInstant][i] = vals.get(i);
            }
        }
        this.lastInstant = currentInstant;
    }


    /**
     * Not supported, only several values can be learnt at same time.
     */
    public void learnValue(final Calendar ignored, final double val) {
        throw new UnsupportedOperationException("PCA cannot be trained with "
                + "only one value per bin");
    }


    public void reset() {
    }


    public String toString() {
        return "PCAPredictor";
    }
}
