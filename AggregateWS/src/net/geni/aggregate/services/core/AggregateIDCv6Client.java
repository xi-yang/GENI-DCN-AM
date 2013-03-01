/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author xyang
 */
public class AggregateIDCv6Client {

    private String idcUrl = "";
    private String clientCommand = "";
    private String clientKeystore = "";
    private String clientKeystorePass = "";
    private String clientKeystoreUser = "";
    private String trustKeystore = "";
    private String trustKeystorePass = "";
    private String gri = null;
    private Logger log = Logger.getLogger(AggregateIDCv6Client.class);

    public AggregateIDCv6Client() {
        this.idcUrl = AggregateState.getIdcURL();
        this.clientCommand = AggregateState.getIdc6ClientCommand();
        this.clientKeystore = AggregateState.getIdc6ClientKeystore();
        this.clientKeystorePass = AggregateState.getIdc6ClientKeystorePassword();
        this.clientKeystoreUser = AggregateState.getIdc6ClientKeystoreUser();
        this.trustKeystore = AggregateState.getIdc6TrustKeystore();
        this.trustKeystorePass = AggregateState.getIdc6TrustKeystorePassword();
    }
    // place holder
    static private String yamlTemplate = "---\n"
            + "command:   <_command_>\n"
            + "url:        '<_idc_url_>'\n"
            + "keystore:   <_client_keystore_>\n"
            + "keystore-password: <_client_keystore_pass_>\n"
            + "key-alias: <_client_keystore_user_>\n"
            + "truststore: <_ssl_keystore_>\n"
            + "truststore-password: <_ssl_keystore_pass_>\n"
            + "output-format: rawResponse\n"
            + "version:    0.6\n";

    private String executeShellCommandWithInput(String cmd, String yaml) throws Exception {
        log.debug("executing IDC client command: "+cmd);
        log.debug("sending yaml: \n"+yaml);
        Process p = null;
        ReadStream rsIn = null;
        ReadStream rsErr = null;
    	try {
	    	p = Runtime.getRuntime().exec(cmd);
	    	OutputStream os = p.getOutputStream();
	    	OutputStreamWriter out = new OutputStreamWriter (os);
	    	out.write(yaml);
	    	out.flush();
	    	out.close();
	    	rsIn = new ReadStream("stdin", p.getInputStream ());
	    	rsErr = new ReadStream("stderr", p.getErrorStream ());
	    	rsIn.start ();
	    	rsErr.start ();
    		p.waitFor();
    	} catch (Exception e) {  
    		throw new Exception("executeShellCommandWithInput Exception: "+e.getMessage());
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
        log.debug("received stdin: \n"+rsIn.getString());
        log.debug("received stderr: \n"+rsErr.getString());
        if (rsErr.getString().contains("Exception") || rsIn.getString().isEmpty()) {
            return rsErr.getString();
        }
        return rsIn.getString();
    }

    private String extractXmlValueByTag(String xml, String tag) {
        int pos1 = xml.indexOf(String.format("<%s>", tag));
        if (pos1 == -1) {
            return "";
        }
        int taglen = xml.indexOf(String.format(">", tag), pos1) - pos1+1;
        int pos2 = xml.indexOf(String.format("</%s>", tag), pos1);
        if (pos2 == -1) {
            return "";
        }
        return xml.substring(pos1 + taglen, pos2);
    }

    /**
     * createReservation
     */
    public String requestCreateReservation(String requestYaml)
            throws Exception {
        String createYaml = fillSecurityInfo(yamlTemplate);
        createYaml = createYaml.replaceAll("<_command_>", "createReservation");
        createYaml += requestYaml;
        String responseXml = executeShellCommandWithInput(this.clientCommand, createYaml);
        if (responseXml.isEmpty() || responseXml.contains("Exception")) {
            return "FAILED";
        }
        String status = extractXmlValueByTag(responseXml, "ns3:status");
        if (status.isEmpty()) {
            return "FAILED";
        }
        // Extract GRI
        this.gri = extractXmlValueByTag(responseXml, "ns3:globalReservationId");
        // convert status v6->v5
        if (status.equalsIgnoreCase("Ok"))
            status = "ACCEPTED";
        return status;
    }

    public String getGlobalReservationId() {
        return gri;
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
        String cancelYaml = fillSecurityInfo(yamlTemplate);
        cancelYaml = cancelYaml.replaceAll("<_command_>", "cancelReservation");
        cancelYaml += String.format("gri:  '%s'\n", aGri);
        String responseXml = executeShellCommandWithInput(this.clientCommand, cancelYaml);
        String status = extractXmlValueByTag(responseXml, "ns3:status");
        // TODO: convert status
        if (status.equalsIgnoreCase("Ok"))
            status = "CANCELLED";
        return status;
    }

    /**
     * queryReservation
     *
     */
    public HashMap requestQueryReservation(String aGri)
            throws Exception {
        /* Send Request */
        if (aGri == null || aGri.isEmpty()) {
            throw new Exception("request to query with null / empty GRI");
        }
        gri = aGri;

        String queryYaml = fillSecurityInfo(yamlTemplate);
        queryYaml = queryYaml.replaceAll("<_command_>", "queryReservation");
        queryYaml += String.format("gri:  '%s'\n", aGri);
        String responseXml = executeShellCommandWithInput(this.clientCommand, queryYaml);

        HashMap hmRet = new HashMap();
        hmRet.put("GRI", extractXmlValueByTag(responseXml, "ns3:globalReservationId"));
        hmRet.put("login", extractXmlValueByTag(responseXml, "ns3:login"));
        hmRet.put("status", extractXmlValueByTag(responseXml, "ns3:status"));
        hmRet.put("startTime", extractXmlValueByTag(responseXml, "ns3:startTime"));
        hmRet.put("endTime", extractXmlValueByTag(responseXml, "ns3:endTime"));
        hmRet.put("cratedTime", extractXmlValueByTag(responseXml, "ns3:cratedTime"));
        hmRet.put("bandwidth", extractXmlValueByTag(responseXml, "ns3:bandwidth"));
        hmRet.put("description", extractXmlValueByTag(responseXml, "ns3:description"));
        hmRet.put("vlanTag", extractXmlValueByTag(responseXml, "ns3:srcVtag")+":"+extractXmlValueByTag(responseXml, "ns3:dstVtag"));
        return hmRet;
    }

    public String fillSecurityInfo(String yamlTemplate)
            throws Exception {
        String yamlStr = yamlTemplate.replaceAll("<_idc_url_>", idcUrl);
        yamlStr = yamlStr.replaceAll("<_client_keystore_>", clientKeystore);
        yamlStr = yamlStr.replaceAll("<_client_keystore_pass_>", clientKeystorePass);
        yamlStr = yamlStr.replaceAll("<_client_keystore_user_>", clientKeystoreUser);
        yamlStr = yamlStr.replaceAll("<_ssl_keystore_>", trustKeystore);
        yamlStr = yamlStr.replaceAll("<_ssl_keystore_pass_>", trustKeystorePass);
        return yamlStr;
    }

    /**
     * generateCreateResevationContent
     *
     */
    public String generateCreateResevationContent(String src, String dst, String vtag, float bw, String descr, long startTime, long endTime)
            throws Exception {

        String createYaml = "login: 'client'\n";
        createYaml += "layer: 2\n";
        createYaml += String.format("bandwidth: %d\n", (int) bw);
        createYaml += String.format("src: '%s'\n", src);
        createYaml += String.format("dst: '%s'\n", dst);
        createYaml += String.format("description: \"%s\"\n", descr);
        createYaml += String.format("srcvlan: '%s'\n", AggregateUtils.parseVlanTag(vtag, true));
        createYaml += String.format("dstvlan: '%s'\n", AggregateUtils.parseVlanTag(vtag, false));
        createYaml += String.format("start-time: '%s'\n", AggregateUtils.idcTimestampToString(startTime));
        createYaml += String.format("end-time: '%s'\n", AggregateUtils.idcTimestampToString(endTime));
        createYaml += "path-setup-mode: 'timer-automatic'\n";
        createYaml += "path-type: 'strict'\n";
        return createYaml;
    }
}
