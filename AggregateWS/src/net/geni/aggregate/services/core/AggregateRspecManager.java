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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 *
 * @author Xi Yang
 */
public class AggregateRspecManager extends Thread{
    final int extendedPollInterval = 900000; // 15 minutes
    private volatile boolean goRun = false;
    private volatile List<AggregateRspec> aggrRspecs;
    private volatile List<AggregateRspecRunner> rspecThreads;
    private static AggregateRspec aggrRspecGlobal = null;
    private static Session session;
    private static org.hibernate.Transaction tx;
    private org.apache.log4j.Logger log;

    public AggregateRspecManager() {
        super();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
        aggrRspecs = new ArrayList<AggregateRspec>();
        rspecThreads = new ArrayList<AggregateRspecRunner>();
    }

    public List<AggregateRspec> getAggrRspecs() {
        return aggrRspecs;
    }

    public static AggregateRspec getAggrRspecGlobal() {
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
        try {
            // only use MAX native handler to load aggregate infrastructure RSpec
            AggregateRspecHandler rspecHandler = AggregateState.getRspecHandler();
            if (!rspecHandler.getClass().getName().contains("RspecHandler_MAX")) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class<?> aClass = null;
                aClass = cl.loadClass("net.geni.aggregate.services.core.RspecHandler_MAX");
                rspecHandler = (AggregateRspecHandler) aClass.newInstance();
            }
            aggrRspecGlobal = rspecHandler.configRspecFromFile(filePath);
        } catch (AggregateException e) {
            log.error("AggregateRspecManager.loadCRDB(" + filePath+") Exception:" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("AggregateRspecManager.loadCRDB(" + filePath+") Exception:" + e.getMessage());
            e.printStackTrace();            
        }
    }

    public void reloadFromDB() {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Query q = session.createQuery("from AggregateRspec where deleted=false");
            if (q.list().size() == 0) {
                return;
            }
            aggrRspecs = (List<AggregateRspec>)q.list();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }

        List<AggregateSlice> slices = AggregateState.getAggregateSlices().getAll();
        List<AggregateP2PVlan> p2pvlans = AggregateState.getAggregateP2PVlans().getAll();
        List<AggregateExternalResource> ERs = AggregateState.getAggregateExtResources().getAll();

        for (AggregateRspec aggrRspec: aggrRspecs) {
            //reload computeSlices
           log.error(String.format("AggregateRspecManager.reloadFromDB: reloading instance for RSpec '%s'", aggrRspec.getRspecName()));
           for (AggregateSlice slice: slices) {
                if (slice.getRspecId() == aggrRspec.getId())
                    aggrRspec.getResources().add(slice);
            }
            //reload p2pVlans
            for (AggregateP2PVlan p2pvlan: p2pvlans) {
                if (p2pvlan.getRspecId() == aggrRspec.getId())
                    aggrRspec.getResources().add(p2pvlan);
            }
            //reload ext_resources
            for (AggregateExternalResource ER: ERs) {
                if (ER.getRspecId() == aggrRspec.getId())
                    aggrRspec.getResources().add(ER);
            }
            //reconstruct nodes and interfaces
            recalibrateRspecResources(aggrRspec);
            //start rspec runner
            AggregateRspecRunner rspecRunner = new AggregateRspecRunner(this, aggrRspec);
            synchronized (rspecThreads) {
                rspecThreads.add(rspecRunner);
            }
            rspecRunner.setReloaded(true);
            rspecRunner.setPollInterval(AggregateState.getPollInterval());
            rspecRunner.start();
        }
    }

    public void recalibrateRspecResources(AggregateRspec rspec) {
        if (rspec.isDeleted()) {
            return;
        }
        if (rspec.getStatus().equalsIgnoreCase("ROLLBACKED") 
                || rspec.getStatus().equalsIgnoreCase("TERMINATED")) {
            rspec.setDeleted(true);
            this.updateRspec(rspec);
            this.aggrRspecs.remove(rspec);
            return;
        }
        for (int n = 0; n < rspec.getResources().size(); n++) {
            AggregateResource rc = rspec.getResources().get(n);
            // scan compute (plc) slices 
            if (rc.getType().equalsIgnoreCase("computeSlice")) {
                AggregateSlice as = (AggregateSlice)rc;
                String[] nodes = as.getNodes().split("[,\\s]");
                for (String nodeId: nodes) {
                    if (nodeId.isEmpty())
                        continue;
                    AggregateNode an = AggregateState.getAggregateNodes().getByNodeId(Integer.valueOf(nodeId));
                    if (an != null) {
                        boolean haveAdded = false;
                        for (AggregateResource rc1: rspec.getResources()) {
                            if ((rc1.getType().equalsIgnoreCase("computeNode") || rc1.getType().equalsIgnoreCase("planetlabNodeSliver"))
                                    && ((AggregateNode)rc1).getNodeId() == Integer.valueOf(nodeId))
                                haveAdded = true;
                        }
                        if (!haveAdded) {
                            AggregateNode an2 = an.duplicate();
                            an2.setClientId("instance-of-node"+nodeId);
                            an2.setType("planetlabNodeSliver");
                            rspec.getResources().add(an2);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < rspec.getResources().size(); i++) {
            AggregateResource rc = rspec.getResources().get(i);
            // scan p2pvlan's
            if (rc.getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan ppv = (AggregateP2PVlan)rc;
                AggregateNetworkInterface aif1 = null;
                AggregateNetworkInterface aif2 = null;
                if (!ppv.getSrcInterface().isEmpty()) {
                    AggregateNetworkInterface aif = AggregateState.getAggregateInterfaces().getByAttachedLink(ppv.getSource());
                    if (aif != null) {
                        for (int n = 0; n < rspec.getResources().size(); n++) {
                            rc = rspec.getResources().get(n);
                            if (rc.getType().equalsIgnoreCase("planetlabNodeSliver") 
                                    && AggregateUtils.getUrnField(((AggregateNode)rc).getUrn(), "node").equals(
                                            AggregateUtils.getUrnField(aif.getUrn(), "node"))) {
                                aif1 = aif.duplicate();
                                aif1.setClientId("instance-of-if"+Integer.toString(aif.getId()));
                                aif1.setParentNode((AggregateNode)rc);
                                aif1.setType("networkInterface");
                                rspec.getResources().add(aif1);
                                break;
                            }
                        }
                    }
                }
                if (!ppv.getDstInterface().isEmpty()) {
                    AggregateNetworkInterface aif = AggregateState.getAggregateInterfaces().getByAttachedLink(ppv.getDestination());
                    if (aif != null) {
                        for (int n = 0; n < rspec.getResources().size(); n++) {
                            rc = rspec.getResources().get(n);
                            if (rc.getType().equalsIgnoreCase("planetlabNodeSliver") 
                                    && AggregateUtils.getUrnField(((AggregateNode)rc).getUrn(), "node").equals(
                                            AggregateUtils.getUrnField(aif.getUrn(), "node"))) {
                                aif2 = aif.duplicate();
                                aif2.setClientId("instance-of-if"+Integer.toString(aif.getId()));
                                aif2.setParentNode((AggregateNode)rc);
                                aif2.setType("networkInterface");
                                rspec.getResources().add(aif2);
                            }
                        }
                    }
                }
                if (aif1 != null && aif2 != null) {
                    aif1.setPeerInterfaces(aif2.getUrn());
                    aif2.setPeerInterfaces(aif1.getUrn());
                }
            }
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
                    if (rspec.getStatus().equalsIgnoreCase("WORKING")) {
                        //rspec in working state, polling interval increased
                        if (rspecThread.getPollInterval() < extendedPollInterval)
                            rspecThread.setPollInterval(extendedPollInterval);
                        if (rspec.getEndTime() <= System.currentTimeMillis()/1000) {
                            rspecThread.setGoRun(false);
                            rspecThread.interrupt();
                        }
                    } else if (rspec.getStatus().startsWith("ROLLBACKED")) {
                        rspecThreads.remove(rspecThread);
                        break;  // go loop again!
                    } else if (rspec.getStatus().startsWith("TERMINATED")) {
                        try {
                            rspec.getResources().clear();
                            rspec.setDeleted(true);
                            session = HibernateUtil.getSessionFactory().openSession();
                            tx = session.beginTransaction();
                            session.update(rspec);
                            session.flush();
                            tx.commit();
                        } catch (Exception e) {
                            tx.rollback();
                            e.printStackTrace();
                        } finally {
                            if (session.isOpen()) session.close();
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

    public synchronized String createRspec(String rspecId, String rspecXML, String authUser, boolean addPlcSlice) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();

        //set the 1st user to the WSS policy authorized user
        if (authUser != null) { 
            List<String> users = new ArrayList<String>();
            users.add(authUser);
            aggrRspec.setUsers(users);
        }
        aggrRspec = AggregateState.getRspecHandler().parseRspecXml(rspecXML);
        aggrRspec.setRspecName(rspecId);
        aggrRspec.setAddPlcSlice(addPlcSlice);

        if (aggrRspec.getRspecName().isEmpty() || aggrRspec.getStartTime() == 0
            || aggrRspec.getStartTime() == 0 || aggrRspec.getResources().size() == 0)
            throw new AggregateException("Rspec parsing failed!");

        synchronized(aggrRspecs) {
            for (AggregateRspec rspec: aggrRspecs)
                if (aggrRspec.getRspecName().equalsIgnoreCase(rspec.getRspecName()) && !aggrRspec.isDeleted())
                    throw new AggregateException("An instance for RSpec name='"+rspec.getRspecName()+"' has already existed!");
        }

        aggrRspec.setStatus("STARTING");
        synchronized(this) {
            if (!(aggrRspec.getRspecName().isEmpty() || aggrRspec.getAggregateName().isEmpty()
              || aggrRspec.getResources().size() == 0)) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.save(aggrRspec);
                    session.flush();
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    return "FAILED";
                } finally {
                    if (session.isOpen()) session.close();
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
                    return "STOPPING";
                }
            }
        }
        // no runner thread, proceed to see if lingering rspec instance
        synchronized(aggrRspecs) {
            for (AggregateRspec aggrRspec : aggrRspecs) {
                if (aggrRspec.getRspecName().equalsIgnoreCase(rspecName)) {
                    try {
                        aggrRspec.getResources().clear();
                        aggrRspec.setDeleted(true);
                        aggrRspec.setStatus("TERMINATED:" + aggrRspec.getStatus());
                        session = HibernateUtil.getSessionFactory().openSession();
                        tx = session.beginTransaction();
                        session.update(aggrRspec);
                        session.flush();
                        tx.commit();
                    } catch (Exception e) {
                        tx.rollback();
                        e.printStackTrace();
                    } finally {
                        if (session.isOpen()) {
                            session.close();
                        }
                    }
                    aggrRspecs.remove(aggrRspec);
                    return "STOPPED";
                }
            }
            throw new AggregateException("deleteRspec: Unkown Rspec: "+rspecName);
        }
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
        synchronized(this) {
            if (aggrRspec.isDeleted()) {
                return;
            }
            if (!(aggrRspec.getRspecName().isEmpty() || aggrRspec.getAggregateName().isEmpty()
              || aggrRspec.getResources().size() == 0)) {
                try {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();
                    session.update(aggrRspec);
                    session.flush();
                    tx.commit();
                } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
                    log.error("AggregateRspecManager.updateRspec (status=" + aggrRspec.getStatus()+") Exception:" + e.getMessage());
                } finally {
                    if (session.isOpen()) session.close();
                }
            } else {
                throw new IllegalArgumentException("Rspec needs to have rspecName, aggregateName and at least one resource item.");
            }
        }
    }

    public synchronized String[] getManifestXml(String scope, String[] rspecNames) throws AggregateException {
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
                    computeResource = AggregateState.getRspecHandler().generateRspecManifest(aggrRspecGlobal);
                }
                if (computeResource != null && !computeResource.isEmpty()) {
                    len++;
                }
            }
            //get nework topology
            if (scope.equalsIgnoreCase("all") || scope.contains("network")) {
                boolean gotTopoFromFile = false;
                if (AggregateState.getIdcTopoFile() != null && !AggregateState.getIdcTopoFile().isEmpty()) {
                    try {
                        int ch;
                        FileInputStream in = new FileInputStream(AggregateState.getIdcTopoFile());
                        while( (ch = in.read()) != -1)
                            networkTopology += ((char)ch);
                        in.close();
                        gotTopoFromFile = true;
                    } catch (IOException e) {
                        ; // no op
                    }
                }
                if (!gotTopoFromFile) {
                    AggregateIDCClient client = AggregateIDCClient.getIDCClient();
                    String errMessage = null;
                    String domainId = AggregateState.getIdcDomainId();
                    try {
                        networkTopology = client.retrieveNetworkTopology(domainId);
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
                    // optional / configurable ?
                    if (statements[0].contains("</rspec>")) {
                        statements[0] = statements[0].replace("</rspec>", statements[1]+"</rspec>");
                    }
                }
            }
        } else {
                Vector<AggregateRspec> retRspecs = new Vector<AggregateRspec>();
                synchronized (aggrRspecs) {
                    for (String name: rspecNames) {
                        for (AggregateRspec rspec: aggrRspecs) {
                            if (rspec.getRspecName().equalsIgnoreCase(name) && rspec.getResources().size() > 0) {
                                retRspecs.add(rspec);
                            }
                        }
                    }
                }
                len = retRspecs.size();
                statements = new String[len];
                for (int i = 0; i < len; i++) {
                    if (retRspecs.get(i).getStatus().equalsIgnoreCase("WORKING")) {
                        statements[i] = retRspecs.get(i).getManifestXml();
                    }
                    else {
                        statements[i] = AggregateState.getRspecHandler().generateRspecManifest(retRspecs.get(i));
                        retRspecs.get(i).setManifestXml(statements[i]);
                        this.updateRspec(retRspecs.get(i));
                    }
                }
        }

        if (len == 0) {
            throw new AggregateException("No resource found under scope: " + scope); // TODO: comply with GENI v3 format
        }

        return statements;
    }

}
