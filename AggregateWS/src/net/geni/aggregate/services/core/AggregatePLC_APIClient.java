/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.regex.Pattern;
import java.util.Vector;
import java.util.HashMap;

/**
 *
 * @author Xi Yang
 */
public class AggregatePLC_APIClient extends AggregateCLIClient {
    protected String plcUrl;
    protected String piEmail;
    protected String password;

    private String loginCmd = "import xmlrpclib;"
        + "api_server = xmlrpclib.ServerProxy('<_url_>',allow_none=True);"
        + "auth={};"
        + "auth['AuthMethod']='password';"
        + "auth['Username']='<_user_>';"
        + "auth['AuthString']='<_pass_>';"
        + "authorized = api_server.AuthCheck(auth);"
        + "print authorized;";

    private String addSliceCmd = "slice_data = {};"
        + "slice_data['name'] = '<_name_>';"
        + "slice_data['url'] = '<_url_>';"
        + "slice_data['description'] = '<_descr_>';"
        + "slice_data['instantiation'] = 'plc-instantiated';"
        + "slice_id = api_server.AddSlice(auth, slice_data);";
    private String fillSliceCmd =
        //"api_server.AddSliceTag(auth, '<_name_>', 'net_share', '1');"
        "ret1 = api_server.AddPersonToSlice(auth, <_user_>, '<_name_>');"
        + "nodes = <_node_list_>;"
        + "ret2 = api_server.AddSliceToNodes(auth, '<_name_>', nodes);"
        + "print ret1, ret2;"; //success pattern: "1 1"

    private String addSliceTagCmd = 
        "ret1 = api_server.AddSliceTag(auth, '<_name_>', 'net_share', '1');";
        + "print ret1;"; //success pattern: "1"

    private String cleanupSliceCmd =
        "ret1 = api_server.DeleteSliceFromNodes (auth, '<_name_>', <_node_list_>);"
        //+ "ret2 = api_server.DeletePersonFromSlice(auth, <_user_>, '<_name_>');"
        + "ret2 = 1;"
        + "print ret1, ret2;"; //success pattern: "1 1"

    private String updateSliceCmd = "slice_data = {};"
        + "slice_data['name'] = '<_name_>';"
        + "slice_data['url'] = '<_url_>';"
        + "slice_data['description'] = '<_descr_>';"
        + "slice_data['expires'] = <_expires_>;"
        + "slice_data['persons'] = <_user_list_>;"
        + "slice_data['nodes'] = <_node_list_>;"
        + "print api_server.UpdateSlice(auth, '<_name_>', slice_data);";

    private String startSliceCmd = "slices = api_server.GetSlices(auth, {'name': '<_name_>'}, ['slice_id']);\n"
        + "if not slices:\n"
        + "slice_id = slices[0]['slice_id'];"
        + "tags = api_server.GetSliceTags(auth, {'slice_id': slice_id, 'name': 'enabled'}, ['slice_tag_id']);"
        + "tag_id = tags[0]['slice_tag_id'];"
        + "print api_server.UpdateSliceTag(auth, tag_id, \"<_tag_id_>\");\n\n";

    private AggregatePLC_APIClient() {
        super(">>> ");
    }

    public AggregatePLC_APIClient(String url, String pi, String pass) {
        super(">>> ");
        plcUrl = url;
        piEmail = pi;
        password = pass;
    }

    /**
     * get an PLCCLient instance
     */
    static public AggregatePLC_APIClient getPLCClient(String url, String pi, String pass) {
        return (new AggregatePLC_APIClient(url, pi, pass));
    }

    static public AggregatePLC_APIClient getPLCClient() {
        return getPLCClient(AggregateState.getPlcURL(), AggregateState.getPlcPI(), AggregateState.getPlcPassword());
    }

    public void finalize() {
        logoff();
    }

    public boolean login() {
        if (!super.login("expect", "-c", "spawn python", "-c", "interact"))
            return false;

        this.readUntil(promptPattern);
        loginCmd = loginCmd.replaceFirst("<_url_>", plcUrl);
        loginCmd = loginCmd.replaceFirst("<_user_>", piEmail);
        loginCmd = loginCmd.replaceFirst("<_pass_>", password);
        this.sendCommand(loginCmd);
        int ret = this.readPattern("^1", ".*Failed to authenticate call", promptPattern);
        log.debug("login code: " + Integer.toString(ret));
        if (ret != 1) {
            log.error("plcapi server failed authenticate the user (PI): " + piEmail);
            proc = null;
            in = null;
            out = null;
            return false;
        }

        return true;
    }

    public boolean logoff() {
        return super.logoff("import sys; sys.exit(0)");
    }

    /**
     *
     * @param hm the hashmap to return the buffer content
     */
    private HashMap[] extractHashMapFromBuffer() {
        buffer = buffer.replaceFirst(promptPattern + "\n*", "");
        String[] chunks = buffer.split("\\s*\\[\\{|\\},\\s*\\{|\\}\\]\\s*"); //split [{A}, {B}]
        HashMap[] hms = new HashMap[chunks.length-1];
        for (int i = 1; i < chunks.length; i++) { //ignore first chunk
            String[] blocks = chunks[i].split(", '");
            hms[i-1] = new HashMap();
            for (String block: blocks) {
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
    public int createSlice(String sliceName, String url, String descr, String user, String nodes, boolean isAddPlcSlice) {
        if (!alive()) {
            if (!login())
                return -1;
        }

        String createSliceCmd = "";
        if (isAddPlcSlice) {
            createSliceCmd = addSliceCmd;
        }
        createSliceCmd += fillSliceCmd;
        createSliceCmd = createSliceCmd.replaceAll("<_name_>", sliceName);
        createSliceCmd = createSliceCmd.replaceFirst("<_url_>", url);
        createSliceCmd = createSliceCmd.replaceFirst("<_descr_>", descr);
        createSliceCmd = createSliceCmd.replaceFirst("<_user_>", user);
        createSliceCmd = createSliceCmd.replaceAll("<_node_list_>", "[" + nodes + "]");
        this.sendCommand(createSliceCmd);
        log.debug("createSlice dump #1: " + createSliceCmd);
        int ret = this.readPattern("^1\\s1", ".*Fault|.*Error", promptPattern);
        log.debug("createSlice dump #2: " + this.buffer);
        if (ret != 1) {
            log.error("plcapi server failed to create Slice '" + sliceName +"' on Nodes: " + nodes);
            logoff();
        }

        // add net_shared=1 tag
        createSliceCmd = addSliceTagCmd;
        createSliceCmd = createSliceCmd.replaceAll("<_name_>", sliceName);
        this.sendCommand(createSliceCmd);
        this.readPattern("^1\\s1", ".*Fault|.*Error", promptPattern);

        return ret;
    }

    public int deleteSlice(String sliceName) {
        if (!alive()) {
            if (!login())
                return -1;
        }
        String cmd = "print api_server.DeleteSlice(auth, '"+sliceName+"');";
        this.sendCommand(cmd);
        int ret = this.readPattern("^1", ".*Fault|.*Error", promptPattern);
        if (ret != 1) {
            log.error("plcapi server failed to delete the slice '" + sliceName +"'");
            logoff();
        }
        return ret;
    }

    public int cleanupSlice(String sliceName, String user, String nodes) {
        if (!alive()) {
            if (!login())
                return -1;
        }
        cleanupSliceCmd = cleanupSliceCmd.replaceAll("<_name_>", sliceName);
        //cleanupSliceCmd = cleanupSliceCmd.replaceAll("<_user_>", user);
        cleanupSliceCmd = cleanupSliceCmd.replaceAll("<_node_list_>", "[" + nodes + "]");
        this.sendCommand(cleanupSliceCmd);
        int ret = this.readPattern("^1\\s1", ".*Fault|.*Error", promptPattern);
        log.debug("cleanupSlice dump: " + this.buffer);
        if (ret != 1) {
            log.error("plcapi server failed to cleanup Slice '" + sliceName +"' on Nodes: " + nodes + "for User: " + user);
            logoff();
        }
        return ret;
    }

    public boolean hasSlice(String sliceName) {
        if (!alive()) {
            if (!login())
                return false;
        }

        String cmd = "print api_server.GetSlices(auth, '"+sliceName+"');";
        this.sendCommand(cmd);
        int ret = this.readPattern("^\\[\\{", "^\\[\\]", promptPattern);
        if (ret == 1) {
            return true;
        }

        return false;
    }

    public int updateSlice(String sliceName, String url, String descr, int expires, String users, String nodes) {
        if (!alive()) {
            if (!login())
                return -1;
        }

        if (!hasSlice(sliceName)) {
            log.error("plcapi server does not recognize '" + sliceName +"'");
            return 0;
        }

        updateSliceCmd = updateSliceCmd.replaceAll("<_name_>", sliceName);
        updateSliceCmd = updateSliceCmd.replaceFirst("<_url_>", url);
        updateSliceCmd = updateSliceCmd.replaceFirst("<_descr_>", descr);
        updateSliceCmd = updateSliceCmd.replaceFirst("<_expires_>", Integer.toString(expires));
        updateSliceCmd = updateSliceCmd.replaceAll("<_user_list_>", "["+users+"]");
        updateSliceCmd = updateSliceCmd.replaceAll("<_node_list_>", "["+nodes+"]");
        this.sendCommand(updateSliceCmd);
        int ret = this.readPattern("^1", ".*Fault|.*Error", promptPattern);
        if (ret == 1) {
            ;
        } else {
            log.error("plcapi server failed to update Slice '" + sliceName +"' on Nodes: " + nodes);
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
    public int querySlice(String[] sliceNames, Vector<HashMap> hmSlices) {
        if (!alive()) {
            if (!login())
                return -1;
        }
        hmSlices.clear();
        String sliceArray = AggregateUtils.makePyArrayString(sliceNames);
        String cmd = "print api_server.GetSlices(auth,"+sliceArray+");";
        this.sendCommand(cmd);
        int ret = this.readPattern("^\\[\\{", "^\\[\\]", promptPattern);
        if (ret == 1) {
             HashMap[] hmResults = extractHashMapFromBuffer();
             if (hmResults.length > 0) {
                for (HashMap hm: hmResults) {
                    hmSlices.add(hm);
                }
             }
             else {
                 log.error("failed to parse slice data:" + this.buffer);
                 logoff();
                 return 0;
             }
        } else if (ret ==2) {
            log.error("plcapi server recognizes none of the slices: " + sliceArray);
        } else {
            log.error("plcapi server failed to query  the slices: " + sliceArray);
            logoff();
        }
        return ret;
    }

    public int startStopSlice(String sliceName, boolean startOrStop) {
        if (!alive()) {
            if (!login())
                return -1;
        }

        startSliceCmd = startSliceCmd.replaceAll("<_name_>", sliceName);
        if (startOrStop) {//start
            startSliceCmd = startSliceCmd.replaceAll("<_tag_id_>", "1");
        }
        else {//stop
            startSliceCmd = startSliceCmd.replaceAll("<_tag_id_>", "0");
        }
        this.sendCommand(startSliceCmd);
        log.debug("startSliceCmd dump #1: " + startSliceCmd);
        int ret = this.readPattern("^1", ".*Fault|.*Error", promptPattern);
        log.debug("startSliceCmd dump #2: " + this.buffer);
        if (ret == 1) {
            ;
        } else {
            log.error("plcapi server failed to " + (startOrStop ? "start slice: ":"stop slice: ") + sliceName);
            logoff();
        }
        return ret;
    }
}
