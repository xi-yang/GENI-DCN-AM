/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

/**
 *
 * @author Xi Yang
 */
public class AggregateSlice {

    private String sliceName;
    private int id;
    private String description;
    private String url;
    private String members;
    private int creatorId;
    private long createdTime;
    private long expiredTime;

    public AggregateSlice(String n, int i, String u,  String d, String m, int cI, long cT, long eT) {
        sliceName = n;
        id = i;
        url = u;
        description = d;
        members = m;
        creatorId = cI;
        createdTime = cT;
        expiredTime = eT;
    }

    public String getURL() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getMembers() {
        return members;
    }

    public int getId() {
        return id;
    }

    public String getSliceName() {
        return sliceName;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getExpiredTime() {
        return expiredTime;
    }
}
