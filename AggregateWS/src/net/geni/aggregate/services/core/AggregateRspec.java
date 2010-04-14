/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.*;
import java.util.*;
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
public class AggregateRspec implements java.io.Serializable {
    private org.apache.log4j.Logger log;
    private int id;
    private String rspecName;
    private String aggregateName;
    private long startTime;
    private long endTime;
    private List<AggregateResource> resources;
    private String status;

    public AggregateRspec() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
            is.setCharacterStream(new StringReader(rspec.trim()));
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
                       else if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:lifetime")) {
                           parseLifetime(child);
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

    void parseComputeNode(Node compNodeRoot) throws AggregateException {
        //assuming planetlab node for now
        String sliverId = compNodeRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        String address = "";
        NodeList children = compNodeRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("address")) {
                address = child.getTextContent().trim();
                //TODO: add address field in node structure
            } else if (nodeName != null && nodeName.equalsIgnoreCase("computeCapability")) {
                parseComputeCapability(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("networkInterface")) {
                parseNetworkInterface(child);
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
        aggrNode.setReference(aggrNode.getId());
        aggrNode.setRspecId(this.id); //rspec entry has been created in db
        AggregateState.getAggregateNodes().update(aggrNode);
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
        //?aggrNetIf.setReference(aggrNetIf.getId());
        aggrNetIf.setRspecId(this.id);
        resources.add(aggrNetIf);
    }

    void prepareP2PVlans() throws AggregateException {
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getType().equalsIgnoreCase("networkInterface")) {
                AggregateNetworkInterface netIf1 = (AggregateNetworkInterface)resources.get(i);
                for (int j = 0; j < resources.size(); j++) {
                    if (resources.get(j).getType().equalsIgnoreCase("networkInterface")) {
                        AggregateNetworkInterface netIf2 = (AggregateNetworkInterface)resources.get(j);
                        int[] ifIndices = netIf1.pairupInterfaces(netIf2);
                        if (ifIndices[0] != -1 && ifIndices[1] != -1) {
                            netIf1.getPeerInterfaces().remove(ifIndices[0]);
                            netIf2.getPeerInterfaces().remove(ifIndices[1]);
                            String source = AggregateUtils.getUrnField(netIf1.getInterfaceId(), "node");
                            String destination = AggregateUtils.getUrnField(netIf2.getInterfaceId(), "node");
                            String description = rspecName + " p2pvlan-" + source + "-" + destination + "-" + netIf1.getVlanTag();
                            int vtag = Integer.valueOf(netIf1.getVlanTag());
                            //AggregateUtils.convertBandwdithToMbps(String ...);
                            float bandwidth = AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity());
                            AggregateP2PVlan p2pvlan = new AggregateP2PVlan(this.rspecName,
                                    source, destination, vtag, bandwidth, description,
                                    this.startTime, this.endTime);
                            p2pvlan.setSrcInterface(netIf1.getDeviceName());
                            p2pvlan.setDstInterface(netIf2.getDeviceName());
                            p2pvlan.setSrcIpAndMask(netIf1.getIpAddress());
                            p2pvlan.setDstIpAndMask(netIf2.getIpAddress());
                            p2pvlan.setType("p2pVlan");
                            resources.add(p2pvlan);
                        }
                    }
                }
            }
        }
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
}
