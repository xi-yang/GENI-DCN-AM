/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.client.examples;

import net.geni.aggregate.client.AggregateGENIStub.*;
import net.geni.aggregate.services.core.RspecHandler_GENIv3;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;
import java.io.*;
import net.geni.www.resources.rspec._3.*;
import net.geni.schema.stitching.topology.genistitch._20110220.*;
import org.apache.xerces.dom.ElementNSImpl;

/**
 *
 * @author xyang
 */
public class Rspecv3Test {
    public static void main(String[] args) {
        String filePath = "/Users/xyang/Work/GENI/rspecv3-and-stitching/max-req-v3.rspec.xml";
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
        GeniStitchTopologyContent stitchTopoObj = null;
        String[] rspecXmls = null;
        try {
            rspecXmls = RspecHandler_GENIv3.extractStitchingRspec(rspecXml);
        } catch (Exception e) {
            System.err.println("Error in extract stitching section from the rspec. " + e.getMessage());
            System.exit(-1);
        }
        if (rspecXmls == null) {
            System.err.println("Missing or malformed <stitching> Rspec section.");
            System.exit(-1);
        }
        try {
            StringReader reader = new StringReader(rspecXmls[0]);
            JAXBContext jc = JAXBContext.newInstance("net.geni.www.resources.rspec._3");
            Unmarshaller unm = jc.createUnmarshaller();
            JAXBElement<RSpecContents> jaxbRspec = (JAXBElement<RSpecContents>) unm.unmarshal(reader);
            rspecV3Obj = jaxbRspec.getValue();
        } catch (Exception e) {
            System.err.println("Error in unmarshling GENI RSpec v3 contents: " + e.getMessage());
            System.exit(-1);
        }
        try {
            StringReader reader = new StringReader(rspecXmls[1]);
            JAXBContext jc = JAXBContext.newInstance("net.geni.schema.stitching.topology.genistitch._20110220");
            Unmarshaller unm = jc.createUnmarshaller();
            JAXBElement<GeniStitchTopologyContent> jaxbRspec = (JAXBElement<GeniStitchTopologyContent>) unm.unmarshal(reader);
            stitchTopoObj = jaxbRspec.getValue();
        } catch (Exception e) {
            System.err.println("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
            System.exit(-1);
        }
        LinkContents lc = (LinkContents)((JAXBElement)rspecV3Obj.getAnyOrNodeOrLink().get(2)).getValue();
        LinkPropertyContents lpc = (LinkPropertyContents)((JAXBElement)lc.getAnyOrPropertyOrLinkType().get(2)).getValue();
        ElementNSImpl anyObj = (ElementNSImpl)lpc.getAny().get(0);
        String vlan = anyObj.getFirstChild().getNodeValue();
    }
}
