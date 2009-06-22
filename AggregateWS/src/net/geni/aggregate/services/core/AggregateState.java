/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.sql.Connection;
import java.util.Vector;
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
public class AggregateState {
 public Preferences nimbisWSPrefs = null;
    public static String awsSecretKey = null;
    public static String awsAccessId = null;
    public static String rsysServer = null;
    public static String sqlAggregateDB = null;
    public static String sqlWolframTab = null;
    public static String sqlRSysTab = null;
    public static String sqlRSysUserTab = null;
    public static String sqlEC2Tab = null;
    private static AggregateSQLStatements sqlStatements = null;
    private static AggregateGENISkeleton skeletonAPI = null;
    private static Connection nimbisDBConnection = null;
    private static String[] imgIDs = null;
    private static String keyPair = null;
    private static String secGroup = null;
    private static String EC2MathLaunchScript = null;
    private static String EC2MathResultScript = null;
    private static String EC2MathErrorScript = null;
    private static String RsysMathLaunchScript = null;
    private static String RsysMathResultScript = null;
    private static String RsysMathErrorScript = null;
    private static Vector<String> groupSet;
    private static final long pollInterval = 5000;
    public static Logger logger = Logger.getLogger("nimbis");

    public AggregateState() {
        //create and load preferences
        nimbisWSPrefs = Preferences.systemNodeForPackage(AggregateWS.class);
        awsSecretKey = nimbisWSPrefs.get("awsSecretKey", null);
        awsAccessId = nimbisWSPrefs.get("awsAccessId", null);
        rsysServer = nimbisWSPrefs.get("rsysServer", null);
        sqlAggregateDB = nimbisWSPrefs.get("sqlAggregateDB", null);
        sqlWolframTab = nimbisWSPrefs.get("sqlWolframTab", null);
        sqlRSysTab = nimbisWSPrefs.get("sqlRSysTab", null);
        sqlRSysUserTab = nimbisWSPrefs.get("sqlRSysUserTab", null);
        sqlEC2Tab = nimbisWSPrefs.get("sqlEC2Tab", null);
        String imgIDs_arr = nimbisWSPrefs.get("imgIDs", null);
        if(imgIDs_arr != null) {
            imgIDs = imgIDs_arr.split("\\s*,\\s*");
        }
        keyPair = nimbisWSPrefs.get("keyPair", null);
        secGroup = nimbisWSPrefs.get("secGroup", null);
        EC2MathLaunchScript = nimbisWSPrefs.get("EC2MathLaunchScript", null);
        EC2MathResultScript = nimbisWSPrefs.get("EC2MathResultScript", null);
        EC2MathErrorScript = nimbisWSPrefs.get("EC2MathErrorScript", null);
        RsysMathLaunchScript = nimbisWSPrefs.get("RsysMathLaunchScript", null);
        RsysMathResultScript = nimbisWSPrefs.get("RsysMathResultScript", null);
        RsysMathErrorScript = nimbisWSPrefs.get("RsysMathErrorScript", null);
        if((awsSecretKey == null) ||
                (awsSecretKey == null) ||
                (awsAccessId == null) ||
                (rsysServer == null) ||
                (sqlAggregateDB == null) ||
                (sqlWolframTab == null) ||
                (sqlRSysTab == null) ||
                (sqlRSysUserTab == null) ||
                (sqlEC2Tab == null) ||
                (imgIDs_arr == null) ||
                (keyPair == null) ||
                (secGroup == null) ||
                (EC2MathLaunchScript == null) ||
                (EC2MathResultScript == null) ||
                (EC2MathErrorScript == null) ||
                (RsysMathLaunchScript == null) ||
                (RsysMathResultScript == null) ||
                (RsysMathErrorScript == null)) {
            throw new IllegalArgumentException("corrupted preferences");
        }
        try {
            nimbisWSPrefs.sync();
        } catch(BackingStoreException ex) {
            Logger.getLogger(AggregateState.class.getName()).log(Level.SEVERE, null, ex);
        }
        groupSet = new Vector<String>();
        groupSet.add(secGroup);
    }
    // sql

    public static void setAggregateDBConnection(Connection nimbisDB) {
        AggregateState.nimbisDBConnection = nimbisDB;
    }

    public static Connection getAggregateDBConnection() {
        return nimbisDBConnection;
    }

    public static String getSqlWolframTab() {
        return sqlWolframTab;
    }


    static void setSkeletonAPI(AggregateGENISkeleton aggregateSkeleton) {
        skeletonAPI = aggregateSkeleton;
    }

    public static AggregateGENISkeleton getSkeletonAPI() {
        return skeletonAPI;
    }

    public static String[] getImgIDs() {
        return imgIDs;
    }

    public static String getKeyPair() {
        return keyPair;
    }

    public static Vector<String> getGroupSet() {
        return groupSet;
    }

    public static void setGroupSet(Vector<String> groupSet) {
        AggregateState.groupSet = groupSet;
    }

    public static String getSqlEC2Tab() {
        return sqlEC2Tab;
    }

    public static String getEC2MathLaunchScript() {
        return EC2MathLaunchScript;
    }

    public static String getEC2MathResultScript() {
        return EC2MathResultScript;
    }

    public static String getEC2MathErrorScript() {
        return EC2MathErrorScript;
    }

    public static String getRsysMathErrorScript() {
        return RsysMathErrorScript;
    }

    public static String getRsysMathLaunchScript() {
        return RsysMathLaunchScript;
    }

    public static String getRsysMathResultScript() {
        return RsysMathResultScript;
    }

    public static long getPollInterval() {
        return pollInterval;
    }

    public static AggregateSQLStatements getSqlStatements() {
        return sqlStatements;
    }

    public static void setSqlStatements(AggregateSQLStatements sqlStatements) {
        AggregateState.sqlStatements = sqlStatements;
    }
}
