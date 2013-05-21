/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import org.apache.log4j.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author jflidr
 */
public class AggregateState
{
    //Logger
    public static Logger log = org.apache.log4j.Logger.getLogger("net.geni.aggregate");

    //Properties
    private static Properties aggregateProps = new Properties();
    private static String amUrn;
    private static String dbPwd;
    private static String dbUser;
    private static String resourcesTab = "resources";
    private static String rspecsTab = "rspecs";
    private static String capsTab = "capabilities";
    private static String nodesTab = "nodes";
    private static String interfacesTab = "interfaces";
    private static String slicesTab = "slices";
    private static String usersTab = "users";
    private static String p2pvlansTab = "p2pvlans";
    private static String networksTab = "networks";
    private static String extResourcesTab = "ext_resources";
    
    private static String idcDomainId = "";
    private static String idcTopoFile = null;
    private static String idcURL = "";
    private static String idcRepo = "";
    private static String idcVersion = "";
    private static String idc6ClientCommand = "";
    private static String idc6ClientKeystore = "";
    private static String idc6ClientKeystorePassword = "";
    private static String idc6ClientKeystoreUser = "";
    private static String idc6TrustKeystore = "";
    private static String idc6TrustKeystorePassword = "";
    private static String idcVerifyEndpoints = "";

    private static String plcURL = "";
    private static String plcPI = "";
    private static String plcPassword = "";
    private static String plcPrefix = "";
    private static String plcSshHost = "";
    private static String plcSshLogin = "";
    private static String plcSshVserver = "";
    private static String plcSshPort = "";
    private static String plcSshKeyfile = "";
    private static String plcSshKeypass = "";
    private static String plcSshExecPrefix = "";
    private static String protoGeniSmUrn = "";
    private static String protoGeniAmUrn = "";
    private static String protoGeniSslCert = "";
    private static String protoGeniSslPass = "";

    // Resrouces
    private static AggregateCapabilities aggregateCaps = null;
    private static AggregateNodes aggregateNodes = null;
    private static AggregateNetworkInterfaces aggregateInterfaces = null;
    private static AggregateSlices aggregateSlices = null;
    private static AggregateP2PVlans aggregateP2PVlans = null;
    private static AggregateExternalResources aggregateExtResources = null;
    private static AggregateUsers aggregateUsers = null;
    
    // Global states
    private static AggregateRspecManager aggregateRspecManager = null;
    private static AggregateRspecHandler aggregateRspecHandler = null;
    private static AggregateSlicesPoller aggregateSlicesPoller = null;
    private static AggregateStitchTopologyRunner stitchTopoRunner = null;
    private static final int pollInterval = 60000; //miliseconds

    public static void init() {
        //init properties
        String aggregateHome = System.getenv("AGGREGATE_HOME");
        if (aggregateHome != null && !aggregateHome.equals(""))
            aggregateHome = aggregateHome + "/AggregateAttic/conf/";
        else
            aggregateHome = "/usr/local/geni-aggregate/AggregateAttic/conf/";
        String propFileName =  aggregateHome + "aggregate.properties";

        try {
            FileInputStream in = new FileInputStream(propFileName);
            aggregateProps.load(in);
            in.close();
        } catch (IOException e) {
            //logging for exception!
        }
        aggregateProps.setProperty("aggregate.conf.dir", aggregateHome);
        amUrn = aggregateProps.getProperty("aggregate.am.urn", "urn:publicid:IDN+maxpl");
        dbUser = aggregateProps.getProperty("aggregate.mysql.user", "geniuser");
        log.info("aggregate.mysql.user set to " + dbUser);
        dbPwd = aggregateProps.getProperty("aggregate.mysql.pass", "genipass");
        
        idcDomainId = aggregateProps.getProperty("aggregate.idc.domainid", "all");
        log.info("aggregate.idc.domainid set to " + idcDomainId);
        idcTopoFile = aggregateProps.getProperty("aggregate.idc.topofile", null);
        idcURL = aggregateProps.getProperty("aggregate.idc.url", "https://idc.dragon.maxgigapop.net:8443/axis2/services/OSCARS");
        log.info("aggregate.idc.url set to " + idcURL);
        idcRepo = aggregateProps.getProperty("aggregate.idc.repo", "/usr/local/geni-aggregate/AggregateAttic/conf/repo");
        log.info("aggregate.idc.repo set to " + idcRepo);
        idcVersion = aggregateProps.getProperty("aggregate.idc.version", "0.5");
        log.info("aggregate.idc.version set to " + idcVersion);
        idc6ClientCommand = aggregateProps.getProperty("aggregate.idc.v6.client_command", "java -Done-jar.verbose=false -Done-jar.info=false "
                + "-Done-jar.main.class=net.es.oscars.api.client.SimpleOSCARSClient -jar "
                + "/usr/local/geni-aggregate/AggregateAttic/conf/repo/SimpleOSCARSClient-0.0.1-SNAPSHOT.one-jar.jar -f -");
        idc6ClientCommand = idc6ClientCommand.replaceAll("[\\'\\\"]", "");
        log.info("aggregate.idc.v6.client_command set to " + idc6ClientCommand);
        idc6ClientKeystore = aggregateProps.getProperty("aggregate.idc.v6.client_keystore", "/usr/local/geni-aggregate/AggregateAttic/conf/repo/oscars-client.jks");
        log.info("aggregate.idc.v6.client_keystore set to " + idc6ClientKeystore);
        idc6ClientKeystorePassword = aggregateProps.getProperty("aggregate.idc.v6.client_keystore_password", "password");
        //log.info("aggregate.idc.v6.client_keystore_password set to " + idc6ClientKeystorePassword);
        idc6ClientKeystoreUser = aggregateProps.getProperty("aggregate.idc.v6.client_keystore_user", "oscarsuser");
        log.info("aggregate.idc.v6.client_keystore_user set to " + idc6ClientKeystoreUser);
        idc6TrustKeystore = aggregateProps.getProperty("aggregate.idc.v6.trust_keystore", "/usr/local/geni-aggregate/AggregateAttic/conf/repo/oscars-ssl.jks");
        log.info("aggregate.idc.v6.trust_keystore set to " + idc6TrustKeystore);
        idc6TrustKeystorePassword = aggregateProps.getProperty("aggregate.idc.v6.trust_keystore_password", "password");
        //log.info("aggregate.idc.v6.trust_keystore_password set to " + idc6TrustKeystorePassword);
        idcVerifyEndpoints = aggregateProps.getProperty("aggregate.idc.verifyendpoints", "true");
        log.info("aggregate.idc.verifyendpoints set to " + idcVerifyEndpoints);
        
        plcURL = aggregateProps.getProperty("aggregate.plc.url", "https://max-myplc.dragon.maxgigapop.net/PLCAPI/");
        log.info("aggregate.plc.url set to " + plcURL);
        plcPI = aggregateProps.getProperty("aggregate.plc.pi", "xyang@east.isi.edu");
        plcPassword = aggregateProps.getProperty("aggregate.plc.pass", "password");
        plcPrefix = aggregateProps.getProperty("aggregate.plc.base", "maxpl");
        plcSshHost = aggregateProps.getProperty("aggregate.plc.ssh.host", "max-myplc.dragon.maxgigapop.net");
        plcSshLogin = aggregateProps.getProperty("aggregate.plc.ssh.login", "root");
        plcSshVserver = aggregateProps.getProperty("aggregate.plc.ssh.vserver", null);
        plcSshPort = aggregateProps.getProperty("aggregate.plc.ssh.port", "22");
        plcSshKeyfile = aggregateProps.getProperty("aggregate.plc.ssh.keyfile", idcRepo + "/plc-ssh.pkey");
        plcSshKeypass = aggregateProps.getProperty("aggregate.plc.ssh.keypass", idcRepo + "");
        plcSshExecPrefix = aggregateProps.getProperty("aggregate.plc.ssh.execprefix", "");
        plcSshExecPrefix = plcSshExecPrefix.replaceAll("[\\'\\\"]", "");

        protoGeniSslCert = aggregateProps.getProperty("aggregate.external.protogeni.ssl_cert_path", "");
        protoGeniSslPass = aggregateProps.getProperty("aggregate.external.protogeni.ssl_password", "");
        protoGeniSmUrn = aggregateProps.getProperty("aggregate.external.protogeni.sm_urn", "https://www.emulab.net:443/protogeni/xmlrpc/sa");
        protoGeniAmUrn = aggregateProps.getProperty("aggregate.external.protogeni.am_urn", "https://www.emulab.net:443/protogeni/xmlrpc/cm");
        
        //init rspec handler
        String rspecHandlerClass = aggregateProps.getProperty("aggregate.rspec.handler", "net.geni.aggregate.services.core.RspecHandler_MAX");
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> aClass = null;        
            aClass = cl.loadClass(rspecHandlerClass);
            aggregateRspecHandler = (AggregateRspecHandler)aClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //init hibernate
        HibernateUtil.initSessionFactory();

        //init instances
        aggregateCaps = new AggregateCapabilities();
        aggregateNodes = new AggregateNodes();
        aggregateInterfaces = new AggregateNetworkInterfaces();
        aggregateSlices = new AggregateSlices();
        aggregateP2PVlans = new AggregateP2PVlans();
        aggregateExtResources = new AggregateExternalResources();
        aggregateUsers = new AggregateUsers();
    }

    //setters/getters 
    public static Properties getProperties() {
        return aggregateProps;
    }

    public static String getAggregateConfDir() {
        return aggregateProps.getProperty("aggregate.conf.dir", null);
    }
    
    public static AggregateRspecManager getRspecManager() {
        return aggregateRspecManager;
    }

    public static AggregateRspecHandler getRspecHandler() {
        return aggregateRspecHandler;
    }

    public static AggregateSlicesPoller getSlicesPoller() {
        return aggregateSlicesPoller;
    }

    public static AggregateStitchTopologyRunner getStitchTopoRunner() {
        return stitchTopoRunner;
    }

    public static void setStitchTopoRunner(AggregateStitchTopologyRunner stitchTopoRunner) {
        AggregateState.stitchTopoRunner = stitchTopoRunner;
    }

    public static void setRspecManager(AggregateRspecManager rspecMan) {
        AggregateState.aggregateRspecManager = rspecMan;
    }

    public static void setSlicesPoller(AggregateSlicesPoller aggregateSlicesPoller) {
        AggregateState.aggregateSlicesPoller = aggregateSlicesPoller;
    }

    public static int getPollInterval() {
        return pollInterval;
    }

    public static AggregateCapabilities getAggregateCaps() {
        return aggregateCaps;
    }

    public static AggregateNodes getAggregateNodes() {
        return aggregateNodes;
    }

    public static AggregateNetworkInterfaces getAggregateInterfaces() {
        return aggregateInterfaces;
    }

    public static AggregateSlices getAggregateSlices() {
        return aggregateSlices;
    }

    public static AggregateP2PVlans getAggregateP2PVlans() {
        return aggregateP2PVlans;
    }

    public static AggregateExternalResources getAggregateExtResources() {
        return aggregateExtResources;
    }

    public static AggregateUsers getAggregateUsers() {
        return aggregateUsers;
    }

    public static String getAmUrn() {
        return amUrn;
    }

    public static String getDbPwd() {
        return dbPwd;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getResourcesTab() {
        return resourcesTab;
    }

    public static String getRspecsTab() {
        return rspecsTab;
    }

    public static String getCapsTab() {
        return capsTab;
    }

    public static String getNodesTab() {
        return nodesTab;
    }

    public static String getInterfacesTab() {
        return interfacesTab;
    }

    public static String getSlicesTab() {
        return slicesTab;
    }

    public static String getUsersTab() {
        return usersTab;
    }

    public static String getP2PVlansTab() {
        return p2pvlansTab;
    }

    public static String getNetworksTab() {
        return networksTab;
    }

    public static String getExtResourcesTab() {
        return extResourcesTab;
    }

    public static String getIdcDomainId() {
        return idcDomainId;
    }

    public static String getIdcTopoFile() {
        return idcTopoFile;
    }

    public static void setIdcDomainId(String idcDomainId) {
        AggregateState.idcDomainId = idcDomainId;
    }

    public static String getIdcURL() {
        return idcURL;
    }

    public static String getIdcRepo() {
        return idcRepo;
    }

    public static String getIdcVersion() {
        return idcVersion;
    }

    public static String getIdc6ClientCommand() {
        return idc6ClientCommand;
    }

    public static String getIdc6ClientKeystore() {
        return idc6ClientKeystore;
    }

    public static String getIdc6ClientKeystorePassword() {
        return idc6ClientKeystorePassword;
    }

    public static String getIdc6ClientKeystoreUser() {
        return idc6ClientKeystoreUser;
    }

    public static String getIdc6TrustKeystore() {
        return idc6TrustKeystore;
    }

    public static String getIdc6TrustKeystorePassword() {
        return idc6TrustKeystorePassword;
    }

    public static boolean isIdcVerifyEndpoints() {
        return idcVerifyEndpoints.equalsIgnoreCase("true");
    }
    
    public static String getPlcURL() {
        return plcURL;
    }

    public static String getPlcPI() {
        return plcPI;
    }

    public static String getPlcPassword() {
        return plcPassword;
    }

    public static String getPlcPrefix() {
        if (plcPrefix.isEmpty())
            return plcPrefix;
        else 
            return plcPrefix + '_';
    }

    public static String getPlcSshHost() {
        return plcSshHost;
    }

    public static String getPlcSshLogin() {
        return plcSshLogin;
    }

    public static String getPlcSshVserver() {
        return plcSshVserver;
    }

    public static String getPlcSshPort() {
        return plcSshPort;
    }

    public static String getPlcSshKeyfile() {
        return plcSshKeyfile;
    }

    public static String getPlcSshKeypass() {
        return plcSshKeypass;
    }

    public static String getPlcSshExecPrefix() {
        return plcSshExecPrefix;
    }

    public static String getProtoGeniSmUrn() {
        return protoGeniSmUrn;
    }


    public static String getProtoGeniAmUrn() {
        return protoGeniAmUrn;
    }

    public static String getProtoGeniSslCertPath() {
        return protoGeniSslCert;
    }

    public static String getProtoGeniSslPassword() {
        return protoGeniSslPass;
    }

    public static String getAggregateCRDBFilePath() { //computeResource DB file
        return aggregateProps.getProperty("aggregate.crdb.path", null);
    }
}
