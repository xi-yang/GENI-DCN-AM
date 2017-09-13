/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author xyang
 */
public class AggregateSdxCorsaClient extends AggregateRESTClient {

    private org.apache.log4j.Logger log;
    String url = null;

    AggregateSdxCorsaClient(String url) {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
        this.url = url;
    }

    public static AggregateSdxCorsaClient getClient(String url) {
        return (new AggregateSdxCorsaClient(url));
    }

    public static AggregateSdxCorsaClient getClient() {
        return getClient(AggregateState.getSdxApiUrl());
    }

    public String generateStitchingXml()
            throws AggregateException {
        String amBase = AggregateUtils.getGeniAmBase(AggregateState.getAmUrn());
        String amUrl = AggregateState.getAmUrl();
        if (amUrl.isEmpty()) {
            amUrl = "http://" + amBase + ":5001";
        }
        String xml = String.format("\n<stitching lastUpdateTime=\"%s\" xmlns=\"http://hpn.east.isi.edu/rspec/ext/stitch/0.1/\">\n", "@20130112:09:30:21");
        xml += String.format("<aggregate id=\"urn:publicid:IDN+%s+authority+am\" url=\"%s\">\n", amBase, amUrl);
        xml += "<aggregatetype>corsa</aggregatetype>\n"
                + " <stitchingmode>tree</stitchingmode>\n"
                + " <scheduledservices>false</scheduledservices>\n"
                + " <negotiatedservices>false</negotiatedservices>\n"
                + " <lifetime id=\"1\">\n"
                + String.format("   <start type=\"ISO-8601\">%s</start>\n", "@2013-01-12T21:30:53Z") 
                + String.format("   <end type=\"ISO-8601\"></end>\n", "@2013-12-31T00:00:00Z")
                + " </lifetime>\n";
        try {
            // get bridge info
            String[] response = executeHttpMethod("GET", url, null);
            if (!response[0].equals("200")) {
                throw new AggregateException("Failed to get info of switch from:"+ url +" with  HTTP response code="+response[0]+" and response message="+response[1]);
            }
            JSONObject jsonRet = AggregateUtils.parseJsonString(response[2]);
            String nodeName = jsonRet.get("switch").toString();
            xml += String.format("<node id=\"urn:publicid:IDN+%s+node+%s\">\n", amBase, nodeName);
            if (jsonRet.containsKey("neighbors")) {
                JSONArray jsonNeighbors = (JSONArray) jsonRet.get("neighbors");
                for (Object obj : jsonNeighbors) {
                    JSONObject jsonPort = (JSONObject)obj;
                    // get portName - bandwidth etc. metrics are placeholders
                    String portName = jsonPort.get("physical-port").toString();
                    xml += String.format("<port id=\"urn:publicid:IDN+%s+stitchport+%s:%s\">\n", amBase, nodeName, portName);
                    xml += "    <capacity>10000000</capacity>\n"
                            + "    <maximumReservableCapacity>10000000</maximumReservableCapacity>\n"
                            + "    <minimumReservableCapacity>1</minimumReservableCapacity>\n"
                            + "    <granularity>1</granularity>\n";
                    String vlanRange = jsonPort.get("vlans").toString();
                    if (vlanRange.length() > 2) {
                        vlanRange = vlanRange.substring(1, vlanRange.length()-1);
                    } else {
                        vlanRange = "";
                    }
                    xml += String.format("<link id=\"urn:publicid:IDN+%s+interface+%s:%s\">\n", amBase, nodeName, portName);
                    xml += String.format("      <remoteLinkId>urn:publicid:IDN+%s+interface+*:*:*</remoteLinkId>\n", amBase)
                            + "      <trafficEngineeringMetric>10</trafficEngineeringMetric>\n"
                            + "      <capacity>10000000</capacity>\n"
                            + "      <maximumReservableCapacity>10000000</maximumReservableCapacity>\n"
                            + "      <minimumReservableCapacity>1</minimumReservableCapacity>\n"
                            + "      <granularity>1</granularity>\n"
                            + "      <switchingCapabilityDescriptor>\n"
                            + "        <switchingcapType>l2sc</switchingcapType>\n"
                            + "        <encodingType>ethernet</encodingType>\n"
                            + "        <switchingCapabilitySpecificInfo>\n"
                            + "          <switchingCapabilitySpecificInfo_L2sc>\n"
                            + "           <interfaceMTU>9000</interfaceMTU>\n"
                            + String.format("           <vlanRangeAvailability>%s</vlanRangeAvailability>\n", vlanRange)
                            + "           <vlanTranslation>false</vlanTranslation>\n"
                            + "          </switchingCapabilitySpecificInfo_L2sc>\n"
                            + "        </switchingCapabilitySpecificInfo>\n"
                            + "      </switchingCapabilityDescriptor>\n";
                    xml += "</link>\n";
                    xml += "</port>\n";
                }
            }
            //$$ bridges
            if (jsonRet.containsKey("bridges")) {
                JSONArray jsonBridges = (JSONArray)jsonRet.get("bridges");
                for (Object obj: jsonBridges) {
                    JSONObject jsonBridge = (JSONObject)obj;
                    //$$ add bridge as internal links (with VLAN)
                    //$$ update stitch interface bandwidth and VLAN range ??
                }
            }
            xml += "</node>\n";
        } catch (IOException ex) {
            throw new AggregateException("Failed to get info of switch from:"+ url +" due to:" +ex);
        }
        xml += "</aggregate>\n";
        xml += "</stitching>\n";
        return xml;
    }

    // port URI in form of  urn:publicid:IDN+aggregate_id+stitchport+switch_id:port_id
    // link URI in form of  urn:publicid:IDN+aggregate_id+interface+switch_id:port_id
    public String createBridge(String bridgeUrn, String controller, String dpid, String src, String dst, String vtag, float bw)
            throws AggregateException {
        String srcVtag = vtag;
        String dstVtag = vtag;
        if (vtag.contains(":")) {
            String[] vtags = vtag.split(":");
            srcVtag = vtags[0];
            dstVtag = vtags[1];
        }
        String srcPort = AggregateUtils.getUrnField(src, "port");
        if (srcPort == null) {
            throw new AggregateException("Invalid port URI: "+src);
        }
        String dstPort = AggregateUtils.getUrnField(dst, "port");
        if (dstPort == null) {
            throw new AggregateException("Invalid port URI: "+dst);
        } 
        // compose bridge data
        JSONObject jsonData = new JSONObject();
        jsonData.put("urn", bridgeUrn);
        String[] controllerUrlParts = controller.split("[/:]");
        if (controllerUrlParts.length <2) {
            throw new AggregateException("Failed to create bridge for "+ bridgeUrn + " due to invalid controller URL:"+controller);
        }
        String controllerAddr = controllerUrlParts[controllerUrlParts.length -2];
        String controllerPort = controllerUrlParts[controllerUrlParts.length -1];
        jsonData.put("controller_addr", controllerAddr);
        jsonData.put("controller_port", Integer.parseInt(controllerPort));
        jsonData.put("dpid", Long.parseLong(dpid));
        String bridgeID = null;
        try {
            // create bridge call
            log.debug("creating bridge: " + jsonData.toJSONString());
            String[] response = super.executeHttpBearerMethod("POST", url+"/bridges", jsonData.toJSONString());
            if (!response[0].equals("200")) {
                throw new AggregateException("HTTP reponse: " + response);
            }
            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(response[2]);
                JSONObject responseJSON = (JSONObject) obj;
                bridgeID = (String)responseJSON.get("bridge");
            } catch (ParseException ex) {
                log.error("Error parsing json: "+response[2]);
                throw (new IOException(ex));
            }
        } catch (IOException ex) {
            throw new AggregateException(ex);
        }
        // compose tunnel data for srcPort
        jsonData = new JSONObject();
        jsonData.put("dstname", "vlan-"+srcVtag);
        jsonData.put("physport", srcPort);
        jsonData.put("dstvlan", Integer.parseInt(srcVtag));
        try {
            // create tunnel call
            super.executeHttpMethod("POST", url+"/bridges/"+bridgeID+"/tunnels", jsonData.toJSONString());
        } catch (IOException ex) {
            throw new AggregateException("Failed to create bridge:"+ bridgeID + " due to:"+ex);
        }
        // compose tunnel data for srcPort
        jsonData = new JSONObject();
        jsonData.put("dstname", "vlan-"+srcVtag);
        jsonData.put("physport", srcPort);
        jsonData.put("dstvlan", Integer.parseInt(srcVtag));
        try {
            // create tunnel call
            super.executeHttpMethod("POST", url+"/bridges/"+bridgeID+"/tunnels", jsonData.toJSONString());
        } catch (IOException ex) {
            throw new AggregateException("Failed to create tunnel vlan-"+srcVtag+" on port-"+srcPort+" on bridge:"+ bridgeID +ex);
        }
        // compose tunnel data for dstPort
        jsonData = new JSONObject();
        jsonData.put("dstname", "vlan-"+dstVtag);
        jsonData.put("physport", dstPort);
        jsonData.put("dstvlan", Integer.parseInt(dstVtag));
        try {
            // create tunnel call
            super.executeHttpMethod("POST", url+"/bridges/"+bridgeID+"/tunnels", jsonData.toJSONString());
        } catch (IOException ex) {
            throw new AggregateException("Failed to create tunnel vlan-"+dstVtag+" on port-"+dstPort+" on bridge:"+ bridgeID +ex);
        }
        return bridgeID+" "+bridgeUrn;
    }

    public String deleteBridge(String gri) throws AggregateException {
        String[] griParts = gri.split(" ");
        String bridgeID = griParts[0];
        // delete bridge call
        try {
            // create bridge call
            super.executeHttpMethod("DELETE", url+"/bridges/"+bridgeID, null);
        } catch (IOException ex) {
            throw new AggregateException("Failed to delete bridge:" + bridgeID + " due to:"+ex);
        }
        return "DELETED";
    }

    public String modifyBridge(String gri, String controller, String dpid, String src, String dst, String vtag, float bw)
            throws AggregateException {
        String[] griParts = gri.split(" ");
        String bridgeID = griParts[0];
        String bridgeUrn = griParts[1];
        deleteBridge(bridgeID);
        return createBridge(bridgeUrn, controller, dpid, src, dst, vtag, bw);
    }

    public JSONObject queryBridge(String gri) throws AggregateException {
        String[] griParts = gri.split(" ");
        String bridgeID = griParts[0];
        try {
            // get bridge info
            String[] response = super.executeHttpMethod("GET", url+"/bridges/"+bridgeID, null);
            if (!response[0].equals("200")) {
                throw new AggregateException("Failed to get info of bridge:"+ bridgeID +" with  HTTP response code="+response[0]+" and response message="+response[1]);
            }
            JSONObject jsonRet = AggregateUtils.parseJsonString(response[2]);
            jsonRet.put("status", "ACTIVE");
            return jsonRet;
        } catch (IOException ex) {
            throw new AggregateException("Failed to get info of bridge:"+ bridgeID + " due to:"+ex);
        }
    }
}
