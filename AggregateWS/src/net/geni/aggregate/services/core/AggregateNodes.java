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
 * @author jflidr, Xi Yang
 */
public class AggregateNodes {
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;
    private List<AggregateNode> cachedNodes = null;

    public AggregateNodes() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateNode n) {
        synchronized(this) {
            if (!((n.getUrn() == null) || (n.getDescription() == null))) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.save(n);
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                    if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("all the fields in the capability object must be specified");
            }
        }
        return true;
    }

    public synchronized boolean update(AggregateNode n) {
        synchronized(this) {
            if (!((n.getUrn() == null) || (n.getDescription() == null))) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.update(n);
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                    if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("all the fields in the capability object must be specified");
            }
        }
        return true;
    }

    public synchronized boolean delete(AggregateNode n) {
        synchronized(this) {
            if (n == null) {
                return false;
            }
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                session.delete(n);
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

    public synchronized List<AggregateNode> getAll() {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateNode");
                return (List<AggregateNode>) q.list();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                } finally {
                    if (session.isOpen()) session.close();
                }
        }
        return null;
    }

    /**
     * get by capacity filter strings
     */
    public synchronized List<AggregateNode> getByCaps(Vector<String> caps) {
        if (caps.size() == 0) {
            return null;
        }
        //form filter
        List<AggregateNode> allnodes = getAll();
        List<AggregateNode> nodes = new ArrayList();
        for (AggregateNode node : allnodes) {
            if (node.hasAllCaps(caps)) {
                nodes.add(node);
            }
        }
        if (nodes.size() == 0) {
            return null;
        }
        return nodes;
    }

    /**
     * get by ID
     */
    public synchronized AggregateNode getByNodeId(int nid) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateNode as node where node.nodeId=" + Integer.toString(nid));
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateNode) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    /**
     * get by urn
     */
    public synchronized AggregateNode getByUrn(String urn) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateNode as node where node.urn='" + urn + "'");
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateNode) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public String getNodeIds(String[] nodes) {
        if (cachedNodes == null) {
            cachedNodes = this.getAll();
        }
        String ret = "";
        if (cachedNodes == null) {
            return ret;
        }
        for (String node : nodes) {
            AggregateNode aggrNode = null;
            if (node.matches("urn:.+")) { //url
                for (AggregateNode n : cachedNodes) {
                    if (n.getUrn().equalsIgnoreCase(node)) {
                        aggrNode = n;
                        break;
                    }
                }
            } else if (node.matches("\\d")) { //id
                for (AggregateNode n : cachedNodes) {
                    if (n.getId() == Integer.valueOf(node)) {
                        aggrNode = n;
                        break;
                    }
                }
            } else if (node.matches("[a-zA-Z0-9\\.\\s]+")) {
                for (AggregateNode n : cachedNodes) {
                    if (n.getDescription().equalsIgnoreCase(node)) {
                        aggrNode = n;
                        break;
                    }
                }
            }
            if (aggrNode != null) {
                ret = ret + ", " + Integer.toString(aggrNode.getNodeId());
            }
        }
        if (!ret.isEmpty()) {
            ret = ret.substring(2);
        }
        return ret;
    }
}
