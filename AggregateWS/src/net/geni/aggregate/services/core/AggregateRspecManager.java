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
                    if (rspec.getStatus().equalsIgnoreCase("terminated")
                      || rspec.getStatus().equalsIgnoreCase("rollbacked")) {
                        //rollbacked thread may get diff. treatment?
                        rspecThreads.remove(rspecThread);
                        aggrRspecs.remove(rspec);
                        break;  // go loop again!
                    }
                    //give other instructions e.g., terminate on expires
                }
            }
        }
    }

    public synchronized void createRspec(String rspecXML) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();
        aggrRspec.parseRspec(rspecXML);
        aggrRspecs.add(aggrRspec);
        //aggrRspec.dumpRspec();
        AggregateRspecRunner rspecRunner = new AggregateRspecRunner(aggrRspec);
        synchronized(rspecThreads) {
            rspecThreads.add(rspecRunner);
        }
        rspecRunner.start();
    }

    public synchronized void deleteRspec(String rspecName) throws AggregateException {
        synchronized(rspecThreads) {
            for (AggregateRspecRunner rspecThread: rspecThreads) {
                if (rspecThread.getRspec() != null && rspecThread.getRspec().getRspecName().equalsIgnoreCase(rspecName)) {
                    rspecThread.setGoRun(false);
                    rspecThread.interrupt();
                    return;
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
}
