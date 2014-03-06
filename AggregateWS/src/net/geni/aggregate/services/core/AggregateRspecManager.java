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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Xi Yang
 */
public class AggregateRspecManager extends Thread{
    final int defaultPollInterval = 30000; // 30 seconds
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

        synchronized(rspecThreads) {
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
                //reload ext_resources
                for (AggregateExternalResource ER: ERs) {
                    if (ER.getRspecId() == aggrRspec.getId())
                        aggrRspec.getResources().add(ER);
                }
                //reconstruct nodes and interfaces
                recalibrateRspecResources(aggrRspec);
                if (aggrRspec.isDeleted()) {
                    log.debug(String.format("AggregateRspecManager.reloadFromDB: removing defunct instance for RSpec '%s'", aggrRspec.getRspecName()));
                    aggrRspecs.remove(aggrRspec);
                    if (aggrRspecs.isEmpty())
                        break;
                    continue;
                } 
                //start rspec runner
                log.debug(String.format("AggregateRspecManager.reloadFromDB: reloading instance for RSpec '%s'", aggrRspec.getRspecName()));
                AggregateRspecRunner rspecRunner = new AggregateRspecRunner(this, aggrRspec);
                synchronized (rspecThreads) {
                    rspecThreads.add(rspecRunner);
                }
                rspecRunner.setReloaded(true);
                rspecRunner.setPollInterval(AggregateState.getPollInterval());
                rspecRunner.start();
            }
        }
    }

    public void recalibrateRspecResources(AggregateRspec rspec) {
        if (rspec.isDeleted()) {
            return;
        }
        if (rspec.getStatus().contains("ROLLBACKED") 
                || rspec.getStatus().contains("TERMINATED")
                || rspec.getStatus().contains("FAILED")) {
            rspec.setDeleted(true);
            try {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();
                session.update(rspec);
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
                    long now = System.currentTimeMillis()/1000;
                    // terminating allocation-expiring (in 30 seconds) thread
                    if (rspecThread.isGoRun() && rspec.getStatus().contains("ALLOCATED") 
                            && (rspec.getStartTime() - now) < 30) {
                        rspecThread.setGoRun(false);
                        rspecThread.interrupt();
                    }
                    if (rspec.getStatus().equalsIgnoreCase("WORKING")) {
                        //rspec in working state, polling interval increased
                        if (rspecThread.getPollInterval() < extendedPollInterval)
                            rspecThread.setPollInterval(extendedPollInterval);
                        // terminating provision-expired thread
                        if (rspec.getEndTime() <= now) {
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

    public synchronized String createRspec(String rspecId, String rspecXML, String authUser, boolean addPlcSlice, long startTime) throws AggregateException {
        if (goRun == false) {
            throw new AggregateException("Initilization not finished yet. Try again later...");
        }
        synchronized(rspecThreads) {
            for (AggregateRspec rspec: aggrRspecs)
                if (rspecId.equalsIgnoreCase(rspec.getRspecName()) && !rspec.isDeleted())
                    throw new AggregateException("An instance for RSpec name='"+rspecId+"' has already existed!");
        }

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
        long now = System.currentTimeMillis()/1000;
        if (startTime > now)
            aggrRspec.setStartTime(startTime);
        if (aggrRspec.getRspecName().isEmpty() || aggrRspec.getStartTime() == 0
            || aggrRspec.getStartTime() == 0 || aggrRspec.getResources().size() == 0)
            throw new AggregateException("Rspec parsing failed!");

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

    public String allocateRspec(String rspecId, String rspecXML, String authUser, boolean addPlcSlice, String expires) throws AggregateException {
        if (goRun == false) {
            throw new AggregateException("Initilization not finished yet. Try again later...");
        }
        long now = System.currentTimeMillis()/1000;
        long startTime = 0;
        try {
            long timeOffset = Long.parseLong(AggregateState.getApiAllocateTimeout());
            startTime = now + timeOffset; 
        } catch (NumberFormatException ex) {
            log.warn("allocateRspec caught NumberFormatException for api.allocate_timeout=" + AggregateState.getApiAllocateTimeout());
        }
        if (expires != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                Date dateExpires = simpleDateFormat.parse(expires);
                startTime = dateExpires.getTime()/1000 + 30; // 30 seconds lead time
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(AggregateRspecManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            
        }
        return createRspec(rspecId, rspecXML, authUser, addPlcSlice, startTime);
    }
    
    public synchronized String provisionRspec(String rspecName) throws AggregateException {
        if (goRun == false) {
            throw new AggregateException("Initilization not finished yet. Try again later...");
        }
        synchronized(rspecThreads) {
            for (AggregateRspec aggrRspec: aggrRspecs) {
                if (rspecName.equalsIgnoreCase(aggrRspec.getRspecName()) && !aggrRspec.isDeleted()) {
                    if (aggrRspec.getStatus().contains("ALLOCATED")) {
                        long now = System.currentTimeMillis() / 1000;
                        aggrRspec.setStartTime(now);
                        aggrRspec.setStatus("PROVISIONING");
                        for (AggregateRspecRunner rspecThread: rspecThreads) {
                            if (rspecThread.getRspec() != null && rspecThread.getRspec().getRspecName().equalsIgnoreCase(rspecName)) {
                                rspecThread.setPollInterval(defaultPollInterval);
                                rspecThread.interrupt();
                                return "PROVISIONING";
                            }
                        }
                        throw new AggregateException("No active RSpecRunnner thread '"+rspecName+"' to provision!");            
                    } else if (aggrRspec.getStatus().equalsIgnoreCase("PROVISIONING")) {
                        return "PROVISIONING";
                    } else {
                        throw new AggregateException(String.format("RSpec '%s' is in '%s' status -- cannot provision for now!", rspecName, aggrRspec.getStatus()));
                    }
                }
            }
            throw new AggregateException("provisionRspec: Unkown Rspec: "+rspecName);
        }
    }
    
    public synchronized String renewRspec(String rspecName, String expires) throws AggregateException {
        if (goRun == false) {
            throw new AggregateException("Initilization not finished yet. Try again later...");
        }
        synchronized(rspecThreads) {
            for (AggregateRspec aggrRspec: aggrRspecs) {
                if (rspecName.equalsIgnoreCase(aggrRspec.getRspecName()) && !aggrRspec.isDeleted()) {
                    if (aggrRspec.getStatus().equalsIgnoreCase("WORKING") || aggrRspec.getStatus().contains("ALLOCATED")) {
                        long now = System.currentTimeMillis()/1000;
                        if (aggrRspec.getEndTime() - now < 900) {
                            throw new AggregateException("Cannot renew an RSpec that will expire in 15 minutes!");
                        }
                        long newEndTime = 0;
                        try {
                            XMLGregorianCalendar xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(expires);
                            Date dateExpires = xgc.toGregorianCalendar().getTime();
                            newEndTime = dateExpires.getTime()/1000;
                        } catch (DatatypeConfigurationException ex) {
                            throw new AggregateException(String.format("Mailformed renewal time '%s'", expires));
                        }
                        if (newEndTime - now < 900) {
                            throw new AggregateException("The new expiration time must be at least 15 minutes from the present time!");                            
                        }
                        aggrRspec.setEndTime(newEndTime);
                        aggrRspec.setStatus("RENEWING");
                        for (AggregateRspecRunner rspecThread: rspecThreads) {
                            if (rspecThread.getRspec() != null && rspecThread.getRspec().getRspecName().equalsIgnoreCase(rspecName)) {
                                rspecThread.setPollInterval(defaultPollInterval);
                                rspecThread.interrupt();
                                return "RENEWING";
                            }
                        }
                        throw new AggregateException("No active RSpecRunnner thread '"+rspecName+"' to renewa!");            
                    } else if (aggrRspec.getStatus().equalsIgnoreCase("RENEWING")) {
                        return "RENEWING";
                    } else {
                        throw new AggregateException(String.format("RSpec '%s' is in '%s' status -- cannot renew for now!", rspecName, aggrRspec.getStatus()));
                    }
                }
            }
            throw new AggregateException("renewRspec: Unkown Rspec: "+rspecName);
        }
    }
    
    public synchronized String deleteRspec(String rspecName) throws AggregateException {
        if (goRun == false) {
            throw new AggregateException("Initilization not finished yet. Try again later...");
        }
        synchronized(rspecThreads) {
            for (AggregateRspecRunner rspecThread: rspecThreads) {
                if (rspecThread.getRspec() != null && rspecThread.getRspec().getRspecName().equalsIgnoreCase(rspecName)) {
                    rspecThread.setGoRun(false);
                    rspecThread.interrupt();
                    return "STOPPING";
                }
            }
            // no runner thread, proceed to see if lingering rspec instance
            for (AggregateRspec aggrRspec : aggrRspecs) {
                if (aggrRspec.getRspecName().equalsIgnoreCase(rspecName)) {
                    log.info("start - delete defunct rspec");
                    try {
                        aggrRspec.getResources().clear();
                        aggrRspec.setDeleted(true);
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
                    log.info("end - delete defunct rspec");
                    return "STOPPED";
                }
            }
            throw new AggregateException("deleteRspec: Unkown Rspec: "+rspecName);
        }
    }

    public synchronized HashMap queryRspec(String rspecName) throws AggregateException {
        if (goRun == false) {
            throw new AggregateException("Initilization not finished yet. Try again later...");
        }
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
                networkTopology = AggregateState.getStitchTopoRunner().getStitchXml();
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
                synchronized(rspecThreads) {
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
