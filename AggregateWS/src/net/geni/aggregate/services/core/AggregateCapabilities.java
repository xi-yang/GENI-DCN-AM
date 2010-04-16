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
    private Session session;
    private org.apache.log4j.Logger log;

    public AggregateCapabilities() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateCapability c) {
        if(!((c.getName() == null) || (c.getUrn() == null) || (c.getControllerURL() == null) || (c.getDescription() == null))) {
            try {
                if (!session.isOpen())
                    this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                org.hibernate.Transaction tx = session.beginTransaction();
                session.save(c);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else
            throw new IllegalArgumentException("all the fields in the capability object must be specified");
        return true;
    }


    public synchronized boolean delete(String urn) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            AggregateCapability c = this.getByUrn(urn);
            if (c == null)
                return false;
            session.delete(c);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized List<AggregateCapability> getAll() {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateCapability");
            return (List<AggregateCapability>)q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateCapability getById(int id) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            return (AggregateCapability)session.get(AggregateCapability.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateCapability getByUrn(String u) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateCapability as cap where cap.urn='" + u + "'");
            if (q.list().size() == 0)
                return null;
            return (AggregateCapability) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
