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
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;

/**
 *
 * @author xyang
 *
 * This code will be revised to use separate thread with status polling/notifications
 */

public class AggregateIDCClient {

    private String idcURL;
    private String idcRepo;
    private GlobalReservationId gri;
    private Logger log;

    AggregateIDCClient(String url, String repo) {
        idcURL = url;
        idcRepo = repo;
        gri = new GlobalReservationId();
        log = Logger.getLogger("net.geni.aggregate");
    }
    /**
     * get an IDCCLient instance
     */
    static AggregateIDCClient getIDCClient(String url, String repo) {
        return (new AggregateIDCClient(url, repo));
    }

    static AggregateIDCClient getIDCClient() {
        return getIDCClient(AggregateState.getIdcURL(), AggregateState.getIdcRepo());
    }

    /**
     * createReservation
     */
    public String createReservation(String src, String dst, int vtag, float bw, String descr, long startTime, long endTime)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {

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
        String vlanTag = "any";
        if (vtag == 0)
            vlanTag = "untagged";
        else if (vtag > 1 && vtag < 4096)
            vlanTag = Integer.toString(vtag);
        VlanTag srcVtag = new VlanTag();
        srcVtag.setString(vlanTag);
        srcVtag.setTagged(true);
        layer2Info.setSrcVtag(srcVtag);
        VlanTag destVtag = new VlanTag();
        // same as srcVtag for now
        destVtag.setString(vlanTag);
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
        /* Extract repsponse information */


        gri.setGri(response.getGlobalReservationId());

        return response.getStatus();
    }

    // 
    public String getGlobalReservationId() {
        return gri.getGri();
    }

    /**
     *  cancelReservation
     *
     */
    public String cancelReservation(String aGri)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {
        if (gri == null)
            return "failed";

        Client client = new Client();

        /* Initialize client instance */
        client.setUp(true, idcURL, idcRepo);
        /* Send Request */
        if (!aGri.equals(""))
            gri.setGri(aGri);
        String status = client.cancelReservation(this.gri);
        /* Extract repsponse information */
        return status;
    }

    /**
     * set queryReservation
     * 
     */
    public HashMap queryReservation(String aGri)
        throws AxisFault, AAAFaultMessage, BSSFaultMessage, RemoteException, Exception {

        HashMap hmRet = new HashMap();
        if (gri == null) {
            hmRet.put("GRI","unknown");
            hmRet.put("status", "failed");
            return hmRet;
        }

        Client client = new Client();

        /* Initialize client instance */
        client.setUp(true, idcURL, idcRepo);
        /* Send Request */
        if (!aGri.equals(""))
            gri.setGri(aGri);
        ResDetails response = client.queryReservation(this.gri);
        PathInfo pathInfo = response.getPathInfo();
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        /* Extract repsponse information */
        hmRet.put("GRI", response.getGlobalReservationId());
        hmRet.put("login", response.getLogin());
        hmRet.put("status", response.getStatus());
        hmRet.put("startTime", response.getStartTime());
        hmRet.put("endTime", response.getEndTime());
        hmRet.put("cratedTime", response.getCreateTime());
        hmRet.put("bandwidth", response.getBandwidth());
        hmRet.put("description", response.getDescription());
        hmRet.put("source", layer2Info.getSrcEndpoint());
        hmRet.put("destination", layer2Info.getDestEndpoint());
        /* Get path ERO */
        String ero = "\n";
        CtrlPlanePathContent path = pathInfo.getPath();
        for (CtrlPlaneHopContent hop : path.getHop()) {
            CtrlPlaneLinkContent link = hop.getLink();
            if (link == null) {
                //should not happen
                ero += "no link";
                continue;
            }
            ero += "\t" + link.getId();
            CtrlPlaneSwcapContent swcap = link.getSwitchingCapabilityDescriptors();
            CtrlPlaneSwitchingCapabilitySpecificInfo swcapInfo = swcap.getSwitchingCapabilitySpecificInfo();
            ero += ", " + swcap.getEncodingType();
            if ("ethernet".equals(swcap.getEncodingType())) {
                ero += ", " + swcapInfo.getVlanRangeAvailability();
            }
            ero += "\n";
        }
        hmRet.put("ERO", ero);
        return hmRet;
    }
}
