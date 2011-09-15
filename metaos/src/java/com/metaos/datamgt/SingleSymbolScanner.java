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
import java.util.regex.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Source of prices for ONE symbol ordered by date.
 */
public class SingleSymbolScanner implements LineScanner {
    private boolean isClosed = false;
    private boolean endReached = false;
    private final BufferedReader fileReader;
    private final LineParser lineParser;
    private final LinesAccumulator linesAccumulator;
    protected final String symbol;
    private String currentLine, firstLine, lastLine;


    /**
     * To be used by extending classes.
     */
    public SingleSymbolScanner(final String filePath, final String symbol,
            final LineParser lineParser,final LinesAccumulator linesAccumulator)
            throws IOException {
        this.fileReader = new BufferedReader(new FileReader(filePath));
        this.symbol = symbol;
        this.lineParser = lineParser;
        this.linesAccumulator = linesAccumulator;
    }


    public final void run() {
        while(this.next());
    }


    public final boolean next() {
        if(this.readNextLine()) {
            final ParseResult result = this.lineParser.parse(this.firstLine);
            this.linesAccumulator.accumulate(result);
            return true;
        } else {
            this.linesAccumulator.endAccumulation();
            return false;
        }
    }


    public boolean first() {
        if(this.isClosed) return false;
        if(this.firstLine == null) {
            while(!this.endReached && !this.isClosed) {
                if(this.readNextLine()) {
                    if(this.firstLine != null) return this.first();
                    else return false;
                }
            }
            return false;
        } else {
            final ParseResult result = this.lineParser.parse(this.firstLine);
            this.linesAccumulator.accumulate(result);
            this.linesAccumulator.endAccumulation();
            return true;
        }
    }


    public boolean last() {
        // Implementation notes: we have no two choices:
        //   a) read the whole source remembering the last valid read line. 
        //   b) read backwards from last line return the first valid line.
        // Particullary, reading only the last line is not useful 
        // (it may be invalid).
        
        // Read the whole source looking for the last valid line.

        if(this.isClosed) return false;
        if(this.lastLine == null) {
            Calendar moment = null;
            while(this.readNextLine());
            this.lastLine = this.currentLine;
            if(this.lastLine != null) return this.last();
            else return false;
        } else {
            final ParseResult result = this.lineParser.parse(this.lastLine);
            this.linesAccumulator.accumulate(result);
            this.linesAccumulator.endAccumulation();
            return true;
        }
    }


    public final void close() {
        this.isClosed = true;
        try {
            this.fileReader.close();
        } catch(IOException ioe) {
            // Maybe nothing should happen in this case.... don't worry.
        }
    }

    //
    // Private stuff -------------------------
    //

    /**
     * Moves to next valid line.
     *
     * @return false if cannot move to next line (no more valid lines to read).
     */
    private boolean readNextLine() {
        if(this.isClosed || this.endReached) return false;
        try {
            final String line = fileReader.readLine();
            if(line==null) {
                this.endReached = true;
                return false;
            }

            final Calendar moment = this.lineParser.getTimestamp(line);
            if(this.symbol.equals(this.lineParser.getSymbol(line, 0)) 
                    && this.lineParser.isValid(line)) {
                this.currentLine = line;
                if(this.firstLine==null) this.firstLine = line;
                return moment != null;
            } else {
                if(this.readNextLine()) {
                    return true;
                } else {
                    this.lastLine = line;
                    this.endReached = true;
                    return false;
                }
            }
        } catch(Exception e) {
            this.endReached = true;
            return false;
        }
    }
}
