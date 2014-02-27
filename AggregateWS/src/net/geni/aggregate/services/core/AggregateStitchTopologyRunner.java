/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.*;
import org.apache.log4j.*;
import edu.isi.east.hpn.rspec.ext.stitch._0_1.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import org.apache.axis2.AxisFault;
import org.w3c.dom.Node;

/**
 *
 * @author xyang
 */
public class AggregateStitchTopologyRunner extends Thread {
    private volatile boolean goRun = true;
    private volatile int runInterval = 60000; //60 secs by default
    private org.apache.log4j.Logger log;
    private String stitchXml = "";
    private StitchContent stitchObj = null;
    private List<String> listCurrentVlanGri = null;
    
    public AggregateStitchTopologyRunner() {
        super();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public boolean isGoRun() {
        return goRun;
    }

    public void setGoRun(boolean goRun) {
        this.goRun = goRun;
    }

    public int getRunInterval() {
        return runInterval;
    }

    public void setRunInterval(int runInterval) {
        this.runInterval = runInterval;
    }

    public String getStitchXml() {
        synchronized(this) {
            return stitchXml;
        }
    }

    public void setStitchXml(String stitchXml) {
        synchronized(this) {
            this.stitchXml = stitchXml;
        }
    }

    public StitchContent getStitchObj() {
        synchronized(this) {
            return stitchObj;
        }
    }

    public void setStitchObj(StitchContent stitchObj) {
        synchronized(this) {
            this.stitchObj = stitchObj;
        }
    }

    private void loadStitchTopologyFile() {
        synchronized(this) {
            try {
                int ch;
                FileInputStream in = new FileInputStream(AggregateState.getIdcTopoFile());
                this.stitchXml = "";
                while ((ch = in.read()) != -1) {
                    this.stitchXml += ((char) ch);
                }
                in.close();
            } catch (IOException e) {
                log.warn("loadStitchTopology caught IOException: "+e.getMessage());
                return;
            }
            try {
                this.stitchObj = new JAXBHelper<StitchContent>(StitchContent.class).partialUnmarshal(stitchXml);
                if (this.stitchObj == null || this.stitchObj.getAggregate().size() == 0) {
                    log.warn(String.format("malformed stitch topology file '%s' - make sure there is xmlns property in <stitching>"));
                }
            } catch (Exception e) {
                log.warn("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
            }
        }
    }
    
    private void calibrateEndPointVlan() {
        /*
        AggregateIDCClient client = AggregateIDCClient.getIDCClient();
        String errMessage = null;
        String domainId = AggregateState.getIdcDomainId();
        try {
            networkTopology = client.retrieveNetworkTopology(domainId);
        } catch (AxisFault e) {
            errMessage = "AxisFault from queryReservation: " + e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from queryReservation: " + e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from queryReservation: " + e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from queryReservation: " + e.getMessage();
        } catch (Exception e) {
            errMessage = "OSCARSStub threw exception in queryReservation: " + e.getMessage();
        }
        if (errMessage != null) {
            throw new AggregateException(errMessage);
        }
        */
    }
    
    public void run() {
        int minutes = 0;
        long lastModified = 0;
        while (goRun) {            
            if (AggregateState.getIdcTopoFile() != null && !AggregateState.getIdcTopoFile().isEmpty()) {
                // check if AggregateState.getIdcTopoFile() has been updated
                File topoFile = new File(AggregateState.getIdcTopoFile());
                if (topoFile.lastModified() > lastModified) {
                    log.info("stitch topology file updated - reload now");
                    lastModified = topoFile.lastModified();
                    loadStitchTopologyFile();
                    updateTopologyPsql();
                }
            }
            
            updateVlanPsql();
            
            //starting calibrate endpoints VLAN range at 5th minute
            if (minutes % 60 == 5) {
                //$$ poll topology from OSCARS, parse, compare and calibrate
            }
            try {
                this.sleep(runInterval);
            } catch (InterruptedException e) {
                if (!goRun) {
                    break;
                }
            }
            minutes++;
        }
    }
    
    private void updateTopologyPsql() {
        synchronized(this) {
            if (stitchObj == null)
                return;
            String baseUrl = AggregateState.getOpsMonBaseUrl();
            String sql = "BEGIN WORK;\n";
            sql += "LOCK TABLE ops_aggregate IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_node IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_interface IN EXCLUSIVE MODE;\n";
            sql += "DELETE from ops_aggregate;\n";
            sql += "DELETE from ops_node;\n";
            sql += "DELETE from ops_interface;\n";
            // add "insert ops_aggregate" row
            /*  
             * $schema      => "http://unis.incntre.iu.edu/schema/20120709/aggregate#"
             * id           => convert from urn (aggr_id) 
             * selfRef      => http://host:port/info/aggregate/id 
             * urn          => geni urn
             * ts           => stitchObj.lastUpdateTime convert to epoch
             * measRef      => http://host:port/data
             */
            SimpleDateFormat sdf = new SimpleDateFormat("yyyymmdd:hh:mm:ss");
            Date date;
            try {
                date = sdf.parse(stitchObj.getLastUpdateTime());
            } catch (ParseException ex) {
                return;
            }                
            long ts = date.getTime();            
            for (AggregateContent aggregate: stitchObj.getAggregate()) {
                String aggrUrn = aggregate.getId();
                String aggrId = AggregateUtils.getUrnField(aggrUrn, "aggregate");
                sql += String.format("INSERT INTO ops_aggregate VALUES ('http://unis.incntre.iu.edu/schema/20120709/aggregate#', '%s', '%s', '%s', %d, '%s');\n",
                        aggrId, baseUrl+"info/aggregate/"+aggrId, aggrUrn, ts, baseUrl+"data");
                for (NodeContent node: aggregate.getNode()) {
                    //$$ add "insert ops_node" row
                    /*
                     * $schema      => "http://unis.incntre.iu.edu/schema/20120709/node#"
                     * id           => convert from urn (aggr_id.node_id) 
                     * selfRef      => http://host:port/info/node/id 
                     * urn          => geni urn
                     * ts           => stitchObj.lastUpdateTime convert to epoch
                     * mem_total_kb => null
                     */
                    String nodeUrn = node.getId();
                    String nodeId = aggrId + "." + AggregateUtils.getUrnField(nodeUrn, "node");;
                    sql += String.format("INSERT INTO ops_node VALUES ('http://unis.incntre.iu.edu/schema/20120709/node#', '%s', '%s', '%s', %d, null);\n",
                        nodeId, baseUrl+"info/node/"+nodeId, nodeUrn, ts);
                    for (PortContent port: node.getPort()) {
                        for (LinkContent link: port.getLink()) {
                            //$$ add "insert ops_interface" row
                            // INSERT INTO ops_interface VALUES ('', '', '', '', 0, null, null, '', 0, null);
                            /*
                             * $schema      => "http://unis.incntre.iu.edu/schema/20120709/port#"
                             * id           => convert from urn (aggr_id.interface_id) 
                             * selfRef      => http://host:port/info/interface/id 
                             * urn          => geni urn
                             * ts           => stitchObj.lastUpdateTime convert to epoch
                             * address_type => n/a
                             * address_address => n/a 
                             * role         => transport
                             * max_bps      => capacity  
                             * max_pps      => n/a
                             */
                            String ifUrn = link.getId();
                            String ifUrnFields[] = ifUrn.split("\\+");
                            String ifId = aggrId + "." + ifUrnFields[ifUrnFields.length-1];
                            sql += String.format("INSERT INTO ops_interface VALUES ('http://unis.incntre.iu.edu/schema/20120709/port#', '%s', '%s', '%s', %d, null, null, 'transport', %d, null);\n",
                                ifId, baseUrl+"info/interface/"+ifId, ifUrn, ts, Long.parseLong(link.getCapacity()));
                        }
                    }
                }
            }
            sql += "COMMIT WORK;\n";
            try {
                FileOutputStream out = new FileOutputStream("/tmp/ops_mon_aggr.sql");
                out.write(sql.getBytes());
                out.close();
            } finally {
                return;
            }
        }
    }

    private void updateVlanPsql() {
        String baseUrl = AggregateState.getOpsMonBaseUrl();
        String sql = "BEGIN WORK;\n";
        sql += "LOCK TABLE ops_vlan IN SHARE ROW EXCLUSIVE MODE;\n";
        List<AggregateP2PVlan> p2pvlans = AggregateState.getAggregateP2PVlans().getAll();
        if (listCurrentVlanGri == null) {
            listCurrentVlanGri = new ArrayList<String>();
        }
        for (AggregateP2PVlan p2pvlan: p2pvlans) {
            if (!listCurrentVlanGri.contains(p2pvlan.getGlobalReservationId())) {
                listCurrentVlanGri.add(p2pvlan.getGlobalReservationId());
                //$$ add "insert VLAN" row(s)
                // INSERT INTO ops_vlan VALUES ('', '', '', '', 0, 0, '', '', '');
                /*
                 * $schema      => "http://unis.incntre.iu.edu/schema/20120709/vlan#"
                 * id           => convert from interface_urn (aggr_id.interface_id:vlan) 
                 * selfRef      => http://host:port/info/interface/id 
                 * urn          => geni interface_urn -> vlan+id
                 * ts           => startTime convert to epoch
                 * expires      => endTime convert to epoch
                 * sliceUrn     => sliceName
                 * sliverUrn    => urn + '_vlan_' + GRI 
                 * circuitRef   => GRI
                 */
                String ifUrn;
                try {
                    ifUrn = AggregateUtils.convertDcnToGeniUrn(p2pvlan.getSrcInterface());
                } catch (AggregateException ex) {
                    continue;
                }
                String vlanUrn = ifUrn.replace("+interface+", "+vlan+");
                String vlans[] = p2pvlan.getVtag().split(":");
                vlanUrn = vlanUrn + ":" + vlans[0];
                String vlanUrnFields[] = vlanUrn.split("\\+");
                String vlanId = vlanUrnFields[1] + "." + vlanUrnFields[vlanUrnFields.length-1];
                String gri = p2pvlan.getGlobalReservationId();
                String sliceUrn = p2pvlan.getSliceName();
                String sliverUrn = sliceUrn.replace("+slice+", "+sliver+");
                sliverUrn = sliverUrn + "_vlan_" + gri;
                // add VLAN for ingress
                sql += String.format("INSERT INTO ops_vlan VALUES ('http://unis.incntre.iu.edu/schema/20120709/vlan#', '%s', '%s', '%s', %d, null, null, 'transport', %d, null);\n",
                    vlanId, baseUrl+"info/vlan/"+vlanId, vlanUrn, p2pvlan.getStartTime(), p2pvlan.getEndTime(), sliceUrn, sliverUrn, gri);
                try {
                    ifUrn = AggregateUtils.convertDcnToGeniUrn(p2pvlan.getDstInterface());
                } catch (AggregateException ex) {
                    continue;
                }
                vlanUrn = ifUrn.replace("+interface+", "+vlan+");
                vlanUrn = vlanUrn + ":" + (vlans.length > 1 ?  vlans[1] : vlans[0]);
                vlanUrnFields = vlanUrn.split("\\+");
                vlanId = vlanUrnFields[1] + "." + vlanUrnFields[vlanUrnFields.length-1];
                // add VLAN for egress
                sql += String.format("INSERT INTO ops_vlan VALUES ('http://unis.incntre.iu.edu/schema/20120709/vlan#', '%s', '%s', '%s', %d, null, null, 'transport', %d, null);\n",
                    vlanId, baseUrl+"info/vlan/"+vlanId, vlanUrn, p2pvlan.getStartTime(), p2pvlan.getEndTime(), sliceUrn, sliverUrn, gri);
            }
        }
        Iterator<String> itGri = listCurrentVlanGri.iterator();
        while (itGri.hasNext()) {
            String gri = itGri.next();
            boolean needToDelete = true;
            for (AggregateP2PVlan p2pvlan: p2pvlans) {
                if (gri.equals(p2pvlan.getGlobalReservationId())) {
                    needToDelete = false;
                }
            }
            if (needToDelete) {
                sql += String.format("DELETE from ops_vlan WHERE circuitRef='%s';\n", gri);
                itGri.remove();
            }
        }
        sql += "COMMIT WORK;\n";
        try {
            FileOutputStream out = new FileOutputStream("/tmp/ops_mon_vlan.sql");
            out.write(sql.getBytes());
            out.close();
        } finally {
            return;
        }
    }
   
    
    public LinkContent getLinkByUrn (String urn) {
        synchronized(this) {
            if (stitchObj == null)
                return null;
            for (AggregateContent aggregate: stitchObj.getAggregate()) {
                for (NodeContent node: aggregate.getNode()) {
                    for (PortContent port: node.getPort()) {
                        for (LinkContent link: port.getLink()) {
                            if (link.getId().equalsIgnoreCase(urn))
                                return link;
                        }
                    }
                }
            }
            return null;
        }
    }
    
    public boolean isValidEndPoint(String urn) {
        if (!AggregateState.isIdcVerifyEndpoints())
            return true;
        if (this.getLinkByUrn(urn) == null) {
            return false;
        }
        return true;
    }

    public boolean isValidBandwidth(String urn, long bw) {
        if (!AggregateState.isIdcVerifyEndpoints())
            return true;
        LinkContent link = this.getLinkByUrn(urn);
        if (link == null) {
            return false;
        }
        synchronized(this) {
            long max = 100000000L; //100G by default, bw in kbps
            if (link.getMaximumReservableCapacity() != null && !link.getMaximumReservableCapacity().isEmpty()) {
                max = Long.valueOf(link.getMaximumReservableCapacity());
            }
            long min = 0;
            if (link.getMinimumReservableCapacity() != null && !link.getMinimumReservableCapacity().isEmpty()) {
                min = Long.valueOf(link.getMinimumReservableCapacity());
            }
            long granularity = 1;
            if (link.getGranularity() != null && !link.getGranularity().isEmpty()) {
                granularity = Long.valueOf(link.getGranularity());
            }
            
            if (bw > max || bw < min || bw % granularity != 0) {
                return false;
            }
            return true;
        }
    }
    
    public boolean isValidVlan(String urn, String vtag) {
        if (!AggregateState.isIdcVerifyEndpoints())
            return true;
        if (vtag.equalsIgnoreCase("any")) {
            return true;
        }
        int tag = 0;
        try {
            tag = Integer.parseInt(vtag);
        } catch (NumberFormatException ex) {
            return false;
        }
        if (tag <= 0 || tag > 4095) {
            return false;
        }
        LinkContent link = this.getLinkByUrn(urn);
        if (link == null) {
            return false;
        }
        synchronized(this) {
            List<SwitchingCapabilityDescriptor> swcapList = link.getSwitchingCapabilityDescriptor();
            if (swcapList == null || swcapList.isEmpty())
                return false;
            for (SwitchingCapabilityDescriptor swcap: swcapList) {
                if (!swcap.getSwitchingcapType().equalsIgnoreCase("l2sc")
                    || swcap.getSwitchingCapabilitySpecificInfo() == null 
                    || swcap.getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc() == null 
                    || swcap.getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc().isEmpty()) {
                    continue;
                }
                SwitchingCapabilitySpecificInfoL2Sc l2scInfo = swcap.getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc().get(0);
                try {
                    VlanRange vlanRange = new VlanRange(l2scInfo.getVlanRangeAvailability());
                    if (!vlanRange.isEmpty() && vlanRange.hasVlan(tag)) {
                        return true;
                    }
                } catch (Exception ex) {
                    return false;
                }
            }
            return false;
        }
    }
}
