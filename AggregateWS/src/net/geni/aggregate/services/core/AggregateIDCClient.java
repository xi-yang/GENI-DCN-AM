/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.import org.apache.axis2.AxisFault;
 */

package net.geni.aggregate.services.core;

import org.apache.log4j.*;
import java.util.Vector;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import org.apache.axis2.AxisFault;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.Client;
import org.ogf.schema.network.topology.ctrlplane.*;
/**
 *
 * @author xyang
 *
 * This code will be revised to use separate thread with status polling/notifications
 */

public class AggregateIDCClient {

    private String idcURL = "";
    private String idcRepo = "";
    private AggregateIDCv6Client v6Client= null;
    private GlobalReservationId gri = null;
    private Logger log;

    AggregateIDCClient(String url, String repo) {
        idcURL = url;
        idcRepo = repo;
        gri = new GlobalReservationId();
        if (AggregateState.getIdcVersion().equals("0.6")) 
            v6Client = new AggregateIDCv6Client();
        log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");
    }
    /**
     * get an IDCCLient instance
     */
    public static AggregateIDCClient getIDCClient(String url, String repo) {
        return (new AggregateIDCClient(url, repo));
    }

    public static AggregateIDCClient getIDCClient() {
        return getIDCClient(AggregateState.getIdcURL(), AggregateState.getIdcRepo());
    }

    /**
     * createReservation
     */
    public String createReservation(String src, String dst, String vtag, float bw, String descr, long startTime, long endTime)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {

        if (v6Client != null) {
            String resCreateYaml = v6Client.generateCreateResevationContent(src, dst, vtag, bw, descr, startTime, endTime);
            String status = v6Client.requestCreateReservation(resCreateYaml);
            this.gri.setGri(v6Client.getGlobalReservationId());
            return status;
        }
        
        Client client = new Client();
        /* Initialize client instance */
        client.setUp(true, idcURL, idcRepo);
        /* Prepare request content */
        ResCreateContent content = new ResCreateContent();

        PathInfo pathInfo = new PathInfo();
        Layer2Info layer2Info = new Layer2Info();
        // for layer 2, this will only be used if the router configured is
        // a Juniper
        /*
        boolean useMpls = false;
        MplsInfo mplsInfo = null;
        if (useMpls) {
            mplsInfo.setBurstLimit(
                    Integer.parseInt(props.getProperty("burstLimit", "10000000")));
            pathInfo.setMplsInfo(mplsInfo);
        }
        */

        layer2Info.setSrcEndpoint(src);
        layer2Info.setDestEndpoint(dst);
        if (vtag.equalsIgnoreCase("untagged"))
            vtag = "0";
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(AggregateUtils.parseVlanTag(vtag, true));
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        // same as srcVtag for now
        destVtag.setString(AggregateUtils.parseVlanTag(vtag, false));
        destVtag.setTagged(true);
        layer2Info.setDestVtag(destVtag);

        content.setDescription(descr);
        content.setStartTime(startTime);
        content.setEndTime(endTime);
        content.setBandwidth((int)bw);

        pathInfo.setPathSetupMode("timer-automatic");
        pathInfo.setLayer2Info(layer2Info);
        content.setPathInfo(pathInfo);

        /* Send Request */
        CreateReply response = client.createReservation(content);
        client.cleanUp();

        /* Extract repsponse information */
        this.gri.setGri(response.getGlobalReservationId());
        return response.getStatus();
    }

    // 
    public String getGlobalReservationId() {
        return gri.getGri();
    }

    /**
     *  modifyReservation
     *
     */
    public String modifyReservation(String aGri, String src, String dst, String vtag, float bw, String descr, long startTime, long endTime)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {
        if (gri == null)
            return "FAILED";

        if (!aGri.equals(""))
            this.gri.setGri(aGri);
        
        if (v6Client != null) {
            return v6Client.requestModifyReservation(aGri, src, dst, vtag, bw, descr, startTime, endTime);
        }

        Client client = new Client();

        /* Initialize client instance */
        client.setUp(true, idcURL, idcRepo);
        /* Send Request */
        ModifyResContent content = new ModifyResContent();
        content.setGlobalReservationId(aGri);
        PathInfo pathInfo = new PathInfo();
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(src);
        layer2Info.setDestEndpoint(dst);
        if (vtag.equalsIgnoreCase("untagged"))
            vtag = "0";
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(AggregateUtils.parseVlanTag(vtag, true));
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        // same as srcVtag for now
        destVtag.setString(AggregateUtils.parseVlanTag(vtag, false));
        destVtag.setTagged(true);
        layer2Info.setDestVtag(destVtag);

        content.setDescription(descr);
        content.setStartTime(startTime);
        content.setEndTime(endTime);
        content.setBandwidth((int)bw);

        pathInfo.setPathSetupMode("timer-automatic");
        pathInfo.setLayer2Info(layer2Info);
        content.setPathInfo(pathInfo);
        ModifyResReply response = client.modifyReservation(content);
        client.cleanUp();
        /* Extract repsponse information */
        return response.getReservation().getStatus();
    }


    /**
     *  cancelReservation
     *
     */
    public String cancelReservation(String aGri)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {
        if (gri == null)
            return "FAILED";

        if (!aGri.equals(""))
            this.gri.setGri(aGri);
        
        if (v6Client != null) {
            return v6Client.requestCancelReservation(aGri);
        }

        Client client = new Client();

        /* Initialize client instance */
        client.setUp(true, idcURL, idcRepo);
        /* Send Request */
        String status = client.cancelReservation(this.gri);
        client.cleanUp();
        /* Extract repsponse information */
        return status;
    }

    /**
     * queryReservation
     * 
     */
    public HashMap queryReservation(String aGri)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {
        if (aGri != null && !aGri.equals("")) {
            if (gri == null) {
                gri = new GlobalReservationId();
            }
            gri.setGri(aGri);
        } else {
            throw new Exception("queryReservation on null or empty GRI");
        }
	log.debug("### queryReservation GRI= "+ gri.getGri());

        if (v6Client != null) {
            return v6Client.requestQueryReservation(aGri);
        }

        HashMap hmRet = new HashMap();
        Client client = new Client();
        /* Initialize client instance */
        client.setUp(true, idcURL, idcRepo);
        /* Send Request */
        ResDetails response = client.queryReservation(this.gri);
        client.cleanUp();
        PathInfo pathInfo = response.getPathInfo();
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        /* Extract repsponse information */
        hmRet.put("GRI", response.getGlobalReservationId());
        hmRet.put("login", response.getLogin());
        hmRet.put("status", response.getStatus());
        hmRet.put("startTime", Long.toString(response.getStartTime()));
        hmRet.put("endTime", Long.toString(response.getEndTime()));
        hmRet.put("cratedTime", Long.toString(response.getCreateTime()));
        hmRet.put("bandwidth", Integer.toString(response.getBandwidth()));
        hmRet.put("description", response.getDescription());
        hmRet.put("source", layer2Info.getSrcEndpoint());
        hmRet.put("destination", layer2Info.getDestEndpoint());
        hmRet.put("vlanTag", layer2Info.getSrcVtag().toString()+":"+layer2Info.getDestVtag().toString());
        /* Get path ERO */
        String ero = " ";
        CtrlPlanePathContent path = pathInfo.getPath();
        for (CtrlPlaneHopContent hop : path.getHop()) {
            CtrlPlaneLinkContent link = hop.getLink();
            if (link == null) {
                //should not happen
                ero += "no link";
                continue;
            }
            ero += " " + link.getId();
            CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
            ero += ", " + swcap.getEncodingType();
            if ("ethernet".equals(swcap.getEncodingType())) {
                ero += ", " + swcapInfo.getVlanRangeAvailability();
            }
        }
        hmRet.put("ERO", ero);
        return hmRet;
    }

    /**
     * retrieveNetworkTopology
     *
     */
    public String retrieveNetworkTopology(String domain)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {
        Client client = new Client();

        /* Initialize client instance */
        client.setUp(true, idcURL, idcRepo);
        GetTopologyContent request = new GetTopologyContent();
        request.setTopologyType(domain);
        GetTopologyResponseContent response = client.getNetworkTopology(request);
        client.cleanUp();

        CtrlPlaneTopologyContent topology = response.getTopology();
        CtrlPlaneDomainContent[] domains = topology.getDomain();

        /* extract topology from response */
        String ret = "<topology id=\"" +topology.getId()+"\">";
        for (CtrlPlaneDomainContent d : domains) {
            ret = ret + "<domain id=\"" +d.getId()+"\">";
            CtrlPlaneNodeContent[] nodes = d.getNode();
            for (CtrlPlaneNodeContent n : nodes) {
                ret = ret + "<node id=\"" +n.getId()+"\">";
                //ret = ret + "<address>" +n.getAddress()+"</address>";
                CtrlPlanePortContent[] ports = n.getPort();
                for (CtrlPlanePortContent p : ports) {
                    ret = ret + "<port id=\"" +p.getId()+"\">";
                    ret = ret + "<capacity>" +p.getCapacity()+"</capacity>";
                    ret = ret + "<maximumReservableCapacity>" +p.getCapacity()+"</maximumReservableCapacity>";
                    ret = ret + "<minimumReservableCapacity>" +p.getMinimumReservableCapacity()+"</minimumReservableCapacity>";
                    ret = ret + "<granularity>" +p.getGranularity()+"</granularity>";
                    CtrlPlaneLinkContent[] links = p.getLink();
                    if (links != null) {
                        for (CtrlPlaneLinkContent l : links) {
                            ret = ret + "<link id=\"" +l.getId()+"\">";
                            ret = ret + "<remoteLinkId>" +l.getRemoteLinkId()+"</remoteLinkId>";
                            ret = ret + "<trafficEngineeringMetric>" +l.getTrafficEngineeringMetric()+"</trafficEngineeringMetric>";
                            ret = ret + "<capacity>" +p.getCapacity()+"</capacity>";
                            ret = ret + "<maximumReservableCapacity>" +p.getCapacity()+"</maximumReservableCapacity>";
                            ret = ret + "<minimumReservableCapacity>" +p.getMinimumReservableCapacity()+"</minimumReservableCapacity>";
                            ret = ret + "<granularity>" +p.getGranularity()+"</granularity>";
                            ret = ret + "<SwitchingCapabilityDescriptors>";
                            ret = ret + "<switchingcapType>"+l.getSwitchingCapabilityDescriptors().getSwitchingcapType()+"</switchingcapType>";
                            ret = ret + "<encodingType>"+l.getSwitchingCapabilityDescriptors().getEncodingType()+"</encodingType>";
                            ret = ret + "<switchingCapabilitySpecificInfo>";
                            ret = ret + "<interfaceMTU>"+l.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getInterfaceMTU()+"</interfaceMTU>";
                            ret = ret + "<vlanRangeAvailability>"+l.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability()+"</vlanRangeAvailability>";
                            ret = ret + "<vlanTranslation>"+(l.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanTranslation()?"true":"false")+"</vlanTranslation>";
                            ret = ret + "</switchingCapabilitySpecificInfo>";
                            ret = ret + "</SwitchingCapabilityDescriptors>";
                            ret += "</link>";
                        }
                    }
                    ret += "</port>";
                }
                ret += "</node>";
            }
            ret += "</domain>";
        }
        ret += "</topology>";
        return ret;
    }

}
