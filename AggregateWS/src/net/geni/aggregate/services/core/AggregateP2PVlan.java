/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.*;
import java.util.*;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import org.apache.log4j.*;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;
import net.es.oscars.PropHandler;


import net.geni.aggregate.services.api.VlanReservationDescriptorType;
import net.geni.aggregate.services.api.VlanReservationResultType;

/**
 *
 * @author xyang
 */
public class AggregateP2PVlan extends AggregateResource {

    //IDCAPIClient
    AggregateIDCClient apiClient = null;
    //Reservation parameters
    String gri = "";
    String sliceName = "";
    String source = "";
    String destination = "";
    String srcInterface = "";
    String dstInterface = "";
    String srcIpAndMask = "";
    String dstIpAndMask = "";
    String vtag = "";
    float bandwidth = 0;
    String description = "";
    long startTime = 0;
    long endTime = 0;
    String errMessage = "";
    String status = "";

    private String stitchingResourceId = "";
    private String externalResourceId = "";

    private org.apache.log4j.Logger log;
    /**
    * constructors
    */
    public AggregateP2PVlan(){
        log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");
        type = "p2pVlan";
    }

    public AggregateP2PVlan(String sl, String s, String d, String v, float b, String desc, long st, long et) {
        apiClient = null;
        gri = "";
        sliceName = sl;
        source = s;
        destination = d;
        vtag = v;
        bandwidth = b;
        description = desc;
        startTime = st;
        endTime = et;
        errMessage = "";
        status = "";
        log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");
        type = "p2pVlan";
    }

    public AggregateP2PVlan(int sid, String v, String s, String d, float b, String g, String ss) {
        apiClient = null;
        sliceName = AggregateState.getAggregateSlices().getById(sid).getSliceName();
        source = s;
        destination = d;
        vtag = v;
        bandwidth = b;
        gri = g;
        status = ss;
        description = "";
        errMessage = "";
        startTime = 0;
        endTime = 0;
        log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");
        type = "p2pVlan";
    }

    public void setBandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setGri(String gri) {
        this.gri = gri;
    }

    public void setSliceName(String sliceName) {
        this.sliceName = sliceName;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setVtag(String vtag) {
        this.vtag = vtag;
    }

    public String getSliceName() {
        return sliceName;
    }

    public float getBandwidth() {
        return bandwidth;
    }

    public String getDescription() {
        return description;
    }

    public String getDestination() {
        return destination;
    }

    public String getSource() {
        return source;
    }

    public String getVtag() {
        return vtag;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getGlobalReservationId() {
        if (!gri.equals(""))
            return gri;
        if (apiClient == null)
            return "";
        gri = apiClient.getGlobalReservationId();
        return gri;
    }

    public String getGri() {
        return gri;
    }

    public String getErrorMessage() {
        return errMessage;
    }

    public String getStatus() {
        return status;
    }

    public String getDstInterface() {
        return dstInterface;
    }

    public void setDstInterface(String dstInterface) {
        this.dstInterface = dstInterface;
    }

    public String getDstIpAndMask() {
        return dstIpAndMask;
    }

    public void setDstIpAndMask(String dstIpAndMask) {
        this.dstIpAndMask = dstIpAndMask;
    }

    public String getSrcInterface() {
        return srcInterface;
    }

    public void setSrcInterface(String srcInterface) {
        this.srcInterface = srcInterface;
    }

    public String getSrcIpAndMask() {
        return srcIpAndMask;
    }

    public void setSrcIpAndMask(String srcIpAndMask) {
        this.srcIpAndMask = srcIpAndMask;
    }

    public String getExternalResourceId() {
        return externalResourceId;
    }

    public void setExternalResourceId(String externalResourceId) {
        this.externalResourceId = externalResourceId;
    }

    public String getStitchingResourceId() {
        return stitchingResourceId;
    }

    public void setStitchingResourceId(String stitchingResourceId) {
        this.stitchingResourceId = stitchingResourceId;
    }

    /**
     * setup p2p vlan
     * @param
     * @return
     */
    public String setupVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        else if (!gri.equals("")) {
            status = "FAILED";
            errMessage = "Error: cannot recreate an existing circuit";
            return status;
        }
        status = "FAILED";
        try {
            status = apiClient.createReservation(source, destination, vtag, bandwidth, description, startTime, endTime);
            errMessage = "";
            gri = apiClient.getGlobalReservationId();
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from createReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from createReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from createReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from createReservation: " +e.getMessage();
        } catch (Exception e) {
            errMessage = "OSCARSStub threw exception in createReservation: " +e.getMessage();
        }

        log.debug("setupVlan: gri="+gri+" source="+source+", destination="+destination+",vtag="+vtag+",startTime="+Long.toString(startTime)+",endTime="+Long.toString(endTime));

        if (gri == null || gri.isEmpty()) {
            status = "FAILED";
            errMessage = "IDC_APIClient::createReservation returned null GRI.";
        }
        /*
        else {
            if (vtag.contains("any")) {
                //wait for 10 seconds for IDC to compute path and vlan tag
                AggregateUtils.justSleep(20);
                this.queryVlan();
            }

            if (!status.equalsIgnoreCase("FAILED") && !setVlanOnNodes(true)) {
                status = "FAILED";
                errMessage = "setupVlan FAILED to add VLAN interface on source or destination node";
            }
        }
        */
        return status;
     }

     /**
     * teardown p2p vlan
     * @param
     * @return
     */
     public String teardownVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        try {
            HashMap hmRet = apiClient.queryReservation(gri);
            status = hmRet.get("status").toString();
            if (status.equals("CANCELLED") || status.equals("FAILED") || status.equals("INTEARDOWN")) {
                return status;
            }
            status = "FAILED";
            status = apiClient.cancelReservation(gri);
            if (status.contains("Reservation cancellation status: ")) {
                status = status.replaceAll("Reservation cancellation status:\\s", "").toUpperCase();
            }
            errMessage = "";
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from cancelReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from cancelReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from cancelReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from cancelReservation: " +e.getMessage();
        }catch (Exception e) {
            errMessage = "OSCARSStub threw exception in cancelReservation: " +e.getMessage();
        }

        if (!setVlanOnNodes(false)) {
            status = "FAILED";
            errMessage += "teardownVlan failed to delete VLAN interface on source or destination node";
        }

        return status;
     }

     /**
     * Query p2p vlan status
     * @param
     * @return
     */
    public VlanReservationResultType getVlanResvResult() {
        VlanReservationDescriptorType vlanDescr = new VlanReservationDescriptorType();
        vlanDescr.setDescription(this.description);
        vlanDescr.setSourceNode(this.source);
        vlanDescr.setSrcInterface(this.srcInterface);
        vlanDescr.setSrcIpAndMask(this.srcIpAndMask);
        vlanDescr.setDestinationNode(this.destination);
        vlanDescr.setDstInterface(this.dstInterface);
        vlanDescr.setDstIpAndMask(this.dstIpAndMask);
        vlanDescr.setBandwidth(this.bandwidth);
        vlanDescr.setVlan(this.vtag);

        VlanReservationResultType vlanResult = new VlanReservationResultType();
        vlanResult.setReservation(vlanDescr);
        vlanResult.setGlobalReservationId(this.getGlobalReservationId());
        vlanResult.setStatus(this.status);
        vlanResult.setMessage(this.errMessage);
        return vlanResult;
    }

     public VlanReservationResultType queryVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        HashMap hmRet = new HashMap();
        status = "unknown";
        hmRet.put("status", status);
        try {
            hmRet = apiClient.queryReservation(gri);
            status = hmRet.get("status").toString();
            vtag = hmRet.get("vlanTag").toString();
        }
        catch (AxisFault e) {
            errMessage = "AxisFault from queryReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from queryReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from queryReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from queryReservation: " +e.getMessage();
        } catch (Exception e) {
            errMessage = "OSCARSStub threw exception in queryReservation: " +e.getMessage();
        }

        return getVlanResvResult();
     }

    public boolean setVlanOnNodes(boolean add) {
        //login to PLC-ssh (new AggregatePLC_SSHClient)
        AggregatePLC_SSHClient client = AggregatePLC_SSHClient.getPLCClient();
        if (srcInterface.isEmpty() && dstInterface.isEmpty()){
            log.info("No interface deviceName avaible for either source or destination node -- skip vlan interface configuration.");
            return true;
        }
        if (vtag.isEmpty() || vtag.equalsIgnoreCase("untagged") || vtag.equalsIgnoreCase("any")){
            log.info("Cannot configure tagged interface on nodes: VLAN tag must be between 1 and 4095");
            return true;
        }
        if (client == null || !client.login()) {
            log.error("cannot instantiate AggregatePLC_SSHClient connection to PLC server");
            return false;
        }

        String srcNode = source;
        if (!srcInterface.isEmpty() && srcNode.startsWith("urn:")) {
            AggregateNetworkInterface srcIf = AggregateState.getAggregateInterfaces().getByAttachedLink(source);
            if (srcIf == null) {
                log.error("cannot find a host interface attached to source link: " + source);
                return false;
            }
            String srcNodeUrn = srcIf.getUrn();
            srcNode = AggregateUtils.getUrnField(srcNodeUrn, "node").toLowerCase();
        }
        String dstNode = destination;
        if (!dstInterface.isEmpty() && dstNode.startsWith("urn:")) {
            AggregateNetworkInterface dstIf = AggregateState.getAggregateInterfaces().getByAttachedLink(destination);
            if (dstIf == null) {
                log.error("cannot find a host interface attached to destination link: " + destination);
                return false;
            }
            String dstNodeUrn = (AggregateState.getAggregateInterfaces().getByAttachedLink(destination)).getUrn();
            dstNode = AggregateUtils.getUrnField(dstNodeUrn, "node").toLowerCase();
        }

        String srcVlan = AggregateUtils.parseVlanTag(vtag, true);
        String dstVlan = AggregateUtils.parseVlanTag(vtag, false);
        
        boolean ret = true;
        

        //TODO: verify the devices exist for srcInterface and dstInterface
        if (srcInterface.isEmpty()){
            log.info("Source interface deviceName unknown: skip "+(add?"adding":"deleting") + " vlan interface to source node.");
        }      //add/delete source vtag interface
        else if (client.vconfigVlan(srcNode, srcInterface, srcVlan, add)) {
            log.info((add?"added":"deleted") + " vlan interface to node "+ srcNode + "on "+ srcInterface + "."  + srcVlan);
        }
        else {
            log.warn("failed to " + (add?"add":"delete") + " vlan interface on node "+ srcNode + " " + srcInterface + "." + srcVlan);
            if (add)
                log.warn("there might be existing vlan interface of the same tag --> continue to try ip config");

        }
        if (srcInterface.isEmpty() || srcIpAndMask.isEmpty()){
            log.info("Source interface deviceName or IP address unknown: skip configuring IP address on source node.");
        }
        else if (add && !client.ifconfigIp(srcNode, srcInterface + "." + srcVlan, srcIpAndMask)) {
            log.error("failed to configure IP address on node " + srcNode + " " + srcInterface + "." + srcVlan);
            ret = false;
        }

        //if (add && !client.restartNodeManager(srcNode)) {
        //    log.error("failed to restart nodemanager on node " + srcNode);
        //}
        
        //add/delete destination vtag interface
        if (dstInterface.isEmpty()){
            log.info("Destination interface deviceName unknown: skip "+(add?"adding":"deleting") + " vlan interface to destination node.");
        }      //add/delete source vtag interface
        else if (client.vconfigVlan(dstNode, dstInterface, dstVlan, add)) {
            log.info((add?"added":"deleted") + " vlan interface to node "+ dstNode + "on "+ dstInterface + "." + dstVlan);
        }
        else {
            log.warn("failed to " + (add?"add":"delete") + " vlan interface on node "+ dstNode + " " + dstInterface + "." + dstVlan);
            if (add)
                log.warn("there might be existing vlan interface of the same tag --> continue to try ip config");
        }
        if (dstInterface.isEmpty() || dstIpAndMask.isEmpty()){
            log.info("Destination interface deviceName or IP address unknown: skip configuring IP address on destination node.");
        }
        else if (add && !client.ifconfigIp(dstNode, dstInterface + "." + dstVlan, dstIpAndMask)) {
            log.error("failed to configure IP address on node " + dstNode + " " + dstInterface + "." + dstVlan);
            ret = false;
        }

        //if (add && !client.restartNodeManager(dstNode)) {
        //    log.error("failed to restart nodemanager on node " + dstNode);
        //}

        return ret;
    }
}
