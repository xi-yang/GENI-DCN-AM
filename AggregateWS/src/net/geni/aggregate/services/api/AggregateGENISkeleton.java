
/**
 * AggregateGENISkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
package net.geni.aggregate.services.api;

import java.util.Vector;
import net.geni.aggregate.services.core.AggregateCapabilities;
import net.geni.aggregate.services.core.AggregateNode;
import net.geni.aggregate.services.core.AggregateSlices;
import net.geni.aggregate.services.core.AggregateSlice;
import net.geni.aggregate.services.core.AggregateState;

/**
     *  AggregateGENISkeleton java skeleton for the axisService
     */
    public class AggregateGENISkeleton implements AggregateGENISkeletonInterface{
        
         
        /**
         * Auto generated method signature
         * 
                                     * @param updateSlice0
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.UpdateSliceResponse UpdateSlice
                  (
                  net.geni.aggregate.services.api.UpdateSlice updateSlice0
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#UpdateSlice");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param stopSlice2
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.StopSliceResponse StopSlice
                  (
                  net.geni.aggregate.services.api.StopSlice stopSlice2
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#StopSlice");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param deleteSliceNetwork4
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.DeleteSliceNetworkResponse DeleteSliceNetwork
                  (
                  net.geni.aggregate.services.api.DeleteSliceNetwork deleteSliceNetwork4
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#DeleteSliceNetwork");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param querySlice6
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.QuerySliceResponse QuerySlice
                  (
                  net.geni.aggregate.services.api.QuerySlice querySlice6
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#QuerySlice");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param createSliceNetwork8
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.CreateSliceNetworkResponse CreateSliceNetwork
                  (
                  net.geni.aggregate.services.api.CreateSliceNetwork createSliceNetwork8
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#CreateSliceNetwork");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param listNodes10
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.ListNodesResponse ListNodes
                  (
                  net.geni.aggregate.services.api.ListNodes listNodes10
                  )
            throws AggregateFaultMessage{
            ListNodesType listNodes = listNodes10.getListNodes();
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
                                     * @param startSlice12
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.StartSliceResponse StartSlice
                  (
                  net.geni.aggregate.services.api.StartSlice startSlice12
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#StartSlice");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param createSlice14
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.CreateSliceResponse CreateSlice
                  (
                  net.geni.aggregate.services.api.CreateSlice createSlice14
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#CreateSlice");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param listSlices16
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.ListSlicesResponse ListSlices
                  (
                  net.geni.aggregate.services.api.ListSlices listSlices16
                  )
            throws AggregateFaultMessage{
            ListSlicesType listSlices = listSlices16.getListSlices();
            ListSlicesTypeSequence[] listSlicesSeq = listSlices.getListSlicesTypeSequence();
            String filter = listSlicesSeq[0].getFilter();
            //form response
            ListSlicesResponseType listSlicesResponseType = new ListSlicesResponseType();
            ListSlicesResponse listSlicesResponse = new ListSlicesResponse();
            AggregateSlices slices = AggregateState.getAggregateSlices();
            Vector<ListSlicesResponseTypeSequence> listSlicesResponseSeq = new Vector<ListSlicesResponseTypeSequence>();
            for(int i = 0; i < slices.size(); i++) {
                SliceDescriptorType sliceDesc = new SliceDescriptorType();
                ListSlicesResponseTypeSequence listSlicesResponseTypeSeq = new ListSlicesResponseTypeSequence();
                sliceDesc.setName(slices.get(i).getSliceName());
                sliceDesc.setUrl(slices.get(i).getURL());
                sliceDesc.setDescription(slices.get(i).getDescription());
                String creator = "userID=";
                creator += Integer.toString(slices.get(i).getCreatorId(), 10);
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
        
                 public net.geni.aggregate.services.api.DeleteSliceResponse DeleteSlice
                  (
                  net.geni.aggregate.services.api.DeleteSlice deleteSlice18
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#DeleteSlice");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param listCapabilities20
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.ListCapabilitiesResponse ListCapabilities
                  (
                  net.geni.aggregate.services.api.ListCapabilities listCapabilities20
                  )
            throws AggregateFaultMessage{
            ListCapabilitiesType listCaps = listCapabilities20.getListCapabilities();
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
                                     * @param resetSlice22
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.ResetSliceResponse ResetSlice
                  (
                  net.geni.aggregate.services.api.ResetSlice resetSlice22
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#ResetSlice");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param querySliceNetwork24
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.QuerySliceNetworkResponse QuerySliceNetwork
                  (
                  net.geni.aggregate.services.api.QuerySliceNetwork querySliceNetwork24
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#QuerySliceNetwork");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param querySliceVlan26
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.QuerySliceVlanResponse QuerySliceVlan
                  (
                  net.geni.aggregate.services.api.QuerySliceVlan querySliceVlan26
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#QuerySliceVlan");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param deleteSliceVlan28
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.DeleteSliceVlanResponse DeleteSliceVlan
                  (
                  net.geni.aggregate.services.api.DeleteSliceVlan deleteSliceVlan28
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#DeleteSliceVlan");
        }
     
         
        /**
         * Auto generated method signature
         * 
                                     * @param createSliceVlan30
             * @throws AggregateFaultMessage : 
         */
        
                 public net.geni.aggregate.services.api.CreateSliceVlanResponse CreateSliceVlan
                  (
                  net.geni.aggregate.services.api.CreateSliceVlan createSliceVlan30
                  )
            throws AggregateFaultMessage{
                //TODO : fill this with the necessary business logic
                throw new  java.lang.UnsupportedOperationException("Please implement " + this.getClass().getName() + "#CreateSliceVlan");
        }
     
    }
    