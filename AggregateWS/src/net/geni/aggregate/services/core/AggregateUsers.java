/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.Vector;
/**
 *
 * @author root
 */
public class AggregateUsers extends Vector<AggregateUser>{

    public synchronized boolean add(AggregateUser u) {
        if(!(u.getName() == null || u.getLastName() == null|| u.getFirstName() == null || u.getEmail() == null || u.getId() == 0) ) {
            super.add(u);
        }
        else
            throw new IllegalArgumentException("all the fields in the User object must be specified");
        return true;
    }

     public AggregateUser getById(int id) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getId() == id) {
                return get(j);
            }
         }
         return null;
     }

    public AggregateUser getByName(String name) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getName().matches(name)) {
                return get(j);
            }
         }
         return null;
     }

     public AggregateUser getByEmail(String email) {
         for(int j = 0; j < size(); j++) {
            if(get(j).getEmail().matches(email)) {
                return get(j);
            }
         }
         return null;
     }
}
