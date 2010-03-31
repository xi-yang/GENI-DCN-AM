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
    public static Logger log = Logger.getLogger("net.geni.aggregate");
    //Properties
    private static Properties aggregateProps = new Properties();
    // Preferences
    private static Preferences AMPrefs = null;
    private static Preferences dbPrefs = null;
    private static Preferences aggregatePrefs = null;
    private static String dbPwd;
    private static String dbUser;
    private static String aggregateDB = null;
    private static String coreTab = null;
    private static String slicerTab = "slicer";
    private static final String capsTab = "capabilities";
    private static final String nodesTab = "nodes";
    private static final String slicesTab = "slices";
    private static final String usersTab = "users";
    private static final String p2pvlansTab = "p2pvlans";
    private static final String networksTab = "networks";

    // Resrouces
    private static AggregateCapabilities aggregateCaps = new AggregateCapabilities();
    private static AggregateNodes aggregateNodes = new AggregateNodes();
    private static AggregateSlices aggregateSlices = new AggregateSlices();
    private static AggregateP2PVlans aggregateP2PVlans = new AggregateP2PVlans();
    private static AggregateUsers aggregateUsers = new AggregateUsers();
    private static String idcURL = "";
    private static String idcRepo = "/usr/local/aggregate/AggregateWS/conf/repo";
    
    // Global states
    private static AggregateSQLStatements sqlStatements = null;
    private static AggregateGENISkeleton skeletonAPI = null;
    private static Connection aggregateDBConnection = null;
    private static final long pollInterval = 5000;

    public static void init() {
        //init properties
        String aggregateHome = System.getenv("AGGREGATE_HOME");
        String propFileName = "aggregate.properties";
        if (aggregateHome != null && !aggregateHome.equals(""))
            propFileName = aggregateHome + "/AggregateWS/conf/" + propFileName;
        else
            propFileName = "/usr/local/aggregate/AggregateWS/conf/" + propFileName;

        try {
            FileInputStream in = new FileInputStream(propFileName);
            aggregateProps.load(in);
            in.close();
        } catch (IOException e) {
            //logging for exception!
        }
        idcURL = aggregateProps.getProperty("aggregate.idc.url", "https://idc.dragon.maxgigapop.net:8443/axis2/services/OSCARS");
        idcRepo = aggregateProps.getProperty("aggregate.idc.repo", "/usr/local/aggregate/AggregateWS/conf/repo");
        log.info("aggregate.idc.rep set to " + idcRepo);
        //create and load preferences
        AMPrefs = Preferences.systemNodeForPackage(AggregateWS.class);
        dbPrefs = AMPrefs.node("database");
        aggregatePrefs = AMPrefs.node("aggregate");

        initPrefs(); //NOTE: this is a shortcut saving me some typing
        // database prefs
        dbPwd = dbPrefs.get("password", null);
        dbUser = dbPrefs.get("user", null);
        aggregateDB = dbPrefs.get("aggregateDB", null);
        coreTab = dbPrefs.get("frontEndTab", null);
        slicerTab = dbPrefs.get("aggregateTab", null);

        if((dbPwd == null) ||
                (dbUser == null) ||
                (aggregateDB == null) ||
                (coreTab == null) ||
                (slicerTab == null)) {
            throw new IllegalArgumentException("corrupted preferences");
        }
        try {
            AMPrefs.sync();
        } catch(BackingStoreException ex) {
            log.error("BackingStoreException ..." + ex.getMessage());
        }
    }

    // initializer
    private static void initPrefs() {
        dbPrefs.put("user", aggregateProps.getProperty("aggregate.mysql.user", "geniuser"));
        dbPrefs.put("password", aggregateProps.getProperty("aggregate.mysql.pass", "genipass"));
        dbPrefs.put("aggregateDB", "aggregate");
        dbPrefs.put("frontEndTab", "front_end");
        dbPrefs.put("aggregateTab", "slicer");
        //aggregate prefs
//        dragonCapsPrefs.put("name", "");
//        dragonCapsPrefs.put("id", "");
//        dragonCapsPrefs.put("description", "");
//        dragonCapsPrefs.put("controllerURL", "");
//        PLCapsPrefs.put("name", "");
//        PLCapsPrefs.put("id", "");
//        PLCapsPrefs.put("description", "");
//        PLCapsPrefs.put("controllerURL", "");
    }
    //properties
    public static Properties getProperties() {
        return aggregateProps;
    }
    // sql
    public static void setAggregateDBConnection(Connection aggregateDB) {
        aggregateDBConnection = aggregateDB;
    }

    public static Connection getAggregateDBConnection() {
        return aggregateDBConnection;
    }

    public static String getSqFrontEndTab() {
        return coreTab;
    }

    public static void setSkeletonAPI(AggregateGENISkeleton aggregateSkeleton) {
        skeletonAPI = aggregateSkeleton;
    }

    public static AggregateGENISkeleton getSkeletonAPI() {
        return skeletonAPI;
    }

    public static long getPollInterval() {
        return pollInterval;
    }

    public static AggregateSQLStatements getSqlStatements() {
        return sqlStatements;
    }

    public static void setSqlStatements(AggregateSQLStatements s) {
        sqlStatements = s;
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

    public static String getAggregateDB() {
        return aggregateDB;
    }

    public static String getSlicerTab() {
        return slicerTab;
    }

    public static String getDbPwd() {
        return dbPwd;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getCoreTab() {
        return coreTab;
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

    public static String getSliceNameById(int id) {
        for (int i = 0; i < aggregateSlices.size(); i++) {
            if (aggregateSlices.get(i).getId() == id) {
                return aggregateSlices.get(i).getSliceName();
            }
        }
        return "";
    }

    public static int getSliceIdByName(String name) {
        for (int i = 0; i < aggregateSlices.size(); i++) {
            if (aggregateSlices.get(i).getSliceName().equalsIgnoreCase(name)) {
                return aggregateSlices.get(i).getId();
            }
        }
        return 0;
    }
}
