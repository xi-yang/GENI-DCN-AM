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
            rspec.setStatus("EXT-SLIVER-STARTING");
            manager.updateRspec(rspec);
            try {
                this.createExternalSliver();
            } catch (AggregateException e) {
                log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
                e.printStackTrace();
                rspec.setStatus("EXT-SLIVER-FAILED");
                manager.updateRspec(rspec);
            }
            if (rspec.getStatus().equalsIgnoreCase("EXT-SLIVER-FAILED")) {
                rollback(); //revert
                return;
            }
            
            //log.info("SLICE-STARTING ");
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

            //log.info("STITCHING-STARTING");
            rspec.setStatus("STITCHING-STARTING");
            manager.updateRspec(rspec);
            try {
                this.createStitchingResources();
            } catch (AggregateException e) {
                log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
                e.printStackTrace();
                rspec.setStatus("STITCHING-FAILED");
                manager.updateRspec(rspec);
            }
            if (rspec.getStatus().equalsIgnoreCase("STITCHING-FAILED")) {
                rollback();
                return;
            }

            //log.info("VLANS-STARTING");
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
                        String manifestXml = AggregateState.getRspecHandler().generateRspecManifest(rspec);
                        rspec.setManifestXml(manifestXml);
                        rspec.setStatus("WORKING");
                        manager.updateRspec(rspec);
                    }
                }catch (AggregateException e) {
                    log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
                    e.printStackTrace();
                    //rollback();
                    rspec.setStatus("VLANS-FAILED");
                    manager.updateRspec(rspec);
                    goRun = false;
                }
            }
        }
        if (rspec.getStatus().contains("FAILED")) {
            rollback();
        } else {
            terminate();
        }
    }

    private void createSlice() throws AggregateException {
        String nodes = "";
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("planetlabNodeSliver")
                || resources.get(i).getType().equalsIgnoreCase("computeNode")) {
                AggregateNode node = (AggregateNode)resources.get(i);
                //verify planetlab capability
                if (!node.getCapabilities().contains("planetlab"))
                    continue;
                nodes = nodes + AggregateUtils.getUrnField(node.getUrn(), "node").toLowerCase();
                if (i < resources.size()-1)
                    nodes += ":";
            }
        }
        if (nodes.isEmpty()) {
            rspec.setStatus("SLICE-SKIPPED");
            return;
        }
        //TODO: Regulate the slicename (limit-length, no dashes etc.)
        String sliceName = AggregateState.getPlcPrefix()+rspec.getRspecName();
        String url = "http://" + rspec.getAggregateName();
        String description = "Rspec:" + rspec.getRspecName() + " on aggregate:" + rspec.getAggregateName();
        AggregateSlice aggrSlice = AggregateState.getAggregateSlices().createSlice(sliceName, url, description,
                ((rspec.getUsers()==null || rspec.getUsers().isEmpty() || rspec.getUsers().size()==1)?AggregateState.getPlcPI():rspec.getUsers().get(1)),
                nodes.split(":"), rspec.isAddPlcSlice());
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
                if (netIf1.getPeers() == null || (netIf1.getParentNode() != null 
                        && !netIf1.getParentNode().getCapabilities().contains("dragon")))
                    continue;
                for (int j = 0; j < resources.size(); j++) {
                    if (resources.get(j).getType().equalsIgnoreCase("networkInterface")) {
                        //verify dragon capability?
                        AggregateNetworkInterface netIf2 = (AggregateNetworkInterface)resources.get(j);
                        if (netIf2.getPeers() == null || (netIf2.getParentNode() != null
                                && !netIf2.getParentNode().getCapabilities().contains("dragon")))
                            continue;
                        int[] ifIndices = netIf1.pairupInterfaces(netIf2);
                        if (ifIndices[0] != -1 && ifIndices[1] != -1) {
                            netIf1.getPeers().remove(ifIndices[0]);
                            netIf2.getPeers().remove(ifIndices[1]);
                            String source = netIf1.getLinks().isEmpty() ? AggregateUtils.getIDCQualifiedUrn(netIf1.getUrn())
                                    : AggregateUtils.getIDCQualifiedUrn(netIf1.getLinks().get(0));
                            if (source == null)
                                throw (new AggregateException("Failed to setup P2PVlan from unrecorgnized srcURN: 2"+netIf1.getUrn()));
                            String destination = netIf2.getLinks().isEmpty() ? AggregateUtils.getIDCQualifiedUrn(netIf2.getUrn())
                                    : AggregateUtils.getIDCQualifiedUrn(netIf2.getLinks().get(0));
                            if (destination == null)
                                throw (new AggregateException("Failed to setup P2PVlan to unrecorgnized dstURN: "+netIf2.getUrn()));
                            String vtag = netIf1.getVlanTag()+":"+netIf2.getVlanTag();
                            String description = rspec.getRspecName() + String.format(" (client_id:'%s'-'%s')", netIf1.getClientId(), netIf2.getClientId());
                            float bandwidth = AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity());
                            HashMap hmRet = new HashMap<String,String>();
                            long startTime = rspec.getStartTime();
                            long endTime = rspec.getEndTime();
                            if (startTime < System.currentTimeMillis()/1000) {
                                    endTime += (System.currentTimeMillis()/1000 - startTime);
                                    startTime = System.currentTimeMillis()/1000;
			    }
                            AggregateP2PVlan p2pvlan = AggregateState.getAggregateP2PVlans().createVlan(
                                    AggregateState.getPlcPrefix()+rspec.getRspecName(), //sliceName
                                    source, netIf1.getDeviceName(), netIf1.getIpAddress(),
                                    destination, netIf2.getDeviceName(), netIf2.getIpAddress(),
                                    vtag, bandwidth, description, startTime, endTime, hmRet);
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
                            if(netIf1.getVlanTag().equalsIgnoreCase("any") && !AggregateUtils.parseVlanTag(p2pvlan.getVtag(), true).equalsIgnoreCase("any")) {
                                netIf1.setVlanTag("any("+AggregateUtils.parseVlanTag(p2pvlan.getVtag(), true)+")");
                            }
                            if(netIf2.getVlanTag().equalsIgnoreCase("any") && !AggregateUtils.parseVlanTag(p2pvlan.getVtag(), false).equalsIgnoreCase("any")) {
                                netIf2.setVlanTag("any("+AggregateUtils.parseVlanTag(p2pvlan.getVtag(), false)+")");
                            }
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
                log.debug("polling p2pVlan:"+p2pvlan.getDescription()+" status="+p2pvlan.getStatus());
                p2pvlan.queryVlan();
                log.debug("polled p2pVlan:"+p2pvlan.getDescription()+" status="+p2pvlan.getStatus());
                if (p2pvlan.getStatus().equalsIgnoreCase("FAILED"))
                    throw (new AggregateException("P2PVlan:"+p2pvlan.getDescription()
                        +" creation failed."));
                if (!AggregateState.getAggregateP2PVlans().update(p2pvlan))
                    throw (new AggregateException("Cannot update P2PVlan:"+p2pvlan.getDescription()
                        +" with AggregateDB."));
                if (p2pvlan.getStatus().equalsIgnoreCase("ACTIVE")) {
                    if (!p2pvlan.setVlanOnNodes(true)) {
                        throw (new AggregateException("P2PVlan:"+p2pvlan.getDescription()
                                + " failed to add VLAN interface on source or destination node"));
                    }
                } else {
                    allActive = false;
                }
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
                if (!p2pvlan.getStitchingResourceId().isEmpty())
                    continue; //have been taken care of by deleteStitchingResources()
                log.debug("start - delete p2pvlan: "+ p2pvlan.getDescription());
                AggregateState.getAggregateP2PVlans().delete(p2pvlan);
                p2pvlan.teardownVlan();
                log.debug("end - delete p2pvlan: "+ p2pvlan.getDescription());
            }
        }
    }


    private void createExternalSliver() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource aggrER = (AggregateExternalResource)resources.get(i);
                if (aggrER.getSubType().equalsIgnoreCase("ProtoGENI")) {
                    log.debug("start - create external protoGENI sliver: "+ aggrER.getUrn());
                    String status = aggrER.createResource();
                    if (status.toUpperCase().contains("FAILED")) {
                        rspec.setStatus("EXT-SLIVER-FAILED");
                        throw (new AggregateException("Failed to allocate externalResource:"+aggrER.getUrn()));
                    }
                    aggrER.setStatus("CREATED");
                    aggrER.setRspecId(rspec.getId());
                    if (AggregateState.getAggregateExtResources().add(aggrER) == false) {
                        throw new AggregateException("Cannot add externalResource:" + aggrER.getUrn() +" to DB ");
                    }
                    log.debug("end - create external protoGENI sliver: "+ aggrER.getUrn());
                }
            }
        }
    }

    private void deleteExternalSliver() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource aggrER = (AggregateExternalResource)resources.get(i);
                log.debug("externalResource = "+ aggrER.getUrn()); //xxxx
                if (aggrER.getSubType().equalsIgnoreCase("ProtoGENI")) {
                    log.debug("start - delete external protoGENI sliver: "+ aggrER.getUrn());
                    AggregateState.getAggregateExtResources().delete(aggrER.getUrn());
                    aggrER.deleteResource();
                    log.debug("end - delete external protoGENI sliver: "+ aggrER.getUrn());
                }
            }
        }
    }


    void createStitchingResources() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan) resources.get(i);
                if (!p2pvlan.getExternalResourceId().isEmpty()) {
                    for (int j = 0; j < resources.size(); j++) {
                        if (resources.get(j).getType().equalsIgnoreCase("externalResource")) {
                            AggregateExternalResource aggrER = (AggregateExternalResource) resources.get(j);
                            if (aggrER.getSubType().equalsIgnoreCase("ProtoGENI")) {
                                log.debug("externalresource-Protogeni: URN=" + aggrER.getUrn() + " VlanTag:" + Integer.toString(aggrER.getVlanTag()));
                                if (p2pvlan.getExternalResourceId().equalsIgnoreCase(aggrER.getUrn()) && (aggrER.getVlanTag() > 0 && aggrER.getVlanTag() < 4096)) {
                                    p2pvlan.setVtag(Integer.toString(aggrER.getVlanTag()));
                                    log.debug("stitching p2pvlan by using obtained vlan " + p2pvlan.getVtag() + " from external resource: " + aggrER.getUrn());
                                    break;
                                }
                            }
                        }
                    }
                    log.debug("start - creating stitching p2pvlan");
                    p2pvlan.setRspecId(rspec.getId());
                    String sliceName = AggregateState.getPlcPrefix() + rspec.getRspecName();
                    p2pvlan.setSliceName(sliceName);
                    AggregateSlice slice = AggregateState.getAggregateSlices().getByName(sliceName);
                    long startTime = rspec.getStartTime();
                    long endTime = rspec.getEndTime();
                    if (slice != null) {
                        startTime = System.currentTimeMillis()/1000;
                        if (slice.getExpiredTime() >= endTime) {
                            endTime = slice.getExpiredTime();
                        } else {//the slice would expire bfore VLAN
                            throw (new AggregateException("Failed to create stitching P2PVlan for an expired internal slice: " + sliceName));
                        }
                    }
                    p2pvlan.setStartTime(startTime);
                    p2pvlan.setEndTime(endTime);
                    p2pvlan.setDescription(rspec.getRspecName() + String.format(" (client_id:'%s')", p2pvlan.getClientId()));
                    String status = p2pvlan.setupVlan();
                    AggregateState.getAggregateP2PVlans().add(p2pvlan);
                    if (status.equalsIgnoreCase("FAILED")) {
                        throw (new AggregateException("Failed to create stitching P2PVlan:" + p2pvlan.getDescription()));
                    }
                    log.debug("end - creating stitching p2pvlan: " + p2pvlan.getDescription());
                }
            }
        }
        //TODO: provision the stub inerface using PLC SSH client
    }

    void deleteStitchingResources() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int j = 0; j < resources.size(); j++) {
            if (resources.get(j).getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan) resources.get(j);
                log.debug("p2pvlan = "+p2pvlan.getDescription()); //xxxx
                if (!p2pvlan.getStitchingResourceId().isEmpty()) {
                    log.debug("start - deleting stitching p2pvlan: " + p2pvlan.getDescription());
                    AggregateState.getAggregateP2PVlans().delete(p2pvlan);
                    p2pvlan.teardownVlan();
                    log.debug("end - deleting stitching p2pvlan: " + p2pvlan.getDescription());
                }
            }
        }
    }

    private void rollback() {
        log.debug("start - rolling back rspec: "+ rspec.getRspecName() + " with status:" + rspec.getStatus());
        try {
            if (rspec.getStatus().matches("^VLANS.*")) {
                deleteP2PVlans();
                deleteStitchingResources();
                deleteSlice();
                deleteExternalSliver();
            }
            if (rspec.getStatus().matches("^STITCHING.*")) {
                deleteStitchingResources();
                deleteSlice();
                deleteExternalSliver();
            }
            if (rspec.getStatus().matches("^SLICE.*")) {
                deleteSlice();
                deleteExternalSliver();
            }
            if (rspec.getStatus().matches("^EXT-SLIVER.*")) {
                deleteExternalSliver();
            }
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        rspec.setStatus("ROLLBACKED:"+rspec.getStatus());
        log.debug("end - rolling back rspec: "+ rspec.getRspecName());
    }

    private void terminate() {
        log.debug("start - terminating rspec: "+ rspec.getRspecName());
        try {
            deleteP2PVlans();
            deleteStitchingResources();
            deleteSlice();
            deleteExternalSliver();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName()+") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        goRun = false;
        goPoll = false;
        rspec.setStatus("TERMINATED:"+rspec.getStatus());
        log.debug("end - terminating rspec: "+ rspec.getRspecName());
    }
}
