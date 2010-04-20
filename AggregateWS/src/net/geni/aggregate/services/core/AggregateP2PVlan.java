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

/**
 *
 * @author xyang
 */
public class AggregateP2PVlan extends AggregateResource {

    //IDCAPIClient
    AggregateIDCClient apiClient = null;
    //Reservation parameters
    int id = 0;
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
    }

    public void setId(int id) {
        this.id = id;
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

    public int getId() {
        return id;
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
            status = "failed";
            errMessage = "Error: cannot recreate an existing circuit";
            return status;
        }
        status = "failed";
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

        if (!status.equalsIgnoreCase("failed") && !setVlanOnNodes(true)) {
            status = "failed";
            errMessage = "setupVlan failed to add VLAN interface on source or destination node";
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
        status = "failed";
        try {
            status = apiClient.cancelReservation(gri);
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
            status = "failed";
            errMessage += "teardownVlan failed to delete VLAN interface on source or destination node";
        }

        return status;
     }

     /**
     * Query p2p vlan status
     * @param
     * @return
     */
     public HashMap queryVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        HashMap hmRet = new HashMap();
        status = "unknown"; //?
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

        return hmRet;
     }

    private boolean setVlanOnNodes(boolean add) {
        //login to PLC-ssh (new AggregatePLC_SSHClient)
        AggregatePLC_SSHClient client = AggregatePLC_SSHClient.getPLCClient();
        if (client == null || !client.login()) {
            log.error("cannot instantiate AggregatePLC_SSHClient connection to PLC server");
            return false;
        }

        boolean ret = true;

        //TODO: verify the devices exist for srcInterface and dstInterface
        
        //add/delete source vtag interface
        if (client.vconfigVlan(source, srcInterface, Integer.toString(vtag), add)) {
            log.info((add?"added":"deleted") + " vlan interface to node "+ source + "on "+ srcInterface + "."  + Integer.toString(vtag));
        }
        else {
            log.warn("failed to " + (add?"add":"delete") + " vlan interface on node "+ source + " " + srcInterface + "." + Integer.toString(vtag));
            if (add)
                log.warn("there might be existing vlan interface of the same tag --> continue to try ip config");

        }
        if (add && !client.ifconfigIp(source, srcInterface + "." + Integer.toString(vtag), srcIpAndMask)) {
            log.error("failed to configure IP address on node " + source + " " + srcInterface + "." + Integer.toString(vtag));
            ret = false;
        }

        //add/delete destination vtag interface
        if (client.vconfigVlan(destination, dstInterface, Integer.toString(vtag), add)) {
            log.info((add?"added":"deleted") + " vlan interface to node "+ destination + "on "+ dstInterface + "." + Integer.toString(vtag));
        }
        else {
            log.warn("failed to " + (add?"add":"delete") + " vlan interface on node "+ destination + " " + dstInterface + "." + Integer.toString(vtag));
            if (add)
                log.warn("there might be existing vlan interface of the same tag --> continue to try ip config");
        }
        if (add && !client.ifconfigIp(destination, dstInterface + "." + Integer.toString(vtag), dstIpAndMask)) {
            log.error("failed to configure IP address on node " + destination + " " + dstInterface + "." + Integer.toString(vtag));
            ret = false;
        }

        return ret;
    }
}
