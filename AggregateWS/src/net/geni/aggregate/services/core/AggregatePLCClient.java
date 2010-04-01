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
import org.apache.log4j.*;

/**
 *
 * @author root
 */
public class AggregatePLCClient {

    protected Process proc;
    protected BufferedReader in;
    protected PrintStream out;
    protected String errorMsg;
    protected String promptPattern;
    protected String passwordPromptPattern;
    protected String buffer;
    private int timeout;
    private Logger log;
    final private int DEFAULT_TIMEOUT = 5; //seconds?

    public AggregatePLCClient() {
        in = null;
        out = null;
        promptPattern = ">>> ";
        passwordPromptPattern = "Password: ";
        timeout = DEFAULT_TIMEOUT;
        log = null;
    }

    public boolean login(String plcUrl, String userEmail, String password) {
        log = Logger.getLogger(this.getClass());
        promptPattern = "[" + userEmail + " any]" + promptPattern;
        try {
            proc = new ProcessBuilder("/usr/bin/plcsh", "-h", plcUrl, "-r", "any", "-u", userEmail).start();
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            out = new PrintStream(proc.getOutputStream());
            this.readUntil(passwordPromptPattern);
            out.println(password);
            out.flush();
            if (!this.readUntil(promptPattern)) {
                log.error("/usr/bin/plcsh failed authenticate user: " + userEmail);
                return false;
            }
        } catch (IOException e) {
            log.error("failed to execute /usr/bin/plcsh: IO error: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean logoff() {
        out.println("exit");
        return true;
    }

    /**
     * Issues commands to PLCAPI
     * @param cmd command to run on PLCAPI
     * @return output of command
     */
    public String command(String cmd) {
        out.println(cmd);
        out.flush();
        this.readUntil(promptPattern);//hopefully can remove this in the future
        if (this.readUntil(promptPattern)) {
            /* Remove command from output */
            buffer = buffer.replaceFirst(cmd + "\n", "");
            /* Remove prompt from output */
            buffer = buffer.replaceFirst(promptPattern + "\n*", "");
        }

        return buffer;
    }

    /**
     * Reads output from plcsh until the specified pattern is reached. promptPattern
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
     * Reads output from plcsh until the specified pattern is reached. promptPattern
     * is often useful when used in conjunction with this method.
     * @param pattern1 Regular expression used to determine the first match
     * @param pattern2 Regular expression used to determine the second match
     * @param readstop Regular expression used to determine the stop condition
     * @return integer 1: first match 2: second match 0: zero match -1: IO error -2: timeout
     */
    public int readPattern(String pattern1, String pattern2, String readstop) {
        //starting the plcsh reading thread
        Reader reader = new Reader(in, log, pattern1, pattern2, readstop);
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
                log.error("/usr/bin/plcsh not repsonding!");
                flags[0] = false;
            }
        }.start();

        // execute the command and wait
        int ret = -2;
        while (flags[0] && (ret < -1)) {
            ret = reader.getExitValue();
        }
        if (flags[0] == false)
            reader.interrupt();

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

    private static class Reader extends Thread {
        private final BufferedReader in;
        private final Logger log;
        private final String pattern1;
        private final String pattern2;
        private final String readstop;
        private int exit;

        private Reader(BufferedReader in, Logger log, String ptn1, String ptn2, String stop) {
            this.in = in;
            this.log = log;
            this.pattern1 = ptn1;
            this.pattern2 = ptn2;
            this.readstop = stop;
            exit = -2;
        }

        public int getExitValue()
        {
            return exit;
        }

        public void run() {
            String line = "";
            String buffer = "";
            char c;
            Pattern regex1 = null;
            if (pattern1 != null) {
                regex1 = Pattern.compile(pattern1);
            }
            Pattern regex2 = null;
            if (pattern2 != null) {
                regex2 = Pattern.compile(pattern2);
            }
            if (readstop == null) {
                exit = -1;
                return;
            }
            Pattern regexstop = Pattern.compile(readstop);
            exit = 0;
            try {
                while ((c = (char) in.read()) != -1) {
                    buffer += c;
                    if (c == '\n') {
                        if (log != null) {
                            log.info(line);
                        }
                        line = "";
                    } else {
                        line += c;
                        if (regex1 != null && regex1.matcher(line).matches()) {
                            if (log != null) {
                                log.info(line);
                            }
                            exit = 1;
                        }
                        if (regex2 != null && regex2.matcher(line).matches()) {
                            if (log != null) {
                                log.info(line);
                            }
                            if (exit == 0) {
                                exit = 2;
                            }
                        }
                        if (regexstop.matcher(line).matches()) {
                            if (log != null) {
                                log.info(line);
                            }
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                exit = -1;
                return;
            }
        }
    }
}
