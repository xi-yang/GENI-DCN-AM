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
 * @author root
 */
public class AggregateUsers {
    private Session session;
    private org.apache.log4j.Logger log;

    public AggregateUsers() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean add(AggregateUser u) {
        if(!(u.getName() == null || u.getLastName() == null|| u.getFirstName() == null || u.getEmail() == null || u.getId() == 0) ) {
            try {
                if (!session.isOpen())
                    this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                org.hibernate.Transaction tx = session.beginTransaction();
                session.save(u);
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else
            throw new IllegalArgumentException("all the fields in the User object must be specified");
        return true;
    }

    public synchronized boolean delete(String name) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            AggregateUser s = this.getByName(name);
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

    public synchronized AggregateUser getById(int id) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            return (AggregateUser)session.get(AggregateUser.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateUser getByName(String name) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateUser as user where user.name='" + name + "'");
            if (q.list().size() == 0)
                return null;
            return (AggregateUser) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized AggregateUser getByEmail(String email) {
        try {
            if (!session.isOpen())
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateUser as user where user.email='" + email + "'");
            if (q.list().size() == 0)
                return null;
            return (AggregateUser) q.list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
