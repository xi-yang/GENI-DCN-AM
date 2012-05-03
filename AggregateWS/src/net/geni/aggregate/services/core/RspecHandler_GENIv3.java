/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBElement;
import java.io.*;
import java.util.*;
import net.geni.www.resources.rspec._3.*;
import net.geni.schema.stitching.topology.genistitch._20110220.*;
        
/**
 *
 * @author xyang
 */
public class RspecHandler_GENIv3 implements AggregateRspecHandler {
    public AggregateRspec parseRspecXml(String rspecXml) throws AggregateException {
        AggregateRspec aggrRspec = new AggregateRspec();
        RSpecContents rspecV3Obj = null;
        GeniStitchTopologyContent stitchTopoObj = null;
        String[] rspecXmls = this.extractStitchingRspec(rspecXml);
        try {
            StringReader reader = new StringReader(rspecXmls[0]);
            JAXBContext jc = JAXBContext.newInstance("net.geni.www.resources.rspec._3");
            Unmarshaller unm = jc.createUnmarshaller();
            JAXBElement<RSpecContents> jaxbRspec = (JAXBElement<RSpecContents>) unm.unmarshal(reader);
            rspecV3Obj = jaxbRspec.getValue();
        } catch (Exception e) {
            throw new AggregateException("Error in unmarshling GENI RSpec v3 contents: " + e.getMessage());
        }
        try {
            StringReader reader = new StringReader(rspecXmls[1]);
            JAXBContext jc = JAXBContext.newInstance("net.geni.schema.stitching.topology.genistitch._20110220");
            Unmarshaller unm = jc.createUnmarshaller();
            JAXBElement<GeniStitchTopologyContent> jaxbRspec = (JAXBElement<GeniStitchTopologyContent>) unm.unmarshal(reader);
            stitchTopoObj = jaxbRspec.getValue();
        } catch (Exception e) {
            throw new AggregateException("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
        }
        // TODO: parse rspecV3Obj and stitchTopoObj to fill up aggrRspec
            // filter nodes and links only with component_manager_id pointing to current AM
            // add elements to node/interface and link/property
            // parse nodes to addComputeNodes and addNetworkInterfaces
            // parse links to addP2PVlans
        
            // set default start and end time
        return aggrRspec;
    }

    public AggregateRspec configRspecFromFile(String filePath) throws AggregateException {
        throw new AggregateException("RspecHandler_GENIv3::configRspecFromFile not implemented");
        // loadCRDB only use this method from MAX rspecHandler instance? 
    }

    public String getRspecManifest(AggregateRspec rspec) throws AggregateException {
        String rspecMan = "";

        return rspecMan;
    }
    
    private String[] extractStitchingRspec(String rspecXml) throws AggregateException {
        String[] rspecs = new String[2];
        int iStitchOpen1 = rspecXml.indexOf("<stitching");
        int iStitchOpen2 = rspecXml.indexOf(">", iStitchOpen1);
        int iStitchClose1 = rspecXml.indexOf("</stitching");
        int iStitchClose2 = rspecXml.indexOf(">", iStitchClose1);
        if (iStitchOpen1 == -1 || iStitchOpen2 == -1 
            || iStitchClose1 == -1 || iStitchClose2 == -1) {
            throw new AggregateException("Missing or malformed <stitching> Rspec section.");
        }
        rspecs[0] = rspecXml.substring(0, iStitchOpen1-1);
        rspecs[0] += rspecXml.substring(iStitchClose2+1);
        rspecs[1] = rspecXml.substring(iStitchOpen2+1, iStitchClose1-1);
        rspecs[1] = rspecs[1].replace("<topology", "<topology xmlns=\"http://geni.net/schema/stitching/topology/geniStitch/20110220/\"");
        return rspecs;
    }
}
