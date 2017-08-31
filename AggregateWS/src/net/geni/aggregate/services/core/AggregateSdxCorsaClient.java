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

/**
 *
 * @author xyang
 */
public class AggregateSdxCorsaClient extends AggregateRESTClient {

    String url = null;

    AggregateSdxCorsaClient(String url) {
        this.url = url;
    }

    public static AggregateSdxCorsaClient getClient(String url) {
        return (new AggregateSdxCorsaClient(url));
    }

    public static AggregateSdxCorsaClient getClient() {
        return getClient(AggregateState.getIdcURL());
    }

    public String generateStitchingXml()
            throws AggregateException {
        String xml = "";
        try {
            // get bridge info
            String[] response = executeHttpMethod("GET", url, null);
            if (!response[0].equals("200")) {
                throw new AggregateException("Failed to get info of switch from:"+ url +" with  HTTP response code="+response[0]+" and response message="+response[1]);
            }
            JSONObject jsonRet = AggregateUtils.parseJsonString(response[2]);
            String nodeName = jsonRet.get("switch").toString();
            //$$ node uri = aggregateURI + "+node+"+nodeName
            if (jsonRet.containsKey("neighbors")) {
                JSONArray jsonNeighbors = (JSONArray)jsonRet.get("neighbors");
                for (Object obj: jsonNeighbors) {
                    JSONObject jsonPort = (JSONObject)obj;
                    // get portName
                    String portName = jsonPort.get("physical-port").toString();
                    // get port VLAN range //@TODO: reformat?
                    String vlanRange = jsonPort.get("vlans").toString();
                    //$$ port uri =  nodeURI + "+stitchport+"+nodeName:portName
                    //$$ interface uri =  nodeURI + "+interface+"+pnodeName:ortName
                    //$$ add ISCD L2SC with default bandwidth and VLAN range 
                }
            }
            if (jsonRet.containsKey("bridges")) {
                JSONArray jsonBridges = (JSONArray)jsonRet.get("bridges");
                for (Object obj: jsonBridges) {
                    JSONObject jsonBridge = (JSONObject)obj;
                    //$$ add bridge as internal links (with VLAN)
                    //$$ update stitch interface bandwidth and VLAN range ??
                }
            }
        } catch (IOException ex) {
            throw new AggregateException("Failed to get info of switch from:"+ url +" due to:" +ex);
        }
        return xml;
    }

    // port URI in form of  urn:publicid:IDN+aggregate_id+stitchport+switch_id:port_id
    // link URI in form of  urn:publicid:IDN+aggregate_id+interface+switch_id:port_id
    public String createBridge(String bridgeID, String controller, String dpid, String src, String dst, String vtag, float bw)
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
        jsonData.put("name", bridgeID);
        String[] controllerUrlParts = controller.split(":");
        if (controllerUrlParts.length <2) {
            throw new AggregateException("Failed to create bridge:"+ bridgeID + " due to invalid controller URL:"+controller);
        }
        String controllerAddr = controllerUrlParts[controllerUrlParts.length -2];
        String controllerPort = controllerUrlParts[controllerUrlParts.length -1];
        jsonData.put("controller_addr", controllerAddr);
        jsonData.put("controller_port", Integer.parseInt(controllerPort));
        jsonData.put("dpid", Long.parseLong(dpid));
        try {
            // create bridge call
            super.executeHttpBearerMethod("POST", url+"/bridges", jsonData.toJSONString());
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
        return "ACTIVE";
    }

    public String deleteBridge(String bridgeID) throws AggregateException {
        // delete bridge call
        try {
            // create bridge call
            super.executeHttpMethod("DELETE", url+"/bridges/"+bridgeID, null);
        } catch (IOException ex) {
            throw new AggregateException("Failed to delete bridge:" + bridgeID + " due to:"+ex);
        }
        return "DELETED";
    }

    public String modifyBridge(String bridgeID, String controller, String dpid, String src, String dst, String vtag, float bw)
            throws AggregateException {
        deleteBridge(bridgeID);
        return createBridge(bridgeID, controller, dpid, src, dst, vtag, bw);
    }

    public JSONObject queryBridge(String bridgeID) throws AggregateException {
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
