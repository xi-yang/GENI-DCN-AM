/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.api;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import net.geni.aggregate.services.core.AggregateCapabilities;
import org.apache.log4j.*;
import net.geni.aggregate.services.core.AggregateState;
import net.geni.aggregate.services.core.AggregateRspecManager;
import net.geni.aggregate.services.core.AggregateCapability;
import net.geni.aggregate.services.core.AggregateException;
import net.geni.aggregate.services.core.AggregateNode;
import net.geni.aggregate.services.core.AggregateP2PVlan;
import net.geni.aggregate.services.core.AggregateP2PVlans;
import net.geni.aggregate.services.core.AggregateStitchTopologyRunner;
import net.geni.aggregate.services.core.AggregateUser;
import net.geni.aggregate.services.core.AggregateUtils;
import net.geni.aggregate.services.core.HibernateUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

/**
 *
 * @author jflidr, xyang
 */
public class AggregateWS implements AggregateGENISkeletonInterface
{
    public static Logger log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");

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

        // create 'aggregate' database
        try {
            AggregateUtils.executeDirectStatement("CREATE DATABASE IF NOT EXISTS aggregate");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        // initialize database tables
        try {
            //init the resources table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getResourcesTab() + " ( " +
                    "id int(11) NOT NULL auto_increment, " +
                    "type VARCHAR(255) NOT NULL, " +
                    "rspecId int(11) NOT NULL, " +
                    "clientId varchar(255) NOT NULL, " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the rspecs table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getRspecsTab() + " ( " +
                    "id int(11) NOT NULL auto_increment, " +
                    "rspecName varchar(255) NOT NULL, " +
                    "aggregateName varchar(255) NOT NULL, " +
                    "description text NOT NULL, " +
                    "geniUser varchar(255) default NULL, " +
                    "startTime bigint(20) default NULL, " +
                    "endTime bigint(20) default NULL, " +
                    "requestXml longtext default NULL, " +
                    "manifestXml longtext default NULL, " +
                    "status varchar(255) NOT NULL, " +
                    "deleted int(1) default NULL, " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the capabilities table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getCapsTab() + " ( " +
                    "id INT NOT NULL AUTO_INCREMENT, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "urn VARCHAR(255) NOT NULL, " +
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
                    "id int(11) NOT NULL, " + // resource ID
                    "nodeId int(11) NOT NULL, " + // node ID by PLC
                    "urn VARCHAR(255) NOT NULL, " +
                    "address VARCHAR(255) NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "capabilities TEXT, " +
                    "PRIMARY KEY (id, nodeId)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the interfaces table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getInterfacesTab() + " ( " +
                    "id int(11) NOT NULL, " + // resource ID
                    "pnid int(11) NOT NULL, " + // parent node resource ID
                    "urn varchar(255) NOT NULL, " +
                    "deviceType varchar(255) NOT NULL, " +
                    "deviceName varchar(255) NOT NULL, " +
                    "capacity varchar(255) NOT NULL, " +
                    "ipAddress varchar(255) NOT NULL, " +
                    "vlanRanges  text NOT NULL, " +
                    "attachedLinks  text NOT NULL, " +
                    "peerInterfaces  text NOT NULL, " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the p2pvlans table
          AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getP2PVlansTab() + " ( " +
                    "id int(11) NOT NULL, " +
                    "vlanTag varchar(255) NOT NULL, " +
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
                    "status varchar(255) NOT NULL default '', " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the sdx_slivers table
          AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS sdx_slivers ( " +
                    "id int(11) NOT NULL, " +
                    "sliceName varchar(255) NOT NULL, " +
                    "serviceUuid varchar(255) NOT NULL, " +
                    "requestJson longtext default NULL, " +
                    "manifestJson longtext default NULL, " +
                    "status varchar(255) NOT NULL default '', " +
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
                    "status varchar(255) NOT NULL default '', " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the resources table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getExtResourcesTab() + " ( " +
                    "id int(11) NOT NULL auto_increment, " +
                    "urn varchar(255) NOT NULL, " +
                    "subType varchar(255) NOT NULL, " +
                    "smUri varchar(255) NOT NULL, " +
                    "amUri varchar(255) NOT NULL, " +
                    "rspecData text NOT NULL, " +
                    "status varchar(255) NOT NULL default '', " +
                    "PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=latin1");
        } catch(AggregateException ex) {
            ex.printStackTrace();
            return;
        }
        try {
            //init the users table
            AggregateUtils.executeDirectStatement("CREATE TABLE IF NOT EXISTS " + AggregateState.getUsersTab() + " ( " +
                    "id int(11) NOT NULL, " +
                    "name varchar(40) NOT NULL default '', " +
                    "password varchar(255) NOT NULL default '', " +
                    "role varchar(40) NOT NULL default '', " +
                    "certSubject varchar(255) NOT NULL default '', " +
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

        // Rspec manager thread
        AggregateRspecManager aggregateRspecManager = new AggregateRspecManager();
        aggregateRspecManager.start();
        AggregateState.setRspecManager(aggregateRspecManager);

        // Ad Rspec stitching topology thread
        AggregateStitchTopologyRunner stitchTopoRunner = new AggregateStitchTopologyRunner();
        stitchTopoRunner.start();
        AggregateState.setStitchTopoRunner(stitchTopoRunner);

        log.info("AggregateWS init() finished!");
    }

    /**
     * terminates running cores
     * @param serviceContext
     */
    public void destroy(ServiceContext serviceContext) {

    }

    /**
     * 
     *
     * @param listNodes10
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ListNodesResponse ListNodes(
            net.geni.aggregate.services.api.ListNodes listNodes10)
            throws AggregateFaultMessage {
        ListNodesType listNodes = listNodes10.getListNodes();
        ListNodesTypeSequence[] listNodesSeq = listNodes.getListNodesTypeSequence();

        ListNodesResponse listNodesResponse = new ListNodesResponse();
        ListNodesResponseType listNodesResponseType = new ListNodesResponseType();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        Vector<String> capURNs = new Vector<String>();
        for (int i = 0; i < listNodesSeq.length; i++) {
            ListNodesTypeSequence listNodesTypeSeq = listNodesSeq[i];
            String nodeCapURN = listNodesTypeSeq.getCapabilityURN();
            if (nodeCapURN != null) {
                capURNs.add(nodeCapURN);
            }
        }
        List<AggregateNode> filtNodes = AggregateState.getAggregateNodes().getByCaps(capURNs);
        Vector<ListNodesResponseTypeSequence> lnrtsV = new Vector<ListNodesResponseTypeSequence>();
        if (filtNodes != null) {
            for (AggregateNode node: filtNodes) {
                NodeDescriptorType nd = new NodeDescriptorType();
                nd.setUrn(node.getUrn());
                nd.setId(node.getId());
                nd.setDescription(node.getDescription());
                nd.setNodeDescriptorTypeSequence_type0(node.getCapTypeSeq());
                ListNodesResponseTypeSequence l = new ListNodesResponseTypeSequence();
                l.setNode(nd);
                lnrtsV.add(l);
            }
        }
        listNodesResponseType.setListNodesResponseTypeSequence((ListNodesResponseTypeSequence[]) lnrtsV.toArray(new ListNodesResponseTypeSequence[]{}));
        listNodesResponse.setListNodesResponse(listNodesResponseType);
        return listNodesResponse;
    }

    /**
     * 
     *
     * @param listCapabilities20
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ListCapabilitiesResponse ListCapabilities(
            net.geni.aggregate.services.api.ListCapabilities listCapabilities20)
            throws AggregateFaultMessage {
        ListCapabilitiesType listCaps = listCapabilities20.getListCapabilities();
        String filter = listCaps.getFilter();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        //form response
        ListCapabilitiesResponseType listCapResponseType = new ListCapabilitiesResponseType();
        ListCapabilitiesResponse listCapResponse = new ListCapabilitiesResponse();
        AggregateCapabilities aggregateCaps = AggregateState.getAggregateCaps();
        Vector<ListCapabilitiesResponseTypeSequence> listCapsSeq = new Vector<ListCapabilitiesResponseTypeSequence>();
        List<AggregateCapability> caps = aggregateCaps.getAll();
        for (int i = 0; i < caps.size(); i++) {
            CapabilityType capDesc = new CapabilityType();
            ListCapabilitiesResponseTypeSequence listCapResponseTypeSeq = new ListCapabilitiesResponseTypeSequence();
            capDesc.setName(caps.get(i).getName());
            capDesc.setUrn(caps.get(i).getUrn());
            capDesc.setId(caps.get(i).getId());
            capDesc.setDescription(caps.get(i).getDescription());
            capDesc.setControllerURL(caps.get(i).getControllerURL());
            listCapResponseTypeSeq.setCapability(capDesc);
            listCapsSeq.add(listCapResponseTypeSeq);
        }
        listCapResponseType.setListCapabilitiesResponseTypeSequence((ListCapabilitiesResponseTypeSequence[]) listCapsSeq.toArray(new ListCapabilitiesResponseTypeSequence[]{}));
        listCapResponse.setListCapabilitiesResponse(listCapResponseType);

        return listCapResponse;
    }

    /**
     * 
     *
     * @param createSliceVlan30
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.CreateSliceVlanResponse CreateSliceVlan(
            net.geni.aggregate.services.api.CreateSliceVlan createSliceVlan30)
            throws AggregateFaultMessage {

        CreateSliceVlanType createSliceVlan = createSliceVlan30.getCreateSliceVlan();
        String sliceId = createSliceVlan.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceId.contains(AggregateState.getPlcPrefix())) {
            sliceId = AggregateState.getPlcPrefix() + sliceId;
        }
        VlanReservationDescriptorType vlanResvDescr = createSliceVlan.getVlanReservation();
        String source = vlanResvDescr.getSourceNode();
        String srcInterface = vlanResvDescr.getSrcInterface();
        String srcIpAndMask = vlanResvDescr.getSrcIpAndMask();
        String destination = vlanResvDescr.getDestinationNode();
        String dstInterface = vlanResvDescr.getDstInterface();
        String dstIpAndMask = vlanResvDescr.getDstIpAndMask();
        String vlan = vlanResvDescr.getVlan();
        float bw = vlanResvDescr.getBandwidth();
        
        String description = vlanResvDescr.getDescription();
        long startTime = System.currentTimeMillis()/1000;
        long endTime = System.currentTimeMillis()/1000;

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        // look for existing sliceVlan
        AggregateP2PVlans p2pvlans = AggregateState.getAggregateP2PVlans();
        HashMap hm = new HashMap<String, String>();
        AggregateP2PVlan p2pvlan = p2pvlans.createVlan(sliceId, source, srcInterface, srcIpAndMask,
            destination, dstInterface, dstIpAndMask, vlan, bw, description, startTime, endTime, hm);
        String status = (String)hm.get("status");
        String message = (String)hm.get("message");

        //form response
        CreateSliceVlanResponseType createSliceVlanResponseType = new CreateSliceVlanResponseType();
        CreateSliceVlanResponse createSliceVlanResponse = new CreateSliceVlanResponse();
        createSliceVlanResponseType.setStatus(status);
        createSliceVlanResponseType.setMessage(message);
        createSliceVlanResponse.setCreateSliceVlanResponse(createSliceVlanResponseType);
        return createSliceVlanResponse;
    }

    /**
     * 
     *
     * @param querySliceVlan26
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.QuerySliceVlanResponse QuerySliceVlan(
            net.geni.aggregate.services.api.QuerySliceVlan querySliceVlan26)
            throws AggregateFaultMessage {

        QuerySliceVlanType deleteSliceVlan = querySliceVlan26.getQuerySliceVlan();
        String sliceId = deleteSliceVlan.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceId.contains(AggregateState.getPlcPrefix())) {
            sliceId = AggregateState.getPlcPrefix() + sliceId;
        }
        String vlan = deleteSliceVlan.getVlan();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        // TODO: re-sync IDC and AggregateDB
        AggregateP2PVlans p2pvlans = AggregateState.getAggregateP2PVlans();
        AggregateP2PVlan p2pvlan = p2pvlans.getBySliceAndVtag(sliceId, vlan);
        VlanReservationResultType vlanResvResult = null;
        if (p2pvlan != null) {
            vlanResvResult = p2pvlan.queryVlan();
            p2pvlans.update(p2pvlan);
        } else {
            throw new AggregateFaultMessage("Unkown SliceVLAN: " + sliceId + ":" + vlan);
        }

        //form response
        QuerySliceVlanResponseType querySliceVlanResponseType = new QuerySliceVlanResponseType();
        QuerySliceVlanResponse querySliceVlanResponse = new QuerySliceVlanResponse();
        querySliceVlanResponseType.setVlanResvResult(vlanResvResult);
        querySliceVlanResponse.setQuerySliceVlanResponse(querySliceVlanResponseType);
        return querySliceVlanResponse;
    }

    /**
     * 
     *
     * @param deleteSliceVlan28
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.DeleteSliceVlanResponse DeleteSliceVlan(
            net.geni.aggregate.services.api.DeleteSliceVlan deleteSliceVlan28)
            throws AggregateFaultMessage {

        DeleteSliceVlanType deleteSliceVlan = deleteSliceVlan28.getDeleteSliceVlan();
        String sliceId = deleteSliceVlan.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceId.contains(AggregateState.getPlcPrefix())) {
            sliceId = AggregateState.getPlcPrefix() + sliceId;
        }
        String vlan = deleteSliceVlan.getVlan();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        AggregateP2PVlans p2pvlans = AggregateState.getAggregateP2PVlans();
        HashMap hm = p2pvlans.deleteVlan(sliceId, vlan);
        String status = (String)hm.get("status");
        String message = (String)hm.get("message");

        //form response
        DeleteSliceVlanResponseType deleteSliceVlanResponseType = new DeleteSliceVlanResponseType();
        DeleteSliceVlanResponse deleteSliceVlanResponse = new DeleteSliceVlanResponse();
        deleteSliceVlanResponseType.setStatus(status);
        deleteSliceVlanResponseType.setMessage(message);
        deleteSliceVlanResponse.setDeleteSliceVlanResponse(deleteSliceVlanResponseType);
        return deleteSliceVlanResponse;
    }

    /********************* TODO Services **************************************/

    /**
     * 
     *
     * @param createSliceNetwork8
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.CreateSliceNetworkResponse CreateSliceNetwork(
            net.geni.aggregate.services.api.CreateSliceNetwork createSliceNetwork8)
            throws AggregateFaultMessage {
        CreateSliceNetworkType createSliceNework = createSliceNetwork8.getCreateSliceNetwork();
        String rspecId = createSliceNework.getRspecID();
        String geniUser = createSliceNework.getUserID();
        RSpecTopologyType rspecTopo = createSliceNework.getRspecNetwork();
        String rspecXml = "";
        for (String s: rspecTopo.getStatement()) {
            rspecXml += s;
        }
        boolean addPlcSlice = createSliceNework.getAddPlcSlice();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        String status = "";
        String message = "";
        try {
            //TODO pass authUser into createRspec!
            status = AggregateState.getRspecManager().createRspec(rspecId, rspecXml, geniUser, authUser.getEmail(), addPlcSlice, 0);
            message = "";
        } catch (AggregateException e) {
            status = "FAILED";
            message = e.getMessage();
        }

        //form response
        CreateSliceNetworkResponseType createSliceNetworkResponseType = new CreateSliceNetworkResponseType();
        CreateSliceNetworkResponse createSliceNetworkResponse = new CreateSliceNetworkResponse();
        createSliceNetworkResponseType.setStatus(status);
        createSliceNetworkResponseType.setMessage(message);
        createSliceNetworkResponse.setCreateSliceNetworkResponse(createSliceNetworkResponseType);
        return createSliceNetworkResponse;
    }

    /**
     * 
     *
     * @param allocateSliceNetwork10
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.AllocateSliceNetworkResponse AllocateSliceNetwork(
            net.geni.aggregate.services.api.AllocateSliceNetwork allocateSliceNetwork10)
            throws AggregateFaultMessage {
        AllocateSliceNetworkType allocateSliceNework = allocateSliceNetwork10.getAllocateSliceNetwork();
        String rspecId = allocateSliceNework.getRspecID();
        String geniUser = allocateSliceNework.getUserID();
        RSpecTopologyType rspecTopo = allocateSliceNework.getRspecNetwork();
        String rspecXml = "";
        for (String s: rspecTopo.getStatement()) {
            rspecXml += s;
        }
        boolean addPlcSlice = allocateSliceNework.getAddPlcSlice();
        String expires = allocateSliceNework.getExpires();
        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        String status = "";
        String message = "";
        try {
            status = AggregateState.getRspecManager().allocateRspec(rspecId, rspecXml, geniUser, authUser.getEmail(), addPlcSlice, expires);
            message = "";
        } catch (AggregateException e) {
            status = "FAILED";
            message = e.getMessage();
        }

        //form response
        AllocateSliceNetworkResponseType allocateSliceNetworkResponseType = new AllocateSliceNetworkResponseType();
        AllocateSliceNetworkResponse allocateSliceNetworkResponse = new AllocateSliceNetworkResponse();
        allocateSliceNetworkResponseType.setStatus(status);
        allocateSliceNetworkResponseType.setMessage(message);
        allocateSliceNetworkResponse.setAllocateSliceNetworkResponse(allocateSliceNetworkResponseType);
        return allocateSliceNetworkResponse;
    }

    /**
     * 
     *
     * @param provisionSliceNetwork12
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ProvisionSliceNetworkResponse ProvisionSliceNetwork(
            net.geni.aggregate.services.api.ProvisionSliceNetwork provisionSliceNetwork12)
            throws AggregateFaultMessage {
        ProvisionSliceNetworkType provisionSliceNework = provisionSliceNetwork12.getProvisionSliceNetwork();
        String rspecId = provisionSliceNework.getRspecID();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        String status = "";
        String message = "";
        try {
            status = AggregateState.getRspecManager().provisionRspec(rspecId);
            message = "";
        } catch (AggregateException e) {
            status = "FAILED";
            message = e.getMessage();
        }

        //form response
        ProvisionSliceNetworkResponseType provisionSliceNetworkResponseType = new ProvisionSliceNetworkResponseType();
        ProvisionSliceNetworkResponse provisionSliceNetworkResponse = new ProvisionSliceNetworkResponse();
        provisionSliceNetworkResponseType.setStatus(status);
        provisionSliceNetworkResponseType.setMessage(message);
        provisionSliceNetworkResponse.setProvisionSliceNetworkResponse(provisionSliceNetworkResponseType);
        return provisionSliceNetworkResponse;
    }

    /**
     * 
     *
     * @param renewSliceNetwork22
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.RenewSliceNetworkResponse RenewSliceNetwork(
            net.geni.aggregate.services.api.RenewSliceNetwork renewSliceNetwork22)
            throws AggregateFaultMessage {
        RenewSliceNetworkType renewSliceNework = renewSliceNetwork22.getRenewSliceNetwork();
        String rspecName = renewSliceNework.getRspecID();
        String expires = renewSliceNework.getExpires();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        String status = "";
        String message = "";
        try {
            status = AggregateState.getRspecManager().renewRspec(rspecName, expires);
            message = "";
        } catch (AggregateException e) {
            status = "FAILED";
            message = e.getMessage();
        }

        //form response
        RenewSliceNetworkResponseType renewSliceNetworkResponseType = new RenewSliceNetworkResponseType();
        RenewSliceNetworkResponse renewSliceNetworkResponse = new RenewSliceNetworkResponse();
        renewSliceNetworkResponseType.setStatus(status);
        renewSliceNetworkResponseType.setMessage(message);
        renewSliceNetworkResponse.setRenewSliceNetworkResponse(renewSliceNetworkResponseType);
        return renewSliceNetworkResponse;
    }

    /**
     * 
     *
     * @param deleteSliceNetwork4
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.DeleteSliceNetworkResponse DeleteSliceNetwork(
            net.geni.aggregate.services.api.DeleteSliceNetwork deleteSliceNetwork4)
            throws AggregateFaultMessage {
        DeleteSliceNetworkType deleteSliceNework = deleteSliceNetwork4.getDeleteSliceNetwork();
        String rspecName = deleteSliceNework.getRspecID();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        String status = "";
        String message = "";
        try {
            status = AggregateState.getRspecManager().deleteRspec(rspecName);
            message = "";
        } catch (AggregateException e) {
            status = "FAILED";
            message = e.getMessage();
        }

        //form response
        DeleteSliceNetworkResponseType deleteSliceNetworkResponseType = new DeleteSliceNetworkResponseType();
        DeleteSliceNetworkResponse deleteSliceNetworkResponse = new DeleteSliceNetworkResponse();
        deleteSliceNetworkResponseType.setStatus(status);
        deleteSliceNetworkResponseType.setMessage(message);
        deleteSliceNetworkResponse.setDeleteSliceNetworkResponse(deleteSliceNetworkResponseType);
        return deleteSliceNetworkResponse;
    }

    /**
     * 
     *
     * @param querySliceNetwork24
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.QuerySliceNetworkResponse QuerySliceNetwork(
            net.geni.aggregate.services.api.QuerySliceNetwork querySliceNetwork24)
            throws AggregateFaultMessage {
        QuerySliceNetworkType querySliceNework = querySliceNetwork24.getQuerySliceNetwork();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        String rspecName = querySliceNework.getRspecID();
        HashMap hmRet = null;
        try {
            hmRet = AggregateState.getRspecManager().queryRspec(rspecName);
            if (hmRet == null)
                throw new AggregateFaultMessage("Unknown Rsepc:"+rspecName);
        } catch (AggregateException e) {
            throw new AggregateFaultMessage("Failed to query Rsepc:"+rspecName
                + " AggregateException: " + e.getMessage());
        }

        //form response
        QuerySliceNetworkResponseType querySliceNetworkResponseType = new QuerySliceNetworkResponseType();
        QuerySliceNetworkResponse querySliceNetworkResponse = new QuerySliceNetworkResponse();
        String status = (String)hmRet.get("sliceStatus");
        if (status == null)
            status = "NONE";
        querySliceNetworkResponseType.setSliceStatus(status);
        Vector<VlanReservationResultType> vlanResvResults = (Vector<VlanReservationResultType>)hmRet.get("vlanResults");
        if (vlanResvResults != null)
            for (VlanReservationResultType r: vlanResvResults)
                querySliceNetworkResponseType.addVlanResvResult(r);
        if (hmRet.containsKey("externalResourceStatus")) {
            String[] extResStatus = {(String)hmRet.get("externalResourceStatus")};
            querySliceNetworkResponseType.setExternalResourceStatus(extResStatus);
        }
        if (hmRet.containsKey("expires")) {
            String expires = (String)hmRet.get("expires");
            querySliceNetworkResponseType.setExpires(expires);
        }
        querySliceNetworkResponse.setQuerySliceNetworkResponse(querySliceNetworkResponseType);
        return querySliceNetworkResponse;
    }

    /**
     * 
     *
     * @param getResourceTopology32
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.GetResourceTopologyResponse GetResourceTopology(
            net.geni.aggregate.services.api.GetResourceTopology getResourceTopology32)
            throws AggregateFaultMessage {
        GetResourceTopologyType getResourceTopology = getResourceTopology32.getGetResourceTopology();
        String scope = getResourceTopology.getScope();
        String [] rspecNames = getResourceTopology.getRspec();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        String[] statements = null;
        try {
            statements = AggregateState.getRspecManager().getManifestXml(scope, rspecNames);
        } catch (AggregateException e) {
            throw new AggregateFaultMessage(e.getMessage());
        }

        GetResourceTopologyResponseType getResourceTopologyResponseType = new GetResourceTopologyResponseType();
        GetResourceTopologyResponse getResourceTopologyResponse = new GetResourceTopologyResponse();
        getResourceTopologyResponseType.setStatus("normal");
        RSpecTopologyType rspecTopo = new RSpecTopologyType();
        rspecTopo.setStatement(statements);
        getResourceTopologyResponseType.setResourceTopology(rspecTopo);
        getResourceTopologyResponse.setGetResourceTopologyResponse(getResourceTopologyResponseType);
        return getResourceTopologyResponse;
    }


    public net.geni.aggregate.services.api.GetAllResourceInfoResponse GetAllResourceInfo(
            net.geni.aggregate.services.api.GetAllResourceInfo getAllResourceInfo) 
            throws AggregateFaultMessage {
        GetAllResourceInfoResponseType getAllResourceInfoResponseType = new GetAllResourceInfoResponseType();
        GetAllResourceInfoResponse getAllResourceInfoResponse = new GetAllResourceInfoResponse();
        String filter = getAllResourceInfo.getGetAllResourceInfo().getFilter();
        try {
            String allRspecsInfo = AggregateState.getRspecManager().getAllRspecsInfo(filter);
            getAllResourceInfoResponseType.setInfo(allRspecsInfo);
            getAllResourceInfoResponse.setGetAllResourceInfoResponse(getAllResourceInfoResponseType);
        } catch (AggregateException e) {
            throw new AggregateFaultMessage("Failed to retrieve all rspec information for the aggregate: "
                + " AggregateException: " + e.getMessage());
        }
        return getAllResourceInfoResponse;
    }
    
    /**
     * Borrowed from OSCARS Project to get the DN out of the message context.
     *
     * @param opContext includes the MessageContext containing the message
     *                  signer
     */
    private HashMap<String, Principal> getSecurityPrincipals() {
        HashMap<String, Principal> result = new HashMap<String, Principal>();

        try {
            MessageContext inContext = MessageContext.getCurrentMessageContext();
            // opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inContext == null) {
                return null;
            }
            Vector results = (Vector) inContext.getProperty(WSHandlerConstants.RECV_RESULTS);

            for (int i = 0; results != null && i < results.size(); i++) {
                WSHandlerResult hResult = (WSHandlerResult) results.get(i);
                Vector hResults = hResult.getResults();
                for (int j = 0; j < hResults.size(); j++) {
                    WSSecurityEngineResult eResult = (WSSecurityEngineResult) hResults.get(j);
                    // An encryption or timestamp action does not have an
                    // associated principal. Only Signature and UsernameToken
                    // actions return a principal.
                    if ((((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.SIGN) ||
                        (((java.lang.Integer) eResult.get(
                            WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.UT)) {
                        Principal subjectDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getSubjectDN();
                        Principal issuerDN = ((X509Certificate) eResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE)).getIssuerDN();
                        result.put("subject", subjectDN);
                        result.put("issuer", issuerDN);
                        return result;
                    } else if (((java.lang.Integer) eResult.get(
                                WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.ENCR) {
                        // Encryption action returns what ?
                        return null;
                    } else if (((java.lang.Integer) eResult.get(
                                WSSecurityEngineResult.TAG_ACTION)).intValue() == WSConstants.TS) {
                        // Timestamp action returns a Timestamp
                        //System.out.println("Timestamp created: " +
                        //eResult.getTimestamp().getCreated());
                        //System.out.println("Timestamp expires: " +
                        //eResult.getTimestamp().getExpires());
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }

        return result;
    }

    /**
     *
     * @return AggregateUser that has certSubject Distinguished Name (DN) matching the message signer
     * @throws AggregateFaultMessage
     */
    public AggregateUser getAuthorizedUser() throws AggregateFaultMessage {
        AggregateUser authUser = null;
        HashMap<String, Principal> principals = getSecurityPrincipals();

        if (principals == null) {
            throw new AggregateFaultMessage("getAuthorizedUser: failed to get security prinfipals");
        } else if (principals.get("subject") == null){
            throw new AggregateFaultMessage("getAuthorizedUser: no certSubject found in message");
        }

        // lookup up using input DN first
        String origDN = principals.get("subject").getName();

        authUser = AggregateState.getAggregateUsers().getByCertSubject(origDN);
        if (authUser == null) {
            throw new AggregateFaultMessage("getAuthorizedUser: unregistered user DN: '"+origDN+"'");
        }
        return authUser;
    }
}
