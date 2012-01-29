/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and header must be included or referenced 
 * in each piece of code derived from project.
 */
package com.metaos.signalgrt.predictors.specific.volume;

import java.util.*;

/**
 * Utility class with methods to remove outliers from a daily volume data.
 *
 * Implementation and design of class is absolutely faulty... but it's
 * very straitghforward to use.
 */
public final class RemoveVolumeData {
    /**
     * Cleans received data, removing values greater in more than 150% over
     * the last valid value (assumed to be closing auction).
     */
    public static void cleanOutliers(final double dailyData[]) {
        double closingVolume = Double.MAX_VALUE;
        int closingIndex = -1;
        for(int i=dailyData.length-1; i>=0; i--) {
            if( ! Double.isNaN(dailyData[i]) && dailyData[i]>0 ) {
                closingVolume = dailyData[i];
                closingIndex = i;
                break;
            }
        }
        for(int i=1; i<dailyData.length-1; i++) {
            if(i==closingIndex) continue;
            if( ! Double.isNaN(dailyData[i])
                    && dailyData[i]>closingVolume*1.50 ) {
                if( ! Double.isNaN(dailyData[i-1]) ) {
                    dailyData[i] = dailyData[i-1];
                } else if( ! Double.isNaN(dailyData[i+1]) ) {
                    dailyData[i] = dailyData[i+1];
                } else dailyData[i] = Double.NaN;
            }
        }
    }


    /**
     * Removes elements from head and from tail.
     */
    public static void cutHeadAndTail(final double dailyData[],
            final int ignoreElementsHead, final int ignoreElementsTail) {
        for(int i=0; i<dailyData.length; i++) {
            if(dailyData[i]!=0 && !Double.isNaN(dailyData[i])) {
                for(int j=0; j<ignoreElementsHead; j++) {
                    dailyData[i+j] = Double.NaN;
                }
                break;
            }
        }
        for(int i=dailyData.length-1; i>=0; i--) {
            if(dailyData[i]!=0 && !Double.isNaN(dailyData[i])) {
                for(int j=0; j<ignoreElementsTail; j++) {
                    dailyData[i-j] = Double.NaN;
                }
                break;
            }
        }
    }
}
