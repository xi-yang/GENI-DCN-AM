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
public class AggregateNetworkInterfaces {
    private Session session;
    private org.apache.log4j.Logger log;
    private List<AggregateNetworkInterface> cachedNetworkInterfaces = null;

    public AggregateNetworkInterfaces() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateNetworkInterface n) {
        if(!((n.getDeviceName() == null || n.getCapacity() == null))) {
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
            throw new IllegalArgumentException("deviceName and capacity  must be specified in the node object");
        }
        return true;
    }

    public synchronized boolean update(AggregateNetworkInterface n) {
        if(!((n.getDeviceName() == null || n.getCapacity() == null))) {
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
            throw new IllegalArgumentException("deviceName and capacity  must be specified in the node object");
        }
        return true;
    }

    public synchronized boolean delete(AggregateNetworkInterface n) {
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

    public synchronized List<AggregateNetworkInterface> getAll() {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateNetworkInterface");
            return (List<AggregateNetworkInterface>)q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get by urn id
     */
    public synchronized AggregateNetworkInterface getByUrn(int urn) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateNetworkInterface as interface where interface.urn=" + urn);
            if (q.list().size() == 0)
                return null;
            return (AggregateNetworkInterface) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
