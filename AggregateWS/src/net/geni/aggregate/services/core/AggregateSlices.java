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
public class AggregateSlices extends Vector<AggregateSlice>
{

    public synchronized boolean add(AggregateSlice s) {
        if(!((s.getSliceName() == null) || (s.getCreatorId() == 0) || (s.getURL() == null) || (s.getDescription() == null)
               || (s.getCreatedTime() == 0) || (s.getExpiredTime() == 0) )) {
            super.add(s);
        }
        else
            throw new IllegalArgumentException("all the fields in the Slice object must be specified");
        return true;
    }

    public AggregateSlice getByName(String name) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getSliceName().matches(name)) {
                return get(j);
            }
         }
         return null;
     }

     public AggregateSlice getByURL(String url) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getURL().matches(url)) {
                return get(j);
            }
         }
         return null;
     }

     //createSlice
     //deleteSlice
     //updateSlice
     //querySlice
     //sliceDB insert/delete/update
}
