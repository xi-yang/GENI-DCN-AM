/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;
import org.hibernate.*;
import org.apache.log4j.*;
import org.apache.axis2.AxisFault;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;

/**
 *
 * @author root
 */
public class AggregateRspecManager extends Thread{
    final int extendedPollInterval = 900000; // 15 minutes
    private volatile boolean goRun = false;
    private volatile List<AggregateRspec> aggrRspecs;
    private volatile List<AggregateRspecRunner> rspecThreads;
    private AggregateRspec aggrRspecGlobal = null;
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

    public AggregateRspec getAggrRspecGlobal() {
        return aggrRspecGlobal;
    }

    public boolean isGoRun() {
        return goRun;
    }

    public void setGoRun(boolean goRun) {
        this.goRun = goRun;
    }

    public void loadCRDB() {
        String filePath = AggregateState.getAggregateCRDBFilePath();
        if (filePath == null) {
            log.error("Fatal Error: aggregate.properties file misses the 'aggregate.crdb.path' property!");
            return;
        }
        aggrRspecGlobal = new AggregateRspec();
        aggrRspecGlobal.configRspecFromFile(filePath);
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
        loadCRDB();
        reloadFromDB();
        goRun = true;
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
        synchronized (this) {
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
        synchronized (this) {
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

    public synchronized String[] getResourceTopologyXML(String scope, String[] rspecNames) throws AggregateException {
        if (goRun == false) {
            throw new AggregateException("Initilization not finished yet. Try again later...");
        }

        String[] statements = null;
        int len = 0;
        if (rspecNames == null || rspecNames.length == 0) {
            String networkTopology = "";
            String computeResource = "";

            //get compute resources
            if (scope.equalsIgnoreCase("all") || scope.contains("compute")) {
                if (aggrRspecGlobal != null) {
                    computeResource = aggrRspecGlobal.getResourcesXml();
                }
                if (computeResource != null && !computeResource.isEmpty()) {
                    len++;
                }
            }
            //get nework topology
            if (scope.equalsIgnoreCase("all") || scope.contains("network")) {
                AggregateIDCClient client = AggregateIDCClient.getIDCClient();
                String errMessage = null;
                try {
                    networkTopology = client.retrieveNetworkTopology("all");
                } catch (AxisFault e) {
                    errMessage = "AxisFault from queryReservation: " + e.getMessage();
                } catch (AAAFaultMessage e) {
                    errMessage = "AAAFaultMessage from queryReservation: " + e.getFaultMessage().getMsg();
                } catch (BSSFaultMessage e) {
                    errMessage = "BSSFaultMessage from queryReservation: " + e.getFaultMessage().getMsg();
                } catch (java.rmi.RemoteException e) {
                    errMessage = "RemoteException returned from queryReservation: " + e.getMessage();
                } catch (Exception e) {
                    errMessage = "OSCARSStub threw exception in queryReservation: " + e.getMessage();
                }
                if (errMessage != null) {
                    throw new AggregateException(errMessage);
                }
                if (networkTopology != null && !networkTopology.isEmpty()) {
                    len++;
                }
            }
            //collect results
            if (len > 0 ) {
                statements = new String[len];
                if (len == 1) {
                    if (computeResource != null && !computeResource.isEmpty()) {
                        statements[0] = computeResource;
                    } else {
                        statements[0] = networkTopology;
                    }
                } else {
                    statements[0] = computeResource;
                    statements[1] = networkTopology;
                }
            }
        } else {
                Vector<AggregateRspec> retRspecs = new Vector<AggregateRspec>();
                synchronized (aggrRspecs) {
                    for (String name: rspecNames) {
                        for (AggregateRspec rspec: aggrRspecs) {
                            if (rspec.getRspecName().equalsIgnoreCase(name) && rspec.getResourcesXml() != null) {
                                retRspecs.add(rspec);
                            }
                        }
                    }
                }
                len = retRspecs.size();
                statements = new String[len];
                for (int i = 0; i < len; i++) {
                    statements[i] = retRspecs.get(i).getResourcesXml();
                }
        }

        if (len == 0) {
            throw new AggregateException("No resouce found under scope: " + scope);
        }

        return statements;
    }

}
