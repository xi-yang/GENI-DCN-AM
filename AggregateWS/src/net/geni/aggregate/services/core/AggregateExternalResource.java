/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

/**
 *
 * @author Xi Yang
 */

public class AggregateExternalResource extends AggregateResource {
    private String urn = "";
    private String subType = "";
    private String smUri = "";
    private String amUri = "";
    private String rspecData = "";
    private String status = "";
    private int vlanTag = -1;
    private long expireTime = 0;

    public AggregateExternalResource() {}

    public String getAmUri() {
        return amUri;
    }

    public void setAmUri(String amUri) {
        this.amUri = amUri;
    }

    public String getRspecData() {
        return rspecData;
    }

    public void setRspecData(String rspecData) {
        this.rspecData = rspecData;
    }

    public String getSmUri() {
        return smUri;
    }

    public void setSmUri(String smUri) {
        this.smUri = smUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public int getVlanTag() {
        return vlanTag;
    }

    public void setVlanTag(int vlanTag) {
        this.vlanTag = vlanTag;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    private String getNameFromUrn() {
        if (urn.isEmpty())
            return null;
        return urn.substring(urn.indexOf("rspec=")+6);
    }

    public String createResource() {
        String status = "SUCCESSFUL";
        if (this.subType.equalsIgnoreCase("ProtoGENI")) {
            ProtoGENI_APIClient apiClient = ProtoGENI_APIClient.getAPIClient();
            String sliceName = getNameFromUrn();
            if (sliceName == null) {
                status = "FAILED";
            }
            else {
                String newRspec = apiClient.createSlice(sliceName, rspecData, expireTime);
                if (newRspec == null)
                    status = "FAILED";
                else {
                    this.rspecData = newRspec;
                    this.vlanTag = apiClient.getCurrentVlanTag();
                }
            }
            apiClient.logoff();
        }
        return status;
    }

    public String deleteResource() {
        String status = "SUCCESSFUL";
        if (this.getSubType().equalsIgnoreCase("ProtoGENI")) {
            ProtoGENI_APIClient apiClient = ProtoGENI_APIClient.getAPIClient();
            String sliceName = getNameFromUrn();
            if (sliceName == null) {
                status = "FAILED";
            }
            else {
                int ret = apiClient.deleteSlice(sliceName);
                if (ret != 0)
                    status = "FAILED";
            }
            apiClient.logoff();
        }
        return status;
    }

    public String queryResource() {
        String status = "UNKNOWN";
        if (this.getSubType().equalsIgnoreCase("ProtoGENI")) {
            ProtoGENI_APIClient apiClient = ProtoGENI_APIClient.getAPIClient();
            String sliceName = getNameFromUrn();
            if (sliceName == null) {
                status = "FAILED";
            }
            else {
                status = apiClient.querySlice(sliceName);
            }
            apiClient.logoff();
        }
        return status;
    }

}
