/**
 * AggregateGENISkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4  Built on : Apr 26, 2008 (06:24:30 EDT)
 */
package net.geni.aggregate.services.api;

import java.util.Vector;
import net.geni.aggregate.services.core.AggregateCapabilities;
import net.geni.aggregate.services.core.AggregateNode;
import net.geni.aggregate.services.core.AggregateState;

/**
 *  AggregateGENISkeleton java skeleton for the axisService
 */
public class AggregateGENISkeleton implements AggregateGENISkeletonInterface
{

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
     * @param querySlice2
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.QuerySliceResponse QuerySlice(
            net.geni.aggregate.services.api.QuerySlice querySlice2)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#QuerySlice");
    }

    /**
     * Auto generated method signature
     *
     * @param stopSlice4
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.StopSliceResponse StopSlice(
            net.geni.aggregate.services.api.StopSlice stopSlice4)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#StopSlice");
    }

    /**
     * Auto generated method signature
     *
     * @param listNodes6
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ListNodesResponse ListNodes(
            net.geni.aggregate.services.api.ListNodes listNodes6)
            throws AggregateFaultMessage {
        ListNodesType listNodes = listNodes6.getListNodes();
        ListNodesTypeSequence[] listNodesSeq = listNodes.getListNodesTypeSequence();

        ListNodesResponse listNodesResponse = new ListNodesResponse();
        ListNodesResponseType listNodesResponseType = new ListNodesResponseType();

        Vector<String> capURNs = new Vector<String>();
        for(int i = 0; i < listNodesSeq.length; i++) {
            ListNodesTypeSequence listNodesTypeSeq = listNodesSeq[i];
            String nodeCapURN = listNodesTypeSeq.getCapabilityURN();
            if(nodeCapURN != null) {
                capURNs.add(nodeCapURN);
            }
        }
        Vector<AggregateNode> filtNodes = AggregateState.getAggregateNodes().get(capURNs);
        Vector<ListNodesResponseTypeSequence> lnrtsV = new Vector<ListNodesResponseTypeSequence>();
        for(int i = 0; i < filtNodes.size(); i++) {
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
     * @param startSlice8
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.StartSliceResponse StartSlice(
            net.geni.aggregate.services.api.StartSlice startSlice8)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#StartSlice");
    }

    /**
     * Auto generated method signature
     *
     * @param createSlice10
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.CreateSliceResponse CreateSlice(
            net.geni.aggregate.services.api.CreateSlice createSlice10)
            throws AggregateFaultMessage {
        CreateSliceType createSlice = createSlice10.getCreateSlice();
        NodeDescriptorType[] nodes = createSlice.getNode();
        int startTime = createSlice.getStart();
        int endTime = createSlice.getEnd();
        // enter the request to the DB
        for(int i = 0; i < nodes.length; i++) {
            NodeDescriptorType nodeDescriptorType = nodes[i];

        }
        return null;
    }

    /**
     * Auto generated method signature
     *
     * @param deleteSlice12
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.DeleteSliceResponse DeleteSlice(
            net.geni.aggregate.services.api.DeleteSlice deleteSlice12)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#DeleteSlice");
    }

    /**
     * Auto generated method signature
     *
     * @param listCapabilities14
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ListCapabilitiesResponse ListCapabilities(
            net.geni.aggregate.services.api.ListCapabilities listCapabilities14)
            throws AggregateFaultMessage {
        ListCapabilitiesType listCaps = listCapabilities14.getListCapabilities();
        String filter = listCaps.getFilter();
        //form response
        ListCapabilitiesResponseType listCapResponseType = new ListCapabilitiesResponseType();
        ListCapabilitiesResponse listCapResponse = new ListCapabilitiesResponse();
        AggregateCapabilities caps = AggregateState.getAggregateCaps();
        Vector<ListCapabilitiesResponseTypeSequence> listCapsSeq = new Vector<ListCapabilitiesResponseTypeSequence>();
        for(int i = 0; i < caps.size(); i++) {
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
     * @param resetSlice16
     * @throws AggregateFaultMessage :
     */
    public net.geni.aggregate.services.api.ResetSliceResponse ResetSlice(
            net.geni.aggregate.services.api.ResetSlice resetSlice16)
            throws AggregateFaultMessage {
        //TODO : fill this with the necessary business logic
        throw new java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#ResetSlice");
    }
}
    