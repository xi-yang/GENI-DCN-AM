/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.*;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    //Properties
    private static Properties aggregateProps = new Properties();
    // preferences
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
    private static AggregateCapabilities aggregateCaps = new AggregateCapabilities();
    private static AggregateNodes aggregateNodes = new AggregateNodes();
    private static AggregateSlices aggregateSlices = new AggregateSlices();
    private static String idcURL = "https://idc.dragon.maxgigapop.net:8443/axis2/services/OSCARS";
    private static String idcRepo = "/usr/local/aggregate/idc_client/repo";
    private static Vector<AggregateP2PVlan> aggregateP2PVlans = new Vector<AggregateP2PVlan>();
    // global state
    private static AggregateSQLStatements sqlStatements = null;
    private static AggregateGENISkeleton skeletonAPI = null;
    private static Connection aggregateDBConnection = null;
    private static final long pollInterval = 5000;
    public static Logger logger = Logger.getLogger("aggregate");

    public static void init() {
        //init properties
        String aggregateHome = System.getenv("AGGREGATE_HOME");
        String propFileName = "aggregate.properties";
        if (aggregateHome != null && !aggregateHome.equals(""))
            propFileName = aggregateHome + "/repo" + propFileName;
        else
            propFileName = "/usr/local/aggregate/repo" + propFileName;

        try {
            FileInputStream in = new FileInputStream(propFileName);
            aggregateProps.load(in);
            in.close();
        } catch (IOException e) {
            //logging for exception!
        }

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
            Logger.getLogger(AggregateState.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // initializer
    private static void initPrefs() {
        dbPrefs.put("password", "genipass");
        dbPrefs.put("user", "geniuser");
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

    public static Vector<AggregateP2PVlan> getAggregateP2PVlans() {
        return aggregateP2PVlans;
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

    public static String getIdcURL() {
        return idcURL;
    }

    public static String getIdcRepo() {
        return idcRepo;
    }

}
