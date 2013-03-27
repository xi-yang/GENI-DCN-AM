/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import edu.isi.east.hpn.rspec.ext.stitch._0_1.*;
import java.io.*;
import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.geni.www.resources.rspec._3.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author xyang
 */
public class RspecHandler_GENIv3 implements AggregateRspecHandler {
    private org.apache.log4j.Logger log;
    
    public RspecHandler_GENIv3() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public AggregateRspec parseRspecXml(String rspecXml) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();
        aggrRspec.setRequestXml(rspecXml);
        RSpecContents rspecV3Obj = null;
        StitchContent stitchObj = null;
        try {
            StringReader reader = new StringReader(rspecXml);
            JAXBContext jc = JAXBContext.newInstance("net.geni.www.resources.rspec._3");
            Unmarshaller unm = jc.createUnmarshaller();
            JAXBElement<RSpecContents> jaxbRspec = (JAXBElement<RSpecContents>) unm.unmarshal(reader);
            rspecV3Obj = jaxbRspec.getValue();
        } catch (Exception e) {
            throw new AggregateException("Error in unmarshling GENI RSpec v3 contents: " + e.getMessage());
        }
        // parse GENI RSpecv3 main section -- root params and nodes
        for (Object obj: rspecV3Obj.getAnyOrNodeOrLink()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("node")) {
                    parseAddNode(aggrRspec, (NodeContents) ((JAXBElement) obj).getValue());
                }
            } else if (obj.getClass().getName().contains("ElementNSImpl")) {
                String elemName = AggregateUtils.getAnyName(obj);
                if (elemName.equalsIgnoreCase("stitching")) {
                    try {
                        JAXBContext jc = JAXBContext.newInstance("edu.isi.east.hpn.rspec.ext.stitch._0_1");
                        JAXBElement<StitchContent> jaxbRspec = (JAXBElement<StitchContent>) jc.createUnmarshaller().unmarshal((Node)obj);
                        stitchObj = jaxbRspec.getValue();
                    } catch (Exception e) {
                        throw new AggregateException("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
                    }

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
        if (stitchObj != null) {
            parseStitchingResources(aggrRspec, stitchObj);
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
            log.debug("unknown node:"+urn+"  -- can be safely ignored only if this node belong to external aggregate");
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
                if (elemName.equalsIgnoreCase("attached_link")) {
                    linkUrns.add(AggregateUtils.getAnyText(obj));
                } else if (elemName.equalsIgnoreCase("vlantag")) {
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
        String descr = rspec.getRspecName() + " ("+ clientId+")";
        String capacity = "1000000"; // 1Mbps by default
        String vlanTag =  AggregateUtils.getAnyAttrString(link.getOtherAttributes(), "http://hpn.east.isi.edu/rspec/ext/stitch/0.1/", "vlantag");
        List<AggregateNetworkInterface> netIfs = new ArrayList<AggregateNetworkInterface>();
        List<String> externalUrns = new ArrayList<String>(); 
        for (Object obj: link.getAnyOrPropertyOrLinkType()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("interface_ref")) {
                    InterfaceRefContents irc = (InterfaceRefContents)((JAXBElement)obj).getValue();
                    String netIfClientId = irc.getClientId();
                    AggregateNetworkInterface netIf = lookupInterfaceByClientId(rspec, netIfClientId);
                    if (netIf == null) {
                        log.debug("interface_ref:'" + netIfClientId + "' cannot be found -- can be safely ignored only if it is a stitching reference or link to external aggregate");
                        externalUrns.add(netIfClientId);
                    } else {
                        netIfs.add(netIf);
                    }
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
            // handle link that binds stitching resource to local interface (link client_id == stitching path id)
            if (externalUrns.size() == 1) {
                netIfs.get(0).setStitchingResourceId(link.getClientId());
                return;
            }
            // local netIf peering with explicit source or dest urn
            else if (destId != null && (sourceId == null || sourceId.equalsIgnoreCase(netIfs.get(0).getUrn()))) {
                sourceId = netIfs.get(0).getLinks().get(0);
                hasSourceIf = true;
            } else if (sourceId != null && (destId == null || destId.equalsIgnoreCase(netIfs.get(0).getUrn()))) {
                destId = netIfs.get(0).getLinks().get(0);
                hasDestIf = true;
            } else {
                log.warn("single interface_ref must be paired with an external source_id or dest_id property in link - skip " + clientId);
                return;
            }
        } else if (netIfs.isEmpty()) {
            //create peering between sourceId and destId, both explicit urn's
            if (sourceId == null || destId == null || sourceId.equalsIgnoreCase(destId)) {
                log.warn("unknown interface_ref and missing source_id and dest_id properties in the link '" 
                        + clientId + "' -- can be safely ignored only if it belongs to external aggregate(s)");
                return;
            }
        } else {
            log.warn("number interface_ref's must be no greater than 2 in link - skip " + clientId);
            return;
        }
        if (netIfs.size() < 2 && sourceId != null && destId != null) {
            String verifySrcUrn = sourceId;
            if (AggregateUtils.isDcnUrn(sourceId)) {
                verifySrcUrn = AggregateUtils.convertDcnToGeniUrn(sourceId);
            }
            // create VLAN link only if the link has a local sourceId urn
            if (verifySrcUrn.contains(AggregateState.getAmUrn())) {
                //create implicit stitching p2pvlan 
                //otherwise a local p2pvlan will be created by RspecRunner later
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
                explicitP2PVlan.setClientId(clientId);
                explicitP2PVlan.setExternalResourceId("legacy-non-empty");
                rspec.getResources().add((AggregateResource) explicitP2PVlan);
            }
        }
    }
    
    void parseStitchingResources(AggregateRspec rspec, StitchContent stitchingTopology) throws AggregateException {
        // stitching resources == <topology> that contatins one or more <path> elements
        if (stitchingTopology.getPath() == null || stitchingTopology.getPath().isEmpty()) {
            throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching <topology> must have at least one <path>.");
        }
        for (PathContent path: stitchingTopology.getPath()) {
            List<HopContent> localHops = getAggregateLocalHops(path);
            // privision edge-to-edge -- skip explicit path hops in between (if any) 
            if (localHops.size() == 0) {
                throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching <path id=\"" 
                        + path.getId() + "\"> must have at least one <hop> elements.");
            }
            LinkContent srcLink = localHops.get(0).getLink();
            LinkContent dstLink = null;
            if (localHops.size() > 1) {
                dstLink = localHops.get(localHops.size()-1).getLink();
            }
            else {
                HopContent nextHop = getAggregateNextHop(path);
                if (nextHop != null)
                    dstLink = nextHop.getLink();
            }
            if (dstLink == null) {
                throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching <path id=\"" 
                        + path.getId() + "\"> missing destination <hop> element for current segment.");                
            }
            String source = srcLink.getId();
            AggregateNetworkInterface netIf1 = AggregateState.getAggregateInterfaces().getByUrn(srcLink.getId());
            if (netIf1 != null) {
                if (netIf1.getLinks() == null || netIf1.getLinks().isEmpty())
                    throw new AggregateException("Cannot resolve hop (link id'="+srcLink.getId()+"')");
                source = netIf1.getLinks().get(0);
            } else {
                source = AggregateUtils.convertGeniToDcnUrn(srcLink.getId());
            }
            String destination = dstLink.getId();
            AggregateNetworkInterface netIf2 = AggregateState.getAggregateInterfaces().getByUrn(dstLink.getId());
            if (netIf2 != null) {
                if (netIf2.getLinks() == null || netIf2.getLinks().isEmpty())
                    throw new AggregateException("Cannot resolve hop (link id'="+dstLink.getId()+"')");
                destination = netIf2.getLinks().get(0);
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
                stitchingP2PVlan.setVtag(srcVlan+":any");
            else if (!srcVlan.isEmpty() && dstVlan.isEmpty())
                stitchingP2PVlan.setVtag("any:"+dstVlan);
            else if (!srcVlan.isEmpty() && !dstVlan.isEmpty())
                stitchingP2PVlan.setVtag(srcVlan+":"+dstVlan);
            else 
                stitchingP2PVlan.setVtag("");
            // attach source or destination interface (device + IP)
            if (netIf1 != null) {
                netIf1 = lookupInterfaceByStitchingResourceId(rspec, path.getId());
                if (netIf1 != null) {
                    stitchingP2PVlan.setSrcInterface(netIf1.getDeviceName());
                    stitchingP2PVlan.setSrcIpAndMask(netIf1.getIpAddress());
                    if (stitchingP2PVlan.getVtag().isEmpty() && netIf1.getVlanTag() != null)
                        stitchingP2PVlan.setVtag(netIf1.getVlanTag());
                }
            }
            if (netIf2 != null) {
                netIf2 = lookupInterfaceByStitchingResourceId(rspec, path.getId());
                if (netIf2 != null) {
                    stitchingP2PVlan.setDstInterface(netIf2.getDeviceName());
                    stitchingP2PVlan.setDstIpAndMask(netIf2.getIpAddress());
                    if (stitchingP2PVlan.getVtag().isEmpty() && netIf2.getVlanTag() != null)
                        stitchingP2PVlan.setVtag(netIf2.getVlanTag());
                    else if (netIf2.getVlanTag() != null && !netIf2.getVlanTag().isEmpty())
                        stitchingP2PVlan.setVtag(stitchingP2PVlan.getVtag()+":"+netIf2.getVlanTag());
                }
            }
            if (stitchingP2PVlan.getVtag().isEmpty()) {
                stitchingP2PVlan.setVtag("any");
            }
            stitchingP2PVlan.setStitchingResourceId(path.getId());
            stitchingP2PVlan.setClientId(path.getId());
            stitchingP2PVlan.setExternalResourceId("legacy-non-empty");
            rspec.getResources().add((AggregateResource)stitchingP2PVlan);
        }
    }

    AggregateNetworkInterface lookupInterfaceByClientId(AggregateRspec rspec, String id) {
        for (int i = 0; i < rspec.getResources().size(); i++) {
            AggregateResource rc = rspec.getResources().get(i);
            if (rc.getType().equalsIgnoreCase("networkInterface") && rc.getClientId().equalsIgnoreCase(id)) {
                return (AggregateNetworkInterface)rc;
            }
        }
        return null;
    }

    AggregateNetworkInterface lookupInterfaceByStitchingResourceId(AggregateRspec rspec, String id) {
        for (int i = 0; i < rspec.getResources().size(); i++) {
            AggregateResource rc = rspec.getResources().get(i);
            if (rc.getType().equalsIgnoreCase("networkInterface") && ((AggregateNetworkInterface)rc).getStitchingResourceId().equalsIgnoreCase(id)) {
                return (AggregateNetworkInterface)rc;
            }
        }
        return null;
    }

    public AggregateRspec configRspecFromFile(String filePath) throws AggregateException {
        throw new AggregateException("RspecHandler_GENIv3::configRspecFromFile not implemented");
        // loadCRDB only use this method from MAX rspecHandler instance? 
    }

    public String generateAdvertisementRspec(AggregateRspec rspec) throws AggregateException {
        Date dateExpires = new Date(rspec.getEndTime() * 1000);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(dateExpires);
        XMLGregorianCalendar xgcExpires = null;
        try {
            xgcExpires = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (Exception e) {
            throw new AggregateException("RspecHandler_GENIv3.generateAdvertisementRspec error: " + e.getMessage());
        }
        String rspecMan = "<rspec type=\"advertisement\" expires=\"" + xgcExpires.toString()
                + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xmlns=\"http://www.geni.net/resources/rspec/3\" xmlns:stitch=\"http://hpn.east.isi.edu/rspec/ext/stitch/0.1/\""
                + " xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/ad.xsd"
                + " http://hpn.east.isi.edu/rspec/ext/stitch/0.1/ http://hpn.east.isi.edu/rspec/ext/stitch/0.1/stitch-schema.xsd\">";
        for (int n = 0; n < rspec.getResources().size(); n++) {
            AggregateResource rc = rspec.getResources().get(n);
            if (rc.getType().equalsIgnoreCase("computeNode") || rc.getType().equalsIgnoreCase("planetlabNodeSliver")) {
                AggregateNode an = (AggregateNode) rc;
                rspecMan = rspecMan + "<node component_id=\"" + an.getUrn() + "\" component_manager_id=\""
                        + rspec.getAggregateName() + "\" exclusive=\"true\">";
                rspecMan += "<hardware_type name=\"plab-pc\"/>";
                rspecMan += "<hardware_type name=\"pc\"/>";
                rspecMan += "<sliver_type name=\"plab-vserver\"/>";
                rspecMan += "<location country=\"unknown\" latitude=\"unknown\" longitude=\"unknown\"/>";
                for (int i = 0; i < rspec.getResources().size(); i++) {
                    rc = rspec.getResources().get(i);
                    if (!rc.getType().equalsIgnoreCase("networkInterface")) {
                        continue;
                    }
                    AggregateNetworkInterface ai = (AggregateNetworkInterface)rc;
                    if (ai.getParentNode() == an) // || AggregateUtils.getUrnField(ai.getUrn(), "node").equalsIgnoreCase(AggregateUtils.getUrnField(an.getUrn(), "node"))) {
                    {
                        rspecMan = rspecMan + "<interface component_id=\"" + ai.getUrn() + "\">";
                        if (!ai.getIpAddress().isEmpty()) {
                            rspecMan = rspecMan + "<ip address=\"" + ai.getIpAddress().split("/")[0]
                                    + "\" netmask=\"" + ai.getIpAddress().split("/")[1] + "\" type=\"ipv4\"/>";
                        }
                        rspecMan += "</interface>";
                    }
                }
                rspecMan += "</node>";
            }
        }
        rspecMan +=  "</rspec>";
        return rspecMan;
    }
    
    public String generateRspecManifest(AggregateRspec rspec) throws AggregateException {
        if (rspec == AggregateRspecManager.getAggrRspecGlobal()) {
            return generateAdvertisementRspec(rspec);
        }
        Date dateNow = new Date();
        Date dateExpires = new Date(rspec.getEndTime() * 1000);
        GregorianCalendar c0 = new GregorianCalendar();
        c0.setTime(dateNow);
        GregorianCalendar c1 = new GregorianCalendar();
        c1.setTime(dateExpires);
        XMLGregorianCalendar xgcGenerated = null;
        XMLGregorianCalendar xgcExpires = null;
        try {
            xgcGenerated = DatatypeFactory.newInstance().newXMLGregorianCalendar(c0);
            xgcExpires = DatatypeFactory.newInstance().newXMLGregorianCalendar(c1);
        } catch (Exception e) {
            throw new AggregateException("RspecHandler_GENIv3.generateRspecManifest error: " + e.getMessage());
        }

        String rspecXml = rspec.getRequestXml();
        if (rspecXml == null || rspecXml.isEmpty()) {
            throw new AggregateException("RspecHandler_GENIv3.generateRspecManifest null or empty request XML");
        }
        JAXBElement<RSpecContents> jaxbRspec;
        RSpecContents rspecV3Obj = null;
        try {
            StringReader reader = new StringReader(rspecXml);
            JAXBContext jc = JAXBContext.newInstance("net.geni.www.resources.rspec._3");
            Unmarshaller unm = jc.createUnmarshaller();
            jaxbRspec = (JAXBElement<RSpecContents>) unm.unmarshal(reader);
            rspecV3Obj = jaxbRspec.getValue();
        } catch (Exception e) {
            throw new AggregateException("RspecHandler_GENIv3.generateRspecManifest error in unmarshling GENI RSpec v3 contents: " + e.getMessage());
        }
        // annotating GENI RSpecv3 - change type, update expires
        rspecV3Obj.setType(RspecTypeContents.MANIFEST);
        rspecV3Obj.setGenerated(xgcGenerated);
        //rspecV3Obj.setGeneratedBy();
        rspecV3Obj.setExpires(xgcExpires);
        
        // get GENI RSpecv3 links and stitching objects
        List<LinkContents> linkObjList = null;
        JAXBElement<StitchContent> jaxbStitch = null;
        StitchContent stitchObj = null;
        Object stitchObjToRemove = null;
        for (Object obj: rspecV3Obj.getAnyOrNodeOrLink()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("link")) {
                    if (linkObjList == null) {
                        linkObjList = new ArrayList<LinkContents>();
                    }
                    linkObjList.add((LinkContents)((JAXBElement) obj).getValue());
                }
            } else if (obj.getClass().getName().contains("ElementNSImpl")) {
                String elemName = AggregateUtils.getAnyName(obj);
                if (elemName.equalsIgnoreCase("stitching")) {
                    try {
                        JAXBContext jc = JAXBContext.newInstance("edu.isi.east.hpn.rspec.ext.stitch._0_1");
                        jaxbStitch = (JAXBElement<StitchContent>) jc.createUnmarshaller().unmarshal((Node)obj);
                        stitchObj = jaxbStitch.getValue();
                        stitchObjToRemove = obj;
                    } catch (Exception e) {
                        throw new AggregateException("RspecHandler_GENIv3.generateRspecManifest error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
                    }
                }
            }
        }
        if (stitchObjToRemove != null) {
            rspecV3Obj.getAnyOrNodeOrLink().remove(stitchObjToRemove);
        }

        ArrayList<AggregateP2PVlan> ppvLinks = new ArrayList<AggregateP2PVlan>();
        ArrayList<AggregateP2PVlan> ppvStitches = new ArrayList<AggregateP2PVlan>();

        for (int l = 0; l < rspec.getResources().size(); l++) {
            if (rspec.getResources().get(l).getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan ppv = (AggregateP2PVlan)rspec.getResources().get(l);
                if (ppv.getSrcInterface().isEmpty() || ppv.getDstInterface().isEmpty())
                    ppvStitches.add(ppv);
                ppvLinks.add(ppv);
            }
        }
        
        for (LinkContents linkObj: linkObjList) {
            AggregateP2PVlan ppvLink = null;
            for (AggregateP2PVlan ppv: ppvLinks) {
                if (ppv.getClientId().equals(linkObj.getClientId())) {
                    ppvLink = ppv;
                    break;
                }
            }
            if (ppvLink == null)
                continue;
            String sliverId = ppvLink.getGri();
            if (sliverId == null || sliverId.isEmpty()) {
                sliverId = "null";
            }
            linkObj.setSliverId(String.format("%s+sliver+%s", AggregateState.getAmUrn(), sliverId));

            String[] vlanTags = ppvLink.getVtag().split(":");
            linkObj.setVlantag(((vlanTags.length == 2 && !vlanTags[0].equals(vlanTags[1]))?ppvLink.getVtag():vlanTags[0]));
        }

        if (!ppvStitches.isEmpty() && stitchObj != null && !stitchObj.getPath().isEmpty()) {
            stitchObj.setLastUpdateTime(xgcGenerated.toString());
            for (PathContent pathObj: stitchObj.getPath()) {
                AggregateP2PVlan ppvStitch = null;
                for (AggregateP2PVlan ppv: ppvStitches) {
                    if (ppv.getClientId().equals(pathObj.getId())) {
                        ppvStitch = ppv;
                        break;
                    }
                }
                if (ppvStitch == null)
                    continue;
                if (ppvStitch.getGri() != null && !ppvStitch.getGri().isEmpty()) {
                    pathObj.setGlobalId(ppvStitch.getGri());
                }
                // look up local ingress and egress hop links
                LinkContent ingLinkObj = null;
                LinkContent egrLinkObj = null;
                for (HopContent hopObj: pathObj.getHop()) {
                    LinkContent linkObj = hopObj.getLink();
                    if (linkObj == null) {
                        continue;
                    }
                    String srcUrn = AggregateUtils.convertDcnToGeniUrn(ppvStitch.getSource());
                    String dstUrn = AggregateUtils.convertDcnToGeniUrn(ppvStitch.getDestination());
                    if (linkObj.getId().equalsIgnoreCase(srcUrn)) {
                        ingLinkObj = linkObj;
                    }
                    if (linkObj.getId().equalsIgnoreCase(dstUrn)) {
                        egrLinkObj = linkObj;
                    }
                }
                if (ingLinkObj == null || egrLinkObj == null)
                    continue;
                // annotate local hops
                String[] vlanTags = ppvStitch.getVtag().split(":");
                ingLinkObj.setCapacity(Float.toString(ppvStitch.getBandwidth()));
                egrLinkObj.setCapacity(Float.toString(ppvStitch.getBandwidth()));
                if (!ingLinkObj.getSwitchingCapabilityDescriptor().isEmpty()
                    && ingLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo() != null
                    && ingLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc() != null
                    && !ingLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc().isEmpty()) {
                        SwitchingCapabilitySpecificInfoL2Sc l2scObj = ingLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc().get(0);
                        l2scObj.setVlanRangeAvailability(vlanTags[0]);
                        l2scObj.setSuggestedVLANRange(vlanTags[0]);
                        if ((vlanTags.length == 2 && !vlanTags[0].equals(vlanTags[1]))) {
                            l2scObj.setVlanTranslation(Boolean.TRUE);
                        }
                }
                if (!egrLinkObj.getSwitchingCapabilityDescriptor().isEmpty()
                    && egrLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo() != null
                    && egrLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc() != null
                    && !egrLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc().isEmpty()) {
                        SwitchingCapabilitySpecificInfoL2Sc l2scObj = egrLinkObj.getSwitchingCapabilityDescriptor().get(0).getSwitchingCapabilitySpecificInfo().getSwitchingCapabilitySpecificInfoL2Sc().get(0);
                        if (vlanTags.length == 2) {
                            l2scObj.setVlanRangeAvailability(vlanTags[1]);
                            l2scObj.setSuggestedVLANRange(vlanTags[1]);
                        } else {                            
                            l2scObj.setVlanRangeAvailability(vlanTags[0]);
                            l2scObj.setSuggestedVLANRange(vlanTags[0]);
                        }
                        if ((vlanTags.length == 2 && !vlanTags[0].equals(vlanTags[1]))) {
                            l2scObj.setVlanTranslation(Boolean.TRUE);
                        }
                }
            }            
        }
        rspecXml = this.marshallJaxbToString(jaxbRspec, "net.geni.www.resources.rspec._3");
        rspecXml = rspecXml.replaceFirst("xmlns=", " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/manifest.xsd\" xmlns=");
        if (jaxbStitch != null) {
            String stitchXml = this.marshallJaxbToString(jaxbStitch, "edu.isi.east.hpn.rspec.ext.stitch._0_1");
            stitchXml = stitchXml.replaceFirst("xmlns=", " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xsi:schemaLocation=\"http://hpn.east.isi.edu/rspec/ext/stitch/0.1/ http://hpn.east.isi.edu/rspec/ext/stitch/0.1/stitch-schema.xsd\" xmlns=");
            rspecXml = rspecXml.replaceFirst("</rspec>", stitchXml+"</rspec>");
        }
        return rspecXml;
    }

    private String marshallJaxbToString(JAXBElement jaxbObj, String jaxbContext) throws AggregateException {
        try {
            Document infoDoc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            infoDoc = db.newDocument();
            JAXBContext jc = JAXBContext.newInstance(jaxbContext);
            Marshaller m = jc.createMarshaller();
            m.marshal(jaxbObj, infoDoc);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            Source source = new DOMSource(infoDoc);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            log.error("RspecHandler_GENIv3.marshallJaxbToString error marshaling rspec: " + e.getMessage());
            throw new AggregateException(e.getMessage());
        }
    }
    
    AggregateNetworkInterface lookupInterfaceReference(AggregateRspec rspec, String urn) {
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
            AggregateNetworkInterface aif = (AggregateNetworkInterface)rc;
            if (isDcnUrn) {
                if (aif.getAttachedLinkUrns().contains(urn))
                    return aif;
            } else {
                if (aif.getUrn().equalsIgnoreCase(urn))
                    return aif;
            }
        }
        return null;
    }

    List<HopContent> getAggregateLocalHops(PathContent path) {
        List<HopContent> hops = new ArrayList<HopContent>();
        for (HopContent hop: path.getHop()) {
            if (hop.getLink() != null && hop.getLink().getId().contains(AggregateState.getAmUrn())) {
                hops.add(hop);
            }
        }
        return hops;
    }
    
    HopContent getAggregateNextHop(PathContent path) {
        boolean pastLocal = false;
        for (HopContent hop: path.getHop()) {
            if (hop.getLink() != null && hop.getLink().getId().contains(AggregateState.getAmUrn())) {
                pastLocal = true;
            }
            if (pastLocal && hop.getLink() != null && !hop.getLink().getId().contains(AggregateState.getAmUrn())) {
                return hop;
            }
        }
        return null;
    }

    String getLinkVlanRange(LinkContent link) {
        List<SwitchingCapabilityDescriptor> swcapList = link.getSwitchingCapabilityDescriptor();
        if (swcapList == null || swcapList.isEmpty())
            return "";
        SwitchingCapabilitySpecificInfoL2Sc l2scInfo = null;
        for (SwitchingCapabilityDescriptor swcap: swcapList) {
            SwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();            
            if (specInfo == null)
                continue;
            List<SwitchingCapabilitySpecificInfoL2Sc> l2scInfoList = specInfo.getSwitchingCapabilitySpecificInfoL2Sc();
            if (l2scInfoList != null && !l2scInfoList.isEmpty()) {
                l2scInfo = l2scInfoList.get(0);
                break;
            }
        }
        if (l2scInfo == null)
            return "";
        if (l2scInfo.getSuggestedVLANRange() != null && l2scInfo.getSuggestedVLANRange().matches("\\s*[0-9]+\\s*"))
            return l2scInfo.getSuggestedVLANRange();
        return l2scInfo.getVlanRangeAvailability();
    }
}
