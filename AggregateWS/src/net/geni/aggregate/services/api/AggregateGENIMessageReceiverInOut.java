
/**
 * AggregateGENIMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
        package net.geni.aggregate.services.api;

        /**
        *  AggregateGENIMessageReceiverInOut message receiver
        */

        public class AggregateGENIMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        AggregateGENISkeletonInterface skel = (AggregateGENISkeletonInterface)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJava(op.getName().getLocalPart())) != null)){

        

            if("GetAllResourceInfo".equals(methodName)){
                
                net.geni.aggregate.services.api.GetAllResourceInfoResponse getAllResourceInfoResponse41 = null;
	                        net.geni.aggregate.services.api.GetAllResourceInfo wrappedParam =
                                                             (net.geni.aggregate.services.api.GetAllResourceInfo)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.GetAllResourceInfo.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               getAllResourceInfoResponse41 =
                                                   
                                                   
                                                         skel.GetAllResourceInfo(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), getAllResourceInfoResponse41, false);
                                    } else 

            if("ProvisionSliceNetwork".equals(methodName)){
                
                net.geni.aggregate.services.api.ProvisionSliceNetworkResponse provisionSliceNetworkResponse43 = null;
	                        net.geni.aggregate.services.api.ProvisionSliceNetwork wrappedParam =
                                                             (net.geni.aggregate.services.api.ProvisionSliceNetwork)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.ProvisionSliceNetwork.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               provisionSliceNetworkResponse43 =
                                                   
                                                   
                                                         skel.ProvisionSliceNetwork(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), provisionSliceNetworkResponse43, false);
                                    } else 

            if("AllocateSliceNetwork".equals(methodName)){
                
                net.geni.aggregate.services.api.AllocateSliceNetworkResponse allocateSliceNetworkResponse45 = null;
	                        net.geni.aggregate.services.api.AllocateSliceNetwork wrappedParam =
                                                             (net.geni.aggregate.services.api.AllocateSliceNetwork)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.AllocateSliceNetwork.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               allocateSliceNetworkResponse45 =
                                                   
                                                   
                                                         skel.AllocateSliceNetwork(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), allocateSliceNetworkResponse45, false);
                                    } else 

            if("UpdateSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.UpdateSliceResponse updateSliceResponse47 = null;
	                        net.geni.aggregate.services.api.UpdateSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.UpdateSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.UpdateSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               updateSliceResponse47 =
                                                   
                                                   
                                                         skel.UpdateSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), updateSliceResponse47, false);
                                    } else 

            if("StopSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.StopSliceResponse stopSliceResponse49 = null;
	                        net.geni.aggregate.services.api.StopSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.StopSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.StopSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               stopSliceResponse49 =
                                                   
                                                   
                                                         skel.StopSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), stopSliceResponse49, false);
                                    } else 

            if("DeleteSliceNetwork".equals(methodName)){
                
                net.geni.aggregate.services.api.DeleteSliceNetworkResponse deleteSliceNetworkResponse51 = null;
	                        net.geni.aggregate.services.api.DeleteSliceNetwork wrappedParam =
                                                             (net.geni.aggregate.services.api.DeleteSliceNetwork)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.DeleteSliceNetwork.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               deleteSliceNetworkResponse51 =
                                                   
                                                   
                                                         skel.DeleteSliceNetwork(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), deleteSliceNetworkResponse51, false);
                                    } else 

            if("QuerySlice".equals(methodName)){
                
                net.geni.aggregate.services.api.QuerySliceResponse querySliceResponse53 = null;
	                        net.geni.aggregate.services.api.QuerySlice wrappedParam =
                                                             (net.geni.aggregate.services.api.QuerySlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.QuerySlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               querySliceResponse53 =
                                                   
                                                   
                                                         skel.QuerySlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), querySliceResponse53, false);
                                    } else 

            if("RenewSliceNetwork".equals(methodName)){
                
                net.geni.aggregate.services.api.RenewSliceNetworkResponse renewSliceNetworkResponse55 = null;
	                        net.geni.aggregate.services.api.RenewSliceNetwork wrappedParam =
                                                             (net.geni.aggregate.services.api.RenewSliceNetwork)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.RenewSliceNetwork.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               renewSliceNetworkResponse55 =
                                                   
                                                   
                                                         skel.RenewSliceNetwork(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), renewSliceNetworkResponse55, false);
                                    } else 

            if("GetResourceTopology".equals(methodName)){
                
                net.geni.aggregate.services.api.GetResourceTopologyResponse getResourceTopologyResponse57 = null;
	                        net.geni.aggregate.services.api.GetResourceTopology wrappedParam =
                                                             (net.geni.aggregate.services.api.GetResourceTopology)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.GetResourceTopology.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               getResourceTopologyResponse57 =
                                                   
                                                   
                                                         skel.GetResourceTopology(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), getResourceTopologyResponse57, false);
                                    } else 

            if("CreateSliceNetwork".equals(methodName)){
                
                net.geni.aggregate.services.api.CreateSliceNetworkResponse createSliceNetworkResponse59 = null;
	                        net.geni.aggregate.services.api.CreateSliceNetwork wrappedParam =
                                                             (net.geni.aggregate.services.api.CreateSliceNetwork)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.CreateSliceNetwork.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               createSliceNetworkResponse59 =
                                                   
                                                   
                                                         skel.CreateSliceNetwork(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), createSliceNetworkResponse59, false);
                                    } else 

            if("ListNodes".equals(methodName)){
                
                net.geni.aggregate.services.api.ListNodesResponse listNodesResponse61 = null;
	                        net.geni.aggregate.services.api.ListNodes wrappedParam =
                                                             (net.geni.aggregate.services.api.ListNodes)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.ListNodes.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               listNodesResponse61 =
                                                   
                                                   
                                                         skel.ListNodes(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), listNodesResponse61, false);
                                    } else 

            if("StartSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.StartSliceResponse startSliceResponse63 = null;
	                        net.geni.aggregate.services.api.StartSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.StartSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.StartSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               startSliceResponse63 =
                                                   
                                                   
                                                         skel.StartSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), startSliceResponse63, false);
                                    } else 

            if("CreateSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.CreateSliceResponse createSliceResponse65 = null;
	                        net.geni.aggregate.services.api.CreateSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.CreateSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.CreateSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               createSliceResponse65 =
                                                   
                                                   
                                                         skel.CreateSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), createSliceResponse65, false);
                                    } else 

            if("ListSlices".equals(methodName)){
                
                net.geni.aggregate.services.api.ListSlicesResponse listSlicesResponse67 = null;
	                        net.geni.aggregate.services.api.ListSlices wrappedParam =
                                                             (net.geni.aggregate.services.api.ListSlices)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.ListSlices.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               listSlicesResponse67 =
                                                   
                                                   
                                                         skel.ListSlices(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), listSlicesResponse67, false);
                                    } else 

            if("DeleteSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.DeleteSliceResponse deleteSliceResponse69 = null;
	                        net.geni.aggregate.services.api.DeleteSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.DeleteSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.DeleteSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               deleteSliceResponse69 =
                                                   
                                                   
                                                         skel.DeleteSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), deleteSliceResponse69, false);
                                    } else 

            if("ListCapabilities".equals(methodName)){
                
                net.geni.aggregate.services.api.ListCapabilitiesResponse listCapabilitiesResponse71 = null;
	                        net.geni.aggregate.services.api.ListCapabilities wrappedParam =
                                                             (net.geni.aggregate.services.api.ListCapabilities)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.ListCapabilities.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               listCapabilitiesResponse71 =
                                                   
                                                   
                                                         skel.ListCapabilities(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), listCapabilitiesResponse71, false);
                                    } else 

            if("QuerySliceNetwork".equals(methodName)){
                
                net.geni.aggregate.services.api.QuerySliceNetworkResponse querySliceNetworkResponse73 = null;
	                        net.geni.aggregate.services.api.QuerySliceNetwork wrappedParam =
                                                             (net.geni.aggregate.services.api.QuerySliceNetwork)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.QuerySliceNetwork.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               querySliceNetworkResponse73 =
                                                   
                                                   
                                                         skel.QuerySliceNetwork(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), querySliceNetworkResponse73, false);
                                    } else 

            if("QuerySliceVlan".equals(methodName)){
                
                net.geni.aggregate.services.api.QuerySliceVlanResponse querySliceVlanResponse75 = null;
	                        net.geni.aggregate.services.api.QuerySliceVlan wrappedParam =
                                                             (net.geni.aggregate.services.api.QuerySliceVlan)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.QuerySliceVlan.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               querySliceVlanResponse75 =
                                                   
                                                   
                                                         skel.QuerySliceVlan(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), querySliceVlanResponse75, false);
                                    } else 

            if("DeleteSliceVlan".equals(methodName)){
                
                net.geni.aggregate.services.api.DeleteSliceVlanResponse deleteSliceVlanResponse77 = null;
	                        net.geni.aggregate.services.api.DeleteSliceVlan wrappedParam =
                                                             (net.geni.aggregate.services.api.DeleteSliceVlan)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.DeleteSliceVlan.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               deleteSliceVlanResponse77 =
                                                   
                                                   
                                                         skel.DeleteSliceVlan(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), deleteSliceVlanResponse77, false);
                                    } else 

            if("CreateSliceVlan".equals(methodName)){
                
                net.geni.aggregate.services.api.CreateSliceVlanResponse createSliceVlanResponse79 = null;
	                        net.geni.aggregate.services.api.CreateSliceVlan wrappedParam =
                                                             (net.geni.aggregate.services.api.CreateSliceVlan)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.CreateSliceVlan.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               createSliceVlanResponse79 =
                                                   
                                                   
                                                         skel.CreateSliceVlan(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), createSliceVlanResponse79, false);
                                    
            } else {
              throw new java.lang.RuntimeException("method not found");
            }
        

        newMsgContext.setEnvelope(envelope);
        }
        } catch (AggregateFaultMessage e) {

            msgContext.setProperty(org.apache.axis2.Constants.FAULT_NAME,"AggregateFault");
            org.apache.axis2.AxisFault f = createAxisFault(e);
            if (e.getFaultMessage() != null){
                f.setDetail(toOM(e.getFaultMessage(),false));
            }
            throw f;
            }
        
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }
        
        //
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.GetAllResourceInfo param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.GetAllResourceInfo.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.GetAllResourceInfoResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.GetAllResourceInfoResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.AggregateFault param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.AggregateFault.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ProvisionSliceNetwork param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ProvisionSliceNetwork.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ProvisionSliceNetworkResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ProvisionSliceNetworkResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.AllocateSliceNetwork param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.AllocateSliceNetwork.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.AllocateSliceNetworkResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.AllocateSliceNetworkResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.UpdateSlice param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.UpdateSlice.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.UpdateSliceResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.UpdateSliceResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.StopSlice param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.StopSlice.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.StopSliceResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.StopSliceResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.DeleteSliceNetwork param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.DeleteSliceNetwork.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.DeleteSliceNetworkResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.DeleteSliceNetworkResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.QuerySlice param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.QuerySlice.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.QuerySliceResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.QuerySliceResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.RenewSliceNetwork param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.RenewSliceNetwork.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.RenewSliceNetworkResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.RenewSliceNetworkResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.GetResourceTopology param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.GetResourceTopology.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.GetResourceTopologyResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.GetResourceTopologyResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.CreateSliceNetwork param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.CreateSliceNetwork.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.CreateSliceNetworkResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.CreateSliceNetworkResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ListNodes param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ListNodes.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ListNodesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ListNodesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.StartSlice param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.StartSlice.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.StartSliceResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.StartSliceResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.CreateSlice param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.CreateSlice.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.CreateSliceResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.CreateSliceResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ListSlices param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ListSlices.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ListSlicesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ListSlicesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.DeleteSlice param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.DeleteSlice.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.DeleteSliceResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.DeleteSliceResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ListCapabilities param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ListCapabilities.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ListCapabilitiesResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ListCapabilitiesResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.QuerySliceNetwork param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.QuerySliceNetwork.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.QuerySliceNetworkResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.QuerySliceNetworkResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.QuerySliceVlan param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.QuerySliceVlan.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.QuerySliceVlanResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.QuerySliceVlanResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.DeleteSliceVlan param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.DeleteSliceVlan.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.DeleteSliceVlanResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.DeleteSliceVlanResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.CreateSliceVlan param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.CreateSliceVlan.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.CreateSliceVlanResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.CreateSliceVlanResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.GetAllResourceInfoResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.GetAllResourceInfoResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.GetAllResourceInfoResponse wrapGetAllResourceInfo(){
                                net.geni.aggregate.services.api.GetAllResourceInfoResponse wrappedElement = new net.geni.aggregate.services.api.GetAllResourceInfoResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.ProvisionSliceNetworkResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.ProvisionSliceNetworkResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.ProvisionSliceNetworkResponse wrapProvisionSliceNetwork(){
                                net.geni.aggregate.services.api.ProvisionSliceNetworkResponse wrappedElement = new net.geni.aggregate.services.api.ProvisionSliceNetworkResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.AllocateSliceNetworkResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.AllocateSliceNetworkResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.AllocateSliceNetworkResponse wrapAllocateSliceNetwork(){
                                net.geni.aggregate.services.api.AllocateSliceNetworkResponse wrappedElement = new net.geni.aggregate.services.api.AllocateSliceNetworkResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.UpdateSliceResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.UpdateSliceResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.UpdateSliceResponse wrapUpdateSlice(){
                                net.geni.aggregate.services.api.UpdateSliceResponse wrappedElement = new net.geni.aggregate.services.api.UpdateSliceResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.StopSliceResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.StopSliceResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.StopSliceResponse wrapStopSlice(){
                                net.geni.aggregate.services.api.StopSliceResponse wrappedElement = new net.geni.aggregate.services.api.StopSliceResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.DeleteSliceNetworkResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.DeleteSliceNetworkResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.DeleteSliceNetworkResponse wrapDeleteSliceNetwork(){
                                net.geni.aggregate.services.api.DeleteSliceNetworkResponse wrappedElement = new net.geni.aggregate.services.api.DeleteSliceNetworkResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.QuerySliceResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.QuerySliceResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.QuerySliceResponse wrapQuerySlice(){
                                net.geni.aggregate.services.api.QuerySliceResponse wrappedElement = new net.geni.aggregate.services.api.QuerySliceResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.RenewSliceNetworkResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.RenewSliceNetworkResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.RenewSliceNetworkResponse wrapRenewSliceNetwork(){
                                net.geni.aggregate.services.api.RenewSliceNetworkResponse wrappedElement = new net.geni.aggregate.services.api.RenewSliceNetworkResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.GetResourceTopologyResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.GetResourceTopologyResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.GetResourceTopologyResponse wrapGetResourceTopology(){
                                net.geni.aggregate.services.api.GetResourceTopologyResponse wrappedElement = new net.geni.aggregate.services.api.GetResourceTopologyResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.CreateSliceNetworkResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.CreateSliceNetworkResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.CreateSliceNetworkResponse wrapCreateSliceNetwork(){
                                net.geni.aggregate.services.api.CreateSliceNetworkResponse wrappedElement = new net.geni.aggregate.services.api.CreateSliceNetworkResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.ListNodesResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.ListNodesResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.ListNodesResponse wrapListNodes(){
                                net.geni.aggregate.services.api.ListNodesResponse wrappedElement = new net.geni.aggregate.services.api.ListNodesResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.StartSliceResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.StartSliceResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.StartSliceResponse wrapStartSlice(){
                                net.geni.aggregate.services.api.StartSliceResponse wrappedElement = new net.geni.aggregate.services.api.StartSliceResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.CreateSliceResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.CreateSliceResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.CreateSliceResponse wrapCreateSlice(){
                                net.geni.aggregate.services.api.CreateSliceResponse wrappedElement = new net.geni.aggregate.services.api.CreateSliceResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.ListSlicesResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.ListSlicesResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.ListSlicesResponse wrapListSlices(){
                                net.geni.aggregate.services.api.ListSlicesResponse wrappedElement = new net.geni.aggregate.services.api.ListSlicesResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.DeleteSliceResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.DeleteSliceResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.DeleteSliceResponse wrapDeleteSlice(){
                                net.geni.aggregate.services.api.DeleteSliceResponse wrappedElement = new net.geni.aggregate.services.api.DeleteSliceResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.ListCapabilitiesResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.ListCapabilitiesResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.ListCapabilitiesResponse wrapListCapabilities(){
                                net.geni.aggregate.services.api.ListCapabilitiesResponse wrappedElement = new net.geni.aggregate.services.api.ListCapabilitiesResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.QuerySliceNetworkResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.QuerySliceNetworkResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.QuerySliceNetworkResponse wrapQuerySliceNetwork(){
                                net.geni.aggregate.services.api.QuerySliceNetworkResponse wrappedElement = new net.geni.aggregate.services.api.QuerySliceNetworkResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.QuerySliceVlanResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.QuerySliceVlanResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.QuerySliceVlanResponse wrapQuerySliceVlan(){
                                net.geni.aggregate.services.api.QuerySliceVlanResponse wrappedElement = new net.geni.aggregate.services.api.QuerySliceVlanResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.DeleteSliceVlanResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.DeleteSliceVlanResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.DeleteSliceVlanResponse wrapDeleteSliceVlan(){
                                net.geni.aggregate.services.api.DeleteSliceVlanResponse wrappedElement = new net.geni.aggregate.services.api.DeleteSliceVlanResponse();
                                return wrappedElement;
                         }
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.CreateSliceVlanResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.CreateSliceVlanResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }
                    
                         private net.geni.aggregate.services.api.CreateSliceVlanResponse wrapCreateSliceVlan(){
                                net.geni.aggregate.services.api.CreateSliceVlanResponse wrappedElement = new net.geni.aggregate.services.api.CreateSliceVlanResponse();
                                return wrappedElement;
                         }
                    


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {
        
                if (net.geni.aggregate.services.api.GetAllResourceInfo.class.equals(type)){
                
                           return net.geni.aggregate.services.api.GetAllResourceInfo.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.GetAllResourceInfoResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.GetAllResourceInfoResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ProvisionSliceNetwork.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ProvisionSliceNetwork.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ProvisionSliceNetworkResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ProvisionSliceNetworkResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AllocateSliceNetwork.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AllocateSliceNetwork.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AllocateSliceNetworkResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AllocateSliceNetworkResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.UpdateSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.UpdateSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.UpdateSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.UpdateSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.StopSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.StopSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.StopSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.StopSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.DeleteSliceNetwork.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSliceNetwork.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.DeleteSliceNetworkResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSliceNetworkResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.QuerySlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.QuerySlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.QuerySliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.QuerySliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.RenewSliceNetwork.class.equals(type)){
                
                           return net.geni.aggregate.services.api.RenewSliceNetwork.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.RenewSliceNetworkResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.RenewSliceNetworkResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.GetResourceTopology.class.equals(type)){
                
                           return net.geni.aggregate.services.api.GetResourceTopology.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.GetResourceTopologyResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.GetResourceTopologyResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.CreateSliceNetwork.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSliceNetwork.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.CreateSliceNetworkResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSliceNetworkResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListNodes.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListNodes.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListNodesResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListNodesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.StartSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.StartSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.StartSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.StartSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.CreateSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.CreateSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListSlices.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListSlices.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListSlicesResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListSlicesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.DeleteSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.DeleteSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListCapabilities.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListCapabilities.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListCapabilitiesResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListCapabilitiesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.QuerySliceNetwork.class.equals(type)){
                
                           return net.geni.aggregate.services.api.QuerySliceNetwork.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.QuerySliceNetworkResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.QuerySliceNetworkResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.QuerySliceVlan.class.equals(type)){
                
                           return net.geni.aggregate.services.api.QuerySliceVlan.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.QuerySliceVlanResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.QuerySliceVlanResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.DeleteSliceVlan.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSliceVlan.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.DeleteSliceVlanResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSliceVlanResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.CreateSliceVlan.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSliceVlan.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.CreateSliceVlanResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSliceVlanResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.AggregateFault.class.equals(type)){
                
                           return net.geni.aggregate.services.api.AggregateFault.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }



    

        /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
        org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
        returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
        return returnMap;
        }

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
    