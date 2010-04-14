/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.api;

import org.apache.log4j.*;
import net.geni.aggregate.services.core.AggregateState;
import net.geni.aggregate.services.core.AggregateWSRunner;
import net.geni.aggregate.services.core.AggregateRspecManager;
import net.geni.aggregate.services.core.AggregateCapability;
import net.geni.aggregate.services.core.AggregateException;
import net.geni.aggregate.services.core.AggregateNode;
import net.geni.aggregate.services.core.AggregateSlice;
import net.geni.aggregate.services.core.AggregateP2PVlan;
import net.geni.aggregate.services.core.AggregateUser;
import net.geni.aggregate.services.core.AggregateUtils;
import net.geni.aggregate.services.core.HibernateUtil;
import org.apache.axis2.context.ServiceContext;

/**
 *
 * @author jflidr, xyang
 */
public class AggregateWS implements AggregateGENISkeletonInterface
{
    public static Logger log = Logger.getLogger("net.geni.aggregate");

    private Thread aggregateServerThread;
    private AggregateWSRunner aggregateWSRunner;
    
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

        // initialize database tables
        try {
            //init the resources table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getResourcesTab() + " ( " +
                    "id int(11) NOT NULL auto_increment, " +
                    "type VARCHAR(255) NOT NULL, " +
                    "reference int(11) NOT NULL, " +
                    "rspecId int(11) NOT NULL, " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
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
                    "users TEXT NOT NULL, " + // slice users/persons
                    "nodes TEXT NOT NULL, " + // slice node members
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
                    "sliceName varchar(255) NOT NULL, " +
                    "description varchar(255) NOT NULL default '', " +
                    "source varchar(255) NOT NULL default '', " +
                    "destination varchar(255) NOT NULL default '', " +
                    "srcInterface varchar(255) NOT NULL default '', " +
                    "dstInterface varchar(255) NOT NULL default '', " +
                    "srcIpAndMask varchar(255) NOT NULL default '', " +
                    "dstIpANdMask varchar(255) NOT NULL default '', " +
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
                    "sliceName varchar(255) NOT NULL, " +
                    "vlanPool text NOT NULL, " +
                    "ipPool text NOT NULL, " +
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
        // TODO: no need for the front_end table?
        try {
            //init the database
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + "front_end" + " ( " +
                    "requestID VARCHAR(255) NOT NULL, " + // job ID 1
                    "status VARCHAR(255) NOT NULL DEFAULT 'no such job', " + // job status
                    "statusMsg VARCHAR(255) NOT NULL DEFAULT '', " + // status message if any
                    "PRIMARY KEY (requestID)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        
        // Web Services handnling thread
        aggregateWSRunner = new AggregateWSRunner();
        aggregateServerThread = new Thread(aggregateWSRunner);
        aggregateServerThread.start();

        // Rspec manager thread
        AggregateRspecManager aggregateRspecManager = new AggregateRspecManager();
        aggregateRspecManager.start();
        AggregateState.setRspecManager(aggregateRspecManager);
        
        // PLC polling and DB sync thread
        // TODO

        log.info("AggregateWS init() finished!");
    }

    /**
     * terminates running cores
     * @param serviceContext
     */
    public void destroy(ServiceContext serviceContext) {
        aggregateWSRunner.stop();
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
        return AggregateState.getSkeletonAPI().CreateSlice(createSlice);
    }

    public DeleteSliceResponse DeleteSlice(net.geni.aggregate.services.api.DeleteSlice deleteSlice) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().DeleteSlice(deleteSlice);
    }

    public UpdateSliceResponse UpdateSlice(net.geni.aggregate.services.api.UpdateSlice updateSlice) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().UpdateSlice(updateSlice);
    }

    public StartSliceResponse StartSlice(net.geni.aggregate.services.api.StartSlice startSlice) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().StartSlice(startSlice);
    }

    public StopSliceResponse StopSlice(net.geni.aggregate.services.api.StopSlice stopSlice) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().StopSlice(stopSlice);
    }


    public QuerySliceResponse QuerySlice(net.geni.aggregate.services.api.QuerySlice querySlice) throws AggregateFaultMessage {
        return AggregateState.getSkeletonAPI().QuerySlice(querySlice);
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
        return AggregateState.getSkeletonAPI().CreateSliceNetwork(createSliceNetwork);
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
