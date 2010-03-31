/**
 * AggregateGENISkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
package net.geni.aggregate.services.api;

import org.apache.log4j.*;
import java.util.Vector;
import java.util.HashMap;
import net.geni.aggregate.services.core.AggregateCapabilities;
import net.geni.aggregate.services.core.AggregateNode;
import net.geni.aggregate.services.core.AggregateSlices;
import net.geni.aggregate.services.core.AggregateSlice;
import net.geni.aggregate.services.core.AggregateState;
import net.geni.aggregate.services.core.AggregateP2PVlan;
import net.geni.aggregate.services.core.AggregateP2PVlans;
import net.geni.aggregate.services.core.AggregateUser;
import net.geni.aggregate.services.core.AggregateUsers;
import net.geni.aggregate.services.core.AggregateException;
/**
 *  AggregateGENISkeleton java skeleton for the axisService
 */
public class AggregateGENISkeleton implements AggregateGENISkeletonInterface {

    /**
     * Auto generated method signature
     *
     * @param updateSlice0
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.UpdateSliceResponse UpdateSlice(
            net.geni.aggregate.services.api.UpdateSlice updateSlice0)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#UpdateSlice");
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
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#StopSlice");
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
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#DeleteSliceNetwork");
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
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#QuerySlice");
    }

    /**
     * Auto generated method signature
     *
     * @param createSliceNetwork8
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.CreateSliceNetworkResponse CreateSliceNetwork(
            net.geni.aggregate.services.api.CreateSliceNetwork createSliceNetwork8)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#CreateSliceNetwork");
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

        Vector<String> capURNs = new Vector<String>();
        for (int i = 0; i < listNodesSeq.length; i++) {
            ListNodesTypeSequence listNodesTypeSeq = listNodesSeq[i];
            String nodeCapURN = listNodesTypeSeq.getCapabilityURN();
            if (nodeCapURN != null) {
                capURNs.add(nodeCapURN);
            }
        }
        Vector<AggregateNode> filtNodes = AggregateState.getAggregateNodes().get(capURNs);
        Vector<ListNodesResponseTypeSequence> lnrtsV = new Vector<ListNodesResponseTypeSequence>();
        for (int i = 0; i < filtNodes.size(); i++) {
            NodeDescriptorType nd = new NodeDescriptorType();
            nd.setUrn(filtNodes.get(i).getUrn());
            nd.setId(filtNodes.get(i).getId());
            nd.setDescription(filtNodes.get(i).getDescription());
            nd.setNodeDescriptorTypeSequence_type0(filtNodes.get(i).getCapTypeSeq());
            ListNodesResponseTypeSequence l = new ListNodesResponseTypeSequence();
            l.setNode(nd);
            lnrtsV.add(l);
        }
        listNodesResponseType.setListNodesResponseTypeSequence((ListNodesResponseTypeSequence[]) lnrtsV.toArray(new ListNodesResponseTypeSequence[]{}));
        listNodesResponse.setListNodesResponse(listNodesResponseType);
        return listNodesResponse;
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
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#StartSlice");
    }

    /**
     * Auto generated method signature
     *
     * @param createSlice14
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.CreateSliceResponse CreateSlice(
            net.geni.aggregate.services.api.CreateSlice createSlice14)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#CreateSlice");
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
        //form response
        ListSlicesResponseType listSlicesResponseType = new ListSlicesResponseType();
        ListSlicesResponse listSlicesResponse = new ListSlicesResponse();
        AggregateSlices slices = AggregateState.getAggregateSlices();
        Vector<ListSlicesResponseTypeSequence> listSlicesResponseSeq = new Vector<ListSlicesResponseTypeSequence>();
        for (int i = 0; i < slices.size(); i++) {
            SliceDescriptorType sliceDesc = new SliceDescriptorType();
            ListSlicesResponseTypeSequence listSlicesResponseTypeSeq = new ListSlicesResponseTypeSequence();
            sliceDesc.setName(slices.get(i).getSliceName());
            sliceDesc.setUrl(slices.get(i).getURL());
            sliceDesc.setDescription(slices.get(i).getDescription());
            sliceDesc.setMembers(slices.get(i).getMembers());
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
     * @param deleteSlice18
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.DeleteSliceResponse DeleteSlice(
            net.geni.aggregate.services.api.DeleteSlice deleteSlice18)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#DeleteSlice");
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
        //form response
        ListCapabilitiesResponseType listCapResponseType = new ListCapabilitiesResponseType();
        ListCapabilitiesResponse listCapResponse = new ListCapabilitiesResponse();
        AggregateCapabilities caps = AggregateState.getAggregateCaps();
        Vector<ListCapabilitiesResponseTypeSequence> listCapsSeq = new Vector<ListCapabilitiesResponseTypeSequence>();
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
     * @param resetSlice22
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ResetSliceResponse ResetSlice(
            net.geni.aggregate.services.api.ResetSlice resetSlice22)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#ResetSlice");
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
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#QuerySliceNetwork");
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
        int vlan = deleteSliceVlan.getVlan();

        HashMap hm = new HashMap();
        // look for slice
        AggregateP2PVlans p2pvlans = AggregateState.getAggregateP2PVlans();
        AggregateP2PVlan p2pvlan = p2pvlans.getBySliceName(sliceId);
        if (p2pvlan != null && p2pvlan.getVlanTag() == vlan) {
            hm = p2pvlan.queryVlan();
        } else {
            throw new AggregateFaultMessage("Unkown SliceVLAN: " + sliceId + Integer.toString(vlan));
        }

        //form response
        QuerySliceVlanResponseType querySliceVlanResponseType = new QuerySliceVlanResponseType();
        QuerySliceVlanResponse querySliceVlanResponse = new QuerySliceVlanResponse();
        querySliceVlanResponseType.setStatus(hm.get("status").toString());
        querySliceVlanResponseType.setMessage("Query Result: " + hm.toString());
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
        int vlan = deleteSliceVlan.getVlan();


        String status = "";
        String message = "";
        // look for slice
        AggregateP2PVlans p2pvlans = AggregateState.getAggregateP2PVlans();
        AggregateP2PVlan p2pvlan = p2pvlans.getBySliceName(sliceId);
        if (p2pvlan != null && p2pvlan.getVlanTag() == vlan) {
            if (p2pvlan.getStatus().equalsIgnoreCase("active")) {
                status = p2pvlan.teardownVlan();
                if (status.equalsIgnoreCase("FAILED")) {
                    message = "Error=" + p2pvlan.getErrorMessage();
                } else {
                    message = "GRI=" + p2pvlan.getGlobalReservationId();
                }
            }
            try {
                if (message.equals("")) {
                    message = "GRI=" + p2pvlan.getGlobalReservationId();
                }
                p2pvlan.deleteVlanFromDB();
                AggregateState.getAggregateP2PVlans().remove(p2pvlan);
                status = "deleted";
            } catch (AggregateException ex) {
                status = "failed";
                message += "\nAggregateException returned from deleteVlanFromDB: " + ex.getMessage();
            }
        } else {
            throw new AggregateFaultMessage("Unkown SliceVLAN: " + sliceId + Integer.toString(vlan));
        }

        //form response
        DeleteSliceVlanResponseType deleteSliceVlanResponseType = new DeleteSliceVlanResponseType();
        DeleteSliceVlanResponse deleteSliceVlanResponse = new DeleteSliceVlanResponse();
        deleteSliceVlanResponseType.setStatus(status);
        deleteSliceVlanResponseType.setMessage(message);
        deleteSliceVlanResponse.setDeleteSliceVlanResponse(deleteSliceVlanResponseType);
        return deleteSliceVlanResponse;
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
        VlanReservationDescriptorType vlanResvDescr = createSliceVlan.getVlanReservation();
        String source = vlanResvDescr.getSourceNode();
        String destination = vlanResvDescr.getDestinationNode();
        int vlan = vlanResvDescr.getVlan();
        float bw = vlanResvDescr.getBandwidth();
        String description = vlanResvDescr.getDescription();

        String status = "";
        String message = "";
        long startTime = System.currentTimeMillis()/1000;
        long endTime = System.currentTimeMillis()/1000;

        // look for existing sliceVlan
        AggregateP2PVlans p2pvlans = AggregateState.getAggregateP2PVlans();
        AggregateP2PVlan p2pvlan = p2pvlans.getBySliceName(sliceId);
        if (p2pvlan != null && p2pvlan.getVlanTag() == vlan) {
            status = "failed";
            message = "GRI=" + p2pvlan.getGlobalReservationId() + ", Status=" + p2pvlan.getStatus() +
                    "\nNote: You may delete the VLAN and re-create.";
        } else {// look for slice
            boolean haveSlice = false;
            AggregateSlices slices = AggregateState.getAggregateSlices();
            AggregateSlice slice = slices.getByName(sliceId);
            if (slice != null) {
                if (slice.getCreatedTime() > startTime) {
                    startTime = slice.getCreatedTime();
                }
                if (slice.getExpiredTime() > endTime) {
                    endTime = slice.getExpiredTime();
                } else {//the slice has already expired
                    status = "failed";
                    message = "Slice=" + sliceId + " has already expired. No VLAN created.";
                }
            } else {
                status = "failed";
                message = "Slice=" + sliceId + " does not exist. No VLAN created.";
            }
            if (!status.matches("failed")) {
                p2pvlan = new AggregateP2PVlan(sliceId, source, destination, vlan, bw, description, startTime, endTime);
                status = p2pvlan.setupVlan();
                if (status.equalsIgnoreCase("failed")) {
                    message = "Error=" + p2pvlan.getErrorMessage();
                } else {
                    message = "GRI=" + p2pvlan.getGlobalReservationId();
                }
                AggregateState.getAggregateP2PVlans().add(p2pvlan);
            }
        }
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
     * @param getResourceTopology32
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.GetResourceTopologyResponse GetResourceTopology(
            net.geni.aggregate.services.api.GetResourceTopology getResourceTopology32)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#QuerySliceNetwork");
    }

}
    
