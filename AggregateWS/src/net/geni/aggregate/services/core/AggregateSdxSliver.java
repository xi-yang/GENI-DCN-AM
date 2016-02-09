/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

/**
 *
 * @author Xi Yang
 */
public class AggregateSdxSliver extends AggregateResource {
    private String sliceName = "";
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

    //@TODO: call VersaStack Client to create, delete and query
    public String createSliver() {
        String status = "SUCCESSFUL";
        //@ get baseURL, username and password from properties
        AggregateRESTClient restClient = new AggregateRESTClient();
        
        return status;
    }

    public String deleteSliver() {
        String status = "SUCCESSFUL";

        return status;
    }

    public String querySliver() {
        String status = "UNKNOWN";

        return status;
    }

}
