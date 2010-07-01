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
public class AggregateExternalResources {
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;

    public AggregateExternalResources() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public boolean add(AggregateExternalResource er) {
        synchronized(this) {
            if (!(er.getUrn() == null || er.getUrn().isEmpty() || er.getSubType() == null || er.getSubType().isEmpty()
                    || er.getRspecData() == null || er.getRspecData().isEmpty())) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.save(er);
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

    public boolean delete(String urn) {
        AggregateExternalResource s = this.getByUrn(urn);
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

    public boolean update(AggregateExternalResource er) {
        synchronized(this) {
            if (!(er.getUrn() == null || er.getUrn().isEmpty() || er.getSubType() == null || er.getSubType().isEmpty()
                    || er.getRspecData() == null || er.getRspecData().isEmpty())) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.update(er);
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

    public AggregateExternalResource getById(int id) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                return (AggregateExternalResource) session.get(AggregateExternalResource.class, id);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public AggregateExternalResource getByUrn(String urn) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateExternalResource as ext_resource where ext_resource.urn='" + urn + "'");
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateExternalResource) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public List<AggregateExternalResource> getAll() {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateExternalResource");
                return (List<AggregateExternalResource>) q.list();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
               if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public synchronized void pollExternalResources() {
        List<AggregateExternalResource> extResources = this.getAll();
        if (extResources == null || extResources.isEmpty())
            return;
        for (AggregateExternalResource er: extResources) {
            String urn = er.getUrn();
            HashMap hm = new HashMap();
            int ret = -1;
            if (er.getSubType().equalsIgnoreCase("ProtoGENI")) {
                ProtoGENI_APIClient apiClient = ProtoGENI_APIClient.getAPIClient();
                hm = apiClient.querySlice(urn);
                //TODO: parse hm and update er attributes ...
                apiClient.logoff();
            }
            this.update(er);
        }
    }
}
