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
    //private Vector<AggregateRspecRunnder> rspecThreads;

    public AggregateRspecManager() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        log = Logger.getLogger(this.getClass());
        aggrRspecs = new Vector<AggregateRspec>();
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
            //give instructions to rspecThreads

            //temp test code
            System.out.println("RspecMan running!");
            try {
                this.sleep(30000);//30 secs
            } catch (Exception e) {
                return;
            }
        }
    }

    public synchronized void createRspec(String rspecXML) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();
        aggrRspec.parseRspec(rspecXML);
        aggrRspec.prepareP2PVlans();
        aggrRspecs.add(aggrRspec);
        aggrRspec.dumpRspec();
        //create AggregateRspecRunner
        //pass aggrRspec to AggregateRspecRunner
        //start  AggregateRspecRunner thread
    }
}
