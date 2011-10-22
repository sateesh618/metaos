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
            "yyyy-MM-dd");


    /**
     * Utility function to get the name of the file containing trades for
     * given symbol and day.
     */
    public static String getFileName(final Calendar when, final String symbol) {
        final StringBuffer fileName = new StringBuffer(symbol).append(".");
        fileName.append(fileDateParser.format(when)).append(".csv");
        return fileName.toString();
    }


    //
    // Inner utility classes  -------------------------------------
    //

    /**
     * Splits information in different outputs.
     */
    public static class CSVReutersSplitter implements Listener {
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
// ¿Qué hacer en este caso?
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
                final File currentFile = new File(getFileName(when, symbol));
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
            output.print(ReutersCSVLineParser.dateFormat.format(when));
            output.print(",");
            output.print(ReutersCSVLineParser.timeFormat.format(when));
            output.print(",");
            output.print(when.get(Calendar.ZONE_OFFSET) / (1000 * 60 * 60));

            for(final Field f : this.fields) {
                output.print(",");
                output.print(values.get(f)!=null ? ReutersCSVLineParser
                        .doubleFormat.format(values.get(f)) : "-");
            }
            output.println();
        }
    }
}
