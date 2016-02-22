/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author Xi Yang
 */
public class AggregateSdxSliver extends AggregateResource {
    private String sliceName = "";
    private String sliceUser = "";
    private String serviceUuid = "";
    private String requestJson = "";
    private String manifestJson = "";
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

    public String getManifestJson() {
        return manifestJson;
    }

    public void setManifestJson(String manifestJson) {
        this.manifestJson = manifestJson;
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

    private JSONObject generateRestData() throws AggregateException {
        JSONObject jsonData = AggregateUtils.parseJsonString(this.requestJson);
        if (jsonData == null) {
            throw new AggregateException("AggregateSdxSliver.generateRestData() cannot parse requst JSON: \n"+this.requestJson);
        }
        JSONObject reqJson = new JSONObject();
        reqJson.put("user", this.sliceUser);
        reqJson.put("type", "netcreate");
        reqJson.put("data", jsonData);
        //$$ TODO: add creationTime, client Tags
        return reqJson;
    }

    public String createSliver() throws AggregateException {
       AggregateRESTClient restClient = this.getRestClient();
        JSONObject restData = generateRestData();
        try {
            String response[] = restClient.executeHttpMethod("POST", AggregateState.getSdxApiUrl()+"service", restData.toJSONString());
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.createSliver POST returns code "+response[0]);
            }
            this.setServiceUuid(response[2]);
        } catch (IOException ex) {
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
            String response[] = restClient.executeHttpMethod("PUT", AggregateState.getSdxApiUrl()+"service/"+serviceUUID+"/cancel", null);
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.cancelSliver PUT returns code "+response[0]);
            }
       } catch (IOException ex) {
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
            String response[] = restClient.executeHttpMethod("DELETE", AggregateState.getSdxApiUrl()+"service/"+serviceUUID, null);
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.deleteSliver DELETE returns code "+response[0]);
            }
       } catch (IOException ex) {
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
            String response[] = restClient.executeHttpMethod("GET", AggregateState.getSdxApiUrl()+"service/"+serviceUUID+(detailed?"/manifest":"/status"), null);
            if (!response[0].equals("200")) {
                throw new AggregateException("AggregateSdxSliver.querySliver GET returns code "+response[0]);
            }
            return response[2];
       } catch (IOException ex) {
            throw new AggregateException(ex);
        }
    }
}
