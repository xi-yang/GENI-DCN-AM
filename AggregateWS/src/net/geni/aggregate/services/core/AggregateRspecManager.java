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
public class AggregateRspecManager extends Thread{
    private volatile boolean goRun = true;
    private volatile Vector<AggregateRspec> aggrRspecs;
    private volatile Vector<AggregateRspecRunner> rspecThreads;
    private Session session;
    private org.apache.log4j.Logger log;

    public AggregateRspecManager() {
        super();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        aggrRspecs = new Vector<AggregateRspec>();
        rspecThreads = new Vector<AggregateRspecRunner>();
    }

    public Vector<AggregateRspec> getAggrRspecs() {
        return aggrRspecs;
    }

    public void setAggrRspecs(Vector<AggregateRspec> aggrRspecs) {
        this.aggrRspecs = aggrRspecs;
    }

    public boolean isGoRun() {
        return goRun;
    }

    public void setGoRun(boolean goRun) {
        this.goRun = goRun;
    }

    public void run() {
        while (goRun) {
            //polling rspecThreads and rspecRspecs for status change
            synchronized(rspecThreads) {
                for (AggregateRspecRunner rspecThread: rspecThreads) {
                    AggregateRspec rspec = rspecThread.getRspec();
                    if (rspec.getStatus().equalsIgnoreCase("working")) {
                        //rspec in working state, increased to 15 minutes poll
                        if (rspecThread.getPollInterval() < 900000)
                            rspecThread.setPollInterval(900000);
                    } else if (rspec.getStatus().equalsIgnoreCase("terminated")
                      || rspec.getStatus().equalsIgnoreCase("rollbacked")) {
                        //rollbacked thread may get diff. treatment?
                        rspecThreads.remove(rspecThread);
                        aggrRspecs.remove(rspec);
                        try {
                            if (!session.isOpen()) {
                                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                            }
                            org.hibernate.Transaction tx = session.beginTransaction();
                            session.delete(rspec);
                            session.flush();
                            tx.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //delete rspec resources to DB
                        for (AggregateResource rc : rspec.getResources()) {
                            try {
                                rc.exitResouceTable();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;  // go loop again!
                    }
                    //give other instructions e.g., terminate on expires
                }
            }
        }
    }

    public synchronized String createRspec(String rspecXML) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();
        aggrRspec.parseRspec(rspecXML);
        aggrRspec.setStatus("starting");
        synchronized (session) {
            if (!(aggrRspec.getRspecName().isEmpty() || aggrRspec.getAggregateName().isEmpty()
              || aggrRspec.getResources().size() == 0)) {
                try {
                    if (!session.isOpen()) {
                        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                    }
                    org.hibernate.Transaction tx = session.beginTransaction();
                    session.save(aggrRspec);
                    session.flush();
                    tx.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "failed";
                }
            } else {
                throw new IllegalArgumentException("Rspec needs to have rspecName, aggregateName and at least one resource item.");
            }
        }
        aggrRspecs.add(aggrRspec);
        //aggrRspec.dumpRspec();
        AggregateRspecRunner rspecRunner = new AggregateRspecRunner(this, aggrRspec);
        synchronized(rspecThreads) {
            rspecThreads.add(rspecRunner);
        }
        rspecRunner.start();
        return aggrRspec.getStatus();
    }

    public synchronized String deleteRspec(String rspecName) throws AggregateException {
        synchronized(rspecThreads) {
            for (AggregateRspecRunner rspecThread: rspecThreads) {
                if (rspecThread.getRspec() != null && rspecThread.getRspec().getRspecName().equalsIgnoreCase(rspecName)) {
                    rspecThread.setGoRun(false);
                    rspecThread.interrupt();
                    return "stopping";
                }
            }
        }
        throw new AggregateException("deleteRspec: Unkown Rspec: "+rspecName);
    }

    public synchronized HashMap queryRspec(String rspecName) throws AggregateException {
        synchronized(rspecThreads) {
            for (AggregateRspec aggrRspec: aggrRspecs) {
                if (aggrRspec.getRspecName().equalsIgnoreCase(rspecName)) {
                    return aggrRspec.retrieveRspecInfo();
                }
            }
        }
        throw new AggregateException("queryRspec: Unkown Rspec: "+rspecName);
    }


    public synchronized void updateRspec(AggregateRspec aggrRspec) {
        synchronized (session) {
            if (!(aggrRspec.getRspecName().isEmpty() || aggrRspec.getAggregateName().isEmpty()
              || aggrRspec.getResources().size() == 0)) {
                try {
                    if (!session.isOpen()) {
                        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
                    }
                    org.hibernate.Transaction tx = session.beginTransaction();
                    session.update(aggrRspec);
                    session.flush();
                    tx.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("AggregateRspecManager.updateRspec (status=" + aggrRspec.getStatus()+") Exception:" + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException("Rspec needs to have rspecName, aggregateName and at least one resource item.");
            }
        }
    }

}
