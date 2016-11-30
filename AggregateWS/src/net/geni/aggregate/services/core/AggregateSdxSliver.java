/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.IOException;
import org.apache.log4j.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Xi Yang
 */
public class AggregateSdxSliver extends AggregateResource {
    //Logger
    public static Logger log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");

    private String sliceName = "";
    private String sliceUser = "";
    private String serviceUuid = "";
    private String requestJson = "";
    private String manifest = "";
    private String status = "";

    public AggregateSdxSliver() {}

    public String getSliceName() {
        return sliceName;
    }

    public void setSliceName(String sliceName) {
        this.sliceName = sliceName;
    }

    public String getSliceUser() {
        return sliceUser;
    }

    public void setSliceUser(String sliceUser) {
        this.sliceUser = sliceUser;
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(String serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public String getManifest() {
        return manifest;
    }

    public void setManifest(String manifest) {
        this.manifest = manifest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private AggregateRESTClient getRestClient() {
        AggregateRESTClient restClient = new AggregateRESTClient(AggregateState.getSdxApiUrl(), AggregateState.getSdxApiUser(), AggregateState.getSdxApiPass());
        return restClient;
    }

    private String probeServiceType(JSONObject jsonData) {
        try {
            JSONArray jsonArrVC = (JSONArray)jsonData.get("virtual_clouds");
            if (jsonArrVC.size() == 2) {
                return "hybridcloud";
            } else if (jsonArrVC.size() == 1) {
                return "netcreate";
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
            
    private JSONObject generateRestData() throws AggregateException {
        JSONObject jsonData = AggregateUtils.parseJsonString(this.requestJson);
        if (jsonData == null) {
            throw new AggregateException("AggregateSdxSliver.generateRestData() cannot parse requst JSON: \n"+this.requestJson);
        }
        String serviceType = this.probeServiceType(jsonData);
        if (serviceType.isEmpty()) {
            throw new AggregateException("AggregateSdxSliver.generateRestData() cannot tell service type for JSON: \n"+this.requestJson);
        }
        JSONObject reqJson = new JSONObject();
        //reqJson.put("user", this.sliceUser);
        reqJson.put("user", "admin"); //@TODO: user name mapping
        reqJson.put("type", serviceType); 
        reqJson.put("alias", this.sliceName);
        reqJson.put("data", jsonData);
        return reqJson;
    }

    public String createSliver() throws AggregateException {
       AggregateRESTClient restClient = this.getRestClient();
        JSONObject restData = generateRestData();
        try {
            log.info("AggregateSdxSliver.createSliver sending: "+restData);
            String response[] = restClient.executeHttpMethod("POST", AggregateState.getSdxApiUrl()+"/service", restData.toJSONString());
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.createSliver POST returns "+response);
            }
            this.setServiceUuid(response[2]);
        } catch (Exception ex) {
            throw new AggregateException(ex);
        }
        return "INSETUP";
    }

    public String cancelSliver() throws AggregateException {
       String serviceUUID = this.getServiceUuid();
        if (serviceUUID == null || serviceUUID.isEmpty()) {
            throw new AggregateException("AggregateSdxSliver.cancelSliver encoutners null/empty service UUID");
        }
        AggregateRESTClient restClient = this.getRestClient();
        try {
            String response[] = restClient.executeHttpMethod("PUT", AggregateState.getSdxApiUrl()+"/service/"+serviceUUID+"/cancel", null);
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.cancelSliver PUT returns "+response);
            }
       } catch (Exception ex) {
            throw new AggregateException(ex);
        }
        return "CANCELLED";
    }

    public String deleteSliver() throws AggregateException {
        String serviceUUID = this.getServiceUuid();
        if (serviceUUID == null || serviceUUID.isEmpty()) {
            throw new AggregateException("AggregateSdxSliver.deleteSliver encoutners null/empty service UUID");
        }
        AggregateRESTClient restClient = this.getRestClient();
        try {
            String response[] = restClient.executeHttpMethod("PUT", AggregateState.getSdxApiUrl()+"/service/"+serviceUUID+"/delete", null);
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.deleteSliver DELETE returns "+response);
            }
       } catch (Exception ex) {
            throw new AggregateException(ex);
        }
        return "DELETED";
    }

    public String querySliver(boolean detailed) throws AggregateException {
       String serviceUUID = this.getServiceUuid();
        if (serviceUUID == null || serviceUUID.isEmpty()) {
            throw new AggregateException("AggregateSdxSliver.querySliver encoutners null/empty service UUID");
        }
        AggregateRESTClient restClient = this.getRestClient();
        try {
            String response[] = null; 
            if (detailed) {
                response = restClient.executeHttpMethod("GET/xml", AggregateState.getSdxApiUrl()+"/manifest/"+serviceUUID, null);
            } else {
                response = restClient.executeHttpMethod("GET", AggregateState.getSdxApiUrl()+"/service/"+serviceUUID+"/status", null);                
            }
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.querySliver GET returns "+response);
            }
            return response[2];
       } catch (Exception ex) {
            throw new AggregateException(ex);
        }
    }
}
