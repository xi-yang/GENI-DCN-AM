/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.Serializable;

/**
 *
 * @author Xi Yang
 */
public class AggregateSlice implements java.io.Serializable {

    private String sliceName;
    private int id;
    private String description;
    private String url;
    private String users;
    private String nodes;
    private int creatorId;
    private long createdTime;
    private long expiredTime;

    public AggregateSlice() {
        sliceName = "";
        id = 0;
        url = "";
        description = "";
        users = "";
        nodes = "";
        creatorId = 0;
        createdTime = 0;
        expiredTime = 0;
    }

    public AggregateSlice(String n, int i, String u,  String d, String us, String ns, int cI, long cT, long eT) {
        sliceName = n;
        id = i;
        url = u;
        description = d;
        users = us;
        nodes = ns;
        creatorId = cI;
        createdTime = cT;
        expiredTime = eT;
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
        return nodes;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
