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
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;
    private List<AggregateNetworkInterface> cachedNetworkInterfaces = null;

    public AggregateNetworkInterfaces() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateNetworkInterface n) {
        if(!((n.getDeviceName() == null || n.getCapacity() == null))) {
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
            throw new IllegalArgumentException("deviceName and capacity  must be specified in the node object");
        }
        return true;
    }

    public synchronized boolean update(AggregateNetworkInterface n) {
        if(!((n.getDeviceName() == null || n.getCapacity() == null))) {
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
            throw new IllegalArgumentException("deviceName and capacity  must be specified in the node object");
        }
        return true;
    }

    public synchronized boolean delete(AggregateNetworkInterface n) {
        if(n == null)
            return false;
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
        return true;
    }

    public synchronized List<AggregateNetworkInterface> getAll() {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateNetworkInterface");
            return (List<AggregateNetworkInterface>)q.list();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return null;
    }

    /**
     * get by urn id
     */
    public synchronized AggregateNetworkInterface getByUrn(int urn) {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateNetworkInterface as interface where interface.urn=" + urn);
            if (q.list().size() == 0)
                return null;
            return (AggregateNetworkInterface) q.list().get(0);
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return null;
    }
}
