/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.geni.aggregate.services.api.AggregateGENISkeleton;
import net.geni.aggregate.services.api.AggregateWS;

/**
 *
 * @author jflidr
 */
public class AggregateState
{

    // preferences
    private static Preferences AMPrefs = null;
    private static Preferences dbPrefs = null;
    private static Preferences aggregatePrefs = null;
    private static String dbPwd;
    private static String dbUser;
    private static String aggregateDB = null;
    private static String coreTab = null;
    private static String slicerTab = null;
    private static String aggregateCaps = "";
    // global state
    private static AggregateSQLStatements sqlStatements = null;
    private static AggregateGENISkeleton skeletonAPI = null;
    private static Connection aggregateDBConnection = null;
    private static final long pollInterval = 5000;
    public static Logger logger = Logger.getLogger("aggregate");

    public static void init() {
        //create and load preferences
        AMPrefs = Preferences.systemNodeForPackage(AggregateWS.class);
        dbPrefs = AMPrefs.node("database");
        aggregatePrefs = AMPrefs.node("aggregate");
        initPrefs();
        // database prefs
        dbPwd = dbPrefs.get("password", null);
        dbUser = dbPrefs.get("user", null);
        aggregateDB = dbPrefs.get("aggregateDB", null);
        coreTab = dbPrefs.get("frontEndTab", null);
        slicerTab = dbPrefs.get("aggregateTab", null);
        //aggregate prefs
        aggregateCaps = aggregatePrefs.get("capabilities", "");

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
        dbPrefs.put("password", "flame");
        dbPrefs.put("user", "dragon");
        dbPrefs.put("aggregateDB", "aggregate");
        dbPrefs.put("frontEndTab", "front_end");
        dbPrefs.put("aggregateTab", "slicer");
        //aggregate prefs
        aggregatePrefs.put("capabilities", "DRAGON, PlanetLab");
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

    public static String getAggregateCaps() {
        return aggregateCaps;
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
}
