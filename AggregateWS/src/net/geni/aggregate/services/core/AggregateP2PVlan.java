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
    int vtag = -1;
    float bandwidth = 0;
    String description = "";
    long startTime = 0;
    long endTime = 0;
    String errMessage = "";
    String status = "";

    private org.apache.log4j.Logger log;
    /**
    * constructors
    */
    public AggregateP2PVlan(){
        log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");
        type = "p2pVlan";
    }

    public AggregateP2PVlan(String sl, String s, String d, int v, float b, String desc, long st, long et) {
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

    public AggregateP2PVlan(int sid, int v, String s, String d, float b, String g, String ss) {
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

    public void setVtag(int vtag) {
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

    public int getVtag() {
        return vtag;
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

        if (!status.equalsIgnoreCase("FAILED") && !setVlanOnNodes(true)) {
            status = "FAILED";
            errMessage = "setupVlan FAILED to add VLAN interface on source or destination node";
        }

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
        status = "FAILED";
        try {
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

    private boolean setVlanOnNodes(boolean add) {
        //login to PLC-ssh (new AggregatePLC_SSHClient)
        AggregatePLC_SSHClient client = AggregatePLC_SSHClient.getPLCClient();
        if (srcInterface.isEmpty() && dstInterface.isEmpty()){
            log.info("No interface deviceName avaible for either source or destination node -- skip vlan interface configuration.");
            return true;
        }

        if (client == null || !client.login()) {
            log.error("cannot instantiate AggregatePLC_SSHClient connection to PLC server");
            return false;
        }

        boolean ret = true;

        //TODO: verify the devices exist for srcInterface and dstInterface
        if (srcInterface.isEmpty()){
            log.info("Source interface deviceName unknown: skip "+(add?"adding":"deleting") + " vlan interface to source node.");
        }      //add/delete source vtag interface
        else if (client.vconfigVlan(source, srcInterface, Integer.toString(vtag), add)) {
            log.info((add?"added":"deleted") + " vlan interface to node "+ source + "on "+ srcInterface + "."  + Integer.toString(vtag));
        }
        else {
            log.warn("failed to " + (add?"add":"delete") + " vlan interface on node "+ source + " " + srcInterface + "." + Integer.toString(vtag));
            if (add)
                log.warn("there might be existing vlan interface of the same tag --> continue to try ip config");

        }
        if (srcInterface.isEmpty() || srcIpAndMask.isEmpty()){
            log.info("Source interface deviceName or IP address unknown: skip configuring IP address on source node.");
        }
        else if (add && !client.ifconfigIp(source, srcInterface + "." + Integer.toString(vtag), srcIpAndMask)) {
            log.error("failed to configure IP address on node " + source + " " + srcInterface + "." + Integer.toString(vtag));
            ret = false;
        }

        //add/delete destination vtag interface
        if (dstInterface.isEmpty()){
            log.info("Destination interface deviceName unknown: skip "+(add?"adding":"deleting") + " vlan interface to destination node.");
        }      //add/delete source vtag interface
        else if (client.vconfigVlan(destination, dstInterface, Integer.toString(vtag), add)) {
            log.info((add?"added":"deleted") + " vlan interface to node "+ destination + "on "+ dstInterface + "." + Integer.toString(vtag));
        }
        else {
            log.warn("failed to " + (add?"add":"delete") + " vlan interface on node "+ destination + " " + dstInterface + "." + Integer.toString(vtag));
            if (add)
                log.warn("there might be existing vlan interface of the same tag --> continue to try ip config");
        }
        if (dstInterface.isEmpty() || dstIpAndMask.isEmpty()){
            log.info("Destination interface deviceName or IP address unknown: skip configuring IP address on destination node.");
        }
        else if (add && !client.ifconfigIp(destination, dstInterface + "." + Integer.toString(vtag), dstIpAndMask)) {
            log.error("failed to configure IP address on node " + destination + " " + dstInterface + "." + Integer.toString(vtag));
            ret = false;
        }

        return ret;
    }
}
