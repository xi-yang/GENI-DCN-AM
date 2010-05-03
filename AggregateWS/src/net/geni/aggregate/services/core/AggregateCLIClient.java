/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Pattern;
import java.util.Vector;
import java.util.HashMap;
import org.apache.log4j.*;

/**
 *
 * @author Xi Yang
 */
public class AggregateCLIClient {
    protected Process proc;
    protected BufferedReader in;
    protected PrintStream out;
    protected String promptPattern;
    protected String errorMsg;
    protected String buffer;
    protected Logger log;
    private int timeout;
    private final int DEFAULT_TIMEOUT = 30000; //miliseconds == 30 seconds

    private AggregateCLIClient() {}

    public AggregateCLIClient(String prompt) {
        in = null;
        out = null;
        promptPattern = prompt;
        timeout = DEFAULT_TIMEOUT;
        log = null;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return this.timeout;
    }
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean alive() {
        return (proc != null && in != null && out != null);
    }

    public boolean login(String... argv) {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
        try {
            proc = new ProcessBuilder(argv).start();
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            out = new PrintStream(proc.getOutputStream());
            if (proc==null || in==null || out==null) {
                log.info("failed to build process");
            }
        } catch (IOException e) {
            log.error("failed to init CLIClient process: " + argv.toString());
            proc = null; in = null; out = null;
            return false;
        }

        return true;
    }

    public boolean logoff(String exitString) {
        //log.debug("CLICLient::logoff w/ " + exitString);
        if (!alive())
            return false;
        try {
            sendCommand(exitString);
            in.close(); out.close();
        } catch (IOException e) {
            log.error("CLICLient::logoff IO error: " + e.getMessage());
        }
        in = null;
        out = null;
        proc = null;
        return true;
    }

    /**
     * Issues commands to PLCAPI
     * @param cmd command to run on PLCAPI
     * @return output of command
     */
    public void sendCommand(String cmd) {
        out.println(cmd);
        out.flush();
    }

    /**
     * Reads output from python until the specified pattern is reached. promptPattern
     * is often useful when used in conjunction with this method.
     * @param pattern Regular expression used to determine when output should stop being read
     * @return output read until pattern is found
     */
    public boolean readUntil(String pattern) {
        int ret = readPattern(null, null, pattern);
        if (ret == 0) {
            return true;
        }
        return false;
    }

    /**
     * Reads output from python until the specified pattern is reached. promptPattern
     * is often useful when used in conjunction with this method.
     * @param pattern1 Regular expression used to determine the first match
     * @param pattern2 Regular expression used to determine the second match
     * @param readstop Regular expression used to determine the stop condition
     * @return integer 1: first match 2: second match 0: zero match -1: IO error -2: timeout
     */
    public int readPattern(String pattern1, String pattern2, String readstop) {
        //starting the python reading thread
        Reader reader = new Reader(proc, pattern1, pattern2, readstop);
        reader.start();

        //starting the timer-sleep thread
        final boolean[] flags = {true};
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    log.error("unexpected intteruption of timer-sleep: " + e.getMessage());
                }
                flags[0] = false;
            }
        }.start();

        // execute the command and wait
        int ret = -2;
        while (flags[0] && (ret < -1)) {
            ret = reader.getExitValue();
        }
        if (flags[0] == false) {
            reader.interrupt();
            log.error("Thread reading from CLI server got stuck! It has been interrupted.");
        }
        buffer = reader.getBuffer();
        return reader.getExitValue();
    }

    public String getBuffer() {
        return buffer;
    }

    public String flush() {
        String bufferAll = "";
        try {
            if (in.ready()) {
                readUntil(promptPattern);
                bufferAll += this.buffer;
                /* Remove prompt from output */
                bufferAll = bufferAll.replaceFirst(promptPattern + "\n*", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bufferAll;
    }

    private static class Reader extends Thread {
        private final String pattern1;
        private final String pattern2;
        private final String readstop;
        private BufferedReader in;
        private Logger log;
        private int exit;
        private String buffer;


        private Reader(Process proc, String ptn1, String ptn2, String stop) {
            this.in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            this.log = null;
            this.pattern1 = ptn1;
            this.pattern2 = ptn2;
            this.readstop = stop;
            exit = -2;
            buffer = "";
        }

        public int getExitValue()
        {
            return exit;
        }

        public String getBuffer()
        {
            return buffer;
        }

        public void run() {
            log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");
            String line = "";
            char c;
            int ret = 0;
            if (readstop == null) {
                exit = -1;
                return;
            }
            try {
                while (true) {
                    if (!in.ready())
                        this.sleep(1);
                    if ((c = (char) in.read()) == -1)
                        break;
                    buffer += c;
                    if (c == '\n') {
                        line = "";
                    } else {
                        line += c;
                        if (pattern1 != null && line.matches(pattern1)) {
                            if (ret == 0)
                                ret = 1;
                        }
                        if (pattern2 != null && line.matches(pattern2)) {
                            if (ret == 0) {
                                ret = 2;
                            }
                        }
                        if (line.matches(readstop)) {
                            exit = ret;
                            return;
                        }
                    }
                }
            } catch (InterruptedException e) {
                exit = -1;
                return;
            } catch (IOException e) {
                exit = -1;
                return;
            }
        }
    }

}
