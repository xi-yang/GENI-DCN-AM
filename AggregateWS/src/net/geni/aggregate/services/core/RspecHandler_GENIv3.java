/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;
import java.io.*;
import java.util.*;
import net.geni.www.resources.rspec._3.*;
import net.geni.schema.stitching.topology.genistitch._20110220.*;
        
/**
 *
 * @author xyang
 */
public class RspecHandler_GENIv3 implements AggregateRspecHandler {
    public AggregateRspec parseRspecXml(String rspecXml) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();
        RSpecContents rspecV3Obj = null;
        GeniStitchTopologyContent stitchTopoObj = null;
        String[] rspecXmls = this.extractStitchingRspec(rspecXml);
        try {
            StringReader reader = new StringReader(rspecXmls[0]);
            JAXBContext jc = JAXBContext.newInstance("net.geni.www.resources.rspec._3");
            Unmarshaller unm = jc.createUnmarshaller();
            JAXBElement<RSpecContents> jaxbRspec = (JAXBElement<RSpecContents>) unm.unmarshal(reader);
            rspecV3Obj = jaxbRspec.getValue();
        } catch (Exception e) {
            throw new AggregateException("Error in unmarshling GENI RSpec v3 contents: " + e.getMessage());
        }
        if (rspecXmls[1] != null) {
            try {
                StringReader reader = new StringReader(rspecXmls[1]);
                JAXBContext jc = JAXBContext.newInstance("net.geni.schema.stitching.topology.genistitch._20110220");
                Unmarshaller unm = jc.createUnmarshaller();
                JAXBElement<GeniStitchTopologyContent> jaxbRspec = (JAXBElement<GeniStitchTopologyContent>) unm.unmarshal(reader);
                stitchTopoObj = jaxbRspec.getValue();
            } catch (Exception e) {
                throw new AggregateException("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
            }
        }
        // parse GENI RSpecv3 main section -- nodes
        for (Object obj: rspecV3Obj.getAnyOrNodeOrLink()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("node")) {
                    parseAddNode(aggrRspec, (NodeContents)((JAXBElement)obj).getValue());
                }
            }
        }
        // parse links -- require interfaces created under nodes
        for (Object obj: rspecV3Obj.getAnyOrNodeOrLink()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("link")) {
                    parseAddLink(aggrRspec, (LinkContents)((JAXBElement)obj).getValue());
                }
            }
        }
        // parse GENI Stitching Rspec section
        if (stitchTopoObj != null) {
            parseStitchingResources(aggrRspec, stitchTopoObj);
        }

        return aggrRspec;
    }

    void parseAddNode(AggregateRspec rspec, NodeContents node) throws AggregateException {
        //assuming Planetlab vserver node for now
        int nodeId = 0;
        String address = "";
        String caps = "";
        String descr = "";
        // attributes:
        String clientId = node.getClientId();
        String urn = node.getComponentId(); //$$$$ TODO: convert URN format ?
        String amUrn = node.getComponentManagerId();
        // sub-elements:
        List<AggregateNetworkInterface> myNetIfs = new ArrayList<AggregateNetworkInterface>();
        for (Object obj: node.getAnyOrRelationOrLocation()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("interface")) {
                    AggregateNetworkInterface netIf = parseNetworkInterface(rspec, (InterfaceContents)((JAXBElement)obj).getValue());
                    myNetIfs.add(netIf);
                } else if (elemName.equalsIgnoreCase("hardware_type")) {
                    HardwareTypeContents hdc = (HardwareTypeContents)((JAXBElement)obj).getValue();
                    //type = hdc.getName();
                }
            }
            if (obj.getClass().getName().contains("ElementNSImpl")) {
                String elemName = AggregateUtils.getAnyName(obj);
                if (elemName.equalsIgnoreCase("address")) {
                    address = AggregateUtils.getAnyText(obj);
                } else if (elemName.equalsIgnoreCase("description")) {
                    descr = AggregateUtils.getAnyText(obj);
                }

            }
        }

        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(urn);
        if (aggrNode == null) {
            throw new AggregateException("unknown aggregateNode "+urn+" (extracted from "+urn+")");
        }
        AggregateNode newNode = aggrNode.duplicate();
        newNode.setClientId(clientId);
        aggrNode.setType("planetlabNodeSliver"); // plab node only
        newNode.setRspecId(rspec.getId()); //rspec entry has been created in db
        rspec.getResources().add(newNode);
        for (AggregateNetworkInterface netIf: myNetIfs)
            netIf.setParentNode(newNode);
    }

    AggregateNetworkInterface parseNetworkInterface(AggregateRspec rspec, InterfaceContents iface) throws AggregateException {
        //assuming VLAN interface for now
        String urn = iface.getComponentId(); //netIfId
        String deviceType = "Ethernet";
        String deviceName = "";
        String ipAddress = "";
        String vlanTag = "";
        String capacity = "";
        String clientId = iface.getClientId();
        ArrayList<String> linkUrns = new ArrayList<String>();

        for (Object obj: iface.getAnyOrIpOrHost()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("ip")) {
                    IpContents ipc = (IpContents)((JAXBElement)obj).getValue();
                    ipAddress = ipc.getAddress();
                    if (ipc.getNetmask() != null) {
                        ipAddress += "/";
                        ipAddress += ipc.getNetmask();
                    }
                    // assume type="ipv4"
                }
            }
            if (obj.getClass().getName().contains("ElementNSImpl")) {
                String elemName = AggregateUtils.getAnyName(obj);
                if (elemName.equalsIgnoreCase("attached_link_urn")) {
                    linkUrns.add(AggregateUtils.getAnyText(obj));
                } else if (elemName.equalsIgnoreCase("vlan_range")) {
                    vlanTag = AggregateUtils.getAnyText(obj);
                } 
            }
        }

        // extract deviceName from urn (assume last field after colon)
        int last = -1;
        int index = 0;
        while (index != -1) {
            index = urn.indexOf(":", index+1);
            if (index != -1)
                last = index;
        }
        if (last != -1) {
            deviceName = urn.substring(last+1);
        } else {
            throw new AggregateException("malformed network interface field in component_id urn: " + urn);
        }
        
        // AggregateNetworkInterface(s) will be further processed to create P2PVlans
        AggregateNetworkInterface aggrNetIf = new AggregateNetworkInterface(urn);
        aggrNetIf.setClientId(clientId);
        aggrNetIf.setDeviceType(deviceType);
        aggrNetIf.setDeviceName(deviceName);
        aggrNetIf.setIpAddress(ipAddress);
        aggrNetIf.setVlanTag(vlanTag); // To be overidden by parseAddLink if empty
        aggrNetIf.setCapacity(capacity); // To be overidden by parseAddLink if empty
        aggrNetIf.setLinks(linkUrns);
        // aggrNetIf.setPeers(peerNetIfs); // To be set by parseAddLink
        aggrNetIf.setType("networkInterface");
        aggrNetIf.setRspecId(rspec.getId());
        rspec.getResources().add(aggrNetIf);
        return aggrNetIf;
    }

    void parseAddLink(AggregateRspec rspec, LinkContents link) throws AggregateException {
        //assuming P2PVlan with interfaces already parsed
        String clientId = link.getClientId();
        String sourceId = null;
        String destId = null;
        String descr = "p2pVlan:rspecId=" + rspec.getRspecName() + ":clientId=" + clientId;
        String vlanTag = link.getVlantag();
        String capacity = "1000000"; // 1Mbps by default
        List<AggregateNetworkInterface> netIfs = new ArrayList<AggregateNetworkInterface>();
        //List<String> externalUrns = new ArrayList<String>(); 
        for (Object obj: link.getAnyOrPropertyOrLinkType()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("interface_ref")) {
                    InterfaceRefContents irc = (InterfaceRefContents)((JAXBElement)obj).getValue();
                    String netIfClientId = irc.getClientId();
                    AggregateNetworkInterface netIf = lookupInterfaceByClientId(rspec, netIfClientId);
                    if (netIf == null)
                        throw new AggregateException("interface_ref (client_id=" + netIfClientId + ") cannot be found.");
                    netIfs.add(netIf);
                } else if (elemName.equalsIgnoreCase("property")) {
                    LinkPropertyContents lpc = (LinkPropertyContents)((JAXBElement)obj).getValue();
                    sourceId = lpc.getSourceId();
                    destId = lpc.getDestId();
                    capacity = lpc.getCapacity();
                }

            }
            if (obj.getClass().getName().contains("ElementNSImpl")) {
                String elemName = AggregateUtils.getAnyName(obj);
                // 'external_urn' is replaced by using the source_id and dest_id in <property>
            }
        }
        for (AggregateNetworkInterface netIf: netIfs) { 
            netIf.setCapacity(capacity);
            if (netIf.getVlanTag().isEmpty()) {
                netIf.setVlanTag(vlanTag);
            }
            if (netIf.getPeers() == null) 
                netIf.setPeers(new ArrayList<String>());
            if (netIf.getLinks() == null)
                netIf.setLinks(new ArrayList<String>());
            if (netIf.getLinks().isEmpty()) {
                String linkUrn = AggregateState.getAggregateInterfaces().lookupAttachedLinkByUrn(netIf.getUrn());
                if (linkUrn == null)
                    throw new AggregateException("interface (urn=" + netIf.getUrn()+") has no attahced network link URN.");
                netIf.getLinks().add(linkUrn);
            }
        }
        boolean hasSourceIf = false;
        boolean hasDestIf = false;
        if (netIfs.size() == 2) {
            // create p2pVlan with peering source_if<-->destination+if, both implicit
            netIfs.get(0).getPeers().add(netIfs.get(1).getUrn());
            netIfs.get(1).getPeers().add(netIfs.get(0).getUrn());
            hasSourceIf = true;
            hasDestIf = true;
        } else if (netIfs.size() == 1) {
            // local netIf peering with explicit source or dest urn
            if (destId != null && (sourceId == null || sourceId.equalsIgnoreCase(netIfs.get(0).getUrn()))) {
                sourceId = netIfs.get(0).getLinks().get(0);
                hasSourceIf = true;
            } else if (sourceId != null && (destId == null || destId.equalsIgnoreCase(netIfs.get(0).getUrn()))) {
                destId = netIfs.get(0).getLinks().get(0);
                hasDestIf = true;
            } else {
                throw new AggregateException("single interface_ref must be paired with an external source_id or dest_id property in link:" + clientId);
            }
        } else if (netIfs.isEmpty()) {
            //create peering between sourceId and destId, both explicit urn
            if (sourceId != null && destId != null && sourceId.equalsIgnoreCase(destId)) {
            } else {
                throw new AggregateException("with zero interface_ref, both source_id or dest_id properties must be present in link:" + clientId);
            }
        } else {
            throw new AggregateException("number interface_ref's must be no greater than 2 in link:" + clientId);
        }
        //create explicit p2pvlan 
        if (netIfs.size() < 2 && sourceId != null && destId != null) {
            AggregateP2PVlan explicitP2PVlan = new AggregateP2PVlan();
            explicitP2PVlan.setSource(AggregateUtils.getIDCQualifiedUrn(sourceId));
            explicitP2PVlan.setDestination(AggregateUtils.getIDCQualifiedUrn(destId));
            explicitP2PVlan.setBandwidth(AggregateUtils.convertBandwdithToMbps(capacity));//50M by default
            explicitP2PVlan.setVtag(vlanTag);
            if (hasSourceIf) {
                explicitP2PVlan.setSrcInterface(netIfs.get(0).getDeviceName());
                explicitP2PVlan.setSrcIpAndMask(netIfs.get(0).getIpAddress());
            }
            if (hasDestIf) {
                explicitP2PVlan.setDstInterface(netIfs.get(0).getDeviceName());
                explicitP2PVlan.setDstIpAndMask(netIfs.get(0).getIpAddress());
            }
            // no device and IP needed if neither end is attached to node netIf
            explicitP2PVlan.setStitchingResourceId("explicit");
            explicitP2PVlan.setExternalResourceId("");
            rspec.getResources().add((AggregateResource)explicitP2PVlan);
        }
    }
    
    void parseStitchingResources(AggregateRspec rspec, GeniStitchTopologyContent stitchingTopogy) {
        // TODO:
    }

    AggregateNetworkInterface lookupInterfaceByClientId(AggregateRspec rspec, String id) {
        for (AggregateResource rc: rspec.getResources()) {
            if (rc.getType().equalsIgnoreCase("networkInterface") && rc.getClientId().equalsIgnoreCase(id)) {
                return (AggregateNetworkInterface)rc;
            }
        }
        return null;
    }
    
    public AggregateRspec configRspecFromFile(String filePath) throws AggregateException {
        throw new AggregateException("RspecHandler_GENIv3::configRspecFromFile not implemented");
        // loadCRDB only use this method from MAX rspecHandler instance? 
    }

    public String getRspecManifest(AggregateRspec rspec) throws AggregateException {
        String rspecMan = "";

        return rspecMan;
    }
    
    String[] extractStitchingRspec(String rspecXml) throws AggregateException {
        String[] rspecs = new String[2];
        int iStitchOpen1 = rspecXml.indexOf("<stitching");
        int iStitchOpen2 = (iStitchOpen1 == -1 ? -1 : rspecXml.indexOf(">", iStitchOpen1));
        int iStitchClose1 = rspecXml.indexOf("</stitching");
        int iStitchClose2 = (iStitchClose1 == -1 ? -1 : rspecXml.indexOf(">", iStitchClose1));
        if (iStitchOpen1 == -1 && iStitchClose1 == -1) {
            rspecs[0] = rspecXml;
            rspecs[1] = null;
        } else if (iStitchOpen1 == -1 || iStitchOpen2 == -1
                || iStitchClose1 == -1 || iStitchClose2 == -1) {
            throw new AggregateException("Missing or malformed <stitching> Rspec section.");
        } else {
            rspecs[0] = rspecXml.substring(0, iStitchOpen1 - 1);
            rspecs[0] += rspecXml.substring(iStitchClose2 + 1);
            rspecs[1] = rspecXml.substring(iStitchOpen2 + 1, iStitchClose1 - 1);
            rspecs[1] = rspecs[1].replace("<topology",
                    "<topology xmlns=\"http://geni.net/schema/stitching/topology/geniStitch/20110220/\"");
        }
        return rspecs;
    }
}
