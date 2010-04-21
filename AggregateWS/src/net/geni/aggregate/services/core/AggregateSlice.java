/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

/**
 *
 * @author Xi Yang
 */
public class AggregateSlice extends AggregateResource {
    private int sliceId;
    private String sliceName;
    private String description;
    private String url;
    private String users;
    private String nodes;
    private int creatorId;
    private long createdTime;
    private long expiredTime;
    private String status;

    public AggregateSlice() {
        sliceId = 0;
        sliceName = "";
        url = "";
        description = "";
        users = "";
        nodes = "";
        creatorId = 0;
        createdTime = 0;
        expiredTime = 0;
        status = "";
        type = "computeSlice";
    }

    public AggregateSlice(String n, int i, String u,  String d, String us, String ns, int cI, long cT, long eT) {
        sliceName = n;
        sliceId = i;
        url = u;
        description = d;
        users = us;
        nodes = ns;
        creatorId = cI;
        createdTime = cT;
        expiredTime = eT;
        status = "";
        type = "computeSlice";
    }

    public int getSliceId() {
        return sliceId;
    }

    public void setSliceId(int sliceId) {
        this.sliceId = sliceId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descr) {
        this.description = descr;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public String getSliceName() {
        return sliceName;
    }

    public void setSliceName(String sliceName) {
        this.sliceName = sliceName;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
