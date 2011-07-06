/*
 * Copyright 2011 - 2012
 * All rights reserved. License and terms according to LICENSE.txt file.
 * The LICENSE.txt file and this header must be included or referenced 
 * in each piece of code derived from this project.
 */
package com.juant.engine;

import java.io.*;
import java.util.*;
import org.python.core.*;
import org.python.util.*;

public class Engine {
    private final PythonInterpreter interpreter;
    private final String args[];

    public Engine(final String pyFile, final String args[]) throws IOException {
        this.args = args;
        final FileReader reader = new FileReader(pyFile);
        final char[] pyBuffer = new char[(int) new File(pyFile).length()];
        reader.read(pyBuffer);
        final String pyCode = new String(pyBuffer);

        this.interpreter = new PythonInterpreter();
//        this.
        this.interpreter.exec(pyCode);
        System.out.println("Engine started up");
    }


    public String execute(final String pyFile) throws IOException {
        final FileReader reader = new FileReader(pyFile);
        final char[] pyBuffer = new char[(int) new File(pyFile).length()];
        reader.read(pyBuffer);
        final String pyCode = new String(pyBuffer);

        this.interpreter.exec(pyCode);
        return "ok";
    }

    public static void main(final String args[]) throws Exception {
        final String args2[] = new String[args.length-2];
        for(int i=0; i<args.length-2; i++) args2[i] = args[i+2];

        final Engine engine = new Engine(args[0], args2);
        engine.execute(args[1]);
    }
}
