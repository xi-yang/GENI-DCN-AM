/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import org.apache.log4j.*;
import java.util.Vector;
import java.util.Properties;
import java.sql.Connection;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import net.geni.aggregate.services.api.AggregateGENISkeleton;
import net.geni.aggregate.services.api.AggregateWS;

/**
 *
 * @author jflidr
 */
public class AggregateState
{
    //Logger
    public static Logger log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");

    //Properties
    private static Properties aggregateProps = new Properties();
    private static String dbPwd;
    private static String dbUser;
    private static String resourcesTab = "resources";
    private static String capsTab = "capabilities";
    private static String nodesTab = "nodes";
    private static String slicesTab = "slices";
    private static String usersTab = "users";
    private static String p2pvlansTab = "p2pvlans";
    private static String networksTab = "networks";
    private static String idcURL = "";
    private static String idcRepo = "";
    private static String plcURL = "";
    private static String plcPI = "";
    private static String plcPassword = "";
    private static String plcPrefix = "";
    private static String plcSshHost = "";
    private static String plcSshLogin = "";
    private static String plcSshPort = "";
    private static String plcSshKeyfile = "";
    private static String plcSshKeypass = "";
    private static String plcSshExecPrefix = "";

    // Resrouces
    private static AggregateCapabilities aggregateCaps = null;
    private static AggregateNodes aggregateNodes = null;
    private static AggregateSlices aggregateSlices = null;
    private static AggregateP2PVlans aggregateP2PVlans = null;
    private static AggregateUsers aggregateUsers = null;
    
    // Global states
    private static AggregateGENISkeleton skeletonAPI = null;
    private static AggregateRspecManager aggregateRspecManager = null;
    private static final long pollInterval = 5000; //miliseconds

    public static void init() {
        //init properties
        String aggregateHome = System.getenv("AGGREGATE_HOME");
        String propFileName = "aggregate.properties";
        if (aggregateHome != null && !aggregateHome.equals(""))
            propFileName = aggregateHome + "/AggregateAttic/conf/" + propFileName;
        else
            propFileName = "/usr/local/geni-aggregate/AggregateAttic/conf/" + propFileName;

        try {
            FileInputStream in = new FileInputStream(propFileName);
            aggregateProps.load(in);
            in.close();
        } catch (IOException e) {
            //logging for exception!
        }

        dbUser = aggregateProps.getProperty("aggregate.mysql.user", "geniuser");
        log.info("aggregate.mysql.user set to " + dbUser);
        dbPwd = aggregateProps.getProperty("aggregate.mysql.pass", "genipass");
        idcURL = aggregateProps.getProperty("aggregate.idc.url", "https://idc.dragon.maxgigapop.net:8443/axis2/services/OSCARS");
        log.info("aggregate.idc.url set to " + idcURL);
        idcRepo = aggregateProps.getProperty("aggregate.idc.repo", "/usr/local/geni-aggregate/AggregateAttic/conf/repo");
        log.info("aggregate.idc.repo set to " + idcRepo);
        plcURL = aggregateProps.getProperty("aggregate.plc.url", "https://max-myplc.dragon.maxgigapop.net/PLCAPI/");
        log.info("aggregate.plc.url set to " + plcURL);
        plcPI = aggregateProps.getProperty("aggregate.plc.pi", "xyang@east.isi.edu");
        plcPassword = aggregateProps.getProperty("aggregate.plc.pass", "password");
        plcPrefix = aggregateProps.getProperty("aggregate.plc.base", "maxpl");
        plcSshHost = aggregateProps.getProperty("aggregate.plc.ssh.host", "max-myplc.dragon.maxgigapop.net");
        plcSshLogin = aggregateProps.getProperty("aggregate.plc.ssh.login", "root");
        plcSshPort = aggregateProps.getProperty("aggregate.plc.ssh.port", "22");
        plcSshKeyfile = aggregateProps.getProperty("aggregate.plc.ssh.keyfile", idcRepo + "/plc-ssh.pkey");
        plcSshKeypass = aggregateProps.getProperty("aggregate.plc.ssh.keypass", idcRepo + "");
        plcSshExecPrefix = aggregateProps.getProperty("aggregate.plc.ssh.execprefix", "");
        plcSshExecPrefix = plcSshExecPrefix.replaceAll("[\\'\\\"]", "");

        //init hibernate
        HibernateUtil.initSessionFactory();

        //init instances
        aggregateCaps = new AggregateCapabilities();
        aggregateNodes = new AggregateNodes();
        aggregateSlices = new AggregateSlices();
        aggregateP2PVlans = new AggregateP2PVlans();
        aggregateUsers = new AggregateUsers();
    }

    //setters/getters 
    public static Properties getProperties() {
        return aggregateProps;
    }

    public static void setSkeletonAPI(AggregateGENISkeleton aggregateSkeleton) {
        skeletonAPI = aggregateSkeleton;
    }

    public static AggregateGENISkeleton getSkeletonAPI() {
        return skeletonAPI;
    }

    public static AggregateRspecManager getRspecManager() {
        return aggregateRspecManager;
    }

    public static void setRspecManager(AggregateRspecManager rspecMan) {
        AggregateState.aggregateRspecManager = rspecMan;
    }

    public static long getPollInterval() {
        return pollInterval;
    }

    public static AggregateCapabilities getAggregateCaps() {
        return aggregateCaps;
    }

    public static AggregateNodes getAggregateNodes() {
        return aggregateNodes;
    }

    public static AggregateSlices getAggregateSlices() {
        return aggregateSlices;
    }

    public static AggregateP2PVlans getAggregateP2PVlans() {
        return aggregateP2PVlans;
    }

    public static AggregateUsers getAggregateUsers() {
        return aggregateUsers;
    }

    public static String getResourcesTab() {
        return resourcesTab;
    }

    public static String getDbPwd() {
        return dbPwd;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getCapsTab() {
        return capsTab;
    }

    public static String getNodesTab() {
        return nodesTab;
    }

    public static String getSlicesTab() {
        return slicesTab;
    }

    public static String getUsersTab() {
        return usersTab;
    }

    public static String getP2PVlansTab() {
        return p2pvlansTab;
    }

    public static String getNetworksTab() {
        return networksTab;
    }

    public static String getIdcURL() {
        return idcURL;
    }

    public static String getIdcRepo() {
        return idcRepo;
    }

    public static String getPlcURL() {
        return plcURL;
    }

    public static String getPlcPI() {
        return plcPI;
    }

    public static String getPlcPassword() {
        return plcPassword;
    }

    public static String getPlcPrefix() {
        return plcPrefix;
    }

    public static String getPlcSshHost() {
        return plcSshHost;
    }

    public static String getPlcSshLogin() {
        return plcSshLogin;
    }

    public static String getPlcSshPort() {
        return plcSshPort;
    }

    public static String getPlcSshKeyfile() {
        return plcSshKeyfile;
    }

    public static String getPlcSshKeypass() {
        return plcSshKeypass;
    }

    public static String getPlcSshExecPrefix() {
        return plcSshExecPrefix;
    }
}
