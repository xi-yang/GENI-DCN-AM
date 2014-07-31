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
        // empty two ops_monitoring script file
        FileOutputStream out;
        try {
            out = new FileOutputStream("/tmp/ops_mon_aggr.sql");
            out.write((new String()).getBytes());
            out.close();
        } catch (Exception ex) {
            log.warn("failed to empty /tmp/ops_mon_aggr.sql");
        }
        try {
            out = new FileOutputStream("/tmp/ops_mon_vlan.sql");
            out.write((new String()).getBytes());
            out.close();
        } catch (Exception ex) {
            log.warn("failed to empty /tmp/ops_mon_vlan.sql");
        }
        // enter check and update loop
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
            String measRefUrl = AggregateState.getOpsMonDataUrl();
            String sql = "BEGIN WORK;\n";
            sql += "LOCK TABLE ops_opsconfig IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_aggregate IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_opsconfig_aggregate IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_node IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_interface IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_aggregate_resource IN EXCLUSIVE MODE;\n";
            sql += "LOCK TABLE ops_node_interface IN EXCLUSIVE MODE;\n";
            sql += "DELETE from ops_opsconfig;\n";
            sql += "DELETE from ops_aggregate;\n";
            sql += "DELETE from ops_opsconfig_aggregate;\n";
            sql += "DELETE from ops_node;\n";
            sql += "DELETE from ops_interface;\n";
            sql += "DELETE from ops_node_interface;\n";
            sql += "DELETE from ops_aggregate_resource WHERE urn LIKE '%%+node+%%';\n";
            
            // INSERT INTO ops_opsconfig
            /*
             $schema        => http://www.gpolab.bbn.com/monitoring/schema/20140501/opsconfig#
             id             => geni-prod
             selfRef        => https://host:port/info/opsconfig/id
             ts             
            */
            // INSERT INTO ops_aggregate
            /*  
             * $schema      => "http://www.gpolab.bbn.com/monitoring/schema/20140501/aggregate#"
             * id           => convert from urn (aggr_id) 
             * selfRef      => http://host:port/info/aggregate/id 
             * urn          => geni urn
             * ts           => stitchObj.lastUpdateTime convert to epoch
             * measRef      => http://host:port/data
             */
            // INSERT INTO ops_opsconfig_aggregate
            /*
             id
             opsconfig_id
             amtype         => max
             urn 
             selfRef
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
                String opsconfigId = "geni-prod";
                String aggrUrn = aggregate.getId();
                String aggrId = aggrUrn.split("\\+")[1];
                sql += String.format("INSERT INTO ops_opsconfig VALUES ('http://www.gpolab.bbn.com/monitoring/schema/20140501/opsconfig#', '%s', '%s', %d);\n",
                        opsconfigId, baseUrl+"info/opsconfig/"+opsconfigId, ts*1000);
                sql += String.format("INSERT INTO ops_aggregate VALUES ('http://www.gpolab.bbn.com/monitoring/schema/20140501/aggregate#', '%s', '%s', '%s', %d, '%s');\n",
                        aggrId, baseUrl+"info/aggregate/"+aggrId, aggrUrn, ts*1000, measRefUrl);
                sql += String.format("INSERT INTO ops_opsconfig_aggregate VALUES ('%s', '%s', 'ion', '%s', '%s');\n",
                        aggrId, opsconfigId, aggrUrn, baseUrl+"info/aggregate/"+aggrId);
                for (NodeContent node: aggregate.getNode()) {
                    //$$ add "insert ops_node" row
                    /*
                     * $schema      => "http://www.gpolab.bbn.com/monitoring/schema/20140501/node#"
                     * id           => convert from urn (aggr_id.node_id) 
                     * selfRef      => http://host:port/info/node/id 
                     * urn          => geni urn
                     * ts           => stitchObj.lastUpdateTime convert to epoch
                     * mem_total_kb => null
                     * vm_server_type => null
                     */
                    String nodeUrn = node.getId();
                    nodeUrn = nodeUrn + "." + aggrId;
                    String nodeId = aggrId + "/" + AggregateUtils.getUrnField(nodeUrn, "node");
                    // hard coded for now: mapping rtr.newy into rtr.newy32aoa
                    nodeUrn = nodeUrn.replaceAll("rtr.newy", "rtr.newy32aoa");
                    // hard coded for now: mapping rtr.newy into rtr.newy32aoa
                    nodeId = nodeId.replaceAll("rtr.newy", "rtr.newy32aoa");
                    sql += String.format("INSERT INTO ops_node VALUES ('http://www.gpolab.bbn.com/monitoring/schema/20140501/node#', '%s', '%s', '%s', %d, null, null);\n",
                        nodeId, baseUrl+"info/node/"+nodeId, nodeUrn, ts*1000);
                    sql += String.format("INSERT INTO ops_aggregate_resource VALUES ('%s', '%s', '%s', '%s');\n",
                        nodeId, aggrId, nodeUrn, baseUrl+"info/node/"+nodeId);
                    for (PortContent port: node.getPort()) {
                            //$$ add "insert ops_interface" row
                            // INSERT INTO ops_interface VALUES ('', '', '', '', 0, null, null, '', 0, null);
                            /*
                             * $schema      => "http://www.gpolab.bbn.com/monitoring/schema/20140501/port#"
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
                            //String ifUrn = port.getId().replace("stitchport", "interface");
                            String portId = AggregateUtils.getUrnField(port.getId(), "port");
                            String ifUrn = nodeUrn.replace("node", "interface");
                            ifUrn = ifUrn + ":" + portId;
                            String ifId = nodeId + "/" + portId;
                            sql += String.format("INSERT INTO ops_interface VALUES ('http://www.gpolab.bbn.com/monitoring/schema/20140501/port#', '%s', '%s', '%s', %d, 'mac', '00:00:00:00:00:00', 'transport', %d, null);\n",
                                ifId, baseUrl+"info/interface/"+ifId, ifUrn, ts*1000, Long.parseLong(port.getCapacity()));
                            sql += String.format("INSERT INTO ops_node_interface VALUES ('%s', '%s', '%s', '%s');\n",
                                ifId, nodeId, ifUrn, baseUrl+"info/interface/"+ifId);
                    }
                }
            }
            sql += "COMMIT WORK;\n";
            try {
                FileOutputStream out = new FileOutputStream("/tmp/ops_mon_aggr.sql");
                out.write(sql.getBytes());
                out.close();
            } catch (Exception ex) {
                log.warn("failed to write ops_mon_aggr.sql");
            } finally {
                log.warn("updated ops_mon_aggr.sql");
                return;
            }
        }
    }

    // TODO: add sliver_vlan table and slice_vlan table ?
    private void updateVlanPsql() {
        String baseUrl = AggregateState.getOpsMonBaseUrl();
        String sql = "BEGIN WORK;\n";
        sql += "LOCK TABLE ops_aggregate_resource IN SHARE ROW EXCLUSIVE MODE;\n";
        sql += "LOCK TABLE ops_sliver_resource IN SHARE ROW EXCLUSIVE MODE;\n";
        sql += "LOCK TABLE ops_sliver IN SHARE ROW EXCLUSIVE MODE;\n";
        sql += "LOCK TABLE ops_link IN SHARE ROW EXCLUSIVE MODE;\n";
        sql += "LOCK TABLE ops_link_interfacevlan IN SHARE ROW EXCLUSIVE MODE;\n";
        sql += "LOCK TABLE ops_interfacevlan IN SHARE ROW EXCLUSIVE MODE;\n";
        sql += "LOCK TABLE ops_aggregate_sliver IN SHARE ROW EXCLUSIVE MODE;\n";
        List<AggregateP2PVlan> p2pvlans = AggregateState.getAggregateP2PVlans().getAll();
        List<AggregateRspec> rspecs = AggregateState.getRspecManager().getAggrRspecs();
        if (listCurrentVlanGri == null) {
            listCurrentVlanGri = new ArrayList<String>();
        }
        for (AggregateP2PVlan p2pvlan: p2pvlans) {
            if (!listCurrentVlanGri.contains(p2pvlan.getGlobalReservationId())) {
                if (p2pvlan.getVtag().isEmpty() || p2pvlan.getVtag().contains("any")
                        || !p2pvlan.getStatus().contains("ACTIVE")) {
                    continue;
                }
                String creator = "";
                for (AggregateRspec rspec: rspecs) {
                    if (rspec.getId() == p2pvlan.getRspecId()) {
                        p2pvlan.setStartTime(rspec.getStartTime());
                        p2pvlan.setEndTime(rspec.getEndTime());
                        if (rspec.getUsers() != null && rspec.getUsers().size() > 0) {
                            creator = rspec.getUsers().get(0);
                        }
                        break;
                    }
                }
                if (p2pvlan.getStartTime() == 0) {
                    continue;
                }
                //$$ add "insert VLAN" row(s)
                // INSERT INTO ops_sliver
                /*
                    $schema        => "http://www.gpolab.bbn.com/monitoring/schema/20140501/sliver#"
                    id             => sliverId
                    selfRef        => http://host:port/info/sliver/id
                    urn            => urn +sliver+id 
                    uuid           => null 
                    ts             => startTime
                    aggregate_urn  => aggrUrn 
                    aggregate_href => url
                    slice_urn      => sliceUrn
                    slice_uuid     => null 
                    creator        => null for now
                    created        => null for now
                    expires        => endTime
                 */
                // INSERT INTO ops_link VALUES ('', '', '', '', 0, 0, '', '', '');
                /*
                    $schema      => "http://www.gpolab.bbn.com/monitoring/schema/20140501/link#"
                    id           => convert from urn (aggr_id/gri) 
                    selfRef      => http://host:port/info/link/id 
                    urn          => geni +link+gri
                    ts           => startTime convert to epoch
                 */               
                // INSERT INTO ops_interfacevlan VALUES ('', '', '', '', 0, 0, '', '');
                /*
                    $schema      => "http://www.gpolab.bbn.com/monitoring/schema/20140501/port-vlan#"
                    id           => convert from urn (aggr_id/interface_id/vlan_id) 
                    selfRef      => http://host:port/info/interfacevlan/id 
                    urn          => geni urn:vlan
                    ts           => startTime convert to epoch
                    tag          => bigint            | 
                    interface_urn  =>  
                    interface_href => 
                 */
                String ifUrn;
                try {
                    ifUrn = AggregateUtils.convertDcnToGeniUrn(p2pvlan.getSource());
                } catch (AggregateException ex) {
                    continue;
                }
                String aggrId = AggregateUtils.getUrnField(p2pvlan.getSource(), "domain");
                String nodeId = AggregateUtils.getUrnField(p2pvlan.getSource(), "node") + "." + aggrId;
                String portId = AggregateUtils.getUrnField(p2pvlan.getSource(), "port");
                String ifId = aggrId + "/" + nodeId + "/" + portId;
                String vlanUrn = ifUrn; //.replace("+interface+", "+vlan+");
                String vlans[] = p2pvlan.getVtag().split(":");
                vlanUrn = vlanUrn + ":" + vlans[0];
                String vlanId = aggrId + "/" + nodeId + "/" + portId + "/" + vlans[0];
                String gri = p2pvlan.getGlobalReservationId();
                String linkId = gri;
                String linkUrn = "urn:publicid:IDN+"+aggrId+"+link+"+gri;
                String aggrUrn = "urn:publicid:IDN+"+aggrId+"+authority+am";
                String sliceUrn = p2pvlan.getSliceName();
                int urnIndex = sliceUrn.indexOf("urn:");
                if (urnIndex > 0) {
                    sliceUrn = sliceUrn.substring(urnIndex);
                }
                String sliverUrn = sliceUrn.replace("+slice+", "+sliver+");
                sliverUrn = sliverUrn + "_vlan_" + gri;
                String sliverId = sliverUrn.split("\\+")[sliverUrn.split("\\+").length-1];
                String slvierUUID = UUID.randomUUID().toString();
                // add VLAN sliver/vlan/circuit one-per-vlan
                sql += String.format("INSERT INTO ops_sliver SELECT 'http://www.gpolab.bbn.com/monitoring/schema/20140501/sliver#', '%s', '%s', '%s', '%s', %d, '%s', '%s', '%s', '', '%s', %d, %d WHERE NOT EXISTS (SELECT * FROM ops_sliver WHERE id = '%s');\n",
                    sliverId, baseUrl+"info/sliver/"+sliverId, sliverUrn, slvierUUID,  p2pvlan.getStartTime()*1000000, aggrUrn, baseUrl+"info/aggregate/"+aggrId, sliceUrn, creator, p2pvlan.getStartTime()*1000000, p2pvlan.getEndTime()*1000000, sliverId);
                sql += String.format("INSERT INTO ops_link SELECT 'http://www.gpolab.bbn.com/monitoring/schema/20140501/link#', '%s', '%s', '%s', %d WHERE NOT EXISTS (SELECT * FROM ops_link WHERE id = '%s');\n",
                    linkId, baseUrl+"info/link/"+linkId, linkUrn, p2pvlan.getStartTime()*1000000, linkId);
                sql += String.format("INSERT INTO ops_aggregate_resource SELECT '%s', '%s', '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_aggregate_resource WHERE id = '%s');\n",
                    linkId, aggrId, linkUrn, baseUrl+"info/link/"+linkId, linkId);
                // add sliver_aggregate relation one-per-vlan
                sql += String.format("INSERT INTO ops_aggregate_sliver SELECT '%s', '%s', '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_aggregate_sliver WHERE id = '%s');\n",
                        sliverId, aggrId, sliverUrn, baseUrl+"info/sliver/"+sliverId, sliverId);
                // add VLAN for ingress one-per-interface (two-per-vlan)
                sql += String.format("INSERT INTO ops_interfacevlan SELECT 'http://www.gpolab.bbn.com/monitoring/schema/20140501/port-vlan#', '%s', '%s', '%s', %d, %d, '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_interfacevlan WHERE id = '%s');\n",
                    vlanId, baseUrl+"info/interfacevlan/"+vlanId, vlanUrn, p2pvlan.getStartTime()*1000000, Long.parseLong(vlans[0]), ifUrn, baseUrl+"info/interface/"+ifId, vlanId);
                sql += String.format("INSERT INTO ops_link_interfacevlan SELECT '%s', '%s', '%s', '%s' WHERE NOT EXISTS  (SELECT * FROM ops_link_interfacevlan WHERE id = '%s');\n",
                    vlanId, linkId, vlanUrn, baseUrl+"info/interfacevlan/"+vlanId, vlanId);
                // add link into ops_sliver_resource instead of endpoint (interfacevlan)
                sql += String.format("INSERT INTO ops_sliver_resource SELECT '%s', '%s', '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_sliver_resource WHERE id = '%s');\n",
                    linkId, sliverId, linkUrn, baseUrl+"info/link/"+linkId, linkId);
                //sql += String.format("INSERT INTO ops_sliver_resource SELECT '%s', '%s', '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_sliver_resource WHERE id = '%s');\n",
                //    vlanId, sliverId, vlanUrn, baseUrl+"info/interfacevlan/"+vlanId, vlanId);
                try {
                    ifUrn = AggregateUtils.convertDcnToGeniUrn(p2pvlan.getDestination()).replace("/", "_");
                } catch (AggregateException ex) {
                    continue;
                }
                aggrId = AggregateUtils.getUrnField(p2pvlan.getDestination(), "domain");
                nodeId = AggregateUtils.getUrnField(p2pvlan.getDestination(), "node") + "." + aggrId;
                portId = AggregateUtils.getUrnField(p2pvlan.getDestination(), "port");
                ifId = aggrId + "/" + nodeId + "/" + portId;
                vlanUrn = ifUrn; //.replace("+interface+", "+vlan+");
                vlanUrn = vlanUrn + ":" + (vlans.length > 1 ?  vlans[1] : vlans[0]);
                vlanId = aggrId + "/" + nodeId + "/" + portId + "/" + (vlans.length > 1 ?  vlans[1] : vlans[0]);
                // add VLAN for egress one-per-interface (two-per-vlan)
                sql += String.format("INSERT INTO ops_interfacevlan SELECT 'http://www.gpolab.bbn.com/monitoring/schema/20140501/port-vlan#', '%s', '%s', '%s', %d, %d, '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_interfacevlan WHERE id = '%s');\n",
                    vlanId, baseUrl+"info/interfacevlan/"+vlanId, vlanUrn, p2pvlan.getStartTime()*1000000, Long.parseLong((vlans.length > 1 ?  vlans[1] : vlans[0])), ifUrn, baseUrl+"info/interface/"+ifId, vlanId);
                sql += String.format("INSERT INTO ops_link_interfacevlan SELECT '%s', '%s', '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_link_interfacevlan WHERE id = '%s');\n",
                    vlanId, linkId, vlanUrn, baseUrl+"info/interfacevlan/"+vlanId, vlanId);
                //sql += String.format("INSERT INTO ops_sliver_resource SELECT '%s', '%s', '%s', '%s' WHERE NOT EXISTS (SELECT * FROM ops_sliver_resource WHERE id = '%s');\n",
                //    vlanId, sliverId, vlanUrn, baseUrl+"info/interfacevlan/"+vlanId, vlanId);
                listCurrentVlanGri.add(gri);
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
                sql += String.format("DELETE from ops_interfacevlan WHERE id IN (SELECT id from ops_link_interfacevlan WHERE link_id='%s');\n", gri);
                sql += String.format("DELETE from ops_link_interfacevlan WHERE link_id='%s';\n", gri);
                sql += String.format("DELETE from ops_link WHERE id='%s';\n", gri);
                sql += String.format("DELETE from ops_sliver_resource WHERE sliver_id LIKE '%%_vlan_%s%%';\n", gri);
                sql += String.format("DELETE from ops_aggregate_sliver WHERE id LIKE '%%_vlan_%s%%';\n", gri);
                sql += String.format("DELETE from ops_aggregate_resource WHERE id='%s';\n", gri);
                sql += String.format("DELETE from ops_sliver WHERE id LIKE '%%_vlan_%s%%';\n", gri);
                itGri.remove();
            }
        }
        sql += "COMMIT WORK;\n";
        if (!sql.contains("INSERT") && !sql.contains("DELETE")) {
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream("/tmp/ops_mon_vlan.sql");
            out.write(sql.getBytes());
            out.close();
            } catch (Exception ex) {
                log.warn("failed to write ops_mon_vlan.sql");
            } finally {
                log.warn("updated ops_mon_vlan.sql");
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
