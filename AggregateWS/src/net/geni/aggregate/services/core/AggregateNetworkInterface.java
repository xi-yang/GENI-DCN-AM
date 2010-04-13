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
    String interfaceId = "";
    String deviceName = "";
    String ipAddress = "";
    String vlanTag = "";
    ArrayList<String> peerInterfaces = null;

    public AggregateNetworkInterface() {}
    
    public AggregateNetworkInterface(String ifd) {
        interfaceId = ifd;
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

    public ArrayList<String> getPeerInterfaces() {
        return peerInterfaces;
    }

    public void setPeerInterfaces(ArrayList<String> peerInterfaces) {
        this.peerInterfaces = peerInterfaces;
    }
}
