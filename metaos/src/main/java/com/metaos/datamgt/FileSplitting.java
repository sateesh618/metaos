/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metaos.datamgt;

import java.io.*;
import java.text.*;
import java.util.*;
import com.metaos.util.*;

/**
 * Splitting of information in several outputs, one per each stock and date,
 * writting it following Reuters format file.
 *
 * Information should be provided in time sequential order.
 *
 * @see ReutersCSVLineParser
 */
public class FileSplitting {
    private static final SimpleDateFormat fileDateParser = new SimpleDateFormat(
            "yyyy/MM/dd");


    private final String resolution, repository;

    /**
     * @param repository complete path to repository base directory.
     * @param resolution prefix for file naming.
     */
    public FileSplitting(final String repository, final String resolution) {
        this.resolution = resolution;
        this.repository = repository;
    }


    /**
     * Utility function to get the name of the file containing trades for
     * given symbol and day.
     */
    public String getFileName(final Calendar when, final String symbol) {
        final StringBuffer fileName = new StringBuffer(repository);
        fileName.append(fileDateParser.format(when.getTime())).append(".")
                .append(symbol).append(".").append(resolution).append(".csv");
        return fileName.toString();
    }


    /**
     * Creates path to desired file.
     */
    public void createPath(final String fileName) {
        final String filePath = fileName.substring(0,fileName.lastIndexOf("/"));
        new File(filePath).mkdirs();
    }


    //
    // Inner utility classes  -------------------------------------
    //

    /**
     * Splits information in different outputs.
     */
    public class CSVReutersSplitter implements Listener {
        private final List<Field> fields;
        private final Map<String, Calendar> processingDays;
        private final Map<String, PrintStream> processingPrintStreams;

        public CSVReutersSplitter() {
            this.fields = new ArrayList<Field>();
            this.processingDays = new HashMap<String, Calendar>();
            this.processingPrintStreams = new HashMap<String, PrintStream>();

            final Field fieldToAvoid = new Field.EXTENDED(
                    Field.Qualifier.NONE,"GMT");
            for(final Field f:ReutersCSVLineParser.reverseFieldsMap.keySet()) {
                if(f.equals(fieldToAvoid)) continue;
                this.fields.add(f);
            }
        }


        /**
         * Receives notification signals.
         */
        public void notify(final ParseResult result) {
            final List<String> symbols = result.getSymbols();
            for(final String s : symbols) {
                try {
                    saveTrade(result.getLocalTimestamp(), s, result.values(s));
                } catch(IOException ioe) {
                    // What should I do? Ignore?
                }
            }
        }


        //
        // Private stuff ------------------------------------
        //


        /**
         * Saves trade information for a symbol and date.
         */
        private void saveTrade(final Calendar when, final String symbol, 
                final Map<Field, Double> values) throws IOException {
            final Calendar currentDay = CalUtils.normalizedClone(when);
            final Calendar previousDay = this.processingDays.get(symbol);
            PrintStream output = this.processingPrintStreams.get(symbol);

            if(previousDay==null || previousDay.before(currentDay)) {
                // Closes previous output (if exists) and opens new one
                if(output != null) {
                    output.flush();
                    output.close();
                }
                final String fileName = this.repository + "/" 
                        + getFileName(when, symbol);
                createPath(fileName);
                final File currentFile = new File(fileName);
                output = new PrintStream(currentFile);
                this.processingPrintStreams.put(symbol, output);

                // Writes header
                output.print("#RIC,Date[G],Time[G],GMT Offset");

                for(final Field f : this.fields) {
                    output.print(",");
                    output.print(ReutersCSVLineParser.reverseFieldsMap.get(f));
                }
                output.println();
            }
            // Writes over previously open output file
            output.print(symbol);
            output.print(",");
            output.print(ReutersCSVLineParser.dateFormat.format(
                    when.getTime()));
            output.print(",");
            output.print(ReutersCSVLineParser.timeFormat.format(
                    when.getTime()));
            output.print(",");
            output.print(when.get(Calendar.ZONE_OFFSET) / (1000 * 60 * 60));

            for(final Field f : this.fields) {
                output.print(",");
                output.print(values.get(f)!=null ? ReutersCSVLineParser
                        .doubleFormat.format(values.get(f)) : "-");
            }
            output.println();

            this.processingDays.put(symbol, currentDay);
        }
    }



    /**
     * Prices source for only one symbol for data split in different files,  
     * one for each day.
     */
    public class CSVReutersSingleSymbol implements LineScanner {
        private final Calendar initDay, endDay;
        private final String symbol;
        private final SpreadTradesMgr spreadTradesMgr;

        /**
         * @param initDay first included day for scanning.
         * @param endDay last included day for scanning.
         */
        public CSVReutersSingleSymbol(final String symbol, 
                final SpreadTradesMgr spreadTradesMgr, final Calendar initDay, 
                final Calendar endDay) {
            this.symbol = symbol;
            this.spreadTradesMgr = spreadTradesMgr;
            this.initDay = CalUtils.normalizedClone(initDay);
            this.endDay = CalUtils.normalizedClone(endDay);
        }


        /**
         * Starts running scanner to get all prices from source.
         */
        public void run() {
            Calendar currentDay = initDay;
            while( ! currentDay.after(endDay) ) {
                final String fileName = getFileName(currentDay, this.symbol);
                try {
                    final LineParser lineParser = new ReutersCSVLineParser(
                            fileName);
                    final LineScanner source = new SingleSymbolScanner(
                            fileName, this.symbol, 
                            lineParser, this.spreadTradesMgr);
                    source.run();
                } catch(IOException ioe) {
                    // Cannot open required file...
                }
                currentDay.add(Calendar.DAY_OF_MONTH, 1);
            }
        }


        /**
         * Resets reader to the start of source.
         */
        public void reset() {
        }


        /**
         * Gets the next set of prices.
         */
        public boolean next() {
            throw new UnsupportedOperationException("Not implemented");
        }

        /**
         * Gets the first set of prices.
         * Optional method: maybe has no sense for realtime sources.
         */
        public boolean first() {
            throw new UnsupportedOperationException("Not implemented");
        }

        /**
         * Gets the last set of prices.
         * Optional method: maybe has no sense for realtime sources.
         */
        public boolean last() {
            throw new UnsupportedOperationException("Not implemented");
        }


        /**
         * Does nothing...
         */
        public void close() {
        }
    }
}
