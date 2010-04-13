/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;
import org.hibernate.*;
import org.apache.log4j.*;

/**
 *
 * @author Xi Yang
 */
public class AggregateSlices {
    private Session session;
    private org.apache.log4j.Logger log;

    public AggregateSlices() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        log = Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateSlice s) {
        if(!((s.getSliceName() == null) || (s.getCreatorId() == 0) || (s.getUrl() == null) || (s.getDescription() == null)
               || (s.getCreatedTime() == 0) || (s.getExpiredTime() == 0) )) {
            try {
                if (!session.isOpen())
                    this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                org.hibernate.Transaction tx = session.beginTransaction();
                session.save(s);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else
            throw new IllegalArgumentException("all the fields in the Slice object must be specified");
        return true;
    }

    public synchronized boolean delete(String name) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            AggregateSlice s = this.getByName(name);
            if (s == null)
                return false;
            session.delete(s);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized boolean update(AggregateSlice s) {
        if(!((s.getSliceName() == null) || (s.getCreatorId() == 0) || (s.getUrl() == null) || (s.getDescription() == null)
               || (s.getCreatedTime() == 0) || (s.getExpiredTime() == 0) )) {
            try {
                if (!session.isOpen())
                    this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                org.hibernate.Transaction tx = session.beginTransaction();
                session.update(s);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else
            throw new IllegalArgumentException("all the fields in the Slice object must be specified");
        return true;
    }

    public synchronized AggregateSlice getById(int id) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            return (AggregateSlice)session.get(AggregateSlice.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateSlice getByName(String name) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateSlice as slice where slice.sliceName='" + name + "'");
            if (q.list().size() == 0)
                return null;
            return (AggregateSlice) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateSlice getByURL(String url) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateSlice as slice where slice.url=" + url + "");
            if (q.list().size() == 0)
                return null;
            return (AggregateSlice) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized List<AggregateSlice> getAll() {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateSlice");
            return (List<AggregateSlice>)q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized int createSlice(String sliceName, String url, String description, String user, String[] nodes) {
        //create slice wit PLC
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        int ret = plcClient.createSlice(sliceName, url, description, user, nodes);

        
        if (ret == 1) { //slice successfully craeted with PLC
            String[] names = new String[1];
            names[0] = sliceName;
            Vector<HashMap> hmV = new Vector<HashMap>();
            //TODO: plcClient.createSlice should return slice data
            ret = plcClient.querySlice(names, hmV); 
            if (ret == 1) {
                int slice_id = Integer.parseInt((String)hmV.get(0).get("slice_id"));
                int creator_person_id = Integer.parseInt((String)hmV.get(0).get("creator_person_id"));
                long created = Integer.parseInt((String)hmV.get(0).get("created"));
                long expires = Integer.parseInt((String)hmV.get(0).get("expires"));
                AggregateSlice slice = new AggregateSlice(sliceName, slice_id, url,
                        description, user, AggregateUtils.makeArrayString(nodes), creator_person_id, created, expires);
                this.add(slice);
            }
        }
        return ret;
    }

    public synchronized int deleteSlice(String sliceName) {
        //create slice wit PLC
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        int ret = plcClient.deleteSlice(sliceName);
        this.delete(sliceName);
        return ret;
    }
    
    public synchronized int updateSlice(String sliceName, String url, String descr, int expires, String[] users, String[] nodes) {
        int ret = -1;
        AggregateSlice slice = this.getByName(sliceName);
        if (slice == null) {
            log.error("Unkonwn sliceName '"+sliceName+"' in Aggregate DB");
            return ret;
        }

        //update slice wit PLC
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        ret = plcClient.updateSlice(sliceName, url, descr, expires, users, nodes);

        if (ret == 1) { //slice successfully updateded with PLC
            //update slice in aggregate DB
            slice.setUrl(url);
            slice.setDescription(descr);
            slice.setExpiredTime(expires);
            slice.setUsers(AggregateUtils.makeArrayString(users));
            slice.setNodes(AggregateUtils.makeArrayString(nodes));
            this.update(slice);
        } else {
            log.error("Failed to update slice '"+sliceName+"' with the PLC");
        }
        return ret;
    }
}
