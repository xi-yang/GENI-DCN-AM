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
public class AggregateSdxSlivers {
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;

    public AggregateSdxSlivers() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public boolean add(AggregateSdxSliver sliver) {
        synchronized(this) {
            if (!(sliver.getSliceName().isEmpty() || sliver.getRequestJson().isEmpty())) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.save(sliver);
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
                throw new IllegalArgumentException("all the fields in the AggregateSdxSliver object must be specified");
            }
        }
        return true;
    }

    public synchronized boolean delete(AggregateSdxSliver sliver) {
        synchronized(this) {
            if (sliver == null) {
                return false;
            }
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                session.delete(sliver);
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

    public boolean delete(String urn) {
        AggregateSdxSliver s = this.getBySliceName(urn);
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

    public boolean update(AggregateSdxSliver sliver) {
        synchronized(this) {
            if (!(sliver.getSliceName().isEmpty() || sliver.getRequestJson().isEmpty())) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.update(sliver);
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
                throw new IllegalArgumentException("all the fields in the ExternalResource object must be specified");
            }
        }
        return true;
    }

    public AggregateSdxSliver getById(int id) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                return (AggregateSdxSliver) session.get(AggregateSdxSliver.class, id);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public AggregateSdxSliver getBySliceName(String urn) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateSdxSliver as sliver where sliver.sliceName='" + urn + "'");
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateSdxSliver) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public List<AggregateSdxSliver> getAll() {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateSdxSliver");
                return (List<AggregateSdxSliver>) q.list();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public synchronized void pollSdxSlivers() {
        List<AggregateSdxSliver> allSlivers = this.getAll();
        if (allSlivers == null || allSlivers.isEmpty())
            return;
        for (AggregateSdxSliver sliver: allSlivers) {
            String sliceName = sliver.getSliceName();
            String status;
            //@TODO: poll status through VersaStack Client
            this.update(sliver);
        }
    }
}
