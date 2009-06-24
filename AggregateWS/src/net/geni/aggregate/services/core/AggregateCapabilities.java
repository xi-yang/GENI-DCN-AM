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
        if(!((c.getName() == null) || (c.getControllerURL() == null) || (c.getDescription() == null) || (c.getId() == null))) {
            super.add(c);
        }
        else
            throw new IllegalArgumentException("all the fields in the capability object must be specified");
        return true;
    }
}
