/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.*;
import org.apache.log4j.*;
import edu.isi.east.hpn.rspec.ext.stitch._0_1.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import org.apache.axis2.AxisFault;
import org.w3c.dom.Node;

/**
 *
 * @author xyang
 */
public class AggregateStitchTopologyRunner extends Thread {
    private volatile boolean goRun = true;
    private volatile int runInterval = 60000; //60 secs by default
    private org.apache.log4j.Logger log;
    private String stitchXml = "";
    private StitchContent stitchObj = null;
    
    public AggregateStitchTopologyRunner() {
        super();
        log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    public boolean isGoRun() {
        return goRun;
    }

    public void setGoRun(boolean goRun) {
        this.goRun = goRun;
    }

    public int getRunInterval() {
        return runInterval;
    }

    public void setRunInterval(int runInterval) {
        this.runInterval = runInterval;
    }

    public String getStitchXml() {
        synchronized(this) {
            return stitchXml;
        }
    }

    public void setStitchXml(String stitchXml) {
        synchronized(this) {
            this.stitchXml = stitchXml;
        }
    }

    public StitchContent getStitchObj() {
        synchronized(this) {
            return stitchObj;
        }
    }

    public void setStitchObj(StitchContent stitchObj) {
        synchronized(this) {
            this.stitchObj = stitchObj;
        }
    }

    private void loadStitchTopologyFile() {
        synchronized(this) {
            try {
                int ch;
                FileInputStream in = new FileInputStream(AggregateState.getIdcTopoFile());
                this.stitchXml = "";
                while ((ch = in.read()) != -1) {
                    this.stitchXml += ((char) ch);
                }
                in.close();
            } catch (IOException e) {
                log.warn("loadStitchTopology caught IOException: "+e.getMessage());
                return;
            }
            try {
                this.stitchObj = new JAXBHelper<StitchContent>(StitchContent.class).partialUnmarshal(stitchXml);
                if (this.stitchObj == null || this.stitchObj.getAggregate().size() == 0) {
                    log.warn(String.format("malformed stitch topology file '%s' - make sure there is xmlns property in <stitching>"));
                }
            } catch (Exception e) {
                log.warn("Error in unmarshling GEBI Stitching RSpec extension: " + e.getMessage());
            }
        }
    }
    
    private void calibrateEndPointVlan() {
        /*
        AggregateIDCClient client = AggregateIDCClient.getIDCClient();
        String errMessage = null;
        String domainId = AggregateState.getIdcDomainId();
        try {
            networkTopology = client.retrieveNetworkTopology(domainId);
        } catch (AxisFault e) {
            errMessage = "AxisFault from queryReservation: " + e.getMessage();
        } catch (AAAFaultMessage e) {
            errMessage = "AAAFaultMessage from queryReservation: " + e.getFaultMessage().getMsg();
        } catch (BSSFaultMessage e) {
            errMessage = "BSSFaultMessage from queryReservation: " + e.getFaultMessage().getMsg();
        } catch (java.rmi.RemoteException e) {
            errMessage = "RemoteException returned from queryReservation: " + e.getMessage();
        } catch (Exception e) {
            errMessage = "OSCARSStub threw exception in queryReservation: " + e.getMessage();
        }
        if (errMessage != null) {
            throw new AggregateException(errMessage);
        }
        */
    }
    
    public void run() {
        int minutes = 0;
        long lastModified = 0;
        while (goRun) {            
            if (AggregateState.getIdcTopoFile() != null && !AggregateState.getIdcTopoFile().isEmpty()) {
                // check if AggregateState.getIdcTopoFile() has been updated
                File topoFile = new File(AggregateState.getIdcTopoFile());
                if (topoFile.lastModified() > lastModified) {
                    log.info("stitch topology file updated - reload now");
                    lastModified = topoFile.lastModified();
                    loadStitchTopologyFile();
                }
            }
            //starting calibrate endpoints VLAN range at 5th minute
            if (minutes % 60 == 5) {
                //$$ poll topology from OSCARS, parse, compare and calibrate
            }
            try {
                this.sleep(runInterval);
            } catch (InterruptedException e) {
                if (!goRun) {
                    break;
                }
            }
            minutes++;
        }
    }
    
    public LinkContent getLinkByUrn (String urn) {
        synchronized(this) {
            if (stitchObj == null)
                return null;
            for (AggregateContent aggregate: stitchObj.getAggregate()) {
                for (NodeContent node: aggregate.getNode()) {
                    for (PortContent port: node.getPort()) {
                        for (LinkContent link: port.getLink()) {
                            if (link.getId().equalsIgnoreCase(urn))
                                return link;
                        }
                    }
                }
            }
            return null;
        }
    }
    
    public boolean isValidEndPoint(String urn) {
        if (this.getLinkByUrn(urn) == null) {
            return false;
        }
        return true;
    }

    public boolean isValidBandwidth(String urn, long bw) {
        LinkContent link = this.getLinkByUrn(urn);
        if (link == null) {
            return false;
        }
        synchronized(this) {
            long max = 100000000000L; //100G by default
            if (link.getMaximumReservableCapacity() != null && !link.getMaximumReservableCapacity().isEmpty()) {
                max = Long.valueOf(link.getMaximumReservableCapacity());
            }
            long min = 0;
            if (link.getMinimumReservableCapacity() != null && !link.getMinimumReservableCapacity().isEmpty()) {
                min = Long.valueOf(link.getMinimumReservableCapacity());
            }
            long granularity = 1;
            if (link.getGranularity() != null && !link.getGranularity().isEmpty()) {
                granularity = Long.valueOf(link.getGranularity());
            }
            
            if (bw > max || bw < min || bw % granularity != 0) {
                return false;
            }
            return true;
        }
    }
}
