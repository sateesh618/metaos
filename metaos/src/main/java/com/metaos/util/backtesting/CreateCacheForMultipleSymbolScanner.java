/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.metaos.datamgt;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.metaos.datamgt.LineParser.ErrorControl;

/**
 * Creates a cache file with points to start reading symbol data.
 */
public final class CreateCacheForMultipleSymbolScanner {
    private static final Logger log = Logger.getLogger(
            CreateCacheForMultipleSymbolScanner.class.getPackage().getName());
    private final String symbols[];
    private final String filePath;
    private final LineParser lineParser;
    private RandomAccessFile mainReader;
    private String nextLine[];
    private long nextMilliseconds[];


    /**
     * Creates cache builder for source MultipleSymbolScanner source scanner.
     *
     * @param filePath complete filename and path containing prices.
     * @param symbols set of symbols present into the file.
     * @param lineProcessor strategy to part each line.
     * @throws IOException if one symbol (at least) cannot be found.
     */
     public CreateCacheForMultipleSymbolScanner(final String filePath, 
            final String[] symbols,
            final LineParser lineParser) throws IOException {
        this.lineParser = lineParser;
        this.filePath = filePath;
        this.symbols = symbols;
    }

    /**
     * Removes previous cache and creates the new one file.
     */
    public void run() throws IOException {
        this.mainReader = new RandomAccessFile(filePath, "r");
        this.nextMilliseconds = new long[symbols.length];
        this.nextLine = new String[symbols.length];

        final ErrorControl errorControl = this.lineParser.getErrorControl();
        this.lineParser.setErrorControl(LineParser.nullErrorControl);

        final String cacheFilePath = filePath + ".cachejump";

        if(new File(cacheFilePath).exists()) {
            new File(cacheFilePath).delete();
            log.info("Removing old cache file " + filePath);
        }
        final ObjectOutputStream cacheJumpsFileOut = new ObjectOutputStream(
                    new FileOutputStream(cacheFilePath));
        log.fine("Caching jumps for future uses in file " + cacheFilePath);

        log.info("Scannig file to place reading pointers");
     
        long previousLastPosition = 0;
        for(int i=0; i<symbols.length; i++) {
            long lastPosition = this.mainReader.getFilePointer();
            long foundPosition;
            log.fine("Starting to search symbol " + symbols[i] 
                    + " from position " + lastPosition);
            for(;;) {
                foundPosition = this.mainReader.getFilePointer();
                final String line = readNextLineCycling(lastPosition);
                if(line==null) {
                    // All file's been read without finding desired symbol.
                    throw new IOException("Cannot find symbol '" 
                            + symbols[i] + "' in file '" + filePath + "'");
                }
                if(this.lineParser.isValid(line) && 
                        symbols[i].equals(this.lineParser.getSymbol(line, 0))) {
                    break;
                }
            }
            log.info("Located place for reader on " + symbols[i] + ":"
                    + foundPosition);
            final int jump = (int) 
                    (foundPosition - previousLastPosition - 1024);
            cacheJumpsFileOut.writeInt(jump>0 ? jump : 0);
            log.info("Writting to cache file jump of " + (jump>0 ? jump : 0));
            previousLastPosition = foundPosition;
        }

        log.info("File scanned. Placing reading pointers");

        cacheJumpsFileOut.flush();
        cacheJumpsFileOut.close();

        this.lineParser.setErrorControl(errorControl);
    }



    public void close() {
        try {
            this.mainReader.close();
        } catch(IOException ioe) {
            // Maybe nothing should happen in this case.... don't worry.
        }
    }


    //
    // Private stuff ----------------------------------
    //
    private String readNextLineCycling(final long positionLimit)
            throws IOException {
        final long previousPos = this.mainReader.getFilePointer();
        final String line = this.mainReader.readLine();
        final long newPos = this.mainReader.getFilePointer();
        if(previousPos<positionLimit && newPos>=positionLimit) {
            return null;
        }
        if(line==null) {
            this.mainReader.seek(0);
            if(positionLimit==0) return null;
            return this.mainReader.readLine();
        } else {
            return line;
        }
    }
}
