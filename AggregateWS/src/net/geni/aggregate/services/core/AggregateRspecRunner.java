/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.*;
import org.apache.log4j.*;
import org.json.simple.JSONObject;

/**
 *
 * @author Xi Yang
 */
public class AggregateRspecRunner extends Thread {

    private volatile boolean goRun = true;
    private volatile boolean goPoll = true;
    private volatile int pollInterval = 30000; //30 secs by default
    private volatile boolean reloaded = false; //true: thread starts with rspec reloaded from DB
    private org.apache.log4j.Logger log;
    private AggregateRspec rspec;
    private AggregateRspecManager manager;

    private AggregateRspecRunner() {
    }

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
            
            rspec.setStatus("SDX-STARTING");
            manager.updateRspec(rspec);
            try {
                this.createSdxSlivers();
            } catch (AggregateException e) {
                log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
                e.printStackTrace();
                rspec.setStatus("SDX-FAILED");
                manager.updateRspec(rspec);
            }
            if (rspec.getStatus().equalsIgnoreCase("SDX-FAILED")) {
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

            long now = System.currentTimeMillis() / 1000;
            if (now < rspec.getStartTime()) {
                rspec.setStatus("VLANS-ALLOCATED");
            } else {
                rspec.setStatus("VLANS-CREATED");
            }
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

            if (rspec.getStatus().equalsIgnoreCase("PROVISIONING")) {
                manager.updateRspec(rspec);
                this.provision();
            } else if (rspec.getStatus().equalsIgnoreCase("RENEWING")) {
                manager.updateRspec(rspec);
                this.renew();
            }

            if (goRun && goPoll) {
                // poll sdx slivers
                try {
                    this.pollSdxSlivers();
                    manager.updateRspec(rspec);
                } catch (AggregateException e) {
                    log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
                    e.printStackTrace();
                    //rollback();
                    rspec.setStatus("SDX-FAILED");
                    manager.updateRspec(rspec);
                    break;
                }
                // poll vlan circuits
                try {
                    this.pollP2PVlans();
                    if (rspec.getStatus().contains("ACTIVE")) {
                        String manifestXml = AggregateState.getRspecHandler().generateRspecManifest(rspec);
                        rspec.setManifestXml(manifestXml);
                        rspec.setStatus("WORKING");
                        manager.updateRspec(rspec);
                    }
                } catch (AggregateException e) {
                    log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
                    e.printStackTrace();
                    //rollback();
                    rspec.setStatus("VLANS-FAILED");
                    manager.updateRspec(rspec);
                    break;
                }
            }
        }

        if (rspec.getStatus().contains("FAILED")) {
            this.rollback();
        } else {
            this.terminate();
        }
    }

    private void createP2PVlans() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("networkInterface")) {
                AggregateNetworkInterface netIf1 = (AggregateNetworkInterface) resources.get(i);
                if (netIf1.getPeers() == null || (netIf1.getParentNode() != null
                        && !netIf1.getParentNode().getCapabilities().contains("dragon"))) {
                    continue;
                }
                for (int j = 0; j < resources.size(); j++) {
                    if (resources.get(j).getType().equalsIgnoreCase("networkInterface")) {
                        //verify dragon capability?
                        AggregateNetworkInterface netIf2 = (AggregateNetworkInterface) resources.get(j);
                        if (netIf2.getPeers() == null || (netIf2.getParentNode() != null
                                && !netIf2.getParentNode().getCapabilities().contains("dragon"))) {
                            continue;
                        }
                        int[] ifIndices = netIf1.pairupInterfaces(netIf2);
                        if (ifIndices[0] != -1 && ifIndices[1] != -1) {
                            netIf1.getPeers().remove(ifIndices[0]);
                            netIf2.getPeers().remove(ifIndices[1]);
                            String source = netIf1.getLinks().isEmpty() ? AggregateUtils.getIDCQualifiedUrn(netIf1.getUrn())
                                    : AggregateUtils.getIDCQualifiedUrn(netIf1.getLinks().get(0));
                            if (source == null) {
                                throw (new AggregateException("Failed to setup P2PVlan from unrecorgnized srcURN: 2" + netIf1.getUrn()));
                            }
                            String destination = netIf2.getLinks().isEmpty() ? AggregateUtils.getIDCQualifiedUrn(netIf2.getUrn())
                                    : AggregateUtils.getIDCQualifiedUrn(netIf2.getLinks().get(0));
                            if (destination == null) {
                                throw (new AggregateException("Failed to setup P2PVlan to unrecorgnized dstURN: " + netIf2.getUrn()));
                            }
                            String vtag = netIf1.getVlanTag() + ":" + netIf2.getVlanTag();
                            String description = rspec.getRspecName() + String.format(" (%s-%s)", netIf1.getClientId(), netIf2.getClientId());
                            float bandwidth = AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity());
                            HashMap hmRet = new HashMap<String, String>();
                            long startTime = rspec.getStartTime();
                            long endTime = rspec.getEndTime();
                            if (startTime < System.currentTimeMillis() / 1000) {
                                endTime += (System.currentTimeMillis() / 1000 - startTime);
                                startTime = System.currentTimeMillis() / 1000;
                            }
                            AggregateP2PVlan p2pvlan = AggregateState.getAggregateP2PVlans().createVlan(
                                    AggregateState.getPlcPrefix() + rspec.getRspecName(), //sliceName
                                    source, netIf1.getDeviceName(), netIf1.getAddress(),
                                    destination, netIf2.getDeviceName(), netIf2.getAddress(),
                                    vtag, bandwidth, description, startTime, endTime, hmRet);
                            log.debug("start - create vlan: " + description + " return status: " + hmRet);
                            if (p2pvlan == null) {
                                throw (new AggregateException("Failed to create P2PVlan:" + description));
                            }
                            p2pvlan.setType("p2pVlan");
                            p2pvlan.setRspecId(rspec.getId());
                            resources.add(p2pvlan);
                            AggregateState.getAggregateP2PVlans().update(p2pvlan);
                            if (p2pvlan.getStatus().equalsIgnoreCase("FAILED")) {
                                throw (new AggregateException("Failed to setup P2PVlan:" + description));
                            }
                            if (netIf1.getVlanTag().equalsIgnoreCase("any") && !AggregateUtils.parseVlanTag(p2pvlan.getVtag(), true).equalsIgnoreCase("any")) {
                                netIf1.setVlanTag("any(" + AggregateUtils.parseVlanTag(p2pvlan.getVtag(), true) + ")");
                            }
                            if (netIf2.getVlanTag().equalsIgnoreCase("any") && !AggregateUtils.parseVlanTag(p2pvlan.getVtag(), false).equalsIgnoreCase("any")) {
                                netIf2.setVlanTag("any(" + AggregateUtils.parseVlanTag(p2pvlan.getVtag(), false) + ")");
                            }
                            log.debug("end - create vlan: " + description + " return status: " + hmRet);
                        }
                    }
                }
            }
        }
    }

    private void pollP2PVlans() throws AggregateException {
        boolean allActive = true;
        boolean allReserved = true;
        boolean hasP2PVlan = false;
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("p2pVlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan) resources.get(i);
                hasP2PVlan = true;
                log.debug("polling p2pVlan:" + p2pvlan.getDescription() + " status=" + p2pvlan.getStatus());
                String lastStatus = p2pvlan.getStatus();
                p2pvlan.queryVlan();
                if (lastStatus.equalsIgnoreCase("UNKNOWN") && p2pvlan.getStatus().equalsIgnoreCase("UNKNOWN")) {
                    // VLAN circuit is considered failed if staying in UNKNOWN twice
                    p2pvlan.setStatus("FAILED");
                }
                log.debug("polled p2pVlan:" + p2pvlan.getDescription() + " status=" + p2pvlan.getStatus());
                if (p2pvlan.getStatus().equalsIgnoreCase("FAILED")) {
                    throw (new AggregateException("P2PVlan:" + p2pvlan.getDescription()
                            + " creation failed."));
                }
                if (!AggregateState.getAggregateP2PVlans().update(p2pvlan)) {
                    throw (new AggregateException("Cannot update P2PVlan:" + p2pvlan.getDescription()
                            + " with AggregateDB."));
                }
                if (p2pvlan.getStatus().equalsIgnoreCase("ACTIVE")) {
                    ;
                } else if (p2pvlan.getStatus().equalsIgnoreCase("RESERVED")) {
                    allActive = false;
                } else {
                    allActive = false;
                    allReserved = false;
                }
            }
        }
        if (allActive && hasP2PVlan) {
            rspec.setStatus("VLANS-ACTIVE");
        } else if (allReserved && hasP2PVlan) {
            rspec.setStatus("VLANS-ALLOCATED");
        }
    }

    private void modifyP2PVlans() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("p2pVlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan) resources.get(i);
                log.debug("start - modify p2pvlan: " + p2pvlan.getDescription());
                long now = System.currentTimeMillis() / 1000;
                if (p2pvlan.getStartTime() - now < 120 && p2pvlan.getEndTime() == rspec.getEndTime()) {
                    log.debug("skip - no need to modify p2pvlan: " + p2pvlan.getDescription());
                    continue;
                }
                p2pvlan.setStartTime(rspec.getStartTime());
                p2pvlan.setEndTime(rspec.getEndTime());
                String status = p2pvlan.modifyVlan();
                if (!status.equalsIgnoreCase("ACTIVE") && !status.equalsIgnoreCase("RESERVED")) {
                    throw new AggregateException(String.format("P2PVlan '%s' failed to modify due to '%s'", p2pvlan.getDescription(), p2pvlan.getErrorMessage()));
                }
                AggregateState.getAggregateP2PVlans().update(p2pvlan);
                log.debug("end - modify p2pvlan: " + p2pvlan.getDescription());
            }
        }
    }

    private void deleteP2PVlans() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("p2pVlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan) resources.get(i);
                if (!p2pvlan.getStitchingResourceId().isEmpty()) {
                    continue; //have been taken care of by deleteStitchingResources()
                }
                log.debug("start - delete p2pvlan: " + p2pvlan.getDescription());
                AggregateState.getAggregateP2PVlans().delete(p2pvlan);
                p2pvlan.teardownVlan();
                log.debug("end - delete p2pvlan: " + p2pvlan.getDescription());
            }
        }
    }

    private void createExternalSliver() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource aggrER = (AggregateExternalResource) resources.get(i);
                if (aggrER.getSubType().equalsIgnoreCase("ProtoGENI")) {
                    log.debug("start - create external protoGENI sliver: " + aggrER.getUrn());
                    String status = aggrER.createResource();
                    if (status.toUpperCase().contains("FAILED")) {
                        rspec.setStatus("EXT-SLIVER-FAILED");
                        throw (new AggregateException("Failed to allocate externalResource:" + aggrER.getUrn()));
                    }
                    aggrER.setStatus("CREATED");
                    aggrER.setRspecId(rspec.getId());
                    if (AggregateState.getAggregateExtResources().add(aggrER) == false) {
                        throw new AggregateException("Cannot add externalResource:" + aggrER.getUrn() + " to DB ");
                    }
                    log.debug("end - create external protoGENI sliver: " + aggrER.getUrn());
                }
            }
        }
    }

    private void deleteExternalSliver() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource aggrER = (AggregateExternalResource) resources.get(i);
                log.debug("externalResource = " + aggrER.getUrn()); //xxxx
                if (aggrER.getSubType().equalsIgnoreCase("ProtoGENI")) {
                    log.debug("start - delete external protoGENI sliver: " + aggrER.getUrn());
                    AggregateState.getAggregateExtResources().delete(aggrER.getUrn());
                    aggrER.deleteResource();
                    log.debug("end - delete external protoGENI sliver: " + aggrER.getUrn());
                }
            }
        }
    }

    private void createSdxSlivers() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("sdxSliver")) {
                AggregateSdxSliver sdxSliver = (AggregateSdxSliver) resources.get(i);
                sdxSliver.setSliceName(rspec.getRspecName());
                sdxSliver.setSliceUser(rspec.getGeniUser());
                log.debug("start - create SDX sliver: " + sdxSliver.getSliceName());
                String status = sdxSliver.createSliver();
                AggregateState.getAggregateSdxSlivers().add(sdxSliver);
                log.debug("end - create SDX sliver: " + sdxSliver.getSliceName());
            }
        }
    }
    
    private void deleteSdxSlivers() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("sdxSliver")) {
                AggregateSdxSliver sdxSliver = (AggregateSdxSliver) resources.get(i);
                log.debug("start - delete SDX sliver: " + sdxSliver.getSliceName());
                sdxSliver.setStatus("Cancellation - INIT");
                AggregateState.getAggregateSdxSlivers().update(sdxSliver);
                //?? always both cancel and delete ?
                String status = sdxSliver.cancelSliver();
                //if (status.equals("CANCELLED")) {
                //    sdxSliver.deleteSliver();
                //}
                AggregateState.getAggregateSdxSlivers().delete(sdxSliver);
                log.debug("end - delete SDX sliver: " + sdxSliver.getSliceName());
            }
        }
    }
    
    private void pollSdxSlivers() throws AggregateException {
        boolean hasSliver = false;
        boolean allActive = true;
        boolean allCancelled = true;
       List<AggregateResource> resources = rspec.getResources();
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("sdxSliver")) {
                AggregateSdxSliver sdxSliver = (AggregateSdxSliver) resources.get(i);
                log.debug("start - query SDX sliver: " + sdxSliver.getSliceName());
                hasSliver = true;
                String status = sdxSliver.querySliver(false);
                sdxSliver.setStatus(status);
                if (status.contains("FAILED")) {
                    throw new AggregateException(String.format("failed to create SDX Sliver for '%s' with service UUID=%s", sdxSliver.getSliceName(), sdxSliver.getServiceUuid()));
                } else if (status.equals("Creation - READY")) {
                    //@TODO: getManifest for SDX sliver
                    //String statusJson = sdxSliver.querySliver(true);
                    //sdxSliver.setManifestJson(statusJson);
                    allCancelled = false;
                } else if (status.equals("Cancellation - READY")) {
                   allActive = false;
                } else {
                    allActive = false;
                    allCancelled = false;
               }
                AggregateState.getAggregateSdxSlivers().update(sdxSliver);
                log.debug("end - query SDX sliver: " + sdxSliver.getSliceName());
            }
        }
        if (hasSliver) {
            if (allActive) {
                rspec.setStatus("SDX-ACTIVE");
            } else if (allCancelled) {
                rspec.setStatus("SDX-CANCELLED");
            } else {
                rspec.setStatus("SDX-INSETUP");
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
                    long startTime = rspec.getStartTime();
                    long endTime = rspec.getEndTime();
                    p2pvlan.setStartTime(startTime);
                    p2pvlan.setEndTime(endTime);
                    p2pvlan.setDescription(rspec.getRspecName() + String.format(" (%s)", p2pvlan.getClientId()));
                    String status = p2pvlan.setupVlan();
                    if (status.equalsIgnoreCase("FAILED")) {
                        throw (new AggregateException(String.format("Failed to create stitching P2PVlan:%s due to %s.", p2pvlan.getDescription(), p2pvlan.getErrorMessage())));
                    }
                    AggregateState.getAggregateP2PVlans().add(p2pvlan);
                    log.debug("end - creating stitching p2pvlan: " + p2pvlan.getDescription());
                }
            }
        }
    }

    void deleteStitchingResources() throws AggregateException {
        List<AggregateResource> resources = rspec.getResources();
        for (int j = 0; j < resources.size(); j++) {
            if (resources.get(j).getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan) resources.get(j);
                log.debug("p2pvlan = " + p2pvlan.getDescription()); //xxxx
                if (!p2pvlan.getStitchingResourceId().isEmpty()) {
                    log.debug("start - deleting stitching p2pvlan: " + p2pvlan.getDescription());
                    AggregateState.getAggregateP2PVlans().delete(p2pvlan);
                    p2pvlan.teardownVlan();
                    log.debug("end - deleting stitching p2pvlan: " + p2pvlan.getDescription());
                }
            }
        }
    }

    private void provision() {
        log.debug("start - provisioning rspec: " + rspec.getRspecName());
        try {
            //this.modifySlice();
            this.modifyP2PVlans();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        log.debug("end - provisioning rspec: " + rspec.getRspecName());
    }

    private void renew() {
        log.debug("start - renewing rspec: " + rspec.getRspecName());
        try {
            this.modifyP2PVlans();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        log.debug("end - renewing rspec: " + rspec.getRspecName());
    }

    private void rollback() {
        log.debug("start - rolling back rspec: " + rspec.getRspecName() + " with status:" + rspec.getStatus());
        try {
            if (rspec.getStatus().matches("^VLANS.*")) {
                deleteP2PVlans();
                deleteStitchingResources();
                deleteSdxSlivers();
                deleteExternalSliver();
            }
            if (rspec.getStatus().matches("^STITCHING.*")) {
                deleteStitchingResources();
                deleteSdxSlivers();
                deleteExternalSliver();
            }
            if (rspec.getStatus().matches("^SDX.*")) {
                deleteP2PVlans();
                deleteStitchingResources();
                deleteSdxSlivers();
                deleteExternalSliver();
            }
            if (rspec.getStatus().matches("^EXT-SLIVER.*")) {
                deleteExternalSliver();
            }
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        rspec.setStatus("ROLLBACKED:" + rspec.getStatus());
        log.debug("end - rolling back rspec: " + rspec.getRspecName());
    }

    private void terminate() {
        log.debug("start - terminating rspec: " + rspec.getRspecName());
        try {
            deleteP2PVlans();
            deleteStitchingResources();
            deleteSdxSlivers();
            deleteExternalSliver();
        } catch (AggregateException e) {
            log.error("AggregateRspecRunner (rsepcName=" + rspec.getRspecName() + ") Exception:" + e.getMessage());
            e.printStackTrace();
        }
        goRun = false;
        goPoll = false;
        rspec.setStatus("TERMINATED:" + rspec.getStatus());
        log.debug("end - terminating rspec: " + rspec.getRspecName());
    }
}
