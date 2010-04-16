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
    private Session session;
    private org.apache.log4j.Logger log;
    private Vector<AggregateRspec> aggrRspecs;
    private Vector<AggregateRspecRunner> rspecThreads;

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
            //polling aggrRspecs for status change
            //give instructions to rspecThreads (e.g., terminate on expires)

        }
    }

    public synchronized void createRspec(String rspecXML) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();
        aggrRspec.parseRspec(rspecXML);
        aggrRspecs.add(aggrRspec);
        //aggrRspec.dumpRspec();
        AggregateRspecRunner rspecRunner = new AggregateRspecRunner(aggrRspec);
        rspecThreads.add(rspecRunner);
        rspecRunner.start();
    }
}
