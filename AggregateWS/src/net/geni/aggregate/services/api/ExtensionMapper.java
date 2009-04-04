
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:58 LKT)
 */

            package net.geni.aggregate.services.api;
            /**
            *  ExtensionMapper class
            */
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "CapabilityType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.CapabilityType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "CreateSliceType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.CreateSliceType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "CreateSliceResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.CreateSliceResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "UpdateSliceType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.UpdateSliceType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "StopSliceType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.StopSliceType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "DeleteSliceType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.DeleteSliceType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "ResetSliceResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.ResetSliceResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "ListCapabilitiesType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.ListCapabilitiesType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "StartSliceResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.StartSliceResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "DescriptorType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.DescriptorType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "QuerySliceType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.QuerySliceType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "ListCapabilitiesResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.ListCapabilitiesResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "ListNodesResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.ListNodesResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "DeleteSliceResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.DeleteSliceResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "UpdateSliceResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.UpdateSliceResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "ResetSliceType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.ResetSliceType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "ListNodesType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.ListNodesType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "StopSliceResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.StopSliceResponseType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "StartSliceType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.StartSliceType.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://aggregate.geni.net/services/api/".equals(namespaceURI) &&
                  "QuerySliceResponseType".equals(typeName)){
                   
                            return  net.geni.aggregate.services.api.QuerySliceResponseType.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    