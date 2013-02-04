package net.geni.aggregate.client.examples;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * Borrowed from the ESnet OSCARS Project
 */

public class Args {

    static public String getArg(BufferedReader br, String prompt)
          throws IOException {

        String inarg = null;
        while (inarg == null || inarg.equals ("")){
            System.out.print(prompt + ": ");
            System.out.flush();
            inarg = br.readLine().trim();
        }
        return inarg;
    }

    static public String getArg(BufferedReader br, String prompt, String def)
            throws IOException {

        String inarg = null;
        if(def == null){
            def = "";
        }
        System.out.print(prompt + ": [" + def.trim() +"] ");
        System.out.flush();
        inarg = br.readLine().trim();
        if (inarg.equals("")) { inarg = def.trim(); }
        return inarg;
    }

    static public String getLines(BufferedReader br, String prompt)
            throws IOException {

        String inarg = "";
        String output = "";
        System.out.print(prompt + ": ");
        System.out.flush();
        inarg = br.readLine().trim();
        while (!inarg.equals("")) {
            output += inarg.trim() + " ";
            inarg = br.readLine();
        }
        return output.trim();
    }
}
