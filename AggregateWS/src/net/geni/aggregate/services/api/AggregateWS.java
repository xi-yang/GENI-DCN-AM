/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import net.geni.aggregate.services.core.AggregateException;
import net.geni.aggregate.services.core.AggregateSlicerCore;
import net.geni.aggregate.services.core.AggregateSQLStatements;
import net.geni.aggregate.services.core.AggregateState;
import net.geni.aggregate.services.core.AggregateUtils;
import org.apache.axis2.context.ServiceContext;

/**
 *
 * @author jflidr
 */
public class AggregateWS implements AggregateGENISkeletonInterface
{

    private AggregateSlicerCore aggregateSlicerCore;
    private Thread aggregateServerThread;

    public void init(ServiceContext serviceContext) {

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
            //init the vendor database - 1 row per job
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
        // get all the mysql statements
        AggregateState.setSqlStatements(new AggregateSQLStatements());

        aggregateSlicerCore = new AggregateSlicerCore();
        aggregateServerThread = new Thread(aggregateSlicerCore);
        aggregateServerThread.start();
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
        throw new UnsupportedOperationException("Not supported yet.");
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
}
