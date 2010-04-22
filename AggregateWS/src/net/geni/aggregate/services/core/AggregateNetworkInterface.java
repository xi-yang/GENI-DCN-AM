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
    String urn = "";
    String deviceType = "";
    String deviceName = "";
    String ipAddress = "";
    String vlanTag = "";
    String capacity = "";
    ArrayList<String> attachedLinkUrns = null;
    ArrayList<String> peerInterfaces = null;
    AggregateNode parentNode = null;
    int pnid = 0;

    public AggregateNetworkInterface() {}
    
    public AggregateNetworkInterface(String urn) {
        this.urn = urn;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
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
        this.pnid = parentNode.getId();
    }

    //for XML config
    public ArrayList<String> getLinks() {
        return attachedLinkUrns;
    }

    public void setLinks(ArrayList<String> links) {
        this.attachedLinkUrns = links;
    }

    public ArrayList<String> getPeers() {
        return peerInterfaces;
    }

    public void setPeers(ArrayList<String> peerInterfaces) {
        this.peerInterfaces = peerInterfaces;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    //for hibernate
    public void setAttachedLinkUrns(String links) {
        attachedLinkUrns = new ArrayList();
        attachedLinkUrns.toArray(links.split("[\\s,]"));
    }

    public String getAttachedLinkUrns() {
        String ret = "";
        for (String link: attachedLinkUrns) {
            ret = ret + ", " + link;
        }
        if (!ret.isEmpty())
            ret = ret.substring(2);
        return ret;
    }

    public void setPeerInterfaces(String peers) {
        peerInterfaces = new ArrayList();
        peerInterfaces.toArray(peers.split("[\\s,]"));
    }

    public String getPeerInterfaces() {
        String ret = "";
        for (String link: peerInterfaces) {
            ret = ret + ", " + link;
        }
        if (!ret.isEmpty())
            ret = ret.substring(2);
        return ret;
    }

    public int getPnid() {
        return pnid;
    }

    public void setPnid(int pnid) {
        this.pnid = pnid;
    }

    public int[] pairupInterfaces(AggregateNetworkInterface peer) {
        int[] ret = new int[2];
        ret[0] = -1; ret[1] = -1;
        for (int i = 0; i < this.peerInterfaces.size(); i++) {
            if (this.getPeers().get(i).equalsIgnoreCase(peer.getUrn())) {
               ret[0] = i;
               break;
            }
        }
        for (int i = 0; i < peer.peerInterfaces.size(); i++) {
            if (peer.getPeers().get(i).equalsIgnoreCase(this.getUrn())) {
               ret[1] = i;
               break;
            }
        }
        return ret;
    }

    public AggregateNetworkInterface duplicate() {
        AggregateNetworkInterface ai = new AggregateNetworkInterface(urn);
        ai.setCapacity(capacity);
        ai.setDeviceType(deviceType);
        ai.setDeviceName(deviceName);
        ai.setIpAddress(ipAddress);
        ai.setParentNode(parentNode);
        ai.setPnid(pnid);
        ai.setVlanTag(vlanTag);
        ai.setLinks(this.getLinks());
        ai.setPeers(this.getPeers());
        ai.setType(type);
        return ai;
    }
}
