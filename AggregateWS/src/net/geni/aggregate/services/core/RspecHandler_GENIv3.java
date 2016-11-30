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
import org.json.simple.JSONObject;
import net.geni.www.resources.rspec._3.*;
import net.geni.www.resources.rspec.ext.sdx._1.*;
import org.json.simple.JSONArray;
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
        SDXContent sdxObj = null;
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
                        JAXBElement<StitchContent> jaxbRspec = (JAXBElement<StitchContent>) jc.createUnmarshaller().unmarshal((Node) obj);
                        stitchObj = jaxbRspec.getValue();
                    } catch (Exception e) {
                        throw new AggregateException("Error in unmarshling GENI Stitching RSpec extension: " + e.getMessage());
                    }

                } else if (elemName.equalsIgnoreCase("sdx")) {
                    try {
                        JAXBContext payloadContext = JAXBContext.newInstance("net.geni.www.resources.rspec.ext.sdx._1");
                        JAXBElement<SDXContent> jaxbRspec = (JAXBElement<SDXContent>) payloadContext.createUnmarshaller().unmarshal((org.w3c.dom.Node) obj);
                        sdxObj = jaxbRspec.getValue();
                    } catch (Exception e) {
                        throw new AggregateException("Error in unmarshling GENI SDX RSpec extension: " + e.getMessage());
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
        // parse GENI SDX Rspec section
        if (sdxObj != null) {
            parseSdxResources(aggrRspec, sdxObj);
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
        
        AggregateNode newNode = null;
        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(urn);
        if (aggrNode == null) {
            aggrNode = new AggregateNode();
            aggrNode.setUrn(urn);
            newNode = aggrNode;
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
                    aggrNode.setType("computeNode:hardware_type="+hdc.getName());
               } else if (elemName.equalsIgnoreCase("sliver_type")) {
                    NodeContents.SliverType stc = (NodeContents.SliverType)((JAXBElement)obj).getValue();
                    aggrNode.setType("computeNode:sliver_type="+stc.getName());
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

        if (newNode == null) {
            newNode = aggrNode.duplicate();
        }
        newNode.setClientId(clientId);
        if (aggrNode.getType() == null || aggrNode.getType().isEmpty()) {
            aggrNode.setType("computeNode"); 
        }
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
        String address = "";
        String vlanTag = "";
        String capacity = "";
        String clientId = iface.getClientId();
        ArrayList<String> linkUrns = new ArrayList<String>();
        for (Object obj: iface.getAnyOrIpOrHost()) {
            if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                String elemName = ((JAXBElement)obj).getName().getLocalPart();
                if (elemName.equalsIgnoreCase("ip")) {
                    IpContents ipc = (IpContents)((JAXBElement)obj).getValue();
                    address = "ipv4+"+ipc.getAddress();
                    if (ipc.getNetmask() != null) {
                        address += "/";
                        //@TODO convert netmask into prefix format
                        address += ipc.getNetmask();
                    }
                    // assume type="ipv4"
                }
            }
            if (iface.getMacAddress() != null && !iface.getMacAddress().isEmpty() && !address.contains("mac+")) {
                if (!address.isEmpty()) {
                    address += ",";
                }
                address += "mac+" + iface.getMacAddress();
            }
            // optional:
            if (obj.getClass().getName().contains("ElementNSImpl")) {
                String elemName = AggregateUtils.getAnyName(obj);
                if (elemName.equalsIgnoreCase("attached_link")) {
                    linkUrns.add(AggregateUtils.getAnyText(obj));
                } else if (elemName.equalsIgnoreCase("vlantag")) {
                    vlanTag = AggregateUtils.getAnyText(obj);
                } else if (elemName.equalsIgnoreCase("gateway")) {
                    deviceType = "SRIOV";
                    deviceName = AggregateUtils.getAnyText(obj);
                }
            }
        }

        // extract deviceName from urn (assume last field after colon)
        if (urn != null && deviceName.isEmpty()) {
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
        }        
        // AggregateNetworkInterface(s) will be further processed to create P2PVlans
        AggregateNetworkInterface aggrNetIf = new AggregateNetworkInterface(urn);
        aggrNetIf.setClientId(clientId);
        aggrNetIf.setDeviceType(deviceType);
        aggrNetIf.setDeviceName(deviceName);
        aggrNetIf.setAddress(address);
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
        String capacity = "1000"; // 1Mbps by default
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
                    } else if (!netIf.getLinks().isEmpty() || netIf.getUrn() != null) {
                        netIfs.add(netIf);
                    }
                } else if (elemName.equalsIgnoreCase("property")) {
                    LinkPropertyContents lpc = (LinkPropertyContents)((JAXBElement)obj).getValue();
                    sourceId = lpc.getSourceId();
                    destId = lpc.getDestId();
                    if (lpc.getCapacity() != null)
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
            String verifyDstUrn = destId;
            if (AggregateUtils.isDcnUrn(destId)) {
                verifyDstUrn = AggregateUtils.convertDcnToGeniUrn(destId);
            }
            long bwKbps = AggregateUtils.convertBandwdithToKbpsLong(capacity);
            //make sure local urn is allowd by Ad RSpec
            if (verifySrcUrn.contains(AggregateState.getAmUrn())) {
                if (!AggregateState.getStitchTopoRunner().isValidEndPoint(verifySrcUrn)) {
                    log.error(String.format("'%s' is not valid end point - check the Ad RSpec", verifySrcUrn));
                    throw new AggregateException(String.format("'%s' is not valid end point - check the Ad RSpec", verifySrcUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidBandwidth(verifySrcUrn, bwKbps)) {
                    log.error(String.format("Requested bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", capacity, verifySrcUrn));
                    throw new AggregateException(String.format("Request bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", capacity, verifySrcUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidVlan(verifySrcUrn, vlanTag)) {
                    log.error(String.format("Requested VLAN '%s' is invalid for link '%s' - check the Ad RSpec", vlanTag, verifySrcUrn));
                    throw new AggregateException(String.format("Request VLAN '%s' is invalid for link '%s' - check the Ad RSpec", vlanTag, verifySrcUrn));
                }
            }
            if (verifyDstUrn.contains(AggregateState.getAmUrn())) {
                if (!AggregateState.getStitchTopoRunner().isValidEndPoint(verifyDstUrn)) {
                    log.error(String.format("'%s' is not valid end point - check the Ad RSpec", verifyDstUrn));
                    throw new AggregateException(String.format("Destination '%s' is not valid end point - check the Ad RSpec", verifyDstUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidBandwidth(verifyDstUrn, bwKbps)) {
                    log.error(String.format("Requested bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", capacity, verifyDstUrn));
                    throw new AggregateException(String.format("Request bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", capacity, verifyDstUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidVlan(verifyDstUrn, vlanTag)) {
                    log.error(String.format("Requested VLAN '%s' is invalid for link '%s' - check the Ad RSpec", vlanTag, verifyDstUrn));
                    throw new AggregateException(String.format("Request VLAN '%s' is invalid for link '%s' - check the Ad RSpec", vlanTag, verifyDstUrn));
                }
            }
            // create VLAN link only if the link has a local sourceId urn
            if (verifySrcUrn.contains(AggregateState.getAmUrn())) {
                //create implicit stitching p2pvlan 
                //otherwise a local p2pvlan will be created by RspecRunner later
                AggregateP2PVlan explicitP2PVlan = new AggregateP2PVlan();
                explicitP2PVlan.setSource(AggregateUtils.getIDCQualifiedUrn(sourceId));
                explicitP2PVlan.setDestination(AggregateUtils.getIDCQualifiedUrn(destId));
                explicitP2PVlan.setBandwidth(AggregateUtils.convertBandwdithToMbps(capacity));
                explicitP2PVlan.setVtag(vlanTag);
                if (hasSourceIf) {
                    explicitP2PVlan.setSrcInterface(netIfs.get(0).getDeviceName());
                    explicitP2PVlan.setSrcIpAndMask(netIfs.get(0).getAddress());
                }
                if (hasDestIf) {
                    explicitP2PVlan.setDstInterface(netIfs.get(0).getDeviceName());
                    explicitP2PVlan.setDstIpAndMask(netIfs.get(0).getAddress());
                }
                // no device and IP needed if neither end is attached to node netIf
                explicitP2PVlan.setStitchingResourceId("geni-implicit-stitching");
                explicitP2PVlan.setClientId(clientId);
                explicitP2PVlan.setExternalResourceId("legacy-non-empty");
                rspec.getResources().add((AggregateResource) explicitP2PVlan);
            }
        }
    }
    
    
    void parseSdxResources(AggregateRspec rspec, SDXContent sdx) throws AggregateException {
        AggregateSdxSliver sdxSliver = new AggregateSdxSliver();
        JSONObject reqJson = new JSONObject();
        if (sdx.getVirtualClouds() != null && !sdx.getVirtualClouds().isEmpty()) {
            JSONArray vpcArray = new JSONArray();
            reqJson.put("virtual_clouds", vpcArray);
            for (VirtualCloudContent sdxVc: sdx.getVirtualClouds()) {
                JSONObject vpcJson = new JSONObject();
                vpcArray.add(vpcJson);
                vpcJson.put("type", sdxVc.getType()); //'aws' or 'openstack'
                if (sdxVc.getClientId() != null) {
                    vpcJson.put("name", sdxVc.getClientId());
                }
                if (sdxVc.getCidr() != null) {
                    vpcJson.put("cidr", sdxVc.getCidr());
                }
                if (sdxVc.getProviderId() != null) {
                    vpcJson.put("parent", sdxVc.getProviderId());
                }
                if (sdxVc.getSubnets() != null && !sdxVc.getSubnets().isEmpty()) {
                    JSONArray subnetArray = new JSONArray();
                    vpcJson.put("subnets", subnetArray);
                    for (SubnetContent subnet: sdxVc.getSubnets()) {
                        JSONObject subnetJson = new JSONObject();
                        subnetArray.add(subnetJson);
                        if (subnet.getClientId() != null) {
                            subnetJson.put("name", subnet.getClientId());
                        }
                        if (subnet.getCidr() != null) {
                            subnetJson.put("cidr", subnet.getCidr());
                        }
                        if (subnet.getVirtualMachines()!= null && !subnet.getVirtualMachines().isEmpty()) {
                            JSONArray vmArray = new JSONArray();
                            for (VirtualMachine node: subnet.getVirtualMachines()) {
                                String clientId = node.getClientId();
                                Iterator itn = rspec.getResources().iterator();
                                while (itn.hasNext()) {
                                    AggregateResource res = (AggregateResource)itn.next();
                                    if (res.getType().startsWith("computeNode:sliver_type=") 
                                            && (res.getType().contains("instance+") || res.getType().contains("flavor+"))
                                            && res.getClientId() != null && clientId.equals(res.getClientId())) {
                                        AggregateNode aggrNode = (AggregateNode)res;
                                        JSONObject vmJson = new JSONObject();
                                        vmArray.add(vmJson);
                                        vmJson.put("name", clientId);
                                        vmJson.put("type", aggrNode.getType().substring("computeNode:sliver_type=".length()));
                                        if (node.getHost() != null && !node.getHost().isEmpty()) {
                                            vmJson.put("host", node.getHost());
                                        }
                                        JSONArray vifArray = null;
                                        Iterator itnif = rspec.getResources().iterator();
                                        while (itnif.hasNext()) {
                                            AggregateResource res2 = (AggregateResource)itnif.next();
                                            if (res2.getType().equals("networkInterface") && ((AggregateNetworkInterface) res2).getParentNode() == aggrNode) {
                                                if (vifArray == null) {
                                                    vifArray = new JSONArray();
                                                    vmJson.put("interfaces", vifArray);
                                                }
                                                AggregateNetworkInterface vif = (AggregateNetworkInterface) res2;
                                                    JSONObject vifJson = new JSONObject();
                                                    vifArray.add(vifJson);
                                                    vifJson.put("name", vif.getClientId());
                                                    if (vif.getDeviceType() != null && !vif.getDeviceType().isEmpty()) {
                                                        vifJson.put("type", vif.getDeviceType());
                                                    }
                                                    if (vif.getDeviceName() != null && !vif.getDeviceName().isEmpty() 
                                                            && vif.getDeviceType().equalsIgnoreCase("SRIOV")) {
                                                        vifJson.put("gateway", vif.getDeviceName());
                                                    }
                                                    if (vif.getAddress() != null && !vif.getAddress().isEmpty()) {
                                                        vifJson.put("address", vif.getAddress());
                                                    }
                                            }
                                        }
                                        // handle VM level routes
                                        if (node.getRoutes() != null && !node.getRoutes().isEmpty()) {
                                        JSONArray routeArray = new JSONArray();
                                        vmJson.put("routes", routeArray);
                                        for (RouteContent route : node.getRoutes()) {
                                            JSONObject routeJson = new JSONObject();
                                            routeArray.add(routeJson);
                                            if (route.getType() != null) {
                                                routeJson.put("type", route.getType());
                                            }
                                            if (route.getTo() != null) {
                                                JSONObject addrJson = new JSONObject();
                                                routeJson.put("to", addrJson);
                                                if (route.getTo().getType() != null) {
                                                    addrJson.put("type", route.getTo().getType());
                                                }
                                                addrJson.put("value", route.getTo().getValue());
                                            }
                                            if (route.getFrom() != null) {
                                                JSONObject addrJson = new JSONObject();
                                                routeJson.put("from", addrJson);
                                                if (route.getFrom().getType() != null) {
                                                    addrJson.put("type", route.getFrom().getType());
                                                }
                                                addrJson.put("value", route.getTo().getValue());
                                            }
                                            if (route.getNextHop() != null) {
                                                JSONObject addrJson = new JSONObject();
                                                routeJson.put("next_hop", addrJson);
                                                if (route.getNextHop().getType() != null) {
                                                    addrJson.put("type", route.getNextHop().getType());
                                                }
                                                addrJson.put("value", route.getNextHop().getValue());
                                            }
                                        }        
                                        }
                                        // handle ceph_rbd
                                        if (node.getCephRbds() != null && !node.getCephRbds().isEmpty()) {
                                        JSONArray rbdArray = new JSONArray();
                                        vmJson.put("ceph_rbd", rbdArray);
                                        for (CephRbdContent cephRbd: node.getCephRbds()) {
                                            JSONObject rbdJson = new JSONObject();
                                            rbdArray.add(rbdJson);
                                            if (cephRbd.getSizeGb() != null) {
                                                rbdJson.put("disk_gb", cephRbd.getSizeGb());
                                            }
                                            if (cephRbd.getMountPoint() != null) {
                                                rbdJson.put("mount_point", cephRbd.getMountPoint());
                                            }
                                        }
                                        }
                                        // handel quagga_bgp
                                        if (node.getQuaggaBgp() != null && !node.getQuaggaBgp().isEmpty()) {
                                            QuaggaBgpContent quaggaBgp = node.getQuaggaBgp().get(0);
                                            JSONObject bgpJson = new JSONObject();
                                            vmJson.put("quagga_bgp", bgpJson);
                                            if (quaggaBgp.getNeighbors() != null && !quaggaBgp.getNeighbors().isEmpty()) {
                                                JSONArray neighborArray = new JSONArray();
                                                bgpJson.put("neighbors", neighborArray);
                                                for (BgpNeighborContent neighbor: quaggaBgp.getNeighbors()) {
                                                    JSONObject neighborJson = new JSONObject();
                                                    neighborArray.add(neighborJson);
                                                    neighborJson.put("remote_asn", neighbor.getRemoteAsn());
                                                    neighborJson.put("bgp_authkey", neighbor.getBgpAuthkey());
                                                }
                                            }
                                            if (quaggaBgp.getNetworks()!= null && !quaggaBgp.getNetworks().isEmpty()) {
                                                JSONArray networkArray = new JSONArray();
                                                bgpJson.put("networks", networkArray);
                                                for (String network: quaggaBgp.getNetworks()) {
                                                    networkArray.add(network);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            subnetJson.put("virtual_machines", vmArray);
                        }
                        if (subnet.getRoutes() != null & !subnet.getRoutes().isEmpty()) {
                            JSONArray routeArray = new JSONArray();
                            subnetJson.put("routes", routeArray);
                            for (RouteContent route: subnet.getRoutes()) {
                                JSONObject routeJson = new JSONObject();
                                routeArray.add(routeJson);
                                if (route.getType() != null) {
                                    routeJson.put("type", route.getType());
                                }
                                if (route.getTo() != null) {
                                    JSONObject addrJson = new JSONObject();
                                    routeJson.put("to", addrJson);
                                    if (route.getTo().getType() != null) {
                                        addrJson.put("type", route.getTo().getType());
                                    }
                                    addrJson.put("value", route.getTo().getValue());
                                }
                                if (route.getFrom() != null) {
                                    JSONObject addrJson = new JSONObject();
                                    routeJson.put("from", addrJson);
                                    if (route.getFrom().getType() != null) {
                                        addrJson.put("type", route.getFrom().getType());
                                    }
                                    addrJson.put("value", route.getTo().getValue());
                                }
                                if (route.getNextHop() != null) {
                                    JSONObject addrJson = new JSONObject();
                                    routeJson.put("next_hop", addrJson);
                                    if (route.getNextHop().getType() != null) {
                                        addrJson.put("type", route.getNextHop().getType());
                                    }
                                    addrJson.put("value", route.getNextHop().getValue());
                                }
                            }
                        }
                    }
                }
                if (sdxVc.getGateways() != null && !sdxVc.getGateways().isEmpty()) {
                    JSONArray gatewayArray = new JSONArray();
                    vpcJson.put("gateways", gatewayArray);
                    for (GatewayContent gateway: sdxVc.getGateways()) {
                        JSONObject gatewayJson = new JSONObject();
                        gatewayArray.add(gatewayJson);
                        if (gateway.getClientId() != null) {
                            gatewayJson.put("name", gateway.getClientId());
                        }
                        if (gateway.getType() != null) {
                            gatewayJson.put("type", gateway.getType());
                        }
                        if (gateway.getTo() != null && !gateway.getTo().isEmpty()) {
                            JSONArray toAddrArray = new JSONArray();
                            gatewayJson.put("to", toAddrArray);
                            for (NetworkAddressContent addr: gateway.getTo()) {
                                JSONObject addrJson = new JSONObject();
                                toAddrArray.add(addrJson);
                                if (addr.getType() != null) {
                                    addrJson.put("type", addr.getType());
                                }
                                addrJson.put("value", addr.getValue());
                            }
                        }
                        if (gateway.getFrom() != null && !gateway.getFrom().isEmpty()) {
                            JSONArray fromAddrArray = new JSONArray();
                            gatewayJson.put("from", fromAddrArray);
                            for (NetworkAddressContent addr: gateway.getFrom()) {
                                JSONObject addrJson = new JSONObject();
                                fromAddrArray.add(addrJson);
                                if (addr.getType() != null) {
                                    addrJson.put("type", addr.getType());
                                }
                                addrJson.put("value", addr.getValue());
                            }
                        }
                    }
                }
                if (sdxVc.getRoutes() != null && !sdxVc.getRoutes().isEmpty()) {
                    JSONArray routeArray = new JSONArray();
                    vpcJson.put("routes", routeArray);
                    for (RouteContent route: sdxVc.getRoutes()) {
                        JSONObject routeJson = new JSONObject();
                        routeArray.add(routeJson);
                        if (route.getType() != null) {
                            routeJson.put("type", route.getType());
                        }
                                if (route.getTo() != null) {
                                    JSONObject addrJson = new JSONObject();
                                    routeJson.put("to", addrJson);
                                    if (route.getTo().getType() != null) {
                                        addrJson.put("type", route.getTo().getType());
                                    }
                                    addrJson.put("value", route.getTo().getValue());
                                }
                                if (route.getFrom() != null) {
                                    JSONObject addrJson = new JSONObject();
                                    routeJson.put("from", addrJson);
                                    if (route.getFrom().getType() != null) {
                                        addrJson.put("type", route.getFrom().getType());
                                    }
                                    addrJson.put("value", route.getTo().getValue());
                                }
                                if (route.getNextHop() != null) {
                                    JSONObject addrJson = new JSONObject();
                                    routeJson.put("next_hop", addrJson);
                                    if (route.getNextHop().getType() != null) {
                                        addrJson.put("type", route.getNextHop().getType());
                                    }
                                    addrJson.put("value", route.getNextHop().getValue());
                                }
                    }
                }
            }
        }
        sdxSliver.setType("sdxSliver");
        sdxSliver.setRspecId(rspec.getId());
        sdxSliver.setRequestJson(reqJson.toJSONString());
        sdxSliver.setManifest("");
        sdxSliver.setStatus("INIT");
        rspec.getResources().add(sdxSliver);
    }
    
    void parseStitchingResources(AggregateRspec rspec, StitchContent stitchingTopology) throws AggregateException {
        // stitching resources == <topology> that contatins one or more <path> elements
        if (stitchingTopology.getPath() == null || stitchingTopology.getPath().isEmpty()) {
            throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching <topology> must have at least one <path>.");
        }
        boolean hasLocalPath = false;
        for (PathContent path: stitchingTopology.getPath()) {
            List<HopContent> localHops = getAggregateLocalHops(path);
            // privision edge-to-edge -- skip explicit path hops in between (if any) 
            if (localHops.size() == 0) {
                continue;
            }
            hasLocalPath = true;
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
                // allow for dangled stitching path (open end) - skip - a use case for SDX
                continue;
                //throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching <path id=\"" 
                //        + path.getId() + "\"> missing destination <hop> element for current segment.");                
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
            //make sure local urn is allowd by Ad RSpec
            String verifySrcUrn = source;
            if (AggregateUtils.isDcnUrn(source)) {
                verifySrcUrn = AggregateUtils.convertDcnToGeniUrn(source);
            }
            String verifyDstUrn = destination;
            long srcBandwidth = AggregateUtils.convertBandwdithToKbpsLong(srcLink.getCapacity());
            long dstBandwidth = AggregateUtils.convertBandwdithToKbpsLong(dstLink.getCapacity());
            String srcVlan = getLinkVlanRange(srcLink);
            String dstVlan = getLinkVlanRange(dstLink);
            if (AggregateUtils.isDcnUrn(destination)) {
                verifyDstUrn = AggregateUtils.convertDcnToGeniUrn(destination);
            }
            if (verifySrcUrn.contains(AggregateState.getAmUrn())) {
                if (!AggregateState.getStitchTopoRunner().isValidEndPoint(verifySrcUrn)) {
                    log.error(String.format("'%s' is not valid end point - check the Ad RSpec", verifySrcUrn));
                    throw new AggregateException(String.format("'%s' is not valid end point - check the Ad RSpec", verifySrcUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidBandwidth(verifySrcUrn, srcBandwidth)) {
                    log.error(String.format("Requested bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", srcLink.getCapacity(), verifySrcUrn));
                    throw new AggregateException(String.format("Request bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", srcLink.getCapacity(), verifySrcUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidVlan(verifySrcUrn, srcVlan)) {
                    log.error(String.format("Requested VLAN '%s' is invalid for link '%s' - check the Ad RSpec", srcVlan, verifySrcUrn));
                    throw new AggregateException(String.format("Request VLAN '%s' is invalid for link '%s' - check the Ad RSpec", srcVlan, verifySrcUrn));
                }
            }
            if (verifyDstUrn.contains(AggregateState.getAmUrn())) {
                if (!AggregateState.getStitchTopoRunner().isValidEndPoint(verifyDstUrn)) {
                    log.error(String.format("'%s' is not valid end point - check the Ad RSpec", verifyDstUrn));
                    throw new AggregateException(String.format("Destination '%s' is not valid end point - check the Ad RSpec", verifyDstUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidBandwidth(verifyDstUrn, dstBandwidth)) {
                    log.error(String.format("Requested bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", dstLink.getCapacity(), verifyDstUrn));
                    throw new AggregateException(String.format("Request bandwidth '%s' is invalid for link '%s' - check the Ad RSpec", dstLink.getCapacity(), verifyDstUrn));
                }
                if (!AggregateState.getStitchTopoRunner().isValidVlan(verifyDstUrn, dstVlan)) {
                    log.error(String.format("Requested VLAN '%s' is invalid for link '%s' - check the Ad RSpec", dstVlan, verifyDstUrn));
                    throw new AggregateException(String.format("Request VLAN '%s' is invalid for link '%s' - check the Ad RSpec", dstVlan, verifyDstUrn));
                }
            }
            //create p2pvlan1
            AggregateP2PVlan stitchingP2PVlan = new AggregateP2PVlan();
            stitchingP2PVlan.setSource(source);
            stitchingP2PVlan.setDestination(destination);
            stitchingP2PVlan.setBandwidth(AggregateUtils.convertBandwdithToMbps(srcLink.getCapacity()));
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
                    stitchingP2PVlan.setSrcIpAndMask(netIf1.getAddress());
                    if (stitchingP2PVlan.getVtag().isEmpty() && netIf1.getVlanTag() != null)
                        stitchingP2PVlan.setVtag(netIf1.getVlanTag());
                }
            }
            if (netIf2 != null) {
                netIf2 = lookupInterfaceByStitchingResourceId(rspec, path.getId());
                if (netIf2 != null) {
                    stitchingP2PVlan.setDstInterface(netIf2.getDeviceName());
                    stitchingP2PVlan.setDstIpAndMask(netIf2.getAddress());
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
        if (!hasLocalPath) {
            throw new AggregateException("RspecHandler_GENIv3::parseStitchingResources stitching extension must have at least one <path> with local <hop> elements.");
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
        Date dateNow = new Date();
        GregorianCalendar c = new GregorianCalendar();
        c.setTimeInMillis(dateNow.getTime());
        c.add(Calendar.DAY_OF_MONTH, 60);
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
            if (rc.getType().equalsIgnoreCase("computeNode") || rc.getType().startsWith("computeNode")) {
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
                        if (!ai.getAddress().isEmpty()) {
                            rspecMan = rspecMan + "<ip address=\"" + ai.getAddress().split("/")[0]
                                    + "\" netmask=\"" + ai.getAddress().split("/")[1] + "\" type=\"ipv4\"/>";
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
        Date dateCreated = new Date(rspec.getStartTime() * 1000);
        Date dateExpires = new Date(rspec.getEndTime() * 1000);
        GregorianCalendar c0 = new GregorianCalendar();
        c0.setTime(dateNow);
        GregorianCalendar c1 = new GregorianCalendar();
        c1.setTime(dateCreated);
        GregorianCalendar c2 = new GregorianCalendar();
        c2.setTime(dateExpires);
        XMLGregorianCalendar xgcGenerated = null;
        XMLGregorianCalendar xgcCreated = null;
        XMLGregorianCalendar xgcExpires = null;
        try {
            xgcGenerated = DatatypeFactory.newInstance().newXMLGregorianCalendar(c0);
            xgcCreated = DatatypeFactory.newInstance().newXMLGregorianCalendar(c1);
            xgcExpires = DatatypeFactory.newInstance().newXMLGregorianCalendar(c2);
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
        Object sdxObjToRemove = null;
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
                } else if (elemName.equalsIgnoreCase("sdx")) {
                    sdxObjToRemove = obj;
                }
            }
        }
        if (stitchObjToRemove != null) {
            rspecV3Obj.getAnyOrNodeOrLink().remove(stitchObjToRemove);
        }
        if (sdxObjToRemove != null) {
            rspecV3Obj.getAnyOrNodeOrLink().remove(sdxObjToRemove);
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
        
        if (linkObjList != null) {
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
                String sliceIdFields[] = rspec.getRspecName().split("\\+");
                linkObj.setSliverId(String.format("%s+sliver+%s_vlan_%s", AggregateState.getAmUrn(), sliceIdFields[sliceIdFields.length-1], sliverId));
                String[] vlanTags = ppvLink.getVtag().split(":");
                linkObj.setVlantag(((vlanTags.length == 2 && !vlanTags[0].equals(vlanTags[1]))?ppvLink.getVtag():vlanTags[0]));
            }
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
                if (ppvStitch == null) {
                    continue;
                }
                // set lifetime to circuit lifetime
                Lifetime lft = new Lifetime();
                lft.setId(ppvStitch.getGri());
                TimeContent start = new TimeContent();
                start.setType("xgc");
                start.setValue(xgcCreated.toString());
                lft.setStart(start);
                TimeContent end = new TimeContent();
                end.setType("xgc");
                end.setValue(xgcExpires.toString());
                lft.setEnd(end);
                pathObj.setLifetime(lft);
                // set globalId to GRI
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
                ingLinkObj.setCapacity(Float.toString(ppvStitch.getBandwidth()*1000)); // convert mbps to kbps unit
                egrLinkObj.setCapacity(Float.toString(ppvStitch.getBandwidth()*1000)); // convert mbps to kbps unit
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

        if (sdxObjToRemove != null) {
            for (int x = 0; x < rspec.getResources().size(); x++) {
                if (rspec.getResources().get(x).getType().equalsIgnoreCase("sdxSliver")) {
                    AggregateSdxSliver sdx = (AggregateSdxSliver)rspec.getResources().get(x);
                    if (rspec.getStatus().equals("WORKING")) {
                        String sdxXml = ("<sdx xmlns=\"http://www.geni.net/resources/rspec/ext/sdx/1/\">" + sdx.getManifest() + "</sdx>");
                        rspecXml = rspecXml.replaceFirst("</rspec>", sdxXml+"</rspec>");
                    }
                }
            }
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
