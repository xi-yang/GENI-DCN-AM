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
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;

    public AggregateSlices() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public boolean add(AggregateSlice s) {
        synchronized(this) {
            if (!((s.getSliceName() == null) || (s.getCreatorId() == 0) || (s.getUrl() == null) || (s.getDescription() == null) || (s.getCreatedTime() == 0) || (s.getExpiredTime() == 0))) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.save(s);
                    session.flush();
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                   if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("all the fields in the Slice object must be specified");
            }
        }
        return true;
    }

    public boolean delete(String name) {
        AggregateSlice s = this.getByName(name);
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                if (s == null) {
                    return false;
                }
                session.delete(s);
                session.flush();
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                return false;
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return true;
    }

    public boolean update(AggregateSlice s) {
        synchronized(this) {
            if (!((s.getSliceName() == null) || (s.getCreatorId() == 0) || (s.getUrl() == null) || (s.getDescription() == null) || (s.getCreatedTime() == 0) || (s.getExpiredTime() == 0))) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.update(s);
                    session.flush();
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                   if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("all the fields in the Slice object must be specified");
            }
        }
        return true;
    }

    public AggregateSlice getById(int id) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                return (AggregateSlice) session.get(AggregateSlice.class, id);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public AggregateSlice getByName(String name) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateSlice as slice where slice.sliceName='" + name + "'");
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateSlice) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public AggregateSlice getByURL(String url) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateSlice as slice where slice.url='" + url + "'");
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateSlice) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public List<AggregateSlice> getAll() {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateSlice");
                return (List<AggregateSlice>) q.list();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public synchronized AggregateSlice createSlice(String sliceName, String url,
            String description, String user, String[] nodes, boolean isAddPlcSlice) {
        String[] users = {user};
        String userId = AggregateState.getAggregateUsers().getUserIds(users);
        String nodeIds = AggregateState.getAggregateNodes().getNodeIds(nodes);
        //create slice wit PLC
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        int ret = plcClient.createSlice(sliceName, url, description, userId, nodeIds, isAddPlcSlice);
        AggregateSlice slice = null;

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
                slice = new AggregateSlice(sliceName, slice_id, url,
                        description, userId, nodeIds, creator_person_id, created, expires);
                this.add(slice);
            }
        }
        plcClient.logoff();
        return slice;
    }

    public synchronized int deleteSlice(String sliceName) {
        //create slice wit PLC
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        int ret = plcClient.deleteSlice(sliceName);
        this.delete(sliceName);
        plcClient.logoff();
        return ret;
    }
    
    public synchronized int updateSlice(String sliceName, String url, String descr, int expires, String[] users, String[] nodes) {
        int ret = -1;
        AggregateSlice slice = this.getByName(sliceName);
        if (slice == null) {
            log.error("Unkonwn sliceName '"+sliceName+"' in Aggregate DB");
            return ret;
        }

        String userIds = AggregateState.getAggregateUsers().getUserIds(users);
        String nodeIds = AggregateState.getAggregateNodes().getNodeIds(nodes);

        //update slice wit PLC
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        ret = plcClient.updateSlice(sliceName, url, descr, expires, userIds, nodeIds);

        if (ret == 1) { //slice successfully updateded with PLC
            //update slice in aggregate DB
            slice.setUrl(url);
            slice.setDescription(descr);
            slice.setExpiredTime(expires);
            slice.setUsers(userIds);
            slice.setNodes(nodeIds);
            this.update(slice);
        } else {
            log.error("Failed to update slice '"+sliceName+"' with the PLC");
        }
        plcClient.logoff();
        return ret;
    }

    public synchronized void pollSlices() {
        List<AggregateSlice> slices = this.getAll();
        if (slices == null || slices.isEmpty())
            return;
        String[] names = new String[1];
        Vector<HashMap> hmV = new Vector<HashMap>();
        AggregatePLC_APIClient plcClient = AggregatePLC_APIClient.getPLCClient();
        for (AggregateSlice slice: slices) {
            names[0] = slice.getSliceName();
            int ret = plcClient.querySlice(names, hmV);
            if (ret == 1) {
                long expires = Integer.parseInt((String)hmV.get(0).get("expires"));
                String users = (String)hmV.get(0).get("person_ids");
                users = users.replaceAll("[\\[\\]]", "");
                //convert id to user name/email
                String nodes = (String)hmV.get(0).get("node_ids");
                nodes = nodes.replaceAll("[\\[\\]]", "");
                //convert id to node name
                String descr = (String)hmV.get(0).get("description");
                //translate users and nodes
                slice.setExpiredTime(expires);
                slice.setUsers(users);
                slice.setNodes(nodes);
                slice.setDescription(descr);
                slice.setStatus("active");
            } else {
                slice.setStatus("unknown");
            }
            this.update(slice);
        }
        plcClient.logoff();
    }
}
