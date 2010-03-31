/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.Vector;

/**
 *
 * @author Xi Yang
 */
public class AggregateP2PVlans extends Vector<AggregateP2PVlan>
{

    public synchronized boolean add(AggregateP2PVlan p2pv) {
        if(!(p2pv.getSliceName() == null)) {
            super.add(p2pv);
        }
        else
            throw new IllegalArgumentException("the P2PVlan object must be associated with a valid sliceName");
        return true;
    }

    public AggregateP2PVlan getBySliceName(String name) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getSliceName().matches(name)) {
                return get(j);
            }
         }
         return null;
     }

     public AggregateP2PVlan getByVlanTag(int vtag) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getVlanTag() == vtag) {
                return get(j);
            }
         }
         return null;
     }

     public AggregateP2PVlan getByGRI(String gri) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getGlobalReservationId().matches(gri)) {
                return get(j);
            }
         }
         return null;
     }
}
