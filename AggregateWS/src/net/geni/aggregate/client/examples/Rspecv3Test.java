/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.client.examples;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;


import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;
import java.io.*;
import java.util.*;
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
        String[] rspecXmls = Rspecv3Test.extractStitchingRspec(rspecXml);
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
    }

    private static String[] extractStitchingRspec(String rspecXml) {
        String[] rspecs = new String[2];
        int iStitchOpen1 = rspecXml.indexOf("<stitching");
        int iStitchOpen2 = rspecXml.indexOf(">", iStitchOpen1);
        int iStitchClose1 = rspecXml.indexOf("</stitching");
        int iStitchClose2 = rspecXml.indexOf(">", iStitchClose1);
        if (iStitchOpen1 == -1 || iStitchOpen2 == -1 
            || iStitchClose1 == -1 || iStitchClose2 == -1) {
            return null;
        }
        rspecs[0] = rspecXml.substring(0, iStitchOpen1-1);
        rspecs[0] += rspecXml.substring(iStitchClose2+1);
        rspecs[1] = rspecXml.substring(iStitchOpen2+1, iStitchClose1-1);
        rspecs[1] = rspecs[1].replace("<topology", "<topology xmlns=\"http://geni.net/schema/stitching/topology/geniStitch/20110220/\"");
        return rspecs;
    }
}
