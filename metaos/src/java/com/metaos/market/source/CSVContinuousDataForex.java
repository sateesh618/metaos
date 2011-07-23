/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.market.source;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Prices source factory for CSV files generated with "ContinuousDataForex.mq4"
 * program.
 */
public final class CSVContinuousDataForex {
    private static final Logger log = Logger.getLogger(
            CSVContinuousDataForex.class.getPackage().getClass().getName());

    private static final CSVContinuousDataForex instance = 
            new CSVContinuousDataForex();
    
    public static CSVContinuousDataForex getInstance() { return instance; }

    private CSVContinuousDataForex() {
    }

    /**
     * Creates a Source of data from a set of CSVs with forex info organized
     * into a set of directories looking like 
     * <i>rootPath/sundir[i]/fileName</i>.
     */
    public PricesSource weekly(final String rootPath, final String[] subdirs,
            final String fileName, final String[] symbols) {
        return new CSVPricesSource(rootPath, subdirs, fileName, symbols);
    }




    //
    // Private inner classes.
    //
    private static final class CSVPricesSource extends BasicPricesSource {
        private BufferedReader fileReader;
        private int currentIndex;
        private final String rootPath, subdirs[], fileName;
        private final String symbols[];
        private final List<String> processedSymbols;
        private boolean isClosed = false;

        public CSVPricesSource(final String rootPath, final String subdirs[],
                final String fileName, final String[] symbols) {
             this.currentIndex = 0;
             this.fileName = fileName;
             this.subdirs = subdirs;
             this.rootPath = rootPath;
             this.symbols = symbols;
             this.processedSymbols = Arrays.asList(symbols);
        }


        public void run() {
            if(this.jumpToNextFile()) {
                while(this.next());
            }
        }

        /**
         * Reads next line and notifies markets and susbscribed observers.
         * @return true if more lines would be available, false if there was
         * no possible to read the line, since the EOF has been reached.
         */
        public boolean next() {
            final String line = readNextLine();
            if(line==null) return false;

            final String[] parts = line.replaceAll("\\s", "").split(",");
            final long millis = Long.parseLong(parts[0]) * 1000;
            final Calendar moment = Calendar.getInstance();
            moment.setTimeInMillis(millis);
            for(int i=1, j=0; i<parts.length; i+=6, j++) {
                final double high = Double.parseDouble(parts[i]);
                final double low = Double.parseDouble(parts[i+1]);
                final double open = Double.parseDouble(parts[i+2]);
                final double close = Double.parseDouble(parts[i+3]);
                final long vol = Long.parseLong(parts[i+4]);
                this.sendPriceToMarkets(moment, symbols[j] + "-HIGH", high); 
                this.sendPriceToMarkets(moment, symbols[j] + "-LOW", low); 
                this.sendPriceToMarkets(moment, symbols[j] + "-OPEN", open); 
                this.sendPriceToMarkets(moment, symbols[j] + "-CLOSE", close); 
                this.sendVolumeToMarkets(moment, symbols[j], vol); 
            }
            this.notifyListeners(moment, processedSymbols);
            return true;
        }


        public void close() {
            this.isClosed = true;
            try {
                this.fileReader.close();
            } catch(IOException ioe) {
                // Maybe nothing should happen in this case.... don't worry.
            }
        }


        //
        // Private stuff ----------------------------------
        //

        /**
         * Reads next line and notifies markets and susbscribed observers.
         * @return null if there is no more lines to read.
         */
        private String readNextLine() {
            try {
                if(this.isClosed) return null;
                String line = fileReader.readLine();
                if(line == null) {
                    if(jumpToNextFile()) {
                        return readNextLine();
                    } else {
                        return null;
                    }
                } else {
                    return line;
                }
            } catch(IOException ioe) {
                log.log(Level.SEVERE, "Error reading line from CSV file", ioe);
                return null;
            }
        }


        /**
         * Moves to next file.
         * @return false if there is no more files to read, true otherwise.
         */
        private boolean jumpToNextFile() {
            try {
                if(this.fileReader!=null) fileReader.close();
                this.currentIndex++;
                if(this.currentIndex<this.subdirs.length) {
                    this.fileReader = new BufferedReader(
                            new FileReader(rootPath + "/" 
                                + subdirs[currentIndex] + "/" + fileName));
                    return true;
                } else {
                    return false;
                }
            } catch(IOException ioe) {
                log.log(Level.SEVERE, "Error jumping to file " + rootPath 
                        + "/" + subdirs[currentIndex] + "/" + fileName, ioe);
                return false;
            }
        }
    }
}
