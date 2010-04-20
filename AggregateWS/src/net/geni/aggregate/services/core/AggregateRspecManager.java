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
    final int extendedPollInterval = 900000; // 15 minutes
    private volatile boolean goRun = true;
    private volatile List<AggregateRspec> aggrRspecs;
    private volatile List<AggregateRspecRunner> rspecThreads;
    private static Session session;
    private org.apache.log4j.Logger log;

    public AggregateRspecManager() {
        super();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        aggrRspecs = new ArrayList<AggregateRspec>();
        rspecThreads = new ArrayList<AggregateRspecRunner>();
    }

    public List<AggregateRspec> getAggrRspecs() {
        return aggrRspecs;
    }

    public void setAggrRspecs(List<AggregateRspec> aggrRspecs) {
        this.aggrRspecs = aggrRspecs;
    }

    public boolean isGoRun() {
        return goRun;
    }

    public void setGoRun(boolean goRun) {
        this.goRun = goRun;
    }

    public void reloadFromDB() {
        try {
            if (!session.isOpen()) {
                this.session = HibernateUtil.getSessionFactory().getCurrentSession();
            }
            org.hibernate.Transaction tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateRspec");
            if (q.list().size() == 0) {
                return;
            }
            aggrRspecs = (List<AggregateRspec>)q.list();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<AggregateSlice> slices = AggregateState.getAggregateSlices().getAll();
        List<AggregateP2PVlan> p2pvlans = AggregateState.getAggregateP2PVlans().getAll();

        for (AggregateRspec aggrRspec: aggrRspecs) {
            //reload computeSlices
            for (AggregateSlice slice: slices) {
                if (slice.getRspecId() == aggrRspec.getId())
                    aggrRspec.getResources().add(slice);
            }
            //reload p2pVlans
            for (AggregateP2PVlan p2pvlan: p2pvlans) {
                if (p2pvlan.getRspecId() == aggrRspec.getId())
                    aggrRspec.getResources().add(p2pvlan);
            }
            AggregateRspecRunner rspecRunner = new AggregateRspecRunner(this, aggrRspec);
            synchronized (rspecThreads) {
                rspecThreads.add(rspecRunner);
            }
            rspecRunner.setReloaded(true);
            rspecRunner.setPollInterval(AggregateState.getPollInterval());
            rspecRunner.start();
        }
    }

    public void run() {
        reloadFromDB();
        while (goRun) {
            //polling rspecThreads and rspecRspecs for status change
            synchronized(rspecThreads) {
                for (AggregateRspecRunner rspecThread: rspecThreads) {
                    AggregateRspec rspec = rspecThread.getRspec();
                    if (rspec.getStatus().equalsIgnoreCase("working")) {
                        //rspec in working state, polling interval increased
                        if (rspecThread.getPollInterval() < extendedPollInterval)
                            rspecThread.setPollInterval(extendedPollInterval);
                    } else if (rspec.getStatus().equalsIgnoreCase("terminated")
                      || rspec.getStatus().equalsIgnoreCase("rollbacked")) {
                        //rollbacked thread may get diff. treatment?
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
                        rspecThreads.remove(rspecThread);
                        aggrRspecs.remove(rspec);
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

        if (aggrRspec.getRspecName().isEmpty() || aggrRspec.getStartTime() == 0
            || aggrRspec.getStartTime() == 0 || aggrRspec.getResources().size() == 0)
            throw new AggregateException("Rspec parsing failed!");

        synchronized(aggrRspecs) {
            for (AggregateRspec rspec: aggrRspecs)
                if (aggrRspec.getRspecName().equalsIgnoreCase(rspec.getRspecName()))
                    throw new AggregateException("Rspec name:"+rspec.getRspecName()+" has already existed!");
        }

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
        rspecRunner.setPollInterval(AggregateState.getPollInterval());
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
