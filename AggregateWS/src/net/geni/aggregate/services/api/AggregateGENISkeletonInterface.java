
/**
 * AggregateGENISkeletonInterface.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
    package net.geni.aggregate.services.api;
    /**
     *  AggregateGENISkeletonInterface java skeleton interface for the axisService
     */
    public interface AggregateGENISkeletonInterface {
     
         
        /**
         * Auto generated method signature
         
                                    * @param listCapabilities
         */

        
                public net.geni.aggregate.services.api.ListCapabilitiesResponse ListCapabilities
                (
                  net.geni.aggregate.services.api.ListCapabilities listCapabilities
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param listNodes
         */

        
                public net.geni.aggregate.services.api.ListNodesResponse ListNodes
                (
                  net.geni.aggregate.services.api.ListNodes listNodes
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param createSlice
         */

        
                public net.geni.aggregate.services.api.CreateSliceResponse CreateSlice
                (
                  net.geni.aggregate.services.api.CreateSlice createSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param deleteSlice
         */

        
                public net.geni.aggregate.services.api.DeleteSliceResponse DeleteSlice
                (
                  net.geni.aggregate.services.api.DeleteSlice deleteSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param updateSlice
         */

        
                public net.geni.aggregate.services.api.UpdateSliceResponse UpdateSlice
                (
                  net.geni.aggregate.services.api.UpdateSlice updateSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param startSlice
         */

        
                public net.geni.aggregate.services.api.StartSliceResponse StartSlice
                (
                  net.geni.aggregate.services.api.StartSlice startSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param stopSlice
         */

        
                public net.geni.aggregate.services.api.StopSliceResponse StopSlice
                (
                  net.geni.aggregate.services.api.StopSlice stopSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param resetSlice
         */

        
                public net.geni.aggregate.services.api.ResetSliceResponse ResetSlice
                (
                  net.geni.aggregate.services.api.ResetSlice resetSlice
                 )
            throws AggregateFaultMessage;
        
         
        /**
         * Auto generated method signature
         
                                    * @param querySlice
         */

        
                public net.geni.aggregate.services.api.QuerySliceResponse QuerySlice
                (
                  net.geni.aggregate.services.api.QuerySlice querySlice
                 )
            throws AggregateFaultMessage;
        
         }
    