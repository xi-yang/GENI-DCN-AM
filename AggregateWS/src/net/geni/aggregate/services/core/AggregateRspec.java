/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.geni.aggregate.services.api.VlanReservationDescriptorType;
import net.geni.aggregate.services.api.VlanReservationResultType;

/**
 *
 * @author Xi Yang
 */
public class AggregateRspec implements java.io.Serializable {
    private org.apache.log4j.Logger log;
    private int id = 0 ;
    private String rspecName = "";
    private String aggregateName = "";
    private String description = "";
    private long startTime = 0;
    private long endTime = 0;
    private List<String> users = null;
    private List<AggregateResource> resources = null;
    private boolean addPlcSlice = false;
    private String status = "";
    private String xml = null;

    public AggregateRspec() {
        log = org.apache.log4j.Logger.getLogger(this.getClass());
        resources = new ArrayList<AggregateResource>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAggregateName(String aggregateName) {
        this.aggregateName = aggregateName;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setRspecName(String rspecName) {
        this.rspecName = rspecName;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public String getAggregateName() {
        return aggregateName;
    }

    public String getRspecName() {
        return rspecName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<AggregateResource> getResources() {
        return resources;
    }

    public boolean isAddPlcSlice() {
        return addPlcSlice;
    }

    public void setAddPlcSlice(boolean addPlcSlice) {
        this.addPlcSlice = addPlcSlice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void parseRspec(String rspec) throws AggregateException {
        xml = rspec.trim().replaceAll("\n\\s*","");
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            //validating document with schemas
            if (AggregateState.getAggregateConfDir() != null) {
                final String ctrlpSchema = "file:" + AggregateState.getAggregateConfDir() + "schema/nmtopo-ctrlp.xsd";
                final String rspecSchema = "file:" + AggregateState.getAggregateConfDir() + "schema/max-rspec.xsd";
                final String[] schemas = {ctrlpSchema, rspecSchema};
                final String JAXP_SHCEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
                final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
                final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
		final String JAXP_SCHEMA_NONAME_LOCATION = "http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation";
                docFactory.setNamespaceAware(true);
                docFactory.setValidating(true);
                docFactory.setAttribute(JAXP_SHCEMA_LANGUAGE, W3C_XML_SCHEMA);
                docFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemas);
                docFactory.setAttribute(JAXP_SCHEMA_NONAME_LOCATION, ctrlpSchema);
            }

            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(rspec.trim()));
            //TODO: docBuilder.setErrorHandler ... 
            Document rspecXMLDoc = docBuilder.parse(is);

            NodeList children = rspecXMLDoc.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
               Node child = children.item(i);
               String nodeName = child.getNodeName();
               if (nodeName != null && nodeName.equalsIgnoreCase("rspec")) {
                   rspecName = child.getAttributes().getNamedItem("id").getTextContent().trim();
                   children = child.getChildNodes();
                   for (i = 0; i < children.getLength(); i++) {
                       child = children.item(i);
                       nodeName = child.getNodeName();
                       if (nodeName != null && nodeName.equalsIgnoreCase("aggregate")) {
                           aggregateName = child.getTextContent().trim();
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("user")) {
                           if (users == null)
                               users = new ArrayList<String>();
                           if (child.getAttributes().getNamedItem("id") != null)
                               users.add(child.getAttributes().getNamedItem("id").getTextContent().trim());
                           else if (child.getAttributes().getNamedItem("name") != null)
                               users.add(child.getAttributes().getNamedItem("name").getTextContent().trim());
                           else if (child.getAttributes().getNamedItem("email") != null)
                               users.add(child.getAttributes().getNamedItem("email").getTextContent().trim());
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("description")) {
                           description = child.getTextContent().trim();
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("lifetime")) {
                           parseLifetime(child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("computeResource")) {
                           parseComputeResources(child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("topology")) {
                           parseNetworkTopology(child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("externalResource")) {
                           parseExternalResources(child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("stitchingResource")) {
                           parseStitchingResources(child);
                       }
                   }

                   break;
               }
            }            
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new AggregateException("AggregateRspec.parseRspec Parser exception: " + e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
            throw new AggregateException("AggregateRspec.parseRspec SAX exception: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new AggregateException("AggregateRspec.parseRspec IO exception: " + e.getMessage());
        }
    }

    void parseLifetime(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:start")) {
                startTime = Integer.valueOf(child.getTextContent().trim());
            } else if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:end")) {
                endTime = Integer.valueOf(child.getTextContent().trim());
            } else if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:duration")) {
                endTime = startTime + Integer.valueOf(child.getTextContent().trim());
            }
        }
    }

    void parseComputeResources(Node compRoot) throws AggregateException {
        NodeList children = compRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("computeNode")) {
                parseAddComputeNode(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("planetlabNodeSliver")) {
                parsePlanetlabNodeSliver(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("eucalyptusNodeSliver")) {
                parseEucalyptusNodeSliver(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("netfpgaNode")) {
                parseNetFPGANode(child);
            }
        }
    }

    void parseNetworkTopology(Node topoRoot) throws AggregateException {
        //TODO ?
    }

    AggregateNode parseAddComputeNode(Node compNodeRoot) throws AggregateException {
        int nodeId = 0;
        String urn = compNodeRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        String address = "";
        String caps = "";
        String descr = "";
        Vector<AggregateNetworkInterface> myNetIfs = new Vector<AggregateNetworkInterface>();
        NodeList children = compNodeRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("address")) {
                address = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("plId")) {
                nodeId = Integer.valueOf(child.getTextContent().trim());
            } else if (nodeName != null && nodeName.equalsIgnoreCase("capabilities")) {
                caps = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("description")) {
                descr = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("networkInterface")) {
                AggregateNetworkInterface netIf = parseNetworkInterface(child);
                myNetIfs.add(netIf);
            }
        }

        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(urn);
        if (aggrNode == null) {
            aggrNode = new AggregateNode(urn, nodeId, descr, caps);
            aggrNode.setAddress(address);
            aggrNode.setType(compNodeRoot.getNodeName());
            aggrNode.setRspecId(this.id); 
            if (AggregateState.getAggregateNodes().add(aggrNode) == false) {
                throw new AggregateException("Cannot add Node:" + aggrNode.getUrn() + "(id:" + Integer.toString(aggrNode.getNodeId()) + ") to DB");
            }
            resources.add(aggrNode);
            for (AggregateNetworkInterface netIf: myNetIfs) {
                netIf.setParentNode(aggrNode);
                if (AggregateState.getAggregateInterfaces().add(netIf) == false)
                    throw new AggregateException("Cannot add Interface:" + netIf.getUrn()
                            +"(id:"+Integer.toString(netIf.getId())+") to DB");
            }
        }
        return aggrNode;
    }

    void parsePlanetlabNodeSliver(Node plNodeRoot) throws AggregateException {
        String sliverId = plNodeRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        String address = "";
        NodeList children = plNodeRoot.getChildNodes();
        Vector<AggregateNetworkInterface> myNetIfs = new Vector<AggregateNetworkInterface>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("address")) {
                address = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("computeCapability")) {
                parseComputeCapability(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("networkInterface")) {
                AggregateNetworkInterface netIf = parseNetworkInterface(child);
                myNetIfs.add(netIf);
            }
        }
        //get node URN from sliverId
        String[] fields = {"domain","node"};
        String urn = "urn:ogf:geni:"+AggregateUtils.getUrnFields(sliverId, fields);
        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(urn);
        if (aggrNode == null) {
            throw new AggregateException("unknown aggregateNode "+urn+" (extracted from "+sliverId+")");
        }
        AggregateNode newNode = aggrNode.duplicate();
        newNode.setType(plNodeRoot.getNodeName());
        newNode.setRspecId(this.id); //rspec entry has been created in db
        resources.add(newNode);
        for (AggregateNetworkInterface netIf: myNetIfs)
            netIf.setParentNode(newNode);
    }

    void parseEucalyptusNodeSliver(Node eucaNodeRoot) throws AggregateException {
        //TODO ?
    }

    void parseNetFPGANode(Node netfNodeRoot) throws AggregateException {
        //TODO ?
    }

    void parseComputeCapability(Node compCapRoot) throws AggregateException {
        //TODO ?
    }

    AggregateNetworkInterface parseNetworkInterface(Node netIfRoot) throws AggregateException {
        //assuming planetlab node and VLAN interface for now
        String netIfId = netIfRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        String deviceType = "";
        String deviceName = "";
        String ipAddress = "";
        String vlanTag = "";
        String capacity = "";
        ArrayList<String> linkUrns = new ArrayList<String>();
        ArrayList<String> peerNetIfs = new ArrayList<String>();
        NodeList children = netIfRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("deviceType")) {
                deviceType = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("deviceName")) {
                deviceName = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("ipAddress")) {
                ipAddress = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("vlanRange")) {
                vlanTag = child.getTextContent().trim();
                //TODO: parse and set range of vlans ...
            } else if (nodeName != null && nodeName.equalsIgnoreCase("capacity")) {
                capacity = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("attachedLinkUrn")) {
                String linkUrn = child.getTextContent().trim();
                linkUrns.add(linkUrn);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("peerNetworkInterface")) {
                String peerNetIf = child.getTextContent().trim();
                peerNetIfs.add(peerNetIf);
            }
        }

        //AggregateNetworkInterface(s) will be further processed to create P2PVlans
        AggregateNetworkInterface aggrNetIf = new AggregateNetworkInterface(netIfId);
        aggrNetIf.setDeviceType(deviceType);
        aggrNetIf.setDeviceName(deviceName);
        aggrNetIf.setIpAddress(ipAddress);
        aggrNetIf.setVlanTag(vlanTag);
        aggrNetIf.setCapacity(capacity);
        aggrNetIf.setLinks(linkUrns);
        aggrNetIf.setPeers(peerNetIfs);
        aggrNetIf.setType(netIfRoot.getNodeName());
        aggrNetIf.setRspecId(this.id);
        resources.add(aggrNetIf);
        return aggrNetIf;
    }

    HashMap retrieveRspecInfo() {
        HashMap hm = new HashMap();
        for (AggregateResource rc: resources) {
            if (rc.getType().equalsIgnoreCase("computeSlice")) {
                AggregateSlice slice = (AggregateSlice)rc;
                hm.put("sliceStatus", slice.getStatus());
            } else if (rc.getType().equalsIgnoreCase("p2pVlan")) {
                AggregateP2PVlan p2pvlan = (AggregateP2PVlan)rc;
                Vector<VlanReservationResultType> vlanResults = (Vector<VlanReservationResultType>)hm.get("vlanResults");
                if (vlanResults == null) {
                    vlanResults = new Vector<VlanReservationResultType>();
                    hm.put("vlanResults", vlanResults);
                }
                vlanResults.add(p2pvlan.getVlanResvResult());
            } else if (rc.getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource ER = (AggregateExternalResource)rc;
                hm.put("externalResourceStatus", ER.getSubType()+":"+ER.getUrn()+":"+ER.getStatus()+":"+ER.getRspecData());
            }
        }
        return hm;
    }

    public void configRspecFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("Cannot open resource file:" + filePath);
            return;
        }
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            //no validating
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document rspecXMLDoc = docBuilder.parse(file);
            Node computeResourceNode = null;
            NodeList children = rspecXMLDoc.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
               Node child = children.item(i);
               String nodeName = child.getNodeName();
               if (nodeName != null && nodeName.equalsIgnoreCase("rspec")) {
                   id = 0;
                   rspecName = child.getAttributes().getNamedItem("id").getTextContent().trim();
                   children = child.getChildNodes();
                   for (i = 0; i < children.getLength(); i++) {
                       child = children.item(i);
                       nodeName = child.getNodeName();
                       if (nodeName != null && nodeName.equalsIgnoreCase("aggregate")) {
                           aggregateName = child.getTextContent().trim();
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("user")) {
                           try {
                               //parse and add users to DB
                               parseAddUser(child);
                           } catch (AggregateException e) {
                               log.error(e.getMessage()+" from resource file:" + filePath);
                           }
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("computeResource")) {
                           try {
                               //parse and add nodes and interfaces to DB inside
                               parseComputeResources(child);
                               computeResourceNode = child;
                           } catch (AggregateException e) {
                               log.error(e.getMessage()+" from resource file:" + filePath);
                           }
                       }
                   }
                   break;
               }
            }

            //reload resources
            resources.clear();
            List<AggregateNode> nodeList = AggregateState.getAggregateNodes().getAll();
            for (AggregateNode an: nodeList)
                resources.add((AggregateResource)an);
            List<AggregateNetworkInterface> interfaceList = AggregateState.getAggregateInterfaces().getAll();
            for (AggregateNetworkInterface ai: interfaceList) {
                for (AggregateNode an: nodeList) 
                    if (an.getId() == ai.getPnid()) {
                        ai.setParentNode(an);
                        break;
                    }
                resources.add((AggregateResource)ai);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            log.error("ParserConfigurationException when parsing resource file:" + filePath);
        } catch (SAXException e) {
            e.printStackTrace();
            log.error("SAXException when parsing resource file:" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("IOException when parsing resource file:" + filePath);
        }        
    }

    void parseAddUser(Node userRoot) throws AggregateException {
        int id = 0;
        String name = "";
        String role = "";
        String certSubject = "";
        String email = "";
        String firstName = "";
        String lastName = "";
        String descr = "";

        if (userRoot.getAttributes().getNamedItem("id") != null) {
            id = Integer.valueOf(userRoot.getAttributes().getNamedItem("id").getTextContent().trim());
        }
        if (userRoot.getAttributes().getNamedItem("name") != null) {
            name = userRoot.getAttributes().getNamedItem("name").getTextContent().trim();
        }
        if (userRoot.getAttributes().getNamedItem("role") != null) {
            role = userRoot.getAttributes().getNamedItem("role").getTextContent().trim();
        }
        if (userRoot.getAttributes().getNamedItem("certSubject") != null) {
            certSubject = userRoot.getAttributes().getNamedItem("certSubject").getTextContent().trim();
        }
        if (userRoot.getAttributes().getNamedItem("email") != null) {
            email = userRoot.getAttributes().getNamedItem("email").getTextContent().trim();
        }
        if (userRoot.getAttributes().getNamedItem("firstName") != null) {
            firstName = userRoot.getAttributes().getNamedItem("firstName").getTextContent().trim();
        }
        if (userRoot.getAttributes().getNamedItem("lastName") != null) {
            lastName = userRoot.getAttributes().getNamedItem("lastName").getTextContent().trim();
        }
        if (userRoot.getAttributes().getNamedItem("description") != null) {
            descr = userRoot.getAttributes().getNamedItem("description").getTextContent().trim();
        }

        AggregateUser aggrUser = AggregateState.getAggregateUsers().getById(id);
        if (aggrUser == null) {
            aggrUser = new AggregateUser(id, name, firstName, lastName, role, certSubject, email, descr);
            if (AggregateState.getAggregateUsers().add(aggrUser) == false)
                throw new AggregateException("Cannot add user:" + name +"(email:"+email+") to DB ");
        }
    }


    void parseExternalResources(Node extRoot) throws AggregateException {
        String urn = "";
        String subType = "";
        String smUri = "";
        String amUri = "";
        String rspecData = "";


        if (extRoot.getAttributes().getNamedItem("id") != null) {
            urn = extRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        }
        if (extRoot.getAttributes().getNamedItem("type") != null) {
            subType = extRoot.getAttributes().getNamedItem("type").getTextContent().trim();
        }

        NodeList children = extRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("sliceManager")) {
                smUri = child.getAttributes().getNamedItem("uri").getTextContent().trim();
                //TODO: optional args
            } else if (nodeName != null && nodeName.equalsIgnoreCase("aggregateManager")) {
                amUri = child.getAttributes().getNamedItem("uri").getTextContent().trim();
                //TODO: optional args
            } else if (nodeName != null && nodeName.equalsIgnoreCase("rspecData")) {
                rspecData = child.getTextContent().trim();
            }
        }
        if (!urn.isEmpty() && !subType.isEmpty()&& !amUri.isEmpty() && !rspecData.isEmpty()) {
            AggregateExternalResource aggrER = new AggregateExternalResource();
            aggrER.setType("externalResource");
            //aggrER.setRspecId(this.getId());
            aggrER.setUrn(urn);
            aggrER.setSubType(subType);
            aggrER.setAmUri(amUri);
            aggrER.setSmUri(smUri);
            aggrER.setRspecData(rspecData);
            aggrER.setExpireTime(this.endTime);
            resources.add((AggregateResource)aggrER);
        }
    }

    void parseStitchingResources(Node srRoot) throws AggregateException {
        String srId = "";
        String erId = "";
        String srType = "";
        List<String> netIfUrns = new ArrayList<String>();
        Vector<AggregateNetworkInterface> myNetIfs = new Vector<AggregateNetworkInterface>();

        if (srRoot.getAttributes().getNamedItem("id") != null) {
            srId = srRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        }
        if (srRoot.getAttributes().getNamedItem("type") != null) {
            srType = srRoot.getAttributes().getNamedItem("type").getTextContent().trim();
        }

        NodeList children = srRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("networkInterfaceUrn")) {
                netIfUrns.add(child.getTextContent().trim());
            }
            else if (nodeName != null && nodeName.equalsIgnoreCase("externalResourceId")) {
                erId = child.getTextContent().trim();
            }
            else if (nodeName != null && nodeName.equalsIgnoreCase("networkInterface")) {
                AggregateNetworkInterface netIf = parseNetworkInterface(child);
                netIf.setParentNode(null);
                myNetIfs.add(netIf);
            }
        }
        if (netIfUrns.size() ==2 && srType.equalsIgnoreCase("p2pvlan")) {
            AggregateNetworkInterface netIf1 = null;
            AggregateNetworkInterface netIf2 = null;
            for (AggregateResource rc: this.resources) {
                if (rc.getType().equalsIgnoreCase("networkInterface")) {
                    AggregateNetworkInterface netIf = (AggregateNetworkInterface)rc;
                    if (netIf.getUrn().equalsIgnoreCase(netIfUrns.get(0))) {
                        netIf1 = netIf;
                    }
                    else if (netIf.getUrn().equalsIgnoreCase(netIfUrns.get(1))) {
                        netIf2 = netIf;
                    }
                    else
                        continue;
                    netIf.setStitchingResourceId(srId);
                    netIf.setExternalResourceId(erId);
                }
            }
            //create p2pvlan
            AggregateP2PVlan stitchingP2PVlan = new AggregateP2PVlan();
            stitchingP2PVlan.setSource(netIfUrns.get(0));
            stitchingP2PVlan.setDestination(netIfUrns.get(1));
            stitchingP2PVlan.setVtag("any");
            stitchingP2PVlan.setBandwidth(50);//50M by default
            if (netIf1 != null) {
                stitchingP2PVlan.setSource(AggregateUtils.getUrnField(netIf1.getUrn(), "node") +"."+AggregateUtils.getUrnField(netIf1.getUrn(), "domain"));
                stitchingP2PVlan.setSrcInterface(netIf1.getDeviceName());
                stitchingP2PVlan.setSrcIpAndMask(netIf1.getIpAddress());
                stitchingP2PVlan.setBandwidth(AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity()));
                if (!netIf1.getVlanTag().isEmpty())
                    stitchingP2PVlan.setVtag(netIf1.getVlanTag());
            }
            if (netIf2 != null) {
                stitchingP2PVlan.setDestination(AggregateUtils.getUrnField(netIf2.getUrn(), "node") +"."+AggregateUtils.getUrnField(netIf2.getUrn(), "domain"));
                stitchingP2PVlan.setDstInterface(netIf2.getDeviceName());
                stitchingP2PVlan.setDstIpAndMask(netIf2.getIpAddress());
                if (netIf1 == null) {
                    stitchingP2PVlan.setBandwidth(AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity()));
                    if (!netIf2.getVlanTag().isEmpty())
                        stitchingP2PVlan.setVtag(netIf2.getVlanTag());
                }
            }
            stitchingP2PVlan.setStitchingResourceId(srId);
            stitchingP2PVlan.setExternalResourceId(erId);
            resources.add((AggregateResource)stitchingP2PVlan);
        }
        else if (netIfUrns.size() == 1 && srType.equalsIgnoreCase("stub")) {
            boolean noParentNode = true;
            for (AggregateResource rc: this.resources) {
                if (rc.getType().equalsIgnoreCase("networkInterface")) {
                    AggregateNetworkInterface netIf = (AggregateNetworkInterface)rc;
                    if (!netIf.getUrn().equalsIgnoreCase(netIfUrns.get(0)))
                        continue;
                    netIf.setStitchingResourceId(srId);
                    netIf.setExternalResourceId(erId);
                    noParentNode = false;
                }
            }
            if (noParentNode)
                throw new AggregateException("stub' stitching resource needs URN network interface on an existing compute node.");
        }
        else if (myNetIfs.size() < 2)
            throw new AggregateException("unknown stiching resource type or malformatted stitching resource information.");
    }


    String getResourcesXml() {
        //if (xml != null)
        //  return xml;
        xml = "<computeResource id=\"urn:aggregate="+this.aggregateName+":rspec="+this.rspecName+"\">";
        for (int n = 0; n < resources.size(); n++) {
            AggregateResource rc = resources.get(n);
            if (rc.getType().equalsIgnoreCase("computeNode") || rc.getType().equalsIgnoreCase("planetlabNodeSliver")) {
                AggregateNode an = (AggregateNode)rc;
                xml = xml + "<"+rc.getType()+" id=\""+an.getUrn()+"\">";
                //xml = xml + "<plId>"+Integer.toString(an.getNodeId())+"</plId>";
                for (int i = 0; i < resources.size(); i++) {
                    if (resources.get(i).getType().equalsIgnoreCase("networkInterface")) {
                        AggregateNetworkInterface ai = (AggregateNetworkInterface)resources.get(i);
                        if (ai.getPnid() == an.getId()) {
                            boolean hasP2PVlan = false;
                            for (int j = 0; j < resources.size(); j++) {
                                if (resources.get(j).getType().equalsIgnoreCase("p2pvlan")) {
                                    AggregateP2PVlan ppv = (AggregateP2PVlan) resources.get(j);
                                    if (ppv.getSource().equalsIgnoreCase(ai.getAttachedLinkUrns())) {
                                        xml = xml + "<networkInterface id=\"" + ai.getUrn() + "\">";
                                        xml = xml + "<deviceType>ethernet</deviceType>";
                                        xml = xml + "<deviceName>" + ppv.getSrcInterface() + "</deviceName>";
                                        xml = xml + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                                        xml = xml + "<ipAddress>" + ppv.getSrcIpAndMask() + "</ipAddress>";
                                        xml = xml + "<vlanRange>" + ppv.getVtag() + "</vlanRange>";
                                        xml = xml + "<attachedLinkUrn>" +ai.getAttachedLinkUrns()+"</attachedLinkUrn>";
                                        if (ai.getPeers().isEmpty())
                                            xml = xml + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=dst" + "</peerNetworkInterface>";
                                        else
                                            xml = xml + "<peerNetworkInterface>" + ai.getPeers().get(0) +"</peerNetworkInterface>";
                                        xml += "</networkInterface>";
                                        hasP2PVlan = true;
                                    }
                                    else if (ppv.getDestination().equalsIgnoreCase(ai.getAttachedLinkUrns())) {
                                        xml = xml + "<networkInterface id=\"" + ai.getUrn() + "\">";
                                        xml = xml + "<deviceType>ethernet</deviceType>";
                                        xml = xml + "<deviceName>" + ppv.getDstInterface() + "</deviceName>";
                                        xml = xml + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                                        xml = xml + "<ipAddress>" + ppv.getDstIpAndMask() + "</ipAddress>";
                                        xml = xml + "<vlanRange>" + ppv.getVtag() + "</vlanRange>";
                                        xml = xml + "<attachedLinkUrn>" +ai.getAttachedLinkUrns()+"</attachedLinkUrn>";
                                        if (ai.getPeers().isEmpty())
                                            xml = xml + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=src" + "</peerNetworkInterface>";
                                        else
                                            xml = xml + "<peerNetworkInterface>" + ai.getPeers().get(0) +"</peerNetworkInterface>";
                                        xml += "</networkInterface>";
                                        hasP2PVlan = true;
                                    }
                                }
                            }
                            if (!hasP2PVlan) {
                                xml = xml + "<networkInterface id=\""+ai.getUrn()+"\">";
                                xml = xml + "<deviceType>"+ai.getDeviceType()+"</deviceType>";
                                xml = xml + "<deviceName>"+ai.getDeviceName()+"</deviceName>";
                                xml = xml + "<capacity>"+ai.getCapacity()+"</capacity>";
                                xml = xml + "<ipAddress>"+ai.getIpAddress()+"</ipAddress>";
                                xml = xml + "<vlanRange>"+ai.getVlanTag()+"</vlanRange>";
                                xml = xml + "<attachedLinkUrn>"+ai.getAttachedLinkUrns()+"</attachedLinkUrn>";
                                xml +=  "</networkInterface>";
                            }
                        }
                    }
                }
                xml +=  "</"+rc.getType()+">";
            } else if (rc.getType().equalsIgnoreCase("computeSlice")) {
                AggregateSlice as = (AggregateSlice)rc;
                xml = xml + "<computeSlice id=\""+as.getSliceName()+"\">";
                xml = xml + "<node_ids>"+as.getNodes()+"</node_ids>";
                xml = xml + "<user_ids>"+as.getUsers()+"</user_ids>";
                xml = xml + "<expires>"+Long.toString(as.getExpiredTime())+"</expires>";
                xml +=  "</computeSlice>";
                String[] nodes = as.getNodes().split("[,\\s]");
                for (String nodeId: nodes) {
                    if (nodeId.isEmpty())
                        continue;
                    AggregateNode an = AggregateState.getAggregateNodes().getByNodeId(Integer.valueOf(nodeId));
                    if (an != null) {
                        boolean found = false;
                        for (AggregateResource rc1: resources) {
                            if ((rc1.getType().equalsIgnoreCase("computeNode") || rc1.getType().equalsIgnoreCase("planetlabNodeSliver"))
                                    && ((AggregateNode)rc1).getNodeId() == Integer.valueOf(nodeId))
                                found = true;
                        }
                        if (!found)
                            resources.add(an);
                    }
                }
            } else if (rc.getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource er = (AggregateExternalResource)rc;
                xml = xml + "<externalResource id=\""+er.getUrn()+"\" type=\""+er.getSubType()+"\">";
                if (!er.getSmUri().isEmpty())
                    xml = xml + "<sliceManager>"+er.getSmUri()+"</sliceManager>";
                if (!er.getAmUri().isEmpty())
                    xml = xml + "<aggregateManager>"+er.getAmUri()+"</aggregateManager>";
                xml = xml + "<rspecData>"+er.getRspecData()+"</rspecData>";
                xml +=  "</externalResource>";
            }
        }
        for (int n = 0; n < resources.size(); n++) {
            if (resources.get(n).getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan ppv = (AggregateP2PVlan)resources.get(n);
                if (ppv.getSrcInterface().isEmpty() || ppv.getDstInterface().isEmpty())
                    xml = xml + "<stitchingResource id=\"p2pvlan-"+ppv.getId()+"\" type=\"p2pvlan\">";
                if (ppv.getSrcInterface().isEmpty()) {
                    xml = xml + "<networkInterface id=\"p2pvlan-" + ppv.getId() + ":interface=src" + "\">";
                    xml = xml + "<deviceType>ethernet</deviceType>";
                    xml = xml + "<deviceName>" + ppv.getSrcInterface() + "</deviceName>";
                    xml = xml + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                    xml = xml + "<ipAddress>" + ppv.getSrcIpAndMask() + "</ipAddress>";
                    xml = xml + "<vlanRange>" + ppv.getVtag() + "</vlanRange>";
                    xml = xml + "<attachedLinkUrn>" + ppv.getSource() +"</attachedLinkUrn>";
                    xml = xml + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=dst" + "</peerNetworkInterface>";
                    xml += "</networkInterface>";
                }
                if (ppv.getDstInterface().isEmpty()) {
                    xml = xml + "<networkInterface id=\"p2pvlan-" + ppv.getId() + ":interface=dst" + "\">";
                    xml = xml + "<deviceType>ethernet</deviceType>";
                    xml = xml + "<deviceName>" + ppv.getDstInterface() + "</deviceName>";
                    xml = xml + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                    xml = xml + "<ipAddress>" + ppv.getDstIpAndMask() + "</ipAddress>";
                    xml = xml + "<vlanRange>" + ppv.getVtag() + "</vlanRange>";
                    xml = xml + "<attachedLinkUrn>" + ppv.getDestination() +"</attachedLinkUrn>";
                    xml = xml + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=src" + "</peerNetworkInterface>";
                    xml += "</networkInterface>";
                }
                if (ppv.getSrcInterface().isEmpty() || ppv.getDstInterface().isEmpty())
                    xml +=  "</stitchingResource>";
            }
        }
        xml +=  "</computeResource>";
        return xml;
    }

    void dumpRspec() {
        log.debug("Rspec name=" + this.rspecName + "aggregateName="+this.aggregateName);
        log.debug("Rspec startTime=" + Integer.toString((int)this.startTime)+" endTime="+Integer.toString((int)this.endTime));
        for (AggregateResource rc: resources) {
            log.debug("Resource: " + rc.getType());
            if (rc.getType().equalsIgnoreCase("planetlabNodeSliver")) {
                log.debug("   >>" + ((AggregateNode)rc).getDescription());
            } else if (rc.getType().equalsIgnoreCase("networkInterface")) {
                log.debug("   >>>>" + ((AggregateNetworkInterface)rc).getUrn());
            } else if (rc.getType().equalsIgnoreCase("p2pVlan")) {
                log.debug("   >>>>" + ((AggregateP2PVlan)rc).getDescription()
                    + " bandwdith="+Float.toString(((AggregateP2PVlan)rc).getBandwidth()));
            }
        }
    }
}
