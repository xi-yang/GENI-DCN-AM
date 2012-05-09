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
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.geni.www.resources.rspec._3.*;
import net.geni.schema.stitching.topology.genistitch._20110220.*;

/**
 *
 * @author xyang
 */
public class RspecHandler_GENIv3 implements AggregateRspecHandler {
    private org.apache.log4j.Logger log;
    
    public RspecHandler_GENIv3() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public static String[] extractStitchingRspec(String rspecXml) throws AggregateException {
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
            if (!rspecs[1].contains("http://geni.net/schema/stitching/topology")) {
                rspecs[1] = rspecs[1].replace("<topology",
                    "<topology xmlns=\"http://geni.net/schema/stitching/topology/geniStitch/20110220/\"");
            }
        }
        return rspecs;
    }

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
        // parse GENI RSpecv3 main section -- root params and nodes
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

        long now = System.currentTimeMillis()/1000;
        XMLGregorianCalendar xgcExpires = rspecV3Obj.getExpires();
        long expires = now + 3600*24; // default = 1 day
        if (xgcExpires != null) {
            Date dateExpires = xgcExpires.toGregorianCalendar().getTime();
            expires = dateExpires.getTime()/1000;
            if (expires - now < 4*60) // duration must be at least 4 minutes
                expires = now + 3600*24; // default = 1 day
        }
        aggrRspec.setStartTime(now);
        aggrRspec.setEndTime(expires);
        String aggrName = "unknown";
        int ind1 = AggregateState.getAmUrn().indexOf("urn:publicid:IDN+");
        if (ind1 != -1)
            aggrName = AggregateState.getAmUrn().substring(ind1+17);
        aggrRspec.setAggregateName(AggregateState.getAmUrn());
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
        
        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(urn);
        if (aggrNode == null) {
            // TODO: log and return -- not an exception
            log.debug("unknown node:"+urn+" (this node could belong to external aggregate)");
            return;
        }

        // sub-elements: interface etc.
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
            // optional:
            if (obj.getClass().getName().contains("ElementNSImpl")) {
                String elemName = AggregateUtils.getAnyName(obj);
                if (elemName.equalsIgnoreCase("address")) {
                    address = AggregateUtils.getAnyText(obj);
                } else if (elemName.equalsIgnoreCase("description")) {
                    descr = AggregateUtils.getAnyText(obj);
                }

            }
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
            // optional:
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
                    if (netIf == null) {
                        log.debug("interface_ref:'" + netIfClientId + "' cannot be found (this could be a link to external aggregate)");
                        return;
                    }
                    netIfs.add(netIf);
                } else if (elemName.equalsIgnoreCase("property")) {
                    LinkPropertyContents lpc = (LinkPropertyContents)((JAXBElement)obj).getValue();
                    sourceId = lpc.getSourceId();
                    destId = lpc.getDestId();
                    capacity = lpc.getCapacity();
                }

            }
            // optional:
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
            explicitP2PVlan.setStitchingResourceId("geni-implicit-stitching");
            explicitP2PVlan.setExternalResourceId("");
            rspec.getResources().add((AggregateResource)explicitP2PVlan);
        }
    }
    
    void parseStitchingResources(AggregateRspec rspec, GeniStitchTopologyContent stitchingTopology) throws AggregateException {
        // stitching resources == <topology> that contatins one or more <path> elements
        if (stitchingTopology.getPath() == null || stitchingTopology.getPath().isEmpty()) {
            throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching <topology> must have at least one <path>.");
        }
        for (GeniStitchPathContent path: stitchingTopology.getPath()) {
            List<GeniStitchHopContent> localHops = getAggregateLocalHops(path);
            // privision edge-to-edge -- skip explicit path hops in between (if any) 
            if (localHops.size() < 2) {
                throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching <path id=\"" 
                        + path.getId() + "\" must have at least two <hop> elements.");
            }
            GeniStitchLinkContent srcLink = localHops.get(0).getLink();
            GeniStitchLinkContent dstLink = localHops.get(localHops.size()-1).getLink();
            String source = srcLink.getId();
            AggregateNetworkInterface netIf = AggregateState.getAggregateInterfaces().getByUrn(srcLink.getId());
            if (netIf != null) {
                if (netIf.getLinks() == null || netIf.getLinks().isEmpty())
                    throw new AggregateException("Cannot resolve hop (link id'="+srcLink.getId()+"')");
                source = netIf.getLinks().get(0);
            } else {
                source = AggregateUtils.convertGeniToDcnUrn(srcLink.getId());
            }
            String destination = dstLink.getId();
            netIf = AggregateState.getAggregateInterfaces().getByUrn(dstLink.getId());
            if (netIf != null) {
                if (netIf.getLinks() == null || netIf.getLinks().isEmpty())
                    throw new AggregateException("Cannot resolve hop (link id'="+dstLink.getId()+"')");
                destination = netIf.getLinks().get(0);
            } else {
                destination = AggregateUtils.convertGeniToDcnUrn(dstLink.getId());
            }
            //create p2pvlan
            AggregateP2PVlan stitchingP2PVlan = new AggregateP2PVlan();
            float bandwidth = AggregateUtils.convertBandwdithToMbps(srcLink.getCapacity());
            stitchingP2PVlan.setSource(source);
            stitchingP2PVlan.setDestination(destination);
            stitchingP2PVlan.setBandwidth(bandwidth);
            String srcVlan = getLinkVlanRange(srcLink);
            String dstVlan = getLinkVlanRange(dstLink);
            if (srcVlan.isEmpty() && !dstVlan.isEmpty())
                stitchingP2PVlan.setVtag(srcVlan+"-any");
            else if (!srcVlan.isEmpty() && dstVlan.isEmpty())
                stitchingP2PVlan.setVtag("any-"+dstVlan);
            else if (!srcVlan.isEmpty() && !dstVlan.isEmpty())
                stitchingP2PVlan.setVtag(srcVlan+"-"+dstVlan);
            else 
                stitchingP2PVlan.setVtag("");
            // attach source or destination interface (device + IP)
            AggregateNetworkInterface netIf1 = AggregateState.getAggregateInterfaces().getByAttachedLink(srcLink.getId());
            if (netIf1 != null) {
                stitchingP2PVlan.setSrcInterface(netIf1.getDeviceName());
                stitchingP2PVlan.setSrcIpAndMask(netIf1.getIpAddress());
                if (stitchingP2PVlan.getVtag().isEmpty())
                    stitchingP2PVlan.setVtag(netIf1.getVlanTag());
            }
            AggregateNetworkInterface netIf2 = AggregateState.getAggregateInterfaces().getByAttachedLink(dstLink.getId());
            if (netIf2 != null) {
                stitchingP2PVlan.setSrcInterface(netIf2.getDeviceName());
                stitchingP2PVlan.setSrcIpAndMask(netIf2.getIpAddress());
                if (stitchingP2PVlan.getVtag().isEmpty())
                    stitchingP2PVlan.setVtag(netIf2.getVlanTag());
                else if (!netIf2.getVlanTag().isEmpty())
                    stitchingP2PVlan.setVtag(stitchingP2PVlan.getVtag()+"-"+netIf2.getVlanTag());
            }
            if (stitchingP2PVlan.getVtag().isEmpty()) {
                stitchingP2PVlan.setVtag("any");
            }
            stitchingP2PVlan.setStitchingResourceId(path.getId()+"-geni-stitching");
            stitchingP2PVlan.setExternalResourceId("legacy-non-empty");
            rspec.getResources().add((AggregateResource)stitchingP2PVlan);
        }
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
        Date dateExpires = new Date(rspec.getEndTime() * 1000);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(dateExpires);
        XMLGregorianCalendar xgcExpires = null;
        try {
            xgcExpires = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (Exception e) {
            throw new AggregateException("RspecHandler_GENIv3.getRspecManifest error: " + e.getMessage());
        }
        //XMLGregorianCalendar xgcExpires = new XMLGregorianCalendar(dateExpires);
        String rspecMan = "<rspec type=\"manifest\" expires=\"" + xgcExpires.toString()
                + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/manifest.xsd\""
                + " xmlns=\"http://www.geni.net/resources/rspec/3\">";
        for (int n = 0; n < rspec.getResources().size(); n++) {
            AggregateResource rc = rspec.getResources().get(n);
            if (rc.getType().equalsIgnoreCase("computeNode") || rc.getType().equalsIgnoreCase("planetlabNodeSliver")) {
                AggregateNode an = (AggregateNode) rc;
                rspecMan = rspecMan + "<node client_id=\"" + an.getClientId()
                        + "\" component_id=\"" + an.getUrn() + "\" component_manager_id=\""
                        + rspec.getAggregateName() + " exclusive=\"true\">";
                rspecMan += "<hardware_type name=\"plab-pc\"/>";
                rspecMan += "<hardware_type name=\"pc\"/>";
                rspecMan += "<sliver_type name=\"plab-vserver\"/>";
                rspecMan += "<location country=\"unknown\" latitude=\"unknown\" longitude=\"unknown\"/>";
                for (int i = 0; i < rspec.getResources().size(); i++) {
                    rc = rspec.getResources().get(i);
                    if (!rc.getType().equalsIgnoreCase("networkInterface")) {
                        continue;
                    }
                    AggregateNetworkInterface ai = (AggregateNetworkInterface) rc;
                    if (ai.getParentNode() == an) // || AggregateUtils.getUrnField(ai.getUrn(), "node").equalsIgnoreCase(AggregateUtils.getUrnField(an.getUrn(), "node"))) {
                    {
                        rspecMan = rspecMan + "<interface client_id=\"" + ai.getClientId() + "\" + component_id=\"" + ai.getUrn() + "\">";
                    }
                    if (!ai.getIpAddress().isEmpty()) {
                        rspecMan = rspecMan + "<ip address=\"" + ai.getIpAddress().split("/")[0]
                                + "\" + mask=\"" + ai.getIpAddress().split("/")[1] + "\" type=\"ipv4\">";
                    }
                    // optional (any extension)
                    if (!ai.getAttachedLinkUrns().isEmpty()) {
                        rspecMan = rspecMan + "<attached_link>" + ai.getAttachedLinkUrns() + "</attached_link>";
                    }
                    rspecMan += "</interface>";
                }
            }
            rspecMan += "</node>";
        }

        ArrayList<AggregateP2PVlan> ppvLinks = new ArrayList<AggregateP2PVlan>();
        ArrayList<AggregateP2PVlan> ppvStitches = new ArrayList<AggregateP2PVlan>();

        for (int l = 0; l < rspec.getResources().size(); l++) {
            if (rspec.getResources().get(l).getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan ppv = (AggregateP2PVlan)rspec.getResources().get(l);
                if (ppv.getSrcInterface().isEmpty() || ppv.getDstInterface().isEmpty())
                    ppvStitches.add(ppv);
                else
                    ppvLinks.add(ppv);
            }
        }
        
        for (AggregateP2PVlan ppv: ppvLinks) {
            rspecMan = rspecMan + "<link client_id=\"p2pvlan-"+ppv.getId()
                        + "\" vlantag=\"" + ppv.getVtag() + "\">";

            AggregateNetworkInterface netIf = lookupInterfaceReference(rspec, ppv.getSource());
            if (netIf != null)
                rspecMan = rspecMan + "<interface_ref client_id=\""+ netIf.getClientId() +"\"/>";
            netIf = lookupInterfaceReference(rspec, ppv.getDestination());
            if (netIf != null)
                rspecMan = rspecMan + "<interface_ref client_id=\""+ netIf.getClientId() +"\"/>";
            
            rspecMan +=  "<property>";
            rspecMan = rspecMan + "<source_id>" + ppv.getSource() + "</source_id>";
            rspecMan = rspecMan + "<dest_id>" + ppv.getDestination() + "</dest_id>";
            //optional (any extension)
            rspecMan = rspecMan + "<global_resource_id>" + ppv.getGlobalReservationId() + "</global_resource_id>";
            rspecMan +=  "</property>";

            rspecMan +=  "</link>";
        }

        if (!ppvStitches.isEmpty()) {
            rspecMan +=  "<stitching>";
            Date dateNow = new Date();
            rspecMan =  rspecMan + "<topology lastUpdateTime=\"" + dateNow.toString() + "\" xmlns=\"http://geni.net/schema/stitching/topology/geniStitch/20110220/\">";
            for (AggregateP2PVlan ppv: ppvStitches) {
                rspecMan = rspecMan + "<path id=\"GRI-" + ppv.getGri() + "\">";
                String[] vlanTags = ppv.getVtag().split("-");
                // create source hop
                String urn = ppv.getSource();
                AggregateNetworkInterface netIf = lookupInterfaceReference(rspec, urn);
                if (netIf != null) {
                    urn = netIf.getUrn();
                }
                rspecMan +=  "<hop id=\"src\" type=\"strict\">";
                rspecMan = rspecMan + "<link id=\""+urn+"\">";
                rspecMan = rspecMan + "<capacity>"+Float.toString(ppv.getBandwidth())+"Mbps</capacity>";
                rspecMan +=  "<switchingCapabilityDescriptor>";
                rspecMan +=  "<switchingcapType>l2sc</switchingcapType>";
                rspecMan +=  "<encodingType>ethernet</encodingType>";
                rspecMan +=  "<switchingCapabilitySpecificInfo>";
                rspecMan +=  "<switchingCapabilitySpecificInfo_L2sc>";
                rspecMan +=  "<interfaceMTU>9000</interfaceMTU>";
                rspecMan = rspecMan +  "<vlanRangeAvailability>"+vlanTags[0]+"</vlanRangeAvailability>";
                rspecMan = rspecMan +  "<suggestedVLANRange>"+vlanTags[0]+"</suggestedVLANRange>";
                rspecMan = rspecMan + "<vlanTranslation>"+((vlanTags.length == 2 && !vlanTags[0].equals(vlanTags[1]))?"true":"false")+"</vlanTranslation>";
                rspecMan +=  "</switchingCapabilitySpecificInfo_L2sc>";
                rspecMan +=  "</switchingCapabilitySpecificInfo>";
                rspecMan +=  "</switchingCapabilityDescriptor>";
                rspecMan +=  "</link>";
                rspecMan = rspecMan + "<nextHop>dst</nextHop>";
                rspecMan +=  "</hop>";
                // TODO: get intermediate hops from OSCARS query (stored in DB?)
                // create destination hop
                urn = ppv.getDestination();
                netIf = lookupInterfaceReference(rspec, urn);
                if (netIf != null) {
                    urn = netIf.getUrn();
                }
                rspecMan +=  "<hop id=\"dst\" type=\"strict\">";
                rspecMan = rspecMan + "<link id=\""+urn+"\">";
                rspecMan = rspecMan + "<capacity>"+Float.toString(ppv.getBandwidth())+"Mbps</capacity>";
                rspecMan +=  "<switchingCapabilityDescriptor>";
                rspecMan +=  "<switchingcapType>l2sc</switchingcapType>";
                rspecMan +=  "<encodingType>ethernet</encodingType>";
                rspecMan +=  "<switchingCapabilitySpecificInfo>";
                rspecMan +=  "<switchingCapabilitySpecificInfo_L2sc>";
                rspecMan +=  "<interfaceMTU>9000</interfaceMTU>";
                rspecMan =  rspecMan + "<vlanRangeAvailability>"+(vlanTags.length == 2?vlanTags[1]:vlanTags[0])+"</vlanRangeAvailability>";
                rspecMan =  rspecMan + "<suggestedVLANRange>"+(vlanTags.length == 2?vlanTags[1]:vlanTags[0])+"</suggestedVLANRange>";
                rspecMan =  rspecMan + "<vlanTranslation>"+((vlanTags.length == 2 && !vlanTags[0].equals(vlanTags[1]))?"true":"false")+"</vlanTranslation>";
                rspecMan +=  "</switchingCapabilitySpecificInfo_L2sc>";
                rspecMan +=  "</switchingCapabilitySpecificInfo>";
                rspecMan +=  "</switchingCapabilityDescriptor>";
                rspecMan +=  "</link>";
                rspecMan +=  "<nextHop>null</nextHop>";
                rspecMan +=  "</hop>";
                rspecMan +=  "</path>";
            }            
            rspecMan +=  "</topology>";
            rspecMan +=  "</stitching>";
        }
        rspecMan +=  "</rspec>";
        return rspecMan;
    }
    
    AggregateNetworkInterface lookupInterfaceReference(AggregateRspec rspec, String urn) {
        AggregateNetworkInterface aif = null;
        boolean isDcnUrn = false;
        if (AggregateUtils.isDcnUrn(urn))
            isDcnUrn = true;
        else if (AggregateUtils.isGeniUrn(urn))
            isDcnUrn = false;
        else 
            return null;
        for (int i = 0; i < rspec.getResources().size(); i++) {
            AggregateResource rc = rspec.getResources().get(i);
            if (!rc.getType().equalsIgnoreCase("networkInterface")) {
                continue;
            }
            aif = (AggregateNetworkInterface)rc;
            if (isDcnUrn) {
                if (aif.getAttachedLinkUrns().contains(urn))
                    return aif;
            } else {
                if (aif.getUrn().equalsIgnoreCase(urn))
                    return aif;
            }
        }
        return aif;
    }

    List<GeniStitchHopContent> getAggregateLocalHops(GeniStitchPathContent path) {
        List<GeniStitchHopContent> hops = new ArrayList<GeniStitchHopContent>();
        for (GeniStitchHopContent hop: path.getHop()) {
            if (hop.getLink() != null || hop.getLink().getId().contains(AggregateState.getAmUrn())) {
                hops.add(hop);
            }
        }
        return hops;
    }
    
    String getLinkVlanRange(GeniStitchLinkContent link) {
        GeniStitchSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
        if (swcap == null)
            return "";
        GeniStitchSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
        if (specInfo == null)
            return "";
        List<GeniStitchSwitchingCapabilitySpecificInfoL2Sc> l2scInfo = specInfo.getSwitchingCapabilitySpecificInfoL2Sc();
        if (l2scInfo == null || l2scInfo.isEmpty())
            return "";
        return l2scInfo.get(0).getSuggestedVLANRange();
    }
}
