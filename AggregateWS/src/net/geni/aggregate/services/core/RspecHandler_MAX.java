/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author xyang
 */
public class RspecHandler_MAX implements AggregateRspecHandler {

    public AggregateRspec parseRspecXml(String rspecXml) throws AggregateException {
        AggregateRspec rspec = new AggregateRspec();
        rspecXml = rspecXml.trim().replaceAll("\n\\s*","");
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
            is.setCharacterStream(new StringReader(rspecXml.trim()));
            //TODO: docBuilder.setErrorHandler ... 
            Document rspecXMLDoc = docBuilder.parse(is);

            NodeList children = rspecXMLDoc.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
               Node child = children.item(i);
               String nodeName = child.getNodeName();
               if (nodeName != null && nodeName.equalsIgnoreCase("rspec")) {
                   rspec.setRspecName(child.getAttributes().getNamedItem("id").getTextContent().trim());
                   children = child.getChildNodes();
                   for (i = 0; i < children.getLength(); i++) {
                       child = children.item(i);
                       nodeName = child.getNodeName();
                       if (nodeName != null && nodeName.equalsIgnoreCase("aggregate")) {
                           rspec.setAggregateName(child.getTextContent().trim());
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("user")) {
                           if (rspec.getUsers() == null)
                               rspec.setUsers(new ArrayList<String>());
                           if (child.getAttributes().getNamedItem("id") != null)
                               rspec.getUsers().add(child.getAttributes().getNamedItem("id").getTextContent().trim());
                           else if (child.getAttributes().getNamedItem("name") != null)
                               rspec.getUsers().add(child.getAttributes().getNamedItem("name").getTextContent().trim());
                           else if (child.getAttributes().getNamedItem("email") != null)
                               rspec.getUsers().add(child.getAttributes().getNamedItem("email").getTextContent().trim());
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("description")) {
                           rspec.setDescription(child.getTextContent().trim());
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("lifetime")) {
                           parseLifetime(rspec, child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("computeResource")) {
                           parseComputeResources(rspec, child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("topology")) {
                           parseNetworkTopology(rspec, child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("externalResource")) {
                           parseExternalResources(rspec, child);
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("stitchingResource")) {
                           parseStitchingResources(rspec, child);
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
        
        return rspec;
    }
    

    void parseLifetime(AggregateRspec rspec, Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:start")) {
                rspec.setStartTime(Integer.valueOf(child.getTextContent().trim()));
            } else if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:end")) {
                rspec.setEndTime(Integer.valueOf(child.getTextContent().trim()));
            } else if (nodeName != null && nodeName.equalsIgnoreCase("CtrlPlane:duration")) {
                rspec.setEndTime(rspec.getStartTime() + Integer.valueOf(child.getTextContent().trim()));
            }
        }
    }

    void parseComputeResources(AggregateRspec rspec, Node compRoot) throws AggregateException {
        NodeList children = compRoot.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();
            if (nodeName != null && nodeName.equalsIgnoreCase("computeNode")) {
                parseAddComputeNode(rspec, child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("planetlabNodeSliver")) {
                parsePlanetlabNodeSliver(rspec, child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("eucalyptusNodeSliver")) {
                parseEucalyptusNodeSliver(child);
            } else if (nodeName != null && nodeName.equalsIgnoreCase("netfpgaNode")) {
                parseNetFPGANode(child);
            }
        }
    }

    void parseNetworkTopology(AggregateRspec rspec, Node topoRoot) throws AggregateException {
        //TODO ?
    }

    AggregateNode parseAddComputeNode(AggregateRspec rspec, Node compNodeRoot) throws AggregateException {
        int nodeId = 0;
        String urn = compNodeRoot.getAttributes().getNamedItem("id").getTextContent().trim();
        String address = "";
        String caps = "";
        String descr = "";
        List<AggregateNetworkInterface> myNetIfs = new ArrayList<AggregateNetworkInterface>();
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
                AggregateNetworkInterface netIf = parseNetworkInterface(rspec, child);
                myNetIfs.add(netIf);
            }
        }

        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(urn);
        if (aggrNode == null) {
            aggrNode = new AggregateNode(urn, nodeId, descr, caps);
            aggrNode.setAddress(address);
            aggrNode.setType(compNodeRoot.getNodeName());
            aggrNode.setRspecId(rspec.getId()); 
            if (AggregateState.getAggregateNodes().add(aggrNode) == false) {
                throw new AggregateException("Cannot add Node:" + aggrNode.getUrn() + "(id:" + Integer.toString(aggrNode.getNodeId()) + ") to DB");
            }
            rspec.getResources().add(aggrNode);
            for (AggregateNetworkInterface netIf: myNetIfs) {
                netIf.setParentNode(aggrNode);
                if (AggregateState.getAggregateInterfaces().add(netIf) == false)
                    throw new AggregateException("Cannot add Interface:" + netIf.getUrn()
                            +"(id:"+Integer.toString(netIf.getId())+") to DB");
            }
        }
        return aggrNode;
    }

    void parsePlanetlabNodeSliver(AggregateRspec rspec, Node plNodeRoot) throws AggregateException {
        String nodeUrn = plNodeRoot.getAttributes().getNamedItem("id").getTextContent().trim();
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
                AggregateNetworkInterface netIf = parseNetworkInterface(rspec, child);
                myNetIfs.add(netIf);
            }
        }
        //get node URN from sliverId
        String[] fields = {"domain","node"};
        AggregateNode aggrNode= AggregateState.getAggregateNodes().getByUrn(nodeUrn);
        if (aggrNode == null) {
            throw new AggregateException("unknown AggregateNode urn="+nodeUrn);
        }
        AggregateNode newNode = aggrNode.duplicate();
        newNode.setType(plNodeRoot.getNodeName());
        newNode.setRspecId(rspec.getId()); //rspec entry has been created in db
        rspec.getResources().add(newNode);
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

    AggregateNetworkInterface parseNetworkInterface(AggregateRspec rspec, Node netIfRoot) throws AggregateException {
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
        aggrNetIf.setRspecId(rspec.getId());
        rspec.getResources().add(aggrNetIf);
        return aggrNetIf;
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


    void parseExternalResources(AggregateRspec rspec, Node extRoot) throws AggregateException {
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
            aggrER.setExpireTime(rspec.getEndTime());
            rspec.getResources().add((AggregateResource)aggrER);
        }
    }

    void parseStitchingResources(AggregateRspec rspec, Node srRoot) throws AggregateException {
        String srId = "";
        String erId = "";
        String srType = "";
        List<String> netIfUrns = new ArrayList<String>();
        List<AggregateNetworkInterface> myNetIfs = new ArrayList<AggregateNetworkInterface>();

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
                AggregateNetworkInterface netIf = parseNetworkInterface(rspec, child);
                netIf.setParentNode(null);
                myNetIfs.add(netIf);
            }
        }
        if (netIfUrns.size() ==2 && srType.equalsIgnoreCase("p2pvlan")) {
            AggregateNetworkInterface netIf1 = null;
            AggregateNetworkInterface netIf2 = null;
            for (AggregateResource rc: rspec.getResources()) {
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
            stitchingP2PVlan.setBandwidth(50);//50M by default
            stitchingP2PVlan.setVtag("");
            if (netIf1 != null) {
                String source = netIf1.getLinks().isEmpty() ? AggregateUtils.getIDCQualifiedUrn(netIf1.getUrn())
                        : AggregateUtils.getIDCQualifiedUrn(netIf1.getLinks().get(0));
                stitchingP2PVlan.setSource(source);
                stitchingP2PVlan.setSrcInterface(netIf1.getDeviceName());
                stitchingP2PVlan.setSrcIpAndMask(netIf1.getIpAddress());
                stitchingP2PVlan.setBandwidth(AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity()));
                if (!netIf1.getVlanTag().isEmpty())
                    stitchingP2PVlan.setVtag(netIf1.getVlanTag());
            }
            if (netIf2 != null) {
                String destination = netIf2.getLinks().isEmpty() ? AggregateUtils.getIDCQualifiedUrn(netIf2.getUrn())
                        : AggregateUtils.getIDCQualifiedUrn(netIf2.getLinks().get(0));
                stitchingP2PVlan.setDestination(destination);
                stitchingP2PVlan.setDstInterface(netIf2.getDeviceName());
                stitchingP2PVlan.setDstIpAndMask(netIf2.getIpAddress());
                if (netIf1 == null) {
                    stitchingP2PVlan.setBandwidth(AggregateUtils.convertBandwdithToMbps(netIf1.getCapacity()));
                    if (!netIf2.getVlanTag().isEmpty()) {
                        if (stitchingP2PVlan.getVtag().isEmpty())
                            stitchingP2PVlan.setVtag(netIf2.getVlanTag());
                        else 
                            stitchingP2PVlan.setVtag(netIf1.getVlanTag() + "-" + netIf2.getVlanTag());
                    }
                }
            }
            if (stitchingP2PVlan.getVtag().isEmpty()) {
                stitchingP2PVlan.setVtag("any");
            }
            stitchingP2PVlan.setStitchingResourceId(srId);
            stitchingP2PVlan.setExternalResourceId(erId);
            rspec.getResources().add((AggregateResource)stitchingP2PVlan);
        }
        else if (netIfUrns.size() == 1 && srType.equalsIgnoreCase("stub")) {
            boolean noParentNode = true;
            for (AggregateResource rc: rspec.getResources()) {
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


    public AggregateRspec configRspecFromFile(String filePath) throws AggregateException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new AggregateException("Cannot open resource file:" + filePath);
        }
        AggregateRspec rspec = new AggregateRspec();
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
                   rspec.setId(0);
                   rspec.setRspecName(child.getAttributes().getNamedItem("id").getTextContent().trim());
                   children = child.getChildNodes();
                   for (i = 0; i < children.getLength(); i++) {
                       child = children.item(i);
                       nodeName = child.getNodeName();
                       if (nodeName != null && nodeName.equalsIgnoreCase("aggregate")) {
                           rspec.setAggregateName(child.getTextContent().trim());
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("user")) {
                           try {
                               //parse and add users to DB
                               parseAddUser(child);
                           } catch (AggregateException e) {
                               throw new AggregateException(e.getMessage()+" from resource file:" + filePath);
                           }
                       }
                       else if (nodeName != null && nodeName.equalsIgnoreCase("computeResource")) {
                           try {
                               //parse and add nodes and interfaces to DB inside
                               parseComputeResources(rspec, child);
                               computeResourceNode = child;
                           } catch (AggregateException e) {
                               throw new AggregateException(e.getMessage()+" from resource file:" + filePath);
                           }
                       }
                   }
                   break;
               }
            }

            //reload resources
            rspec.getResources().clear();
            List<AggregateNode> nodeList = AggregateState.getAggregateNodes().getAll();
            for (AggregateNode an: nodeList)
                rspec.getResources().add((AggregateResource)an);
            List<AggregateNetworkInterface> interfaceList = AggregateState.getAggregateInterfaces().getAll();
            for (AggregateNetworkInterface ai: interfaceList) {
                for (AggregateNode an: nodeList) 
                    if (an.getId() == ai.getPnid()) {
                        ai.setParentNode(an);
                        break;
                    }
                rspec.getResources().add((AggregateResource)ai);
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new AggregateException("ParserConfigurationException when parsing resource file:" + filePath);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new AggregateException("SAXException when parsing resource file:" + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AggregateException("IOException when parsing resource file:" + filePath);
        }        
        return rspec;
    }


    public String getRspecManifest(AggregateRspec rspec) throws AggregateException {
        String rspecMan = "<computeResource id=\"urn:aggregate="+rspec.getAggregateName()+":rspec="+rspec.getRspecName()+"\">";
        for (int n = 0; n < rspec.getResources().size(); n++) {
            AggregateResource rc = rspec.getResources().get(n);
            if (rc.getType().equalsIgnoreCase("computeNode") || rc.getType().equalsIgnoreCase("planetlabNodeSliver")) {
                AggregateNode an = (AggregateNode)rc;
                rspecMan = rspecMan + "<computeNode id=\""+an.getUrn()+"\">";
                for (int i = 0; i < AggregateState.getAggregateInterfaces().getAll().size(); i++) {
                    AggregateNetworkInterface ai = AggregateState.getAggregateInterfaces().getAll().get(i);
                    if (AggregateUtils.getUrnField(ai.getUrn(), "node").equalsIgnoreCase(AggregateUtils.getUrnField(an.getUrn(), "node"))) {
                        if (rspec.getId() == 0) { // globalAggregate rspec
                            rspecMan = rspecMan + "<networkInterface id=\"" + ai.getUrn() + "\">";
                            rspecMan = rspecMan + "<deviceType>ethernet</deviceType>";
                            rspecMan = rspecMan + "<deviceName>" + ai.getDeviceName() + "</deviceName>";
                            rspecMan = rspecMan + "<capacity>" + ai.getCapacity() + "Mbps</capacity>";
                            rspecMan = rspecMan + "<vlanRange>" + ai.getVlanTag() + "</vlanRange>";
                            rspecMan = rspecMan + "<attachedLinkUrn>" + ai.getAttachedLinkUrns() + "</attachedLinkUrn>";
                            rspecMan += "</networkInterface>";
                        } else { // slice/sliver rspecs
                            for (int j = 0; j < rspec.getResources().size(); j++) {
                                if (rspec.getResources().get(j).getType().equalsIgnoreCase("p2pvlan")) {
                                    AggregateP2PVlan ppv = (AggregateP2PVlan) rspec.getResources().get(j);
                                    if (ai.getAttachedLinkUrns().contains(ppv.getSource())) {
                                        rspecMan = rspecMan + "<networkInterface id=\"" + ai.getUrn() + "\">";
                                        rspecMan = rspecMan + "<deviceType>ethernet</deviceType>";
                                        rspecMan = rspecMan + "<deviceName>" + ppv.getSrcInterface() + "</deviceName>";
                                        rspecMan = rspecMan + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                                        rspecMan = rspecMan + "<ipAddress>" + ppv.getSrcIpAndMask() + "</ipAddress>";
                                        rspecMan = rspecMan + "<vlanRange>" + AggregateUtils.parseVlanTag(ppv.getVtag(), true) + "</vlanRange>";
                                        rspecMan = rspecMan + "<attachedLinkUrn>" + ai.getAttachedLinkUrns() + "</attachedLinkUrn>";
                                        if (ai.getPeers().isEmpty() || ai.getPeers().get(0).isEmpty()) {
                                            rspecMan = rspecMan + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=dst" + "</peerNetworkInterface>";
                                        } else {
                                            rspecMan = rspecMan + "<peerNetworkInterface>" + ai.getPeers().get(0) + "</peerNetworkInterface>";
                                        }
                                        rspecMan += "</networkInterface>";
                                    } else if (ai.getAttachedLinkUrns().contains(ppv.getDestination())) {
                                        rspecMan = rspecMan + "<networkInterface id=\"" + ai.getUrn() + "\">";
                                        rspecMan = rspecMan + "<deviceType>ethernet</deviceType>";
                                        rspecMan = rspecMan + "<deviceName>" + ppv.getDstInterface() + "</deviceName>";
                                        rspecMan = rspecMan + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                                        rspecMan = rspecMan + "<ipAddress>" + ppv.getDstIpAndMask() + "</ipAddress>";
                                        rspecMan = rspecMan + "<vlanRange>" + AggregateUtils.parseVlanTag(ppv.getVtag(), false) + "</vlanRange>";
                                        rspecMan = rspecMan + "<attachedLinkUrn>" + ai.getAttachedLinkUrns() + "</attachedLinkUrn>";
                                        if (ai.getPeers().isEmpty() || ai.getPeers().get(0).isEmpty()) {
                                            rspecMan = rspecMan + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=src" + "</peerNetworkInterface>";
                                        } else {
                                            rspecMan = rspecMan + "<peerNetworkInterface>" + ai.getPeers().get(0) + "</peerNetworkInterface>";
                                        }
                                        rspecMan += "</networkInterface>";
                                    }
                                }
                            }
                        }
                    }
                }
                rspecMan +=  "</computeNode>";
            } else if (rc.getType().equalsIgnoreCase("computeSlice")) {
                AggregateSlice as = (AggregateSlice)rc;
                rspecMan = rspecMan + "<computeSlice id=\""+as.getSliceName()+"\">";
                rspecMan = rspecMan + "<node_ids>"+as.getNodes()+"</node_ids>";
                rspecMan = rspecMan + "<user_ids>"+as.getUsers()+"</user_ids>";
                rspecMan = rspecMan + "<expires>"+Long.toString(as.getExpiredTime())+"</expires>";
                rspecMan +=  "</computeSlice>";
                String[] nodes = as.getNodes().split("[,\\s]");
                for (String nodeId: nodes) {
                    if (nodeId.isEmpty())
                        continue;
                    AggregateNode an = AggregateState.getAggregateNodes().getByNodeId(Integer.valueOf(nodeId));
                    if (an != null) {
                        boolean found = false;
                        for (AggregateResource rc1: rspec.getResources()) {
                            if ((rc1.getType().equalsIgnoreCase("computeNode") || rc1.getType().equalsIgnoreCase("planetlabNodeSliver"))
                                    && ((AggregateNode)rc1).getNodeId() == Integer.valueOf(nodeId))
                                found = true;
                        }
                        if (!found)
                            rspec.getResources().add(an);
                    }
                }
            } else if (rc.getType().equalsIgnoreCase("externalResource")) {
                AggregateExternalResource er = (AggregateExternalResource)rc;
                rspecMan = rspecMan + "<externalResource id=\""+er.getUrn()+"\" type=\""+er.getSubType()+"\">";
                if (!er.getSmUri().isEmpty())
                    rspecMan = rspecMan + "<sliceManager>"+er.getSmUri()+"</sliceManager>";
                if (!er.getAmUri().isEmpty())
                    rspecMan = rspecMan + "<aggregateManager>"+er.getAmUri()+"</aggregateManager>";
                rspecMan = rspecMan + "<rspecData>"+er.getRspecData()+"</rspecData>";
                rspecMan +=  "</externalResource>";
            }
        }
        for (int n = 0; n < rspec.getResources().size(); n++) {
            if (rspec.getResources().get(n).getType().equalsIgnoreCase("p2pvlan")) {
                AggregateP2PVlan ppv = (AggregateP2PVlan)rspec.getResources().get(n);
                if (ppv.getSrcInterface().isEmpty() || ppv.getDstInterface().isEmpty())
                    rspecMan = rspecMan + "<stitchingResource id=\"p2pvlan-"+ppv.getId()+"\" type=\"p2pvlan\">";
                if (ppv.getSrcInterface().isEmpty()) {
                    rspecMan = rspecMan + "<networkInterface id=\"p2pvlan-" + ppv.getId() + ":interface=src" + "\">";
                    rspecMan = rspecMan + "<deviceType>ethernet</deviceType>";
                    rspecMan = rspecMan + "<deviceName>" + ppv.getSrcInterface() + "</deviceName>";
                    rspecMan = rspecMan + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                    rspecMan = rspecMan + "<ipAddress>" + ppv.getSrcIpAndMask() + "</ipAddress>";
                    rspecMan = rspecMan + "<vlanRange>" + AggregateUtils.parseVlanTag(ppv.getVtag(), true) + "</vlanRange>";
                    rspecMan = rspecMan + "<attachedLinkUrn>" + ppv.getSource() +"</attachedLinkUrn>";
                    AggregateNetworkInterface netIf = AggregateState.getAggregateInterfaces().getByAttachedLink(ppv.getDestination());
                    if (ppv.getDstInterface().isEmpty() || netIf == null)
                        rspecMan = rspecMan + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=dst" + "</peerNetworkInterface>";
                    else
                        rspecMan = rspecMan + "<peerNetworkInterface>" + netIf.getUrn() + "</peerNetworkInterface>";
                    rspecMan += "</networkInterface>";
                }
                if (ppv.getDstInterface().isEmpty()) {
                    rspecMan = rspecMan + "<networkInterface id=\"p2pvlan-" + ppv.getId() + ":interface=dst" + "\">";
                    rspecMan = rspecMan + "<deviceType>ethernet</deviceType>";
                    rspecMan = rspecMan + "<deviceName>" + ppv.getDstInterface() + "</deviceName>";
                    rspecMan = rspecMan + "<capacity>" + Float.toString(ppv.getBandwidth()) + "Mbps</capacity>";
                    rspecMan = rspecMan + "<ipAddress>" + ppv.getDstIpAndMask() + "</ipAddress>";
                    rspecMan = rspecMan + "<vlanRange>" + AggregateUtils.parseVlanTag(ppv.getVtag(), false) + "</vlanRange>";
                    rspecMan = rspecMan + "<attachedLinkUrn>" + ppv.getDestination() +"</attachedLinkUrn>";
                    AggregateNetworkInterface netIf = AggregateState.getAggregateInterfaces().getByAttachedLink(ppv.getSource());
                    if (ppv.getSrcInterface().isEmpty() || netIf == null)
                        rspecMan = rspecMan + "<peerNetworkInterface>p2pvlan-" + ppv.getId() + ":interface=src" + "</peerNetworkInterface>";
                    else 
                        rspecMan = rspecMan + "<peerNetworkInterface>" + netIf.getUrn() + "</peerNetworkInterface>";
                    rspecMan += "</networkInterface>";
                }
                if (ppv.getSrcInterface().isEmpty() || ppv.getDstInterface().isEmpty())
                    rspecMan +=  "</stitchingResource>";
            }
        }
        rspecMan +=  "</computeResource>";
        return rspecMan;
    }

}