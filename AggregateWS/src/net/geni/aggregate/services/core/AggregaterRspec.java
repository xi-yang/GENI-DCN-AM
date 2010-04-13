/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.*;
import java.util.*;
import org.hibernate.*;
import org.apache.log4j.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Xi Yang
 */
public class AggregaterRspec implements java.io.Serializable {
    private Session session;
    private org.apache.log4j.Logger log;
    private int id;
    private String rspecName;
    private String aggregateName;
    private long startTime;
    private long endTime;
    private List<AggregateResource> resources;

    public AggregaterRspec() {
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();
        log = Logger.getLogger(this.getClass());
        rspecName = "";
        aggregateName = "";
        startTime = endTime = 0;
        resources = new ArrayList<AggregateResource>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAggregateName() {
        return aggregateName;
    }

    public String getRspecName() {
        return rspecName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public List<AggregateResource> getResources() {
        return resources;
    }

    public void parseRspec(String rspec) throws AggregateException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

            //TODO: Validating document with schemas
            //final String rspecSchema = "max-rspec.xsd";
            //final String ctrlpSchema = "nmtopo-ctrlp.xsd";
            //final String[] schemas = {rspecSchema, ctrlpSchema};
            //final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
            //docFactory.setNamespaceAware(true);
            //docFactory.setValidating(true);
            //factory.setAttribute(JAXP_SCHEMA_SOURCE, schemas);

            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(rspec));
            Document rspecXMLDoc = docBuilder.parse(is);

            NodeList children = rspecXMLDoc.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
               Node child = children.item(i);
               String nodeName = child.getNodeName();
               if (nodeName != null && nodeName.equalsIgnoreCase("rspec")) {
                   rspecName = child.getAttributes().getNamedItem("id").getNodeValue();
                   children = child.getChildNodes();
                   for (i = 0; i < children.getLength(); i++) {
                       child = children.item(i);
                       nodeName = child.getNodeName();
                       if (nodeName != null && nodeName.equalsIgnoreCase("aggregate")) {
                           aggregateName = child.getNodeValue();
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:lieftime")) {
                           long[] lifetime = {this.startTime, this.endTime};
                           parseLifetime(child, lifetime);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("computeResource")) {
                           parseComputeResources(child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:topology")) {
                           parseNetworkTopology(child);
                       }
                   }

                   break;
               }
            }
            
        } catch (ParserConfigurationException e) {
            throw new AggregateException("AggregaterRspec.parseRspec Parser exception: " + e.getMessage());
        } catch (SAXException e) {
            throw new AggregateException("AggregaterRspec.parseRspec SAX exception: " + e.getMessage());
        } catch (IOException e) {
            throw new AggregateException("AggregaterRspec.parseRspec IO exception: " + e.getMessage());
        }
    }

    void parseLifetime(Node node, long[] lifetime) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("start")) {
                lifetime[0] = Integer.valueOf(child.getNodeValue());
            } else if (nodeName != null && nodeName.equalsIgnoreCase("end")) {
                lifetime[1] = Integer.valueOf(child.getNodeValue());
            } else if (nodeName != null && nodeName.equalsIgnoreCase("duration")) {
                lifetime[1] = lifetime[0] + Integer.valueOf(child.getNodeValue());
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
            } else if (nodeName != null && nodeName.equalsIgnoreCase("planetlabSliveNode")) {
                parseComputeNode(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("euclyptusSliveNode")) {
                parseComputeNode(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("netfpgaNode")) {
                parseNetFPGANode(child);
            }
        }
    }

    void parseNetworkTopology(Node topoRoot) throws AggregateException {
        //TODO ?
    }

    void parseComputeNode(Node compNodeRoot) throws AggregateException {
        //assuming planetlab node for now
        String sliverId = compNodeRoot.getAttributes().getNamedItem("id").getNodeValue();
        String address = "";
        NodeList children = compNodeRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("address")) {
                address = child.getNodeValue();
                //TODO: add address field in node structure
            } else if (nodeName != null && nodeName.equalsIgnoreCase("computeCapability")) {
                parseComputeCapability(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("networkInterface")) {
                parseNetworkInterface(child);
            }
        }
        //get node URN from sliverId
        String urn = "urn:"+AggregateUtils.getUrnField(sliverId, "node");
        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(urn);
        aggrNode.setType(compNodeRoot.getNodeName());
        aggrNode.setReference(aggrNode.getId());
        aggrNode.setRspecId(this.id); //rspec entry has been created in db
        //AggregateState.getAggregateNodes().update(aggrNode);
        resources.add(aggrNode);
    }

    void parseNetFPGANode(Node netfNodeRoot) throws AggregateException {
        //TODO ?
    }

    void parseComputeCapability(Node compCapRoot) throws AggregateException {
        //TODO ?
    }

    void parseNetworkInterface(Node netIfRoot) throws AggregateException {
        //assuming planetlab node and VLAN interface for now
        String netIfId = netIfRoot.getAttributes().getNamedItem("id").getNodeValue();
        String deviceName = "";
        String ipAddress = "";
        String vlanTag = "";
        ArrayList<String> peerNetIfs = new ArrayList<String>();
        NodeList children = netIfRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("deviceName")) {
                deviceName = child.getNodeValue();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("ipAddress")) {
                ipAddress = child.getNodeValue();
            } else if (nodeName != null && nodeName.equalsIgnoreCase("vlanRange")) {
                vlanTag = child.getNodeValue();
                //TODO: parse and set range of vlans ...
            } else if (nodeName != null && nodeName.equalsIgnoreCase("peerNetworkInterface")) {
                String peerNetIf = child.getNodeValue();
                peerNetIfs.add(peerNetIf);
            }
        }

        //AggregateNetworkInterface(s) will be future processed to create P2PVlans
        AggregateNetworkInterface aggrNetIf = new AggregateNetworkInterface(netIfId);
        aggrNetIf.setDeviceName(deviceName);
        aggrNetIf.setIpAddress(ipAddress);
        aggrNetIf.setVlanTag(vlanTag);
        aggrNetIf.setPeerInterfaces(peerNetIfs);
        resources.add(aggrNetIf);
    }
}
