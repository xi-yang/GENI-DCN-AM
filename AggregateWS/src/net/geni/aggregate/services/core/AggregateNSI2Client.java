/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author xyang
 */
public class AggregateNSI2Client {
    private String gri = null;
    private String nsiDir = "";
    private String nsaUrl = "";
    static public int defaultBandwidth = 100; // 100 Mbps
    static public long defaultDuration = 3600*24*5; // 5 days
    private Logger log = Logger.getLogger(AggregateNSI2Client.class);

    public AggregateNSI2Client() {
        this.nsiDir = AggregateState.getNsiDir();
        this.nsaUrl = AggregateState.getNsaUrl();
    }

    private String executeShellCommand(String cmd) throws Exception {
    	Process p = null;
    	ReadStream rsIn = null;
    	ReadStream rsErr = null;
        boolean interrupted = false;
    	try {
	    	p = Runtime.getRuntime().exec(cmd);
	    	OutputStream os = p.getOutputStream();
	    	rsIn = new ReadStream("stdin", p.getInputStream ());
	    	rsErr = new ReadStream("stderr", p.getErrorStream ());
	    	rsIn.start ();
	    	rsErr.start ();
    		p.waitFor();
            Thread.sleep(3000); // wait extra 3 secs for readers to finish
    	} catch (InterruptedException ex1) {
            interrupted = true;
        } catch (Exception ex2) {  
    		throw new Exception("executeShellCommand Exception: "+ex2.getMessage());
    	} finally {
    		if (p != null) {
    			p.destroy();
    			try {
	    			rsIn.interrupt();
    			} catch (Exception e) {
    				;
    			}
    			try {
	    			rsErr.interrupt();
    			} catch (Exception e) {
    				;
    			}
    		}
    	}
        if (interrupted)
            return "INTERRUPTED";
    	if (rsErr.getString().contains("Exception") || rsIn.getString().isEmpty())
    		return rsErr.getString();
    	return rsIn.getString();
    }
    
    public String generateReserveOptions(String srcUrn, String dstUrn, String srcVlan, String dstVlan, float bw, String descr, long startTime, long endTime) 
        throws Exception {
        String options = " -c " + (int)bw + " -p '" + descr +"'";
    	options += String.format(" --src-stp %s?vlan=%s", srcUrn, srcVlan);
    	options += String.format(" --dst-stp %s?vlan=%s", dstUrn, dstVlan);
        options += String.format(" -b %d -e %d", startTime, endTime);
    	return options;
    }
    
    /**
     * createReservation
     */
    public String requestCreateReservation(String resvOptions)
            throws Exception {
        this.gri = this.nsiReserve(resvOptions);
        if (this.gri == null || this.gri.isEmpty()) {
            return "FAILED";
        }
        String nsiStatus = this.nsiUpdate(this.gri, "RESERVE_COMMIT", true);
        if (nsiStatus.equals("UNKNOWN")) {
            throw new Exception(String.format("applyDelta for connection '%s' cannot get status after RESERVE_COMMIT", this.gri));
        }
        String triStatus[] = nsiStatus.split("\\s");
        if (triStatus.length != 3 || !triStatus[0].equals("Created") || !triStatus[1].equals("ReserveStart") || !triStatus[2].equals("Released")) {
            throw new Exception(String.format("applyDelta for connection '%s' get tri-states='%s' after RESERVE_COMMIT", this.gri, nsiStatus));
        }
        nsiStatus = this.nsiUpdate(this.gri, "PROVISION", true);
        if (nsiStatus.equals("UNKNOWN")) {
            throw new Exception(String.format("applyDelta for connection '%s' cannot get status after PROVISION", this.gri));
        }
        triStatus = nsiStatus.split("\\s");
        if (triStatus.length != 3 || !triStatus[0].equals("Created") || !triStatus[1].equals("ReserveStart") || !triStatus[2].equals("Provisioned")) {
            throw new Exception(String.format("applyDelta for connection '%s' get tri-states='%s' after RESERVE_COMMIT", this.gri, nsiStatus));
        }
        return "ACCEPTED";
    }

    public String getGlobalReservationId() {
        return this.gri;
    }

    /**
     * modifyReservation
     *
     */
    public String requestModifyReservation(String aGri, String src, String dst, String vtag, float bw, String descr, long startTime, long endTime)
            throws Exception {

        if (aGri == null || aGri.isEmpty()) {
            throw new Exception("request to modify with null / empty GRI");
        }
        // Not support modification yet
        return "FAILED";
    }
    
    /**
     * cancelReservation
     *
     */
    public String requestCancelReservation(String aGri)
            throws Exception {
        if (aGri == null || aGri.isEmpty()) {
            throw new Exception("request to cancel with null / empty GRI");
        }
        this.gri = aGri;
        String nsiStatus = this.nsiUpdate(this.gri, "TERMINATE", true);
        if (nsiStatus.equals("UNKNOWN")) {
            throw new Exception(String.format("deleteDelta for connection '%s' cannot get status after TERMINATE", this.gri));
        }
        String triStatus[] = nsiStatus.split("\\s");
        if (triStatus.length != 3 || !triStatus[0].equals("Terminated") || !triStatus[1].equals("ReserveStart") || !triStatus[2].equals("Released")) {
            throw new Exception(String.format("deleteDelta for connection '%s' get tri-states='%s' after TERMINATE", this.gri, nsiStatus));
        }
        return "DELETED"; 
    }

    /**
     * queryReservation
     *
     */
    public HashMap requestQueryReservation(String aGri)
            throws Exception {
        if (aGri == null || aGri.isEmpty()) {
            throw new Exception("request to query with null / empty GRI");
        }
        this.gri = aGri;
        String response = this.nsiQueryRaw(this.gri);
        String connId = extractValue(response, "Connection ID");
        if (connId.isEmpty() || !connId.equals(this.gri)) {
            throw new Exception("queryReservation cannot retrive information for " + this.gri);
        }
        HashMap hmRet = new HashMap();
        hmRet.put("GRI", this.gri);
        hmRet.put("login", extractValue(response, "Requester NSA"));
        String lifeCycleStatus = extractValue(response, "Life Cycle State");
        String reserveStatus = extractValue(response, "Reservations State");
        String provisionStatus = extractValue(response, "Provision State");
        //TODO: map the three status into a single status
        hmRet.put("status", lifeCycleStatus);
        hmRet.put("startTime", extractValue(response, "Start Time"));
        hmRet.put("endTime", extractValue(response, "End Time"));
        hmRet.put("cratedTime", extractValue(response, "Start Time"));
        hmRet.put("bandwidth", extractValue(response, "Capacity"));
        hmRet.put("description", extractValue(response, "Description"));
        String srcStp = extractValue(response, "Source STP");
        String dstStp = extractValue(response, "Destination STP");
        String srcVlan = srcStp.substring(srcStp.indexOf("vlan=")+5);
        String dstVlan = dstStp.substring(srcStp.indexOf("vlan=")+5);
        hmRet.put("vlanTag", srcVlan+":"+dstVlan);
        
        return hmRet;
    }

    /**
     * reserve
     */
    private String nsiReserve(String options) 
        throws Exception {
    	String reserveCmd = String.format("java -Done-jar.main.class=net.es.oscars.nsibridge.client.cli.ReserveCLIClient -Djava.net.preferIPv4Stack=true -jar %s/nsibridge.one-jar.jar -f %s/client-bus-ssl.xml  -u %s %s",
                this.nsiDir, this.nsiDir, this.nsaUrl, options);
    	String response = this.executeShellCommand(reserveCmd);
    	if (response.isEmpty() || response.contains("Error") || response.contains("Exception"))
    		return null;
        // response should looke like: Connection created with ID urn:uuid:fc8541e3-4d16-4bbc-885c-031dfc19e41e
        String expectPrefix = "Connection created with ID ";
        if (!response.startsWith(expectPrefix)) {
            return null;
        }
        // extract connection ID
        return response.substring(expectPrefix.length()); 
    }

    /**
     *  update status
     */
    private String nsiUpdate(String connId, String operation, boolean queryAfter)
        throws Exception {
    	String updateCmd = String.format("java -Done-jar.main.class=net.es.oscars.nsibridge.client.cli.SimpleCLIClient -Djava.net.preferIPv4Stack=true -jar %s/nsibridge.one-jar.jar -f %s/client-bus-ssl.xml  -u %s -i %s -o %s",
                this.nsiDir, this.nsiDir, this.nsaUrl, connId, operation);
    	String response = this.executeShellCommand(updateCmd);
    	if (response.contains("Error") || response.contains("Exception"))
    		throw new Exception(String.format("nsiUpdate(%s, %s) Exception: %s", connId, operation, response));
        if (queryAfter == true)
            return nsiQuery(connId); 
        return "";
    }
    
    /**
     * query
     */
    private String nsiQuery(String connId) 
        throws Exception {
    	String queryCmd = String.format("java -Done-jar.main.class=net.es.oscars.nsibridge.client.cli.QueryCLIClient -Djava.net.preferIPv4Stack=true -jar %s/nsibridge.one-jar.jar -f %s/client-bus-ssl.xml  -u %s -i %s",
                this.nsiDir, this.nsiDir, this.nsaUrl, connId);
    	String response = this.executeShellCommand(queryCmd);
    	if (response.isEmpty() || response.contains("Exception"))
    		return "UNKNOWN";
        if (response.equals("INTERRUPTED")) {
            return response;
        }
        String nsiStatus =  "UNKNOWN";
        /* look for this pattern:
        "Life Cycle State: CreatedReservations State: ReserveStartProvision State: ReleasedDataplane Status:"
        */
        String patterns[] = {
            "Life Cycle State: ", //trailing space
            "Reservations State:", // no trailing space
            "Provision State:",
            "Dataplane Status:"};
        int indx0 = response.indexOf(patterns[0]);
        int indx1 = response.indexOf(patterns[1]);
        int indx2 = response.indexOf(patterns[2]);
        int indx3 = response.indexOf(patterns[3]);
        if (indx0 > 0 && indx1 > 0 && indx2 > 0 && indx3 > 0) {
            nsiStatus = response.substring(indx0+patterns[0].length(), indx1)
                    + response.substring(indx1+patterns[1].length(), indx2)
                    + response.substring(indx2+patterns[2].length(), indx3);
        }
        return nsiStatus; 
    }
    
    private String nsiQueryRaw(String connId) 
        throws Exception {
    	String queryCmd = String.format("java -Done-jar.main.class=net.es.oscars.nsibridge.client.cli.QueryCLIClient -Djava.net.preferIPv4Stack=true -jar %s/nsibridge.one-jar.jar -f %s/client-bus-ssl.xml  -u %s -i %s",
                this.nsiDir, this.nsiDir, this.nsaUrl, connId);
    	String response = this.executeShellCommand(queryCmd);
    	if (response.isEmpty() || response.contains("Exception"))
    		return "UNKNOWN";
        return response;
    }
    
    private String extractValue(String text, String key) {
        String pattern = String.format("%s:\\s+([^\\s]+)", key);
        Pattern pat = Pattern.compile(pattern);
        Matcher match = pat.matcher(text);
        if (match.find())
            return match.group(0);
        return "";
    }
    
}
