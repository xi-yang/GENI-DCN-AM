/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.Vector;

/**
 *
 * @author jflidr
 */
public class AggregateCapabilities extends Vector<AggregateCapability>
{

    @Override
    public synchronized boolean add(AggregateCapability c) {
        if(!((c.getName() == null) || (c.getUrn() == null) || (c.getControllerURL() == null) || (c.getDescription() == null))) {
            super.add(c);
        }
        else
            throw new IllegalArgumentException("all the fields in the capability object must be specified");
        return true;
    }

    public AggregateCapability getCap(String u) {
        for(int i = 0; i < size(); i++) {
            AggregateCapability c = get(i);
            if(c.getUrn().matches(u)) {
                return c;
            }
        }
        return null;
    }
     public String getUrn(int i) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getId()==i) {
                return get(j).getUrn();
            }
         }
         return null;
     }
}
