/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

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
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

            //validating document with schemas
            if (AggregateState.getAggregateConfDir() != null) {
                /*
                URL schemaUrl = getClass().getClassLoader().getResource("max-rspec.xsd");
                final String rspecSchema = schemaUrl.toString();
                schemaUrl = getClass().getClassLoader().getResource("nmtopo-ctrlp.xsd");
                log.debug("shcema URL:"+ rspecSchema);
                final String ctrlpSchema = schemaUrl.toString();
                */
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
                           users.add(child.getTextContent().trim());
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
            if (nodeName != null && nodeName.equalsIgnoreCase("start")) {
                startTime = Integer.valueOf(child.getTextContent().trim());
            } else if (nodeName != null && nodeName.equalsIgnoreCase("end")) {
                endTime = Integer.valueOf(child.getTextContent().trim());
            } else if (nodeName != null && nodeName.equalsIgnoreCase("duration")) {
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
                parseComputeNode(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("planetlabNodeSliver")) {
                parseComputeNode(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("eucalyptusNodeSliver")) {
                parseComputeNode(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("netfpgaNode")) {
                parseNetFPGANode(child);
            }
        }
    }

    void parseNetworkTopology(Node topoRoot) throws AggregateException {
        //TODO ?
    }

    AggregateNode parseComputeNode(Node compNodeRoot) throws AggregateException {
        //assuming planetlab node for now
        String sliverId = compNodeRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        String address = "";
        NodeList children = compNodeRoot.getChildNodes();
        Vector<AggregateNetworkInterface> myNetIfs = new Vector<AggregateNetworkInterface>();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("address")) {
                address = child.getTextContent().trim();
                //TODO: add address field in node structure
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
        aggrNode.setType(compNodeRoot.getNodeName());
        aggrNode.setRspecId(this.id); //rspec entry has been created in db
        //AggregateState.getAggregateNodes().update(aggrNode);
        resources.add(aggrNode);
        for (AggregateNetworkInterface netIf: myNetIfs)
            netIf.setParentNode(aggrNode);
        return aggrNode;
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
        String deviceName = "";
        String ipAddress = "";
        String vlanTag = "";
        String capacity = "";
        ArrayList<String> peerNetIfs = new ArrayList<String>();
        NodeList children = netIfRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("deviceName")) {
                deviceName = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("ipAddress")) {
                ipAddress = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("vlanRange")) {
                vlanTag = child.getTextContent().trim();
                //TODO: parse and set range of vlans ...
            } else if (nodeName != null && nodeName.equalsIgnoreCase("capacity")) {
                capacity = child.getTextContent().trim();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("peerNetworkInterface")) {
                String peerNetIf = child.getTextContent().trim();
                peerNetIfs.add(peerNetIf);
            }
        }

        //AggregateNetworkInterface(s) will be future processed to create P2PVlans
        AggregateNetworkInterface aggrNetIf = new AggregateNetworkInterface(netIfId);
        aggrNetIf.setDeviceName(deviceName);
        aggrNetIf.setIpAddress(ipAddress);
        aggrNetIf.setVlanTag(vlanTag);
        aggrNetIf.setCapacity(capacity);
        aggrNetIf.setPeerInterfaces(peerNetIfs);
        aggrNetIf.setType(netIfRoot.getNodeName());
        //TODO: add AggregateNetworkInterfaces collection class
        //      add the aggrNetIf into DB (interfaces table)
        //      --> delete the aggrNetIf from DB when rspec is terminated?
        aggrNetIf.setRspecId(this.id);
        resources.add(aggrNetIf);
        return aggrNetIf;
    }

    void dumpRspec() {
        log.debug("Rspec name=" + this.rspecName + "aggregateName="+this.aggregateName);
        log.debug("Rspec startTime=" + Integer.toString((int)this.startTime)+" endTime="+Integer.toString((int)this.endTime));
        for (AggregateResource rc: resources) {
            log.debug("Resource: " + rc.getType());
            if (rc.getType().equalsIgnoreCase("planetlabNodeSliver")) {
                log.debug("   >>" + ((AggregateNode)rc).getDescription());
            } else if (rc.getType().equalsIgnoreCase("networkInterface")) {
                log.debug("   >>>>" + ((AggregateNetworkInterface)rc).getInterfaceId());
            } else if (rc.getType().equalsIgnoreCase("p2pVlan")) {
                log.debug("   >>>>" + ((AggregateP2PVlan)rc).getDescription() 
                    + " bandwdith="+Float.toString(((AggregateP2PVlan)rc).getBandwidth()));
            }
        }
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
                VlanReservationDescriptorType vlanDescr = new VlanReservationDescriptorType();
                vlanDescr.setDescription(p2pvlan.getDescription());
                vlanDescr.setSourceNode(p2pvlan.getSource());
                vlanDescr.setSrcInterface(p2pvlan.getSrcInterface());
                vlanDescr.setSrcIpAndMask(p2pvlan.getSrcIpAndMask());
                vlanDescr.setDestinationNode(p2pvlan.getDestination());
                vlanDescr.setDstInterface(p2pvlan.getDstInterface());
                vlanDescr.setDstIpAndMask(p2pvlan.getDstIpAndMask());
                vlanDescr.setBandwidth(p2pvlan.getBandwidth());
                vlanDescr.setVlan(p2pvlan.getVtag());

                VlanReservationResultType vlanResult = new VlanReservationResultType();
                vlanResult.setReservation(vlanDescr);
                vlanResult.setGlobalReservationId(p2pvlan.getGlobalReservationId());
                vlanResult.setStatus(p2pvlan.getStatus());
                vlanResult.setMessage(p2pvlan.getErrorMessage());
                vlanResults.add(vlanResult);
            }
        }
        return hm;
    }
}
