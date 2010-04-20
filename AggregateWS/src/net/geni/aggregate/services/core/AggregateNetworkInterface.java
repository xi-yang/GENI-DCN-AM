/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.util.*;

/**
 *
 * @author root
 */
public class AggregateNetworkInterface extends AggregateResource {
    int id = 0;
    String interfaceId = "";
    String deviceName = "";
    String ipAddress = "";
    String vlanTag = "";
    String capacity = "";
    AggregateNode parentNode = null;
    ArrayList<String> peerInterfaces = null;

    public AggregateNetworkInterface() {}
    
    public AggregateNetworkInterface(String ifd) {
        interfaceId = ifd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String ifd) {
        this.interfaceId = ifd;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getVlanTag() {
        return vlanTag;
    }

    public void setVlanTag(String vlanTag) {
        this.vlanTag = vlanTag;
    }

    public AggregateNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(AggregateNode parentNode) {
        this.parentNode = parentNode;
    }

    public ArrayList<String> getPeerInterfaces() {
        return peerInterfaces;
    }

    public void setPeerInterfaces(ArrayList<String> peerInterfaces) {
        this.peerInterfaces = peerInterfaces;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public int[] pairupInterfaces(AggregateNetworkInterface peer) {
        int[] ret = new int[2];
        ret[0] = -1; ret[1] = -1;
        for (int i = 0; i < this.peerInterfaces.size(); i++) {
            if (this.getPeerInterfaces().get(i).equalsIgnoreCase(peer.getInterfaceId())) {
               ret[0] = i;
               break;
            }
        }
        for (int i = 0; i < peer.peerInterfaces.size(); i++) {
            if (peer.getPeerInterfaces().get(i).equalsIgnoreCase(this.getInterfaceId())) {
               ret[1] = i;
               break;
            }
        }
        return ret;
    }

}
