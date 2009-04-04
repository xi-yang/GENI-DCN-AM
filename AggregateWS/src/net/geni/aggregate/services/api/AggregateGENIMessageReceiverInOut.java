
/**
 * AggregateGENIMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
        package net.geni.aggregate.services.api;

        /**
        *  AggregateGENIMessageReceiverInOut message receiver
        */

        public class AggregateGENIMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver{


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

        

            if("ListCapabilities".equals(methodName)){
                
                net.geni.aggregate.services.api.ListCapabilitiesResponse listCapabilitiesResponse37 = null;
	                        net.geni.aggregate.services.api.ListCapabilities wrappedParam =
                                                             (net.geni.aggregate.services.api.ListCapabilities)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.ListCapabilities.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               listCapabilitiesResponse37 =
                                                   
                                                   
                                                         skel.ListCapabilities(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), listCapabilitiesResponse37, false);
                                    } else 

            if("ListNodes".equals(methodName)){
                
                net.geni.aggregate.services.api.ListNodesResponse listNodesResponse39 = null;
	                        net.geni.aggregate.services.api.ListNodes wrappedParam =
                                                             (net.geni.aggregate.services.api.ListNodes)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.ListNodes.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               listNodesResponse39 =
                                                   
                                                   
                                                         skel.ListNodes(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), listNodesResponse39, false);
                                    } else 

            if("CreateSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.CreateSliceResponse createSliceResponse41 = null;
	                        net.geni.aggregate.services.api.CreateSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.CreateSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.CreateSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               createSliceResponse41 =
                                                   
                                                   
                                                         skel.CreateSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), createSliceResponse41, false);
                                    } else 

            if("DeleteSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.DeleteSliceResponse deleteSliceResponse43 = null;
	                        net.geni.aggregate.services.api.DeleteSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.DeleteSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.DeleteSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               deleteSliceResponse43 =
                                                   
                                                   
                                                         skel.DeleteSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), deleteSliceResponse43, false);
                                    } else 

            if("UpdateSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.UpdateSliceResponse updateSliceResponse45 = null;
	                        net.geni.aggregate.services.api.UpdateSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.UpdateSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.UpdateSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               updateSliceResponse45 =
                                                   
                                                   
                                                         skel.UpdateSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), updateSliceResponse45, false);
                                    } else 

            if("StartSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.StartSliceResponse startSliceResponse47 = null;
	                        net.geni.aggregate.services.api.StartSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.StartSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.StartSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               startSliceResponse47 =
                                                   
                                                   
                                                         skel.StartSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), startSliceResponse47, false);
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

            if("ResetSlice".equals(methodName)){
                
                net.geni.aggregate.services.api.ResetSliceResponse resetSliceResponse51 = null;
	                        net.geni.aggregate.services.api.ResetSlice wrappedParam =
                                                             (net.geni.aggregate.services.api.ResetSlice)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    net.geni.aggregate.services.api.ResetSlice.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));
                                                
                                               resetSliceResponse51 =
                                                   
                                                   
                                                         skel.ResetSlice(wrappedParam)
                                                    ;
                                            
                                        envelope = toEnvelope(getSOAPFactory(msgContext), resetSliceResponse51, false);
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
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.AggregateFault param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.AggregateFault.MY_QNAME,
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
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ResetSlice param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ResetSlice.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(net.geni.aggregate.services.api.ResetSliceResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {

            
                        try{
                             return param.getOMElement(net.geni.aggregate.services.api.ResetSliceResponse.MY_QNAME,
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
                    
                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, net.geni.aggregate.services.api.ResetSliceResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                           
                                    emptyEnvelope.getBody().addChild(param.getOMElement(net.geni.aggregate.services.api.ResetSliceResponse.MY_QNAME,factory));
                                

                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
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
        
                if (net.geni.aggregate.services.api.CreateSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.CreateSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.CreateSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

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
           
                if (net.geni.aggregate.services.api.ResetSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ResetSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ResetSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ResetSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

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
           
                if (net.geni.aggregate.services.api.ListNodes.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListNodes.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListNodesResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListNodesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

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
           
                if (net.geni.aggregate.services.api.ListCapabilities.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListCapabilities.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.ListCapabilitiesResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.ListCapabilitiesResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

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
           
                if (net.geni.aggregate.services.api.DeleteSlice.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSlice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (net.geni.aggregate.services.api.DeleteSliceResponse.class.equals(type)){
                
                           return net.geni.aggregate.services.api.DeleteSliceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

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
    