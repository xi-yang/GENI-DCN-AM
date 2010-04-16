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
    private org.apache.log4j.Logger log;
    private AggregateRspec rspec;

    private AggregateRspecRunner() {}

    public AggregateRspecRunner(AggregateRspec rspec) {
        super();
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

    public void run() {
        try {
            this.createSlice();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
            rspec.setStatus("slice-failed");
        }
        if (rspec.getStatus().equalsIgnoreCase("slice-failed")) {
            rollback(); //revert
            return;
        }
        rspec.setStatus("slice-created");

        try {
            this.createP2PVlans();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
            rspec.setStatus("vlans-failed");
        }
        if (rspec.getStatus().equalsIgnoreCase("vlans-failed")) {
            rollback(); //revert
            return;
        }
        rspec.setStatus("vlans-created");

        while (goRun) {
            try {
                this.sleep(30000); //30 secs
            } catch (InterruptedException e) {
                if (!goRun) {
                    break;
                }
            }
            if (goRun && goPoll) {
                try {
                    this.pollP2PVlans();
                }catch (AggregateException e) {
                    log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
                    e.printStackTrace();
                    rollback();
                    rspec.setStatus("vlans-failed");
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
                AggregateState.getPlcPI(), nodes.split(":"));
        if (aggrSlice != null) {
            aggrSlice.setType("computeSlice");
            resources.add(aggrSlice);
        } else {
            rspec.setStatus("slice-failed");
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
                            netIf1.getPeerInterfaces().remove(ifIndices[0]);
                            netIf2.getPeerInterfaces().remove(ifIndices[1]);
                            //lookup service should have names such as planetlab2.dragon.maxgigapop.net
                            String source = AggregateUtils.getUrnField(netIf1.getInterfaceId(), "node")
                                +"."+AggregateUtils.getUrnField(netIf1.getInterfaceId(), "domain");
                            String destination = AggregateUtils.getUrnField(netIf2.getInterfaceId(), "node")
                                +"."+AggregateUtils.getUrnField(netIf1.getInterfaceId(), "domain");
                            String description = rspec.getRspecName() + " p2pvlan-" + source + "-" + destination + "-" + netIf1.getVlanTag();
                            int vtag = Integer.valueOf(netIf1.getVlanTag());
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
                            resources.add(p2pvlan);
                            if (p2pvlan.getStatus().equalsIgnoreCase("failed"))
                                throw (new AggregateException("Failed to setup P2PVlan:"+description));
                            log.debug("end - create vlan: "+description+" return status: "+hmRet);
                        }
                    }
                }
            }
        }
    }

    private void pollP2PVlans() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("p2pVlans")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan)resources.get(i);
                p2pvlan.queryVlan();
                log.debug("polled p2pVlan:"+p2pvlan.getDescription()+" status="+p2pvlan.getStatus());
                if (p2pvlan.getStatus().equalsIgnoreCase("failed"))
                    throw (new AggregateException("P2PVlan:"+p2pvlan.getDescription()
                        +" creation failed."));
                if (AggregateState.getAggregateP2PVlans().update(p2pvlan))
                    throw (new AggregateException("Cannot update P2PVlan:"+p2pvlan.getDescription()
                        +" with AggregateDB."));
            }
        }
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
        log.debug("rolling back rspec: "+ rspec.getRspecName());
        try {
            if (rspec.getStatus().matches("^vlans") || rspec.getStatus().equalsIgnoreCase("vlans-failed")) {
                deleteP2PVlans();
                deleteSlice();
            }
            if (rspec.getStatus().matches("^slice")) {
                deleteSlice();
            }
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
            rspec.setStatus("rollbacked");
        }
    }

    private void terminate() {
        log.debug("terminating rspec: "+ rspec.getRspecName());
        try {
            deleteP2PVlans();
            deleteSlice();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        goRun = false;
        goPoll = false;
        rspec.setStatus("terminated");
    }
}
