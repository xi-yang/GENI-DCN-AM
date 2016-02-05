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
    String status = "INIT"; // a "IN*" status -> 'changing'

    private String stitchingResourceId = "";
    private String externalResourceId = "";

    private org.apache.log4j.Logger log;
    /**
    * constructors
    */
    public AggregateP2PVlan(){
        log = org.apache.log4j.Logger.getLogger(this.getClass());
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
        } finally {
            if (status.equals("INIT")) {
                status = "FAILED";
            }
        }

        log.debug("setupVlan: gri="+gri+" source="+source+", destination="+destination+",vtag="+vtag+",startTime="+Long.toString(startTime)+",endTime="+Long.toString(endTime));

        if (gri == null || gri.isEmpty()) {
            status = "FAILED";
            errMessage = "IDC_APIClient::createReservation returned null GRI.";
        }

        return status;
     }

     /**
     * modify p2p vlan
     * @param
     * @return
     */
     public String modifyVlan() {
        if (apiClient == null)
            apiClient = AggregateIDCClient.getIDCClient();
        try {
            status = apiClient.modifyReservation(gri, source, destination, vtag, bandwidth, description, startTime, endTime);
            for (int i = 0; i < 3; i++) {
                AggregateUtils.justSleep(60); // sleep 60 seconds waiting for modification to finish
                HashMap hmRet = apiClient.queryReservation(gri);
                status = hmRet.get("status").toString();
                if (status.contains("IN") || status.contains("CACULAT") || status.contains("COMMIT")) {
                    continue;
                } else {
                    return status;
                }
            }
        } catch (AxisFault e) {
            errMessage = "AxisFault from modifyReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from modifyReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from modifyReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from modifyReservation: " +e.getMessage();
        }catch (Exception e) {
            errMessage = "OSCARS modifyReservation has failed or aborted.";
        }
        if (errMessage.isEmpty()) {
            errMessage = "Failed to modify the VLAN circuit via modifyReservation.";
        }
        return "FAILED";
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
            if (status.equals("CANCELLED") || status.equals("FAILED") || status.equals("UNKNOWN") || status.equals("INTEARDOWN")) {
                return status;
            }
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
        boolean finishedQuery = false;
        try {
            HashMap hmRet = apiClient.queryReservation(gri);
            if (hmRet.get("status") != null) {
                status = hmRet.get("status").toString();
            }
            if (hmRet.get("vlanTag") != null) {
                vtag = hmRet.get("vlanTag").toString();
            }
            if (hmRet.get("errMessage") != null) {
                errMessage = hmRet.get("errMessage").toString();
            }
            if (status.equals("unknown")) {
                status = "UNKNOWN";
                if (errMessage.isEmpty()) {
                    errMessage = "VLAN circuit in unknown status";
                }
            }
            finishedQuery = true;
        } catch (AxisFault e) {
            if (errMessage.isEmpty())
                errMessage = "AxisFault from queryReservation: " +e.getMessage();
        } catch (AAAFaultMessage e) {
            if (errMessage.isEmpty())
                errMessage = "AAAFaultMessage from queryReservation: " +e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            if (errMessage.isEmpty())
                errMessage = "BSSFaultMessage from queryReservation: " +e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            if (errMessage.isEmpty())
                errMessage = "RemoteException returned from queryReservation: " +e.getMessage();
        } catch (Exception e) {
            if (errMessage.isEmpty())
                errMessage = "OSCARSStub threw exception in queryReservation: " +e.getMessage();
        } finally {
            if (!finishedQuery) {
                status = "UNKNOWN"; // Allow for second chance to correct query result.
            }
        }

        return getVlanResvResult();
     }
}
