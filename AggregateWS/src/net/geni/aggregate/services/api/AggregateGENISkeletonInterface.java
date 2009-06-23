
/**
 * AggregateGENISkeletonInterface.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4  Built on : Apr 26, 2008 (06:24:30 EDT)
 */
    package net.geni.aggregate.services.api;
    /**
     *  AggregateGENISkeletonInterface java skeleton interface for the axisService
     */
    public interface AggregateGENISkeletonInterface {
     
         
        /**
         * Auto generated method signature
         * 
                                    * @param updateSlice
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.UpdateSliceResponse UpdateSlice
                (
                  net.geni.aggregate.services.api.UpdateSlice updateSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param querySlice
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.QuerySliceResponse QuerySlice
                (
                  net.geni.aggregate.services.api.QuerySlice querySlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param stopSlice
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.StopSliceResponse StopSlice
                (
                  net.geni.aggregate.services.api.StopSlice stopSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param listNodes
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.ListNodesResponse ListNodes
                (
                  net.geni.aggregate.services.api.ListNodes listNodes
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param startSlice
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.StartSliceResponse StartSlice
                (
                  net.geni.aggregate.services.api.StartSlice startSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param createSlice
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.CreateSliceResponse CreateSlice
                (
                  net.geni.aggregate.services.api.CreateSlice createSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param deleteSlice
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.DeleteSliceResponse DeleteSlice
                (
                  net.geni.aggregate.services.api.DeleteSlice deleteSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param listCapabilities
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.ListCapabilitiesResponse ListCapabilities
                (
                  net.geni.aggregate.services.api.ListCapabilities listCapabilities
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         * 
                                    * @param resetSlice
             * @throws AggregateFaultMessage : 
         */

        
                public net.geni.aggregate.services.api.ResetSliceResponse ResetSlice
                (
                  net.geni.aggregate.services.api.ResetSlice resetSlice
                 )
            throws AggregateFaultMessage;
        
         }
    