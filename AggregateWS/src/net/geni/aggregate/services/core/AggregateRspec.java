/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.geni.aggregate.services.api.VlanReservationResultType;

/**
 *
 * @author Xi Yang
 */
public class AggregateRspec implements java.io.Serializable {
    private org.apache.log4j.Logger log;
    private int id = 0 ;
    private String rspecName = "";
    private String aggregateName = "";
    private String description = "";
    private String geniUser = null;
    private long startTime = 0;
    private long endTime = 0;
    private List<String> users = null;
    private List<AggregateResource> resources = null;
    private boolean addPlcSlice = false;
    private String requestXml = null;
    private String manifestXml = null;
    private String status = "";
    private boolean deleted = false;

    public AggregateRspec() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
        resources = new ArrayList<AggregateResource>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAggregateName(String aggregateName) {
        this.aggregateName = aggregateName;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setRspecName(String rspecName) {
        this.rspecName = rspecName;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public String getAggregateName() {
        return aggregateName;
    }

    public String getRspecName() {
        return rspecName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeniUser() {
        return geniUser;
    }

    public void setGeniUser(String geniUser) {
        this.geniUser = geniUser;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<AggregateResource> getResources() {
        return resources;
    }

    public boolean isAddPlcSlice() {
        return addPlcSlice;
    }

    public void setAddPlcSlice(boolean addPlcSlice) {
        this.addPlcSlice = addPlcSlice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestXml() {
        return requestXml;
    }

    public void setRequestXml(String requestXml) {
        this.requestXml = requestXml;
    }

    public String getManifestXml() {
        return manifestXml;
    }

    public void setManifestXml(String manifestXml) {
        this.manifestXml = manifestXml;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    HashMap retrieveRspecInfo() {
        HashMap hm = new HashMap();
        if (geniUser != null) {
            hm.put("geniUser", geniUser);
        }
        for (AggregateResource rc: resources) {
            if (rc.getType().equalsIgnoreCase("sdxSliver")) {
                hm.put("sliceStatus", ((AggregateSdxSliver)rc).getStatus());
            } else if (rc.getType().equalsIgnoreCase("p2pVlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan)rc;
                Vector<VlanReservationResultType> vlanResults = (Vector<VlanReservationResultType>)hm.get("vlanResults");
                if (vlanResults == null) {
                    vlanResults = new Vector<VlanReservationResultType>();
                    hm.put("vlanResults", vlanResults);
                }
                vlanResults.add(p2pvlan.getVlanResvResult());
            } else if (rc.getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource ER = (AggregateExternalResource)rc;
                hm.put("externalResourceStatus", ER.getSubType()+":"+ER.getUrn()+":"+ER.getStatus()+":"+ER.getRspecData());
            }
        }
        // updated expiration
        try {
            Date expires = new Date(this.getEndTime() * 1000);
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(expires);
            XMLGregorianCalendar xgcExpires = null;
            xgcExpires = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            hm.put("expires", xgcExpires.toString());
        } catch (Exception e) {
            ; //nnop
        }
        return hm;
    }


    void dumpRspec() {
        log.debug("Rspec name=" + this.rspecName + "aggregateName="+this.aggregateName);
        log.debug("Rspec startTime=" + Integer.toString((int)this.startTime)+" endTime="+Integer.toString((int)this.endTime));
        for (AggregateResource rc: resources) {
            log.debug("Resource: " + rc.getType());
            if (rc.getType().startsWith("computeNode")) {
                log.debug("   >>" + ((AggregateNode)rc).getDescription());
            } else if (rc.getType().equalsIgnoreCase("networkInterface")) {
                log.debug("   >>>>" + ((AggregateNetworkInterface)rc).getUrn());
            } else if (rc.getType().equalsIgnoreCase("p2pVlan")) {
                log.debug("   >>>>" + ((AggregateP2PVlan)rc).getDescription()
                    + " bandwdith="+Float.toString(((AggregateP2PVlan)rc).getBandwidth()));
            }
        }
    }
}
