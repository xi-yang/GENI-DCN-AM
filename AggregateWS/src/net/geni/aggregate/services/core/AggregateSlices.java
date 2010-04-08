/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.List;
import org.hibernate.*;

/**
 *
 * @author Xi Yang
 */
public class AggregateSlices
{
    private Session session;

    public AggregateSlices() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
    }

    public synchronized boolean add(AggregateSlice s) {
        if(!((s.getSliceName() == null) || (s.getCreatorId() == 0) || (s.getUrl() == null) || (s.getDescription() == null)
               || (s.getCreatedTime() == 0) || (s.getExpiredTime() == 0) )) {
            session.save(s);
        }
        else
            throw new IllegalArgumentException("all the fields in the Slice object must be specified");
        return true;
    }

    public synchronized AggregateSlice getById(int id) {
        try {
            return (AggregateSlice)session.get(AggregateSlice.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateSlice getByName(String name) {
        try {
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from slices as slice where slice.sliceName='" + name + "'");
            return (AggregateSlice) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateSlice getByURL(String url) {
        try {
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from slices as slice where slice.url=" + url + "");
            return (AggregateSlice) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized List<AggregateSlice> getAll() {
        try {
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateSlice");
            return (List<AggregateSlice>)q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
     //createSlice
     //deleteSlice
     //updateSlice
     //querySlice
     //sliceDB insert/delete/update
}
