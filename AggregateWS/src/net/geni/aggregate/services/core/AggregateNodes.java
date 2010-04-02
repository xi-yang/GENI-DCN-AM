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
public class AggregateNodes extends Vector<AggregateNode>
{

    @Override
    public synchronized boolean add(AggregateNode n) {
        if(!((n.getUrn() == null) || (n.getDescription() == null))) {
            super.add(n);
        } else {
            throw new IllegalArgumentException("all the fields in the capability object must be specified");
        }
        return true;
    }

    /**
     * get by capacity filter strings
     */
    public Vector<AggregateNode> get(Vector<String> u) {
        Vector<AggregateNode> ret = new Vector<AggregateNode>();
        if(u.size() == 0) {
            return this;
        }
        for(int i = 0; i < size(); i++) {
            AggregateNode n = get(i);
            if(n.hasAll(u)) {
                ret.add(n);
            }
        }
        return ret;
    }

    /**
     * get by urn
     */
    public AggregateNode getById(String urn) {
        for(int i = 0; i < size(); i++) {
            AggregateNode n = get(i);
            if(n.getUrn().matches(urn)) {
                return n;
            }
        }
        return null;
    }

    /**
     * get by id
     */
    public AggregateNode getById(int id) {
        for(int i = 0; i < size(); i++) {
            AggregateNode n = get(i);
            if(n.getId() == id) {
                return n;
            }
        }
        return null;
    }

}
