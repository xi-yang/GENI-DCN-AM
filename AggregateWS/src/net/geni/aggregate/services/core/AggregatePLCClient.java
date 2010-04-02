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
import expect4j.*;

/**
 *
 * @author root
 */
public class AggregatePLCClient {
    protected String plcUrl;
    protected String piEmail;
    protected String password;
    protected Process proc;
    protected BufferedReader in;
    protected PrintStream out;
    protected String errorMsg;
    protected String promptPattern;
    protected String buffer;
    private int timeout;
    private Logger log;
    private final int DEFAULT_TIMEOUT = 15000; //miliseconds == 15 seconds

    private String loginCmd = "import xmlrpclib;"
        + "api_server = xmlrpclib.ServerProxy('<_url_>',allow_none=True);"
        + "auth={};"
        + "auth['AuthMethod']='password';"
        + "auth['Username']='<_user_>';"
        + "auth['AuthString']='<_pass_>';"
        + "authorized = api_server.AuthCheck(auth);"
        + "print authorized;";

    private String createSliceCmd = "slice_data = {};"
        + "slice_data['name'] = '<_name_>';"
        + "slice_data['url'] = '<_url_>';"
        + "slice_data['description'] = '<_descr_>';"
        + "slice_data['instantiation'] = 'plc-instantiated';"
        + "slice_id = api_server.AddSlice(auth, slice_data);"
        + "ret1 = api_server.AddPersonToSlice(auth, auth['Username'], slice_id);"
        + "oldnodes =api_server.GetNodes(api.auth, slice_id);"
        + "newnodes = [<_node_list_>];"
        + "deleted_nodes = list(set(newnodes).difference(oldnodes));"
        + "added_nodes = list(set(oldnodes).difference(newnodes));"
        + "ret2 = print api_server.AddSliceToNodes('<_name_>', added_nodes);"
        + "ret3 = print api_server.DeleteSliceFromNodes('<_name_>', deleted_nodes);"
        + "print ret1, ret2, ret3;"; //success pattern: "1 1 1"

    private String updateSliceCmd = "slice_data = {};"
        + "slice_data['name'] = '<_name_>';"
        + "slice_data['url'] = '<_url_>';"
        + "slice_data['description'] = '<_descr_>';"
        + "slice_data['instantiation'] = 'plc-instantiated';"
        + "slice_id = api_server.AddSlice(auth, slice_data);"
        + "ret1 = api_server.AddPersonToSlice(auth, auth['Username'], slice_id);"
        + "oldnodes =api_server.GetNodes(api.auth, slice_id);"
        + "newnodes = [<_node_list_>];"
        + "deleted_nodes = list(set(newnodes).difference(oldnodes));"
        + "added_nodes = list(set(oldnodes).difference(newnodes));"
        + "ret2 = print api_server.AddSliceToNodes('<_name_>', added_nodes);"
        + "ret3 = print api_server.DeleteSliceFromNodes('<_name_>', deleted_nodes);"
        + "print ret1, ret2, ret3;"; //success pattern: "1 1 1"

    private AggregatePLCClient() {}

    public AggregatePLCClient(String url, String pi, String pass) {
        plcUrl = url;
        piEmail = pi;
        password = pass;
        proc = null;
        in = null;
        out = null;
        promptPattern = ">>> ";
        timeout = DEFAULT_TIMEOUT;
        log = null;
    }

    /**
     * get an PLCCLient instance
     */
    static public AggregatePLCClient getPLCClient(String url, String pi, String pass) {
        return (new AggregatePLCClient(url, pi, pass));
    }

    static public AggregatePLCClient getPLCClient() {
        return getPLCClient(AggregateState.getPlcURL(), AggregateState.getPlcPI(), AggregateState.getPlcPassword());
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

    private boolean alive() {
        return (proc != null && in != null && out != null);
    }

    public boolean login() {
        log = Logger.getLogger(this.getClass());
        try {
            proc = new ProcessBuilder("expect", "-c", "spawn python", "-c", "interact").start();
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            out = new PrintStream(proc.getOutputStream());
            if (proc==null || in==null || out==null) {
                log.info("failed to build process");
            }
            this.readUntil(promptPattern);
            log.debug("plcapi login buffer dump #1: " + this.buffer);
            loginCmd = loginCmd.replaceFirst("<_url_>", plcUrl);
            loginCmd = loginCmd.replaceFirst("<_user_>", piEmail);
            loginCmd = loginCmd.replaceFirst("<_pass_>", password);
            this.sendCommand(loginCmd);
            int ret = this.readPattern("^1", "Failed to authenticate call", promptPattern);
            log.debug("login code: " + Integer.toString(ret));
            log.debug("plcapi login buffer dump #2: " + this.buffer);
            if (ret != 1) {
                log.error("plcapi server failed authenticate the PI: " + piEmail);
                log.debug("plcapi server IO failure with buffer dump: " + this.buffer);
                proc = null; in = null; out = null;
                return false;
            } 
        } catch (IOException e) {
            log.error("failed to execute python: IO error: " + e.getMessage());
            proc = null; in = null; out = null;
            return false;
        }

        return true;
    }

    public boolean logoff() {
        if (!alive())
            return false;
        try {
            this.sendCommand("import sys; sys.exit(0)");
            in.close(); out.close();
        } catch (IOException e) {
            log.error("PLCCLient::logoff IO error: " + e.getMessage());
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
        Reader reader = new Reader(in, pattern1, pattern2, readstop);
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
        buffer = reader.getBuffer();
        if (flags[0] == false) {
            reader.interrupt();
            log.error("Thread reading python plcapi-server got stuck! It has been interrupted.");
        }
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


        private Reader(BufferedReader in, String ptn1, String ptn2, String stop) {
            this.in = in;
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
            log = Logger.getLogger("net.geni.aggregate");
            String line = "";
            char c;
            int ret = 0;
            if (readstop == null) {
                exit = -1;
                return;
            }
            try {
                while ((c = (char) in.read()) != -1) {
                    buffer += c;
                    if (c == '\n') {
                        line = "";
                    } else {
                        line += c;
                        if (pattern1 != null && line.matches(pattern1)) {
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
            } catch (IOException e) {
                exit = -1;
                return;
            }
        }
    }

    /**
     *
     * @param cmd the command executed before this operation
     * @param hm the hashmap to return the buffer content
     */
    private HashMap[] extractHashMapFromBuffer(String cmd) {
        buffer = buffer.replaceFirst(promptPattern + "\n*", "");
        String[] chunks = buffer.split("\\s*\\[\\{|\\},\\s*\\{|\\}\\]\\s*"); //split [{A}, {B}]
        HashMap[] hms = new HashMap[chunks.length];
        for (int i = 1; i < chunks.length; i++) { //ignore first chunk
            log.debug("extractHashMapFromBuffer chunk: " + chunks[i]);
            String[] blocks = chunks[i].split(", '");
            hms[i-1] = new HashMap();
            for (String block: blocks) {
                log.debug("extractHashMapFromBuffer block: " + block);
                if (block.contains("':")) {
                    String[] pair = block.split("':");
                    pair[0] = pair[0].replaceAll("'|^(\\s+)", "");
                    pair[1] = pair[1].replaceAll("'|^(\\s+)", "");
                    hms[i-1].put(pair[0], pair[1]);
                }
            }
        }
        return hms;
    }

    /*commands for PLC slice operations*/

    public int createSlice(String sliceName, String url, String descr, String user, Vector<String> nodeUrns) {
        if (!alive()) {
            if (!login())
                return -1;
        }

        createSliceCmd = createSliceCmd.replaceAll("<_name_>", sliceName);
        createSliceCmd = createSliceCmd.replaceFirst("<_url_>", url);
        createSliceCmd = createSliceCmd.replaceFirst("<_descr_>", descr);
        createSliceCmd = createSliceCmd.replaceFirst("<_user_>", user);
        String nodeArray = "";
        for (String node: nodeUrns) {
            nodeArray += node;
            nodeArray += ',';
        }
        nodeArray.trim();
        createSliceCmd = createSliceCmd.replaceAll("<_node_list_>", nodeArray);
        this.sendCommand(createSliceCmd);
        int ret = this.readPattern("^1\\s1\\s1", "Fault", promptPattern);
        if (ret != 1) {
            log.error("plcapi server failed to create Slice '" + sliceName +"' on Nodes: " + nodeArray);
            logoff();
        }
        return ret;
    }

    public int deleteSlice(String sliceName) {
        if (!alive()) {
            if (!login())
                return -1;
        }
        this.sendCommand("print api_server.DeleteSlice(auth,'"+sliceName+"');");
        int ret = this.readPattern("^1", "Fault", promptPattern);
        if (ret != 1) {
            log.error("plcapi server failed to delete Slice '" + sliceName +"'");
            logoff();
        }
        return ret;
    }

    public int updateSlice(String sliceName, String url, String descr, int expire, Vector<String> users, Vector<String> nodes) {
        if (!alive()) {
            if (!login())
                return -1;
        }

        updateSliceCmd = updateSliceCmd.replaceAll("<_name_>", sliceName);
        updateSliceCmd = updateSliceCmd.replaceFirst("<_url_>", url);
        updateSliceCmd = updateSliceCmd.replaceFirst("<_descr_>", descr);
        updateSliceCmd = updateSliceCmd.replaceFirst("<_expire_>", Integer.toString(expire));
        String userArray = "";
        for (String user: users) {
            userArray += user;
            userArray += ',';
        }
        userArray.trim();
        updateSliceCmd = updateSliceCmd.replaceAll("<_user_list_>", userArray);
        String nodeArray = "";
        for (String node: nodes) {
            nodeArray += node;
            nodeArray += ',';
        }
        nodeArray.trim();
        updateSliceCmd = updateSliceCmd.replaceAll("<_node_list_>", nodeArray);
        this.sendCommand(loginCmd);
        int ret = this.readPattern("^1\\s1\\s1", "Fault", promptPattern);
        if (ret != 1) {
            log.error("plcapi server failed to update Slice '" + sliceName +"' on Nodes: " + nodeArray);
            logoff();
        }
        return ret;
    }

    /**
     *
     * @param sliceName
     * @param sliceData
     * @return int code: 1 success; 2 unknow slice; 0 failed to exact data
     */
    public int querySlice(String sliceName, Vector<HashMap> hmSlices) {
        if (!alive()) {
            if (!login())
                return -1;
        }
        String cmd = "print api_server.GetSlices(auth,'"+sliceName+"');";
        this.sendCommand(cmd);
        int ret = this.readPattern("^\\[\\{", "^\\[\\]", promptPattern);
        hmSlices.clear();
        if (ret == 1) {
             HashMap[] hmResults = extractHashMapFromBuffer(cmd);
             if (hmResults.length > 0) {
                for (HashMap hm: hmResults)
                    hmSlices.add(hm);
             }
             else {
                 log.error("failed to parse slice data:" + this.buffer);
                 logoff();
                 return 0;
             }
        } else if (ret ==2) {
            log.error("plcapi server cannot recognize Slice '" + sliceName +"'");
        } else {
            log.error("plcapi server failed to query Slice '" + sliceName +"'");
            logoff();
        }
        return ret;
    }
}
