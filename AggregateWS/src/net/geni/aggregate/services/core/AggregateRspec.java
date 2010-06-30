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
                docFactory.setNamespaceAware(true);
                docFactory.setValidating(true);
                docFactory.setAttribute(JAXP_SHCEMA_LANGUAGE, W3C_XML_SCHEMA);
                docFactory.setAttribute(JAXP_SCHEMA_SOURCE, schemas);
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

        //AggregateNetworkInterface(s) will be future processed to create P2PVlans
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
                rspecData = child.getAttributes().getNamedItem("rspecData").getTextContent().trim();
            }
        }
        if (!urn.isEmpty() && !subType.isEmpty()&& !amUri.isEmpty() && !rspecData.isEmpty()) {
            AggregateExternalResource aggrER = new AggregateExternalResource();
            aggrER.setUrn(urn);
            aggrER.setSubType(subType);
            aggrER.setAmUri(amUri);
            aggrER.setSmUri(smUri);
            aggrER.setRspecData(rspecData);
            if (AggregateState.getAggregateExtResources().add(aggrER) == false) {
                throw new AggregateException("Cannot add externalResourcece (URN:" + urn +") to DB ");
            }
            resources.add((AggregateResource)aggrER);
        }
    }

    String getResourcesXml() {
        if (xml != null)
            return xml;
        xml = "<computeResource id=\"urn:aggregate="+this.aggregateName+":rspec="+this.rspecName+"\">";
        for (AggregateResource rc: resources) {
            if (rc.getType().equalsIgnoreCase("computeNode")) {
                AggregateNode an = (AggregateNode)rc;
                xml = xml + "<computeNode id=\""+an.getUrn()+"\">";
                xml = xml + "<plId>"+Integer.toString(an.getNodeId())+"</plId>";
                for (int i = 0; i < resources.size(); i++) {
                    if (resources.get(i).getType().equalsIgnoreCase("networkInterface")) {
                        AggregateNetworkInterface ai = (AggregateNetworkInterface)resources.get(i);
                        if (ai.getPnid() == an.getId()) {
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
                xml +=  "</computeNode>";
            } else if (rc.getType().equalsIgnoreCase("computeSlice")) {
                AggregateSlice as = (AggregateSlice)rc;
                xml = xml + "<computeSlice id=\""+as.getSliceName()+"\">";
                xml = xml + "<node_ids>"+as.getNodes()+"</node_ids>";
                xml = xml + "<user_ids>"+as.getUsers()+"</user_ids>";
                xml = xml + "<expires>"+Long.toString(as.getExpiredTime())+"</expires>";
                xml +=  "</computeSlice>";
            } else if (rc.getType().equalsIgnoreCase("planetlabNodeSliver")) {
                AggregateNode an = (AggregateNode)rc;
                xml = xml + "<planetlabNodeSliver id=\""+an.getUrn()+"\">";
                xml = xml + "<address>"+an.getAddress()+"</address>";
                xml = xml + "<computeCapacity>";
                //xml = xml + "<cpuType>" + an.getComputeCapacity().??? + "</cpuType>";
                xml +=  "</computeCapacity>";
                for (int i = 0; i < resources.size(); i++) {
                    if (resources.get(i).getType().equalsIgnoreCase("networkInterface")) {
                        AggregateNetworkInterface ai = (AggregateNetworkInterface)resources.get(i);
                        xml = xml + "<networkInterface id=\""+ai.getUrn()+"\">";
                        xml = xml + "<deviceType>"+ai.getDeviceType()+"</deviceType>";
                        xml = xml + "<deviceName>"+ai.getDeviceName()+"</deviceName>";
                        xml = xml + "<capacity>"+ai.getCapacity()+"</capacity>";
                        xml = xml + "<ipAddress>"+ai.getIpAddress()+"</ipAddress>";
                        xml = xml + "<vlanRange>"+ai.getVlanTag()+"</vlanRange>";
                        xml = xml + "<peerNetworkInterface>"+ai.getPeerInterfaces()+"</peerNetworkInterface>";
                        xml +=  "</networkInterface>";
                    }
                }
                xml +=  "</planetlabNodeSliver>";
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
