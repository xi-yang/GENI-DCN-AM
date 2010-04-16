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
 * @author jflidr
 */
public class AggregateNodes {
    private Session session;
    private org.apache.log4j.Logger log;

    public AggregateNodes() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateNode n) {
        if(!((n.getUrn() == null) || (n.getDescription() == null))) {
            try {
                if (!session.isOpen())
                    this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                org.hibernate.Transaction tx = session.beginTransaction();
                session.save(n);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            throw new IllegalArgumentException("all the fields in the capability object must be specified");
        }
        return true;
    }

    public synchronized boolean update(AggregateNode n) {
        if(!((n.getUrn() == null) || (n.getDescription() == null))) {
            try {
                if (!session.isOpen())
                    this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                org.hibernate.Transaction tx = session.beginTransaction();
                session.update(n);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            throw new IllegalArgumentException("all the fields in the capability object must be specified");
        }
        return true;
    }

    public synchronized boolean delete(AggregateNode n) {
        if(n == null)
            return false;
        try {
            if (!session.isOpen()) {
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            }
            org.hibernate.Transaction tx = session.beginTransaction();
            session.delete(n);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized List<AggregateNode> getAll() {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateNode");
            return (List<AggregateNode>)q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get by capacity filter strings
     */
    public synchronized List<AggregateNode> getByCaps(Vector<String> caps) {
        if(caps.size() == 0) {
            return null;
        }
        //form filter
        List<AggregateNode> allnodes = getAll();
        List<AggregateNode> nodes = new ArrayList();
        for (AggregateNode node: allnodes) {
            if (node.hasAllCaps(caps))
                nodes.add(node);
        }
        if (nodes.size() == 0)
            return null;
        return nodes;
    }

    /**
     * get by urn
     */
    public synchronized AggregateNode getByUrn(String urn) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateNode as node where node.urn='" + urn + "'");
            if (q.list().size() == 0)
                return null;
            return (AggregateNode) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
