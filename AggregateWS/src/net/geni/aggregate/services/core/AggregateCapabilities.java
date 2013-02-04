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
public class AggregateCapabilities {
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;

    public AggregateCapabilities() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateCapability c) {
        if(!((c.getName() == null) || (c.getUrn() == null) || (c.getControllerURL() == null) || (c.getDescription() == null))) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                session.save(c);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
                return false;
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        else
            throw new IllegalArgumentException("all the fields in the capability object must be specified");
        return true;
    }


    public synchronized boolean delete(String urn) {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            AggregateCapability c = this.getByUrn(urn);
            if (c == null)
                return false;
            session.delete(c);
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

    public synchronized List<AggregateCapability> getAll() {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateCapability");
            return (List<AggregateCapability>)q.list();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return null;
    }

    public synchronized AggregateCapability getById(int id) {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            return (AggregateCapability)session.get(AggregateCapability.class, id);
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return null;
    }

    public synchronized AggregateCapability getByUrn(String u) {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateCapability as cap where cap.urn='" + u + "'");
            if (q.list().size() == 0)
                return null;
            return (AggregateCapability) q.list().get(0);
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return null;
    }
}
