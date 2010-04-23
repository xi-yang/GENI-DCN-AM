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
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;
    private List<AggregateUser> cachedUsers = null;

    public AggregateUsers() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public boolean add(AggregateUser u) {
        synchronized(this) {
            if (!(u.getName() == null || u.getLastName() == null || u.getFirstName() == null || u.getEmail() == null || u.getId() == 0)) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.save(u);
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return false;
                } finally {
                    if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("all the fields in the User object must be specified");
            }
        }
        return true;
    }

    public boolean delete(String name) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                AggregateUser s = this.getByName(name);
                if (s == null) {
                    return false;
                }
                session.delete(s);
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

    public AggregateUser getById(int id) {
        synchronized(this) {
            try {
                this.session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                return (AggregateUser) session.get(AggregateUser.class, id);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public synchronized List<AggregateUser> getAll() {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateUser");
                if (q.list().size() == 0) {
                    return null;
                }
                return (List<AggregateUser>) q.list();
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public synchronized AggregateUser getByName(String name) {
        synchronized(this) {
            try {
                this.session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateUser as user where user.name='" + name + "'");
                if (q.list().size() == 0) {
                    return null;
                }
                return (AggregateUser) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public synchronized AggregateUser getByEmail(String email) {
        synchronized(this) {
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                Query q = session.createQuery("from AggregateUser as user where user.email='" + email + "'");
                if (q.list().size() == 0)
                    return null;
                return (AggregateUser) q.list().get(0);
            } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
            } finally {
                if (session.isOpen()) session.close();
            }
        }
        return null;
    }

    public String getUserIds(String[] users) {
        if (cachedUsers == null)
            cachedUsers = this.getAll();
        String ret = "";
        if (cachedUsers == null)
            return ret;
        for (String user: users) {
            AggregateUser aggrUser = null;
            if (user.matches(".+@.+")) { //email
                for (AggregateUser u: cachedUsers) {
                    if (u.getEmail().equalsIgnoreCase(user)) {
                        aggrUser = u;
                        break;
                    }
                }
            } else if (user.matches("\\d")) { //ID
                for (AggregateUser u: cachedUsers) {
                    if (u.getId() == Integer.valueOf(user)) {
                        aggrUser = u;
                        break;
                    }
                }
            } else if (user.matches("[a-zA-Z]+")) { //name
                for (AggregateUser u: cachedUsers) {
                    if (u.getEmail().equalsIgnoreCase(user)) {
                        aggrUser = u;
                        break;
                    }
                }
            }
            if (aggrUser != null)
                ret = ret + ", " + Integer.toString(aggrUser.getId());
        }
        if (!ret.isEmpty())
            ret = ret.substring(2);
        return ret;
    }
}
