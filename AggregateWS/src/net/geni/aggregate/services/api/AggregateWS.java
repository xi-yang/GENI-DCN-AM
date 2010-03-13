/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.api;

import org.apache.log4j.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import net.geni.aggregate.services.core.AggregateCapability;
import net.geni.aggregate.services.core.AggregateException;
import net.geni.aggregate.services.core.AggregateNode;
import net.geni.aggregate.services.core.AggregateSlice;
import net.geni.aggregate.services.core.AggregateP2PVlan;
import net.geni.aggregate.services.core.AggregateUser;
import net.geni.aggregate.services.core.AggregateSlicerCore;
import net.geni.aggregate.services.core.AggregateSQLStatements;
import net.geni.aggregate.services.core.AggregateState;
import net.geni.aggregate.services.core.AggregateUtils;
import org.apache.axis2.context.ServiceContext;

/**
 *
 * @author jflidr, xyang
 */
public class AggregateWS implements AggregateGENISkeletonInterface
{
    public static Logger log = Logger.getLogger("net.geni.aggregate");

    private AggregateSlicerCore aggregateSlicerCore;
    private Thread aggregateServerThread;
    
    public void init(ServiceContext serviceContext) {

        System.err.println("AggregateWS init...");
        // initialize preferences
        AggregateState.init();
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch(Exception ex) {
            ex.printStackTrace();
            return;
        }
        Connection aggregateDB;
        try {
            String sqlURL = "jdbc:mysql://" + "127.0.0.1" +
                    "/" + AggregateState.getAggregateDB() +
                    "?user=" + AggregateState.getDbUser() +
                    "&password=" + AggregateState.getDbPwd() +
                    "&connectTimeout=10000&socketTimeout=5000";
            aggregateDB = DriverManager.getConnection(sqlURL);
            AggregateState.setAggregateDBConnection(aggregateDB);
        } catch(SQLException ex) {
            System.err.println("mysql connection failed...");
            ex.printStackTrace();
            return;
        }
        try {
            //init the capabilities table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getCapsTab() + " ( " +
                    "name VARCHAR(255) NOT NULL, " +
                    "urn VARCHAR(255) NOT NULL, " +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "description TEXT NOT NULL, " +
                    "controllerURL VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (id, urn)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the nodes table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getNodesTab() + " ( " +
                    "urn VARCHAR(255) NOT NULL, " +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "description TEXT NOT NULL, " +
                    "capabilities TEXT, " +
                    "PRIMARY KEY (id, urn)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the slices table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getSlicesTab() + " ( " +
                    "sliceName VARCHAR(255), " + // slice Name
                    "id int(11) NOT NULL auto_increment, " + // slice ID
                    "url TEXT NOT NULL, " + // slice URL
                    "description TEXT NOT NULL, " + // slice description
                    "members TEXT NOT NULL, " + // slice node members
                    "creatorId INT(11) NOT NULL, " + // user who created the slice
                    "createdTime BIGINT(20), " + // launch timestamp
                    "expiredTime BIGINT(20), " + // expire timestamp
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the p2pvlans table
          AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getP2PVlansTab() + " ( " +
                    "id int(11) NOT NULL auto_increment, " +
                    "vlanTag int(11) NOT NULL, " +
                    "sliceId int(11) NOT NULL, " +
                    "source varchar(255) NOT NULL default '', " +
                    "destination varchar(255) NOT NULL default '', " +
                    "bandwidth float NOT NULL, " +
                    "globalReservationId varchar(255) NOT NULL default '', " +
                    "status varchar(20) NOT NULL default '', " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the networks table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getNetworksTab() + " ( " +
                    "id int(11) NOT NULL auto_increment, " +
                    "sliceId int(11) NOT NULL, " +
                    "vlanPool text NOT NULL, " +
                    "status varchar(20) NOT NULL default '', " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }

          try {
            //init the users table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getUsersTab() + " ( " +
                    "id int(11) NOT NULL auto_increment, " +
                    "name varchar(40) NOT NULL default '', " +
                    "firstName varchar(40) NOT NULL default '', " +
                    "lastName varchar(40) NOT NULL default '', " +
                    "email varchar(40) NOT NULL default '', " +
                    "description text NOT NULL, " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the database
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getCoreTab() + " ( " +
                    "requestID VARCHAR(255) NOT NULL, " + // job ID 1
                    "status VARCHAR(255) NOT NULL DEFAULT 'no such job', " + // job status
                    "statusMsg VARCHAR(255) NOT NULL DEFAULT '', " + // status message if any
                    "PRIMARY KEY (requestID)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        // init all the mysql statements
        AggregateState.setSqlStatements(new AggregateSQLStatements());
        AggregateSQLStatements sql = AggregateState.getSqlStatements();
        boolean dummy = true;
        // load aggregate configuration
        // caps
        try {
            sql.aggCaps_Stmt.select();
            while(dummy) {
                AggregateState.getAggregateCaps().add(new AggregateCapability(
                        sql.aggCaps_Stmt.getNextString("name"),
                        sql.aggCaps_Stmt.getString("urn"),
                        sql.aggCaps_Stmt.getInt("id"),
                        sql.aggCaps_Stmt.getString("description"),
                        sql.aggCaps_Stmt.getString("controllerURL")));

            }
        } catch(AggregateException ex) {
            if(ex.getType() == AggregateException.FATAL) {
                log.error("FATAL error: terminating ..." + ex.getMessage());
                return;
            }
        }
        // nodes
        try {
            sql.aggNodes_Stmt.select();
            while(dummy) {
                AggregateState.getAggregateNodes().add(new AggregateNode(
                        sql.aggNodes_Stmt.getNextString("urn"),
                        sql.aggNodes_Stmt.getInt("id"),
                        sql.aggNodes_Stmt.getString("description"),
                        sql.aggNodes_Stmt.getString("capabilities")));
            }
        } catch(AggregateException ex) {
            if(ex.getType() == AggregateException.FATAL) {
                log.error("FATAL error: terminating ..." + ex.getMessage());
                return;
            }
        }
        // slices
        try {
            sql.aggSlices_Stmt.select();
            while(dummy) {
                AggregateState.getAggregateSlices().add(new AggregateSlice(
                        sql.aggSlices_Stmt.getNextString("sliceName"),
                        sql.aggSlices_Stmt.getInt("id"),
                        sql.aggSlices_Stmt.getString("url"),
                        sql.aggSlices_Stmt.getString("description"),
                        sql.aggSlices_Stmt.getString("members"),
                        sql.aggSlices_Stmt.getInt("creatorId"),
                        sql.aggSlices_Stmt.getLong("createdTime"),
                        sql.aggSlices_Stmt.getLong("expiredTime")));
            }
        } catch(AggregateException ex) {
            if(ex.getType() == AggregateException.FATAL) {
                log.error("FATAL error: terminating ..." + ex.getMessage());
                return;
            }
        }
        // p2pvlans AggregateP2PVlan(int sid, int v, String s, String d, float b, String g, String ss)
        try {
            sql.aggP2PVlans_Stmt.select();
            while(dummy) {
                AggregateState.getAggregateP2PVlans().add(new AggregateP2PVlan(
                        sql.aggP2PVlans_Stmt.getNextInt("sliceId"),
                        sql.aggP2PVlans_Stmt.getInt("vlanTag"),
                        sql.aggP2PVlans_Stmt.getString("source"),
                        sql.aggP2PVlans_Stmt.getString("destination"),
                        sql.aggP2PVlans_Stmt.getFloat("bandwidth"),
                        sql.aggP2PVlans_Stmt.getString("globalReservationId"),
                        sql.aggP2PVlans_Stmt.getString("status")));
            }
        } catch(AggregateException ex) {
            if(ex.getType() == AggregateException.FATAL) {
                log.error("FATAL error: terminating ..." + ex.getMessage());
                return;
            }
        }
        // users
        try {
            sql.aggUsers_Stmt.select();
            while(dummy) {
                AggregateState.getAggregateUsers().add(new AggregateUser(
                        sql.aggUsers_Stmt.getNextInt("id"),
                        sql.aggUsers_Stmt.getString("name"),
                        sql.aggUsers_Stmt.getString("firstName"),
                        sql.aggUsers_Stmt.getString("lastName"),
                        sql.aggUsers_Stmt.getString("email"),
                        sql.aggUsers_Stmt.getString("description")));
            }
        } catch(AggregateException ex) {
            if(ex.getType() == AggregateException.FATAL) {
                log.error("FATAL error: terminating ..." + ex.getMessage());
                return;
            }
        }
        aggregateSlicerCore = new AggregateSlicerCore();
        aggregateServerThread = new Thread(aggregateSlicerCore);
        aggregateServerThread.start();
        log.info("AggregateWS init() finished!");
    }

    /**
     * terminates running cores
     * @param serviceContext
     */
    public void destroy(ServiceContext serviceContext) {
        aggregateSlicerCore.stopCore();
    }

    public ListCapabilitiesResponse ListCapabilities(net.geni.aggregate.services.api.ListCapabilities listCapabilities) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().ListCapabilities(listCapabilities);
    }

    public ListNodesResponse ListNodes(net.geni.aggregate.services.api.ListNodes listNodes) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().ListNodes(listNodes);
    }

    public ListSlicesResponse ListSlices(net.geni.aggregate.services.api.ListSlices listSlices) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().ListSlices(listSlices);
    }

    public CreateSliceResponse CreateSlice(net.geni.aggregate.services.api.CreateSlice createSlice) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DeleteSliceResponse DeleteSlice(net.geni.aggregate.services.api.DeleteSlice deleteSlice) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UpdateSliceResponse UpdateSlice(net.geni.aggregate.services.api.UpdateSlice updateSlice) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public StartSliceResponse StartSlice(net.geni.aggregate.services.api.StartSlice startSlice) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public StopSliceResponse StopSlice(net.geni.aggregate.services.api.StopSlice stopSlice) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ResetSliceResponse ResetSlice(net.geni.aggregate.services.api.ResetSlice resetSlice) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public QuerySliceResponse QuerySlice(net.geni.aggregate.services.api.QuerySlice querySlice) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CreateSliceVlanResponse CreateSliceVlan(net.geni.aggregate.services.api.CreateSliceVlan createSliceVlan) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().CreateSliceVlan(createSliceVlan);
    }

    public DeleteSliceVlanResponse DeleteSliceVlan(net.geni.aggregate.services.api.DeleteSliceVlan deleteSliceVlan) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().DeleteSliceVlan(deleteSliceVlan);
    }

    public QuerySliceVlanResponse QuerySliceVlan(net.geni.aggregate.services.api.QuerySliceVlan querySliceVlan) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().QuerySliceVlan(querySliceVlan);
    }

    public CreateSliceNetworkResponse CreateSliceNetwork(net.geni.aggregate.services.api.CreateSliceNetwork createSliceNetwork) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public DeleteSliceNetworkResponse DeleteSliceNetwork(net.geni.aggregate.services.api.DeleteSliceNetwork deleteSliceNetwork) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public QuerySliceNetworkResponse QuerySliceNetwork(net.geni.aggregate.services.api.QuerySliceNetwork querySliceNetwork) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public GetResourceTopologyResponse  GetResourceTopology(net.geni.aggregate.services.api.GetResourceTopology getResourceTopology) throws AggregateFaultMessage {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
