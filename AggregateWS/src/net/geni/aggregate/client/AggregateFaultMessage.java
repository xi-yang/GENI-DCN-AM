
/**
 * AggregateFaultMessage.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

package net.geni.aggregate.client;

public class AggregateFaultMessage extends java.lang.Exception{
    
    private net.geni.aggregate.client.AggregateGENIStub.AggregateFault faultMessage;
    
    public AggregateFaultMessage() {
        super("AggregateFaultMessage");
    }
           
    public AggregateFaultMessage(java.lang.String s) {
       super(s);
    }
    
    public AggregateFaultMessage(java.lang.String s, java.lang.Throwable ex) {
      super(s, ex);
    }
    
    public void setFaultMessage(net.geni.aggregate.client.AggregateGENIStub.AggregateFault msg){
       faultMessage = msg;
    }
    
    public net.geni.aggregate.client.AggregateGENIStub.AggregateFault getFaultMessage(){
       return faultMessage;
    }
}
    