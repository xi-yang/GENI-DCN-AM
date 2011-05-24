/**
 * AggregateGENISkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
package net.geni.aggregate.services.api;

import java.util.*;
import java.security.Principal;
import java.security.cert.X509Certificate;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.*;
import org.apache.ws.security.handler.*;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSConstants;

import net.geni.aggregate.services.core.AggregateState;
import net.geni.aggregate.services.core.AggregateCapability;
import net.geni.aggregate.services.core.AggregateCapabilities;
import net.geni.aggregate.services.core.AggregateNode;
import net.geni.aggregate.services.core.AggregateNodes;
import net.geni.aggregate.services.core.AggregateSlice;
import net.geni.aggregate.services.core.AggregateSlices;
import net.geni.aggregate.services.core.AggregateP2PVlan;
import net.geni.aggregate.services.core.AggregateP2PVlans;
import net.geni.aggregate.services.core.AggregateUser;
import net.geni.aggregate.services.core.AggregateUsers;
import net.geni.aggregate.services.core.AggregateException;
import net.geni.aggregate.services.core.AggregatePLC_APIClient;

/**
 *  AggregateGENISkeleton java skeleton for the axisService
 */
public class AggregateGENISkeleton implements AggregateGENISkeletonInterface {

    /**
     * Auto generated method signature
     *
     * @param createSlice14
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.CreateSliceResponse CreateSlice(
            net.geni.aggregate.services.api.CreateSlice createSlice14)
            throws AggregateFaultMessage {
        CreateSliceType createSlice = createSlice14.getCreateSlice();
        String sliceName = createSlice.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceName.contains(AggregateState.getPlcPrefix())) {
            sliceName = AggregateState.getPlcPrefix() + sliceName;
        }
        String url = createSlice.getUrl();
        String description = createSlice.getDescription();
        String user = createSlice.getUser();
        String[] nodes = createSlice.getNode();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();
        if (!(user.equalsIgnoreCase(authUser.getName()) || user.equalsIgnoreCase(authUser.getEmail()))){
            throw new AggregateFaultMessage("CreateSlice: user " + user + " is not the message signer");
        }

        AggregateSlices slices = AggregateState.getAggregateSlices();
        AggregateSlice slice = slices.createSlice(sliceName, url, description, user, nodes, true);
        String status = (slice == null?"FAILED":"SUCCESSFUL");

        //form response
        CreateSliceResponseType createSliceResponseType = new CreateSliceResponseType();
        CreateSliceResponse createSliceResponse = new CreateSliceResponse();
        createSliceResponseType.setSliceID(sliceName);
        createSliceResponseType.setStatus(status);
        createSliceResponse.setCreateSliceResponse(createSliceResponseType);
        return createSliceResponse;
    }

    /**
     * Auto generated method signature
     *
     * @param updateSlice0
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.UpdateSliceResponse UpdateSlice(
            net.geni.aggregate.services.api.UpdateSlice updateSlice0)
            throws AggregateFaultMessage {
        UpdateSliceType updateSlice = updateSlice0.getUpdateSlice();
        String sliceName = updateSlice.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceName.contains(AggregateState.getPlcPrefix())) {
            sliceName = AggregateState.getPlcPrefix() + sliceName;
        }
        String url = updateSlice.getUrl();
        String description = updateSlice.getDescription();
        String[] users = updateSlice.getUser();
        String[] nodes = updateSlice.getNode();
        int expires = updateSlice.getExpires();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();
        //TODO: verify the authUser is the creator of the slice

        AggregateSlices slices = AggregateState.getAggregateSlices();
        int ret = slices.updateSlice(sliceName, url, description, expires, users, nodes);

        //form response
        UpdateSliceResponseType updateSliceResponseType = new UpdateSliceResponseType();
        UpdateSliceResponse updateSliceResponse = new UpdateSliceResponse();
        String status = "";
        switch (ret) {
            case 1: 
                status = "SUCCESSFUL";
                break;
            case 2: 
                status = "FAILED";
                break;
            default: status = "FAILED";
        }
        updateSliceResponseType.setStatus(status);
        updateSliceResponse.setUpdateSliceResponse(updateSliceResponseType);
        return updateSliceResponse;
    }

    /**
     * Auto generated method signature
     *
     * @param deleteSlice18
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.DeleteSliceResponse DeleteSlice(
            net.geni.aggregate.services.api.DeleteSlice deleteSlice18)
            throws AggregateFaultMessage {
        DeleteSliceType deleteSlice = deleteSlice18.getDeleteSlice();
        String sliceName = deleteSlice.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceName.contains(AggregateState.getPlcPrefix())) {
            sliceName = AggregateState.getPlcPrefix() + sliceName;
        }

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();
        //TODO: verify the authUser is the creator of the slice

        AggregateSlices slices = AggregateState.getAggregateSlices();
        int ret = slices.deleteSlice(sliceName);

        //form response
        DeleteSliceResponseType deleteSliceResponseType = new DeleteSliceResponseType();
        DeleteSliceResponse deleteSliceResponse = new DeleteSliceResponse();
        String status = "";
        switch (ret) {
            case 1: 
                status = "SUCCESSFUL";
                break;
            case 2: 
                status = "FAILED";
                break;
            default: status = "FAILED";
        }

        deleteSliceResponseType.setStatus(status);
        deleteSliceResponse.setDeleteSliceResponse(deleteSliceResponseType);
        return deleteSliceResponse;
    }

    /**
     * Auto generated method signature
     *
     * @param querySlice6
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.QuerySliceResponse QuerySlice(
            net.geni.aggregate.services.api.QuerySlice querySlice6)
            throws AggregateFaultMessage {
        QuerySliceType querySlice = querySlice6.getQuerySlice();
        String[] sliceNames = querySlice.getSliceID();
        for (int i = 0; i < sliceNames.length; i++) {
            if (!AggregateState.getPlcPrefix().isEmpty() && !sliceNames[i].contains(AggregateState.getPlcPrefix())) {
                sliceNames[i] = AggregateState.getPlcPrefix() + sliceNames[i];
            }
        }

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        Vector<HashMap> hmSlices = new Vector<HashMap>();
        plcClient.querySlice(sliceNames, hmSlices);
        plcClient.logoff();
        if (hmSlices.isEmpty() || hmSlices.get(0).isEmpty()) {
            throw new AggregateFaultMessage("Unkown Slice '" + sliceNames[0] + "' or Failure in retrieve slice data from PLC");
        }

        //form response
        QuerySliceResponseType querySliceResponseType = new QuerySliceResponseType();
        QuerySliceResponse querySliceResponse = new QuerySliceResponse();
        Vector<String> qrsV = new Vector<String>();
        for (HashMap hm: hmSlices) {
            qrsV.add(hm.toString());
        }
        String[] qrs = new String[qrsV.size()];
        qrs = qrsV.toArray(qrs);
        querySliceResponseType.setQueryResult(qrs);
        querySliceResponse.setQuerySliceResponse(querySliceResponseType);
        return querySliceResponse;        
    }

    /**
     * Auto generated method signature
     *
     * @param startSlice12
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.StartSliceResponse StartSlice(
            net.geni.aggregate.services.api.StartSlice startSlice12)
            throws AggregateFaultMessage {
        StartSliceType startSlice = startSlice12.getStartSlice();
        String sliceName = startSlice.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceName.contains(AggregateState.getPlcPrefix())) {
            sliceName = AggregateState.getPlcPrefix() + sliceName;
        }

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        //The below logic wil be moved into AggregateSlices
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        int ret = plcClient.startStopSlice(sliceName, true);

        //form response
        StartSliceResponseType startSliceResponseType = new StartSliceResponseType();
        StartSliceResponse startSliceResponse = new StartSliceResponse();
        String status = "";
        switch (ret) {
            case 1: 
                status = "SUCCESSFUL";
                break;
            case 2: 
                status = "FAILED";
                break;
            default: status = "FAILED";
        }
        startSliceResponseType.setStatus(status);
        startSliceResponse.setStartSliceResponse(startSliceResponseType);
        return startSliceResponse;
    }

    /**
     * Auto generated method signature
     *
     * @param stopSlice2
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.StopSliceResponse StopSlice(
            net.geni.aggregate.services.api.StopSlice stopSlice2)
            throws AggregateFaultMessage {
        StopSliceType stopSlice = stopSlice2.getStopSlice();
        String sliceName = stopSlice.getSliceID();
        if (!AggregateState.getPlcPrefix().isEmpty() && !sliceName.contains(AggregateState.getPlcPrefix())) {
            sliceName = AggregateState.getPlcPrefix() + sliceName;
        }

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        //The below logic wil be moved into AggregateSlices
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        int ret = plcClient.startStopSlice(sliceName, false);

        //form response
        StopSliceResponseType stopSliceResponseType = new StopSliceResponseType();
        StopSliceResponse stopSliceResponse = new StopSliceResponse();
        String status = "";
        switch (ret) {
            case 1: 
                status = "SUCCESSFUL";
                break;
            case 2: 
                status = "FAILED";
                break;
            default: status = "FAILED";
        }
        stopSliceResponseType.setStatus(status);
        stopSliceResponse.setStopSliceResponse(stopSliceResponseType);
        return stopSliceResponse;
    }

    /**
     * Auto generated method signature
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
     * Auto generated method signature
     *
     * @param listSlices16
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ListSlicesResponse ListSlices(
            net.geni.aggregate.services.api.ListSlices listSlices16)
            throws AggregateFaultMessage {
        ListSlicesType listSlices = listSlices16.getListSlices();
        ListSlicesTypeSequence[] listSlicesSeq = listSlices.getListSlicesTypeSequence();
        String filter = listSlicesSeq[0].getFilter();

        //get authorized/registered user. AggregateFaultMessage thrown if failed
        AggregateUser authUser = this.getAuthorizedUser();

        //form response
        ListSlicesResponseType listSlicesResponseType = new ListSlicesResponseType();
        ListSlicesResponse listSlicesResponse = new ListSlicesResponse();
        List<AggregateSlice> slices = AggregateState.getAggregateSlices().getAll();
        Vector<ListSlicesResponseTypeSequence> listSlicesResponseSeq = new Vector<ListSlicesResponseTypeSequence>();

        for (int i = 0; i < slices.size(); i++) {
            SliceDescriptorType sliceDesc = new SliceDescriptorType();
            ListSlicesResponseTypeSequence listSlicesResponseTypeSeq = new ListSlicesResponseTypeSequence();
            sliceDesc.setName(slices.get(i).getSliceName());
            sliceDesc.setUrl(slices.get(i).getUrl());
            sliceDesc.setDescription(slices.get(i).getDescription());
            sliceDesc.setNodes(slices.get(i).getNodes());
            int userId = slices.get(i).getCreatorId();
            String creator = "userID=" + Integer.toString(userId, 10);
            AggregateUser user = AggregateState.getAggregateUsers().getById(userId);
            if (user != null) { //get the user name
                creator = user.getName();
            }
            
            sliceDesc.setCreator(creator);
            sliceDesc.setCreatedTime(slices.get(i).getCreatedTime());
            sliceDesc.setExpiredTime(slices.get(i).getExpiredTime());
            listSlicesResponseTypeSeq.setSlice(sliceDesc);
            listSlicesResponseSeq.add(listSlicesResponseTypeSeq);
        }
        listSlicesResponseType.setListSlicesResponseTypeSequence((ListSlicesResponseTypeSequence[]) listSlicesResponseSeq.toArray(new ListSlicesResponseTypeSequence[]{}));
        listSlicesResponse.setListSlicesResponse(listSlicesResponseType);

        return listSlicesResponse;
    }

    /**
     * Auto generated method signature
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
     * Auto generated method signature
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
     * Auto generated method signature
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
     * Auto generated method signature
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
     * Auto generated method signature
     *
     * @param createSliceNetwork8
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.CreateSliceNetworkResponse CreateSliceNetwork(
            net.geni.aggregate.services.api.CreateSliceNetwork createSliceNetwork8)
            throws AggregateFaultMessage {
        CreateSliceNetworkType createSliceNework = createSliceNetwork8.getCreateSliceNetwork();
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
            status = AggregateState.getRspecManager().createRspec(rspecXml, authUser.getEmail(), addPlcSlice);
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
     * Auto generated method signature
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
     * Auto generated method signature
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
            status = "PENDING";
        querySliceNetworkResponseType.setSliceStatus(status);
        Vector<VlanReservationResultType> vlanResvResults = (Vector<VlanReservationResultType>)hmRet.get("vlanResults");
        if (vlanResvResults != null)
            for (VlanReservationResultType r: vlanResvResults)
                querySliceNetworkResponseType.addVlanResvResult(r);
        if (hmRet.containsKey("externalResourceStatus")) {
            String[] extResStatus = {(String)hmRet.get("externalResourceStatus")};
            querySliceNetworkResponseType.setExternalResourceStatus(extResStatus);
        }
        querySliceNetworkResponse.setQuerySliceNetworkResponse(querySliceNetworkResponseType);
        return querySliceNetworkResponse;
    }

    /**
     * Auto generated method signature
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
            statements = AggregateState.getRspecManager().getResourceTopologyXML(scope, rspecNames);
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
     * @return AggregateUser that has certSubject Distiguished Name (DN) matching the message signer
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
