/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.client.examples;

import net.geni.aggregate.client.AggregateGENIStub.*;
import net.geni.aggregate.services.core.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBElement;
import java.io.*;
import java.util.*;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import net.geni.www.resources.rspec._3.*;
import edu.isi.east.hpn.rspec.ext.stitch._0_1.*;
import org.apache.xerces.dom.ElementNSImpl;

import javax.imageio.metadata.IIOMetadataNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import java.io.StringWriter;
import java.io.StringReader;
import net.geni.www.resources.rspec.ext.sdx._1.SDXContent;

/**
 *
 * @author xyang
 */
public class Rspecv3Test {
    public static void main(String[] args) {
        String filePath = "/Users/xyang/Work/GENI/gcf-1.6.1/geni-stitching.rspec";
        if (args.length > 0)
            filePath = args[0];
        byte[] buffer = new byte[(int)new File(filePath).length()];
        try {
            FileInputStream f = new FileInputStream(filePath);
            f.read(buffer);
        } catch (Exception e) {
            System.err.println("Error in reading XML file: "+filePath);
            System.exit(-1);
        }
        String rspecXml = new String(buffer);
        RSpecContents rspecV3Obj = null;
        JAXBElement<RSpecContents> jaxbRspec = null;
        StitchContent stitchObj = null;
        JAXBElement<StitchContent> jaxbRspec2 = null;
        SDXContent sdxObj = null;
        JAXBElement<SDXContent> jaxbRspec3 = null;
        try {
            StringReader reader = new StringReader(rspecXml);
            JAXBContext jc = JAXBContext.newInstance("net.geni.www.resources.rspec._3");
            Unmarshaller unm = jc.createUnmarshaller();
            jaxbRspec = (JAXBElement<RSpecContents>) unm.unmarshal(reader);
            rspecV3Obj = jaxbRspec.getValue();
            
            Iterator itr = rspecV3Obj.getAnyOrNodeOrLink().iterator();
            while (itr.hasNext()) {
                Object obj = itr.next();
                if (obj.getClass().getName().equalsIgnoreCase("javax.xml.bind.JAXBElement")) {
                    String elemName = ((JAXBElement)obj).getName().getLocalPart();
                    if (elemName.equalsIgnoreCase("node")) {
                        NodeContents node = (NodeContents)((JAXBElement)obj).getValue();
                    } else if (elemName.equalsIgnoreCase("link")) {
                        LinkContents link = (LinkContents)((JAXBElement)obj).getValue();
                        String vlantag = AggregateUtils.getAnyAttrString(link.getOtherAttributes(),"http://hpn.east.isi.edu/rspec/ext/stitch/0.1/", "vlantag");                        
                    }
                } else if (obj.getClass().getName().contains("ElementNSImpl")) {
                    String elemName = AggregateUtils.getAnyName(obj);
                    if (elemName.equalsIgnoreCase("stitching")) {
                        try {
                            JAXBContext payloadContext = JAXBContext.newInstance("edu.isi.east.hpn.rspec.ext.stitch._0_1");
                            jaxbRspec2 = (JAXBElement<StitchContent>)payloadContext.createUnmarshaller().unmarshal((org.w3c.dom.Node)obj);
                            stitchObj = jaxbRspec2.getValue();
                        } catch (Exception e) {
                            throw new AggregateException("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
                        }
                    } else if (elemName.equalsIgnoreCase("sdx")) {
                        try {
                            JAXBContext payloadContext = JAXBContext.newInstance("net.geni.www.resources.rspec.ext.sdx._1");
                            jaxbRspec3 = (JAXBElement<SDXContent>)payloadContext.createUnmarshaller().unmarshal((org.w3c.dom.Node)obj);
                            sdxObj = jaxbRspec3.getValue();
                        } catch (Exception e) {
                            throw new AggregateException("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in unmarshling GENI RSpec v3 contents: " + e.getMessage());
            System.exit(-1);
        }
        Date dateExpires = new Date();
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(dateExpires);
        XMLGregorianCalendar xgcExpires = null;
        try {
            xgcExpires = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
            rspecV3Obj.setExpires(xgcExpires);
        } catch (Exception e) {
            System.err.println("RspecHandler_GENIv3.getRspecManifest error: " + e.getMessage());
        }
        try {
            Document infoDoc = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            infoDoc = db.newDocument();
            JAXBContext jc = JAXBContext.newInstance("net.geni.www.resources.rspec._3");
            Marshaller m = jc.createMarshaller();
            m.marshal(jaxbRspec, infoDoc);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            Source source = new DOMSource(infoDoc);
            transformer.transform(source, result);
            rspecXml = writer.toString();
            writer.close();
        } catch (Exception e) {
            String msg = e.getMessage();
            System.out.println("Error marshaling rspec: " + msg);
        }

    }
}
