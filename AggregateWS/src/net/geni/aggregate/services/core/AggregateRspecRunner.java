/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;
import org.apache.log4j.*;

/**
 *
 * @author root
 */
public class AggregateRspecRunner extends Thread {
    private volatile boolean goRun = true;
    private volatile boolean goPoll = true;
    private volatile int pollInterval = 30000; //30 secs by default
    private volatile boolean reloaded = false; //true: thread starts with rspec reloaded from DB
    private org.apache.log4j.Logger log;
    private AggregateRspec rspec;
    private AggregateRspecManager manager;

    private AggregateRspecRunner() {}

    public AggregateRspecRunner(AggregateRspecManager manager, AggregateRspec rspec) {
        super();
        this.manager = manager;
        this.rspec = rspec;
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public synchronized boolean isGoPoll() {
        return goPoll;
    }

    public synchronized void setGoPoll(boolean goPoll) {
        this.goPoll = goPoll;
    }

    public synchronized boolean isGoRun() {
        return goRun;
    }

    public synchronized void setGoRun(boolean goRun) {
        this.goRun = goRun;
    }

    public synchronized AggregateRspec getRspec() {
        return rspec;
    }

    public synchronized void setRspec(AggregateRspec rspec) {
        this.rspec = rspec;
    }

    public synchronized int getPollInterval() {
        return pollInterval;
    }

    public synchronized void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }

    public boolean isReloaded() {
        return reloaded;
    }

    public void setReloaded(boolean reloaded) {
        this.reloaded = reloaded;
    }

    public void run() {
        if (!reloaded) {
            rspec.setStatus("SLICE-STARTING");
            manager.updateRspec(rspec);
            try {
                this.createSlice();
            } catch (AggregateException e) {
                log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
                e.printStackTrace();
                rspec.setStatus("SLICE-FAILED");
                manager.updateRspec(rspec);
            }
            if (rspec.getStatus().equalsIgnoreCase("SLICE-FAILED")) {
                rollback(); //revert
                return;
            }
            rspec.setStatus("VLANS-STARTING");
            manager.updateRspec(rspec);
            try {
                this.createP2PVlans();
            } catch (AggregateException e) {
                log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
                e.printStackTrace();
                rspec.setStatus("VLANS-FAILED");
                manager.updateRspec(rspec);
            }
            if (rspec.getStatus().equalsIgnoreCase("VLANS-FAILED")) {
                rollback(); //revert
                return;
            }
            rspec.setStatus("VLANS-CREATED");
            manager.updateRspec(rspec);
        }

        while (goRun) {
            try {
                this.sleep(pollInterval); //30 secs
            } catch (InterruptedException e) {
                if (!goRun) {
                    break;
                }
            }
            if (goRun && goPoll) {
                try {
                    this.pollP2PVlans();
                    if (rspec.getStatus().equalsIgnoreCase("VLANS-ACTIVE")) {
                        rspec.setStatus("WORKING");
                        manager.updateRspec(rspec);
                    }
                }catch (AggregateException e) {
                    log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
                    e.printStackTrace();
                    rollback();
                    rspec.setStatus("VLANS-FAILED");
                    manager.updateRspec(rspec);
                    goRun = false;
                }
            }
        }

        terminate();
    }

    private void createSlice() throws AggregateException {
        String nodes = "";
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("planetlabNodeSliver")
                || resources.get(i).getType().equalsIgnoreCase("computeNode")) {
                AggregateNode node = (AggregateNode)resources.get(i);
                //verify planetlab capability
                if (!node.getCapabilities().contains("capability=planetlab"))
                    continue;
                nodes = nodes + AggregateUtils.getUrnField(node.getUrn(), "node").toLowerCase()
                        +"."+AggregateUtils.getUrnField(node.getUrn(), "domain").toLowerCase();
                if (i < resources.size()-1)
                    nodes += ":";
            }
        }
        //TODO: Regulate the slicename (limit-length, no dashes etc.)
        String sliceName = AggregateState.getPlcPrefix()+"_"+rspec.getRspecName();
        String url = "http://" + rspec.getAggregateName();
        String description = "Rspec:" + rspec.getRspecName() + " on aggregate:" + rspec.getAggregateName();
        AggregateSlice aggrSlice = AggregateState.getAggregateSlices().createSlice(sliceName, url, description,
                ((rspec.getUsers()==null || rspec.getUsers().isEmpty())?AggregateState.getPlcPI():rspec.getUsers().get(0)), nodes.split(":"));
        if (aggrSlice != null) {
            aggrSlice.setStatus("CREATED");
            aggrSlice.setType("computeSlice");
            aggrSlice.setRspecId(rspec.getId());
            resources.add(aggrSlice);
            AggregateState.getAggregateSlices().update(aggrSlice);
        } else {
            rspec.setStatus("SLICE-FAILED");
            throw (new AggregateException("Failed to create slice:"+sliceName));
        }
    }

    private void deleteSlice() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("computeSlice")) {
                AggregateSlice slice = (AggregateSlice)resources.get(i);
                log.debug("start - delete slice: "+ slice.getSliceName());
                AggregateState.getAggregateSlices().deleteSlice(slice.getSliceName());
                log.debug("end - delete slice: "+ slice.getSliceName());
            }
        }
    }

    private void createP2PVlans() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("networkInterface")) {
                AggregateNetworkInterface netIf1 = (AggregateNetworkInterface)resources.get(i);
                if (!netIf1.getParentNode().getCapabilities().contains("capability=dragon"))
                    continue;
                for (int j = 0; j < resources.size(); j++) {
                    if (resources.get(j).getType().equalsIgnoreCase("networkInterface")) {
                        //verify dragon capability?
                        AggregateNetworkInterface netIf2 = (AggregateNetworkInterface)resources.get(j);
                        if (!netIf2.getParentNode().getCapabilities().contains("capability=dragon"))
                            continue;
                        int[] ifIndices = netIf1.pairupInterfaces(netIf2);
                        if (ifIndices[0] != -1 && ifIndices[1] != -1) {
                            netIf1.getPeers().remove(ifIndices[0]);
                            netIf2.getPeers().remove(ifIndices[1]);
                            //lookup service should have names such as planetlab2.dragon.maxgigapop.net
                            String source = AggregateUtils.getUrnField(netIf1.getUrn(), "node")
                                +"."+AggregateUtils.getUrnField(netIf1.getUrn(), "domain");
                            String destination = AggregateUtils.getUrnField(netIf2.getUrn(), "node")
                                +"."+AggregateUtils.getUrnField(netIf1.getUrn(), "domain");
                            String description = rspec.getRspecName() + " p2pvlan-" + source + "-" + destination + "-" + netIf1.getVlanTag();
                            String vtag = netIf1.getVlanTag();
                            float bandwidth = AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity());
                            HashMap hmRet = new HashMap<String,String>();
                            AggregateP2PVlan p2pvlan = AggregateState.getAggregateP2PVlans().createVlan(
                                    AggregateState.getPlcPrefix()+"_"+rspec.getRspecName(), //sliceName
                                    source, netIf1.getDeviceName(), netIf1.getIpAddress(),
                                    destination, netIf2.getDeviceName(), netIf2.getIpAddress(),
                                    vtag, bandwidth, description,
                                    rspec.getStartTime(), rspec.getEndTime(), hmRet);
                            log.debug("start - create vlan: "+description+" return status: "+hmRet);
                            if (p2pvlan == null) {
                                throw (new AggregateException("Failed to create P2PVlan:"+description));
                            }
                            p2pvlan.setType("p2pVlan");
                            p2pvlan.setRspecId(rspec.getId());
                            resources.add(p2pvlan);
                            AggregateState.getAggregateP2PVlans().update(p2pvlan);
                            if (p2pvlan.getStatus().equalsIgnoreCase("FAILED"))
                                throw (new AggregateException("Failed to setup P2PVlan:"+description));
                            log.debug("end - create vlan: "+description+" return status: "+hmRet);
                        }
                    }
                }
            }
        }
    }

    private void pollP2PVlans() throws AggregateException {
        boolean allActive = true;
        boolean hasP2PVlan = false;
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("p2pVlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan)resources.get(i);
                hasP2PVlan = true;
                p2pvlan.queryVlan();
                log.debug("polled p2pVlan:"+p2pvlan.getDescription()+" status="+p2pvlan.getStatus());
                if (p2pvlan.getStatus().equalsIgnoreCase("FAILED"))
                    throw (new AggregateException("P2PVlan:"+p2pvlan.getDescription()
                        +" creation failed."));
                if (!AggregateState.getAggregateP2PVlans().update(p2pvlan))
                    throw (new AggregateException("Cannot update P2PVlan:"+p2pvlan.getDescription()
                        +" with AggregateDB."));
                if (!p2pvlan.getStatus().equalsIgnoreCase("ACTIVE"))
                    allActive = false;
            }
        }
        if (allActive && hasP2PVlan)
            rspec.setStatus("VLANS-ACTIVE");
    }

    private void deleteP2PVlans() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("p2pVlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan)resources.get(i);
                log.debug("start - delete p2pvlan: "+ p2pvlan.getDescription());
                AggregateState.getAggregateP2PVlans().delete(p2pvlan);
                p2pvlan.teardownVlan();
                log.debug("end - delete p2pvlan: "+ p2pvlan.getDescription());
            }
        }
    }

    private void rollback() {
        log.debug("start - rolling back rspec: "+ rspec.getRspecName());
        try {
            if (rspec.getStatus().matches("^VLANS") || rspec.getStatus().equalsIgnoreCase("VLANS-FAILED")) {
                deleteP2PVlans();
                deleteSlice();
            }
            if (rspec.getStatus().matches("^SLICE")) {
                deleteSlice();
            }
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        rspec.setStatus("ROLLBACKED");
        log.debug("end - rolling back rspec: "+ rspec.getRspecName());
    }

    private void terminate() {
        log.debug("start - terminating rspec: "+ rspec.getRspecName());
        try {
            deleteP2PVlans();
            deleteSlice();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        goRun = false;
        goPoll = false;
        rspec.setStatus("TERMINATED");
        log.debug("end - terminating rspec: "+ rspec.getRspecName());
    }
}
