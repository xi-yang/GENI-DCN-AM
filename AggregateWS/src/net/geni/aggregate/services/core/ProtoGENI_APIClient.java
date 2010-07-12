/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;


import java.util.regex.Pattern;
import java.util.Vector;
import java.util.HashMap;

/**
 *
 * @author Xi Yang
 */
public class ProtoGENI_APIClient extends AggregateCLIClient {
    protected String smUrn = "";
    protected String amUrn = "";
    protected String sslCertPath = "";
    protected String sslPassword = "";

    private int currentVlanTag = -1;

    private String commonCmd = "import xmlrpclib\n"
        + "import sys\n"
        + "import pwd\n"
        + "import os\n"
        + "import re\n"
        + "import zlib\n"
        + "import socket\n"
        + "import M2Crypto\n"
        + "from M2Crypto import X509\n"
        + "from urlparse import urlsplit, urlunsplit\n"
        + "from urllib import splitport\n"
        + "from M2Crypto.m2xmlrpclib import SSL_Transport\n"
        + "from M2Crypto import SSL\n"
        + "CERTIFICATE='<_ssl_cert_path_>'\n"
        + "PASSWORD='<_ssl_password_>'\n"
        + "cert = X509.load_cert( CERTIFICATE )\n"
        + "SMURN = '<_sm_urn_>'\n"
        + "AMURN = '<_am_urn_>'\n"
        + "HOSTNAME = AMURN[AMURN.find('//')+2:]\n"
        + "HOSTNAME = HOSTNAME[:HOSTNAME.find(':')];\n"
        + "DOMAIN   = HOSTNAME[HOSTNAME.find('.')+1:]\n"
        + "def Fatal(message):\n"
        + "    print 'FATAL:'+message\n"
        + "    sys.exit(1)\n"
        + "\n"
        + "def PassPhraseCB(v, prompt1='', prompt2=''):\n"
        + "    return PASSWORD\n"
        + "\n"
        + "def geni_am_response_handler(method, method_args):\n"
        + "    return apply(method, method_args)\n"
        + "\n"
        + "def do_method(module, method, params, URI=None, quiet=False, version=None, response_handler=None):\n"
        + "    if not os.path.exists(CERTIFICATE):\n"
        + "        return Fatal('missing emulab ssl certificate: wrong path?')\n"
        + "        pass\n"
        + "    if URI == None:\n"
        + "        if (module == 'sa'):\n"
        + "            URI = SMURN\n"
        + "        else:\n"
        + "            URI = AMURN\n"
        + "            pass\n"
        + "        pass\n"
        + "    if not ('/'+module) in URI:\n"
        + "        URI = URI + '/' + module\n"
        + "        pass\n"
        + "    if version:\n"
        + "        URI = URI + '/' + version\n"
        + "        pass\n"
        + "    scheme, netloc, path, query, fragment = urlsplit(URI)\n"
        + "    if not scheme:\n"
        + "        URI = 'https://' + URI\n"
        + "        pass\n"
        + "    scheme, netloc, path, query, fragment = urlsplit(URI)\n"
        + "    if scheme == 'https':\n"
        + "        host,port = splitport(netloc)\n"
        + "        if not port:\n"
        + "            netloc = netloc + ':443'\n"
        + "            URI = urlunsplit((scheme, netloc, path, query, fragment));\n"
        + "            pass\n"
        + "        pass\n"
        + "    ctx = SSL.Context('sslv23')\n"
        + "    ctx.load_cert(CERTIFICATE, CERTIFICATE, PassPhraseCB)\n"
        + "    ctx.set_verify(SSL.verify_none, 16)\n"
        + "    ctx.set_allow_unknown_ca(0)\n"
        + "    server = xmlrpclib.ServerProxy(URI, SSL_Transport(ctx), verbose=0)\n"
        + "    meth      = getattr(server, method)\n"
        + "    meth_args = [ params ]\n"
        + "    if response_handler:\n"
        + "        return response_handler(meth, params)\n"
        + "        pass\n"
        + "    try:\n"
        + "        response = apply(meth, meth_args)\n"
        + "        pass\n"
        + "    except xmlrpclib.Fault, e:\n"
        + "        return (-1, None)\n"
        + "    except xmlrpclib.ProtocolError, e:\n"
        + "        return (-1, None)\n"
        + "    except M2Crypto.SSL.Checker.WrongHost, e:\n"
        + "        return (-1, None)\n"
        + "        pass\n"
        + "    rval = response['code']\n"
        + "    if rval:\n"
        + "        if response['value']:\n"
        + "            rval = response['value']\n"
        + "            pass\n"
        + "        pass\n"
        + "    return (rval, response)\n"
        + "\n"
        + "def get_self_credential():\n"
        + "    params = {}\n"
        + "    rval,response = do_method('sa', 'GetCredential', params)\n"
        + "    if rval:\n"
        + "        Fatal('Could not get my credential')\n"
        + "        pass\n"
        + "    return response['value']\n"
        + "\n"
        + "def resolve_slice( name, selfcredential ):\n"
        + "    params = {}\n"
        + "    params['credential'] = mycredential\n"
        + "    params['type']       = 'Slice'\n"
        + "    params['hrn']        = name\n"
        + "    rval,response = do_method('sa', 'Resolve', params)\n"
        + "    if rval:\n"
        + "        Fatal('Slice does not exist');\n"
        + "        pass\n"
        + "    else:\n"
        + "        return response['value']\n"
        + "\n"
        + "def get_slice_credential( slice, selfcredential ):\n"
        + "    params = {}\n"
        + "    params['credential'] = selfcredential\n"
        + "    params['type']       = 'Slice'\n"
        + "    if 'urn' in slice:\n"
        + "        params['urn']       = slice['urn']\n"
        + "    else:\n"
        + "        params['uuid']      = slice['uuid']\n"
        + "    rval,response = do_method('sa', 'GetCredential', params)\n"
        + "    if rval:\n"
        + "        Fatal('Could not get Slice credential')\n"
        + "        pass\n"
        + "    return response['value']\n"
        + "\n"
        + "mycredential = get_self_credential()\n"
        + "params = {}\n"
        + "params['credential'] = mycredential\n"
        + "print 'DONE!'\n";

    private String createSliceCmd = "SLICENAME='<_slice_name_>'\n"
        + "SLICEURN = 'urn:publicid:IDN+' + DOMAIN + '+slice+' + SLICENAME\n"
        + "RSPEC = '<_rspec_>\\n'\n"
        + "params = {}\n"
        + "params['credential'] = mycredential\n"
        + "rval,response = do_method('sa', 'GetKeys', params)\n"
        + "if rval:\n"
        + "    Fatal('Could not get my keys')\n"
        + "    pass\n"
        + "\n"
        + "mykeys = response['value']\n"
        + "params = {}\n"
        + "params['credential'] = mycredential\n"
        + "params['type']       = 'Slice'\n"
        + "params['hrn']        = SLICENAME\n"
        + "rval,response = do_method('sa', 'Resolve', params)\n"
        + "if rval:\n"
        + "    params = {}\n"
        + "    params['credential'] = mycredential\n"
        + "    params['type']       = 'Slice'\n"
        + "    params['hrn']        = SLICENAME\n"
        + "    rval,response = do_method('sa', 'Register', params)\n"
        + "    if rval:\n"
        + "        Fatal('Could not create new slice')\n"
        + "        pass\n"
        + "    myslice = response['value']\n"
        + "    print 'New slice created'\n"
        + "    pass\n"
        + "else:\n"
        + "    myslice = response['value']\n"
        + "    myslice = get_slice_credential( myslice, mycredential )\n"
        + "    pass\n"
        + "\n"
        + "\n"
        + "secs_to_expire=<_expire_time_>\n"
        + "if secs_to_expire>21600: \n"
        + "    valid_until = time.strftime('%Y%m%dT%H:%M:%S', time.gmtime(time.time() + secs_to_expire))\n"
        + "    params = {}\n"
        + "    params['credential'] = myslice\n"
        + "    params['expiration'] = valid_until\n"
        + "    rval,response = do_method('sa', 'RenewSlice', params)\n"
        + "    if rval:\n"
        + "        Fatal('Could not change slice expiration time at the SA ')\n"
        + "        pass\n"
        + "    pass\n"
        + "\n"
        + "\n"
        + "params = {}\n"
        + "params['credentials'] = (myslice,)\n"
        + "params['slice_urn']   = SLICEURN\n"
        + "params['rspec']       = RSPEC\n"
        + "params['keys']        = mykeys\n"
        + "params['impotent']    = 0\n"
        + "rval,response = do_method('cm', 'CreateSliver', params, version='2.0')\n"
        + "if rval:\n"
        + "    Fatal('Could not create sliver')\n"
        + "    pass\n"
        + "\n"
        + "sliver,manifest = response['value']\n"
        + "print 'Created the sliver'\n"
        + "print '<MANIFEST'+'>'+str(manifest)+'</'+'MANIFEST>'\n"
        + "print 'DONE!'\n";

    private String deleteSliceCmd = "SLICENAME='<_slice_name_>'\n"
        + "SLICEURN = 'urn:publicid:IDN+' + DOMAIN + '+slice+' + SLICENAME\n"
        + "myslice = resolve_slice( SLICENAME, mycredential )\n"
        + "slicecred = get_slice_credential( myslice, mycredential )\n"
        + "params = {}\n"
        + "params['credentials'] = (slicecred,)\n"
        + "params['urn']         = myslice['urn']\n"
        + "rval,response = do_method('cm', 'Resolve', params, version='2.0')\n"
        + "if rval:\n"
        + "    Fatal('Could not resolve slice')\n"
        + "    pass\n"
        + "\n"
        + "myslice = response['value']\n"
        + "if not 'sliver_urn' in myslice:\n"
        + "    Fatal('No sliver exists for slice')\n"
        + "    pass\n"
        + "\n"
        + "params = {}\n"
        + "params['credentials'] = (slicecred,)\n"
        + "params['slice_urn']   = SLICEURN\n"
        + "rval,response = do_method('cm', 'GetSliver', params, version='2.0')\n"
        + "if rval:\n"
        + "    Fatal('Could not get Sliver credential')\n"
        + "    pass\n"
        + "\n"
        + "slivercred = response['value']\n"
        + "params = {}\n"
        + "params['credentials'] = (slivercred,)\n"
        + "params['sliver_urn']  = myslice['sliver_urn']\n"
        + "rval,response = do_method('cm', 'DeleteSliver', params, version='2.0')\n"
        + "if rval:\n"
        + "    Fatal('Could not delete sliver')\n"
        + "    pass\n"
        + "\n"
        + "print 'Sliver has been deleted. Ticket for remaining time:'\n"
        + "ticket = response['value']\n"
        + "print str(ticket);\n"
        + "print 'DONE!'\n";

        private String querySliceCmd = "SLICENAME='<_slice_name_>'\n"
        + "SLICEURN = 'urn:publicid:IDN+' + DOMAIN + '+slice+' + SLICENAME\n"
        + "myslice = resolve_slice( SLICENAME, mycredential )\n"
        + "slicecred = get_slice_credential( myslice, mycredential )\n"
        + "params = {}\n"
        + "params['credentials'] = (slicecred,)\n"
        + "params['urn']         = myslice['urn']\n"
        + "rval,response = do_method('cm', 'Resolve', params, version='2.0')\n"
        + "if rval:\n"
        + "    Fatal('Could not resolve slice')\n"
        + "    pass\n"
        + "\n"
        + "myslice = response['value']\n"
        + "if not 'sliver_urn' in myslice:\n"
        + "    Fatal('No sliver exists for slice')\n"
        + "    pass\n"
        + "\n"
        + "params = {}\n"
        + "params['slice_urn']   = SLICEURN\n"
        + "params['credentials'] = (slicecred,)\n"
        + "rval,response = do_method('cm', 'GetSliver', params, version='2.0')\n"
        + "if rval:\n"
        + "    Fatal('Could not get Sliver credential')\n"
        + "    pass\n"
        + "\n"
        + "slivercred = response['value']\n"
        + "print 'Got the sliver credential, asking for sliver status';\n"
        + "params = {}\n"
        + "params['slice_urn']   = SLICEURN\n"
        + "params['credentials'] = (slivercred,)\n"
        + "rval,response = do_method('cm', 'SliverStatus', params, version='2.0')\n"
        + "if rval:\n"
        + "    Fatal('Could not get sliver status')\n"
        + "    pass\n"
        + "\n"
        + "print '<query_status>'+str(response['value'])+'</query_status>'\n"
        + "print 'DONE!'\n";

    private ProtoGENI_APIClient() {
        super("DONE!");
    }

    public ProtoGENI_APIClient(String smUrn, String amUrn, String sslCert, String sslPass) {
        super("DONE!");
        this.smUrn = smUrn;
        this.amUrn = amUrn;
        if (smUrn == null || smUrn.isEmpty())
            this.smUrn = amUrn;
        this.sslCertPath = sslCert;
        this.sslPassword = sslPass;
    }

    /**
     * get an PLCCLient instance
     */
    static public ProtoGENI_APIClient getAPIClient(String smUrn, String amUrn, String sslCertPath, String sslPass) {
        return (new ProtoGENI_APIClient(smUrn, amUrn, sslCertPath, sslPass));
    }

    static public ProtoGENI_APIClient getAPIClient() {
        return getAPIClient(AggregateState.getProtoGeniSmUrn(), AggregateState.getProtoGeniAmUrn(),
                AggregateState.getProtoGeniSslCertPath(), AggregateState.getProtoGeniSslPassword());
    }

    public int getCurrentVlanTag() {
        return currentVlanTag;
    }

    public void setCurrentVlanTag(int vtag) {
        currentVlanTag = vtag;
    }

    //@Override
    public void finalize() {
        logoff();
    }

    public boolean login() {
        if (!super.login("expect", "-c", "spawn python", "-c", "interact"))
            return false;

        commonCmd = commonCmd.replaceFirst("<_sm_urn_>", smUrn);
        commonCmd = commonCmd.replaceFirst("<_am_urn_>", amUrn);
        commonCmd = commonCmd.replaceFirst("<_ssl_cert_path_>", sslCertPath);
        commonCmd = commonCmd.replaceFirst("<_ssl_password_>", sslPassword);
        this.sendCommand(commonCmd);
        log.debug("login sendCommand: " + commonCmd);

        //TODO $$$$ parsing output
        this.setTimeout(60000);
        int ret = this.readPattern("FATAL", null, promptPattern);
        log.debug("login response: " + this.buffer);
        log.debug("login retCode: " + Integer.toString(ret));
        if (ret != 0) {
            log.error("ProtoGENI_API server failed authenticate the SSL client.");
            proc = null;
            in = null;
            out = null;
            return false;
        }
        return true;
    }

    public boolean logoff() {
        log.debug("ProtoGENI_APIClient logoff");
        return super.logoff("sys.exit(0)");
    }

    /*commands for ProtoGENI slice operations*/
    public String createSlice(String sliceName, String rspecData, long expireTime) {
        if (!alive()) {
            if (!login())
                return null;
        }

        createSliceCmd = createSliceCmd.replaceAll("<_slice_name_>", sliceName);
        rspecData = rspecData.replaceAll("[\r\n]", ""); //make rspecData single line
        rspecData = rspecData.replace("\"", "\\\\\""); //escape quotes in rspecData
        createSliceCmd = createSliceCmd.replaceFirst("<_rspec_>", rspecData);
        long secsToExpire = expireTime - System.currentTimeMillis()/1000;
        createSliceCmd = createSliceCmd.replaceFirst("<_expire_time_>", Long.toString(secsToExpire));
        this.sendCommand(createSliceCmd);
        log.debug("createSlice sendCommand: " + createSliceCmd);
        this.setTimeout(240000);
        int ret = this.readPattern("FATAL", null, promptPattern);
        log.debug("createSlice response: " + this.buffer);
        log.debug("createSlice retCode: " + Integer.toString(ret));
        if (ret != 0) {
            log.error("ProtoGENI failed to create Slice '" + sliceName +"' on for Rspec: " + rspecData);
            logoff();
            return null;
        }
        else {
            String vlanTagStr = AggregateUtils.extractString(this.buffer, "vlantag=\"", "\">");
            if (vlanTagStr != null && !vlanTagStr.isEmpty())
                this.currentVlanTag = Integer.valueOf(vlanTagStr);
        }
        rspecData = AggregateUtils.extractString(this.buffer, "<MANIFEST>", "</MANIFEST>");
        return rspecData;
    }

    public int deleteSlice(String sliceName) {
        if (!alive()) {
            if (!login())
                return -1;
        }
        deleteSliceCmd = deleteSliceCmd.replaceAll("<_slice_name_>", sliceName);
        this.setTimeout(180000);
        this.sendCommand(deleteSliceCmd);
        log.debug("deleteSlice sendCommand: " + deleteSliceCmd);
        int ret = this.readPattern("FATAL", null, promptPattern);
        log.debug("deleteSlice response: " + this.buffer);
        log.debug("deleteSlice retCode: " + Integer.toString(ret));
        if (ret != 0) {
            log.error("ProtoGENI failed to delete the slice '" + sliceName +"'");
            logoff();
        }
        return ret;
    }

    /**
     *
     * @param sliceName
     * @return HashMap
     */
    public String querySlice(String sliceName) {
        if (!alive()) {
            if (!login())
                return null;
        }

        String status = "";
        querySliceCmd = querySliceCmd.replaceAll("<_slice_name_>", sliceName);
        this.sendCommand(querySliceCmd);
        log.debug("querySlice sendCommand: " + querySliceCmd);

        this.setTimeout(60000);
        int ret = this.readPattern("FATAL", null, promptPattern);
        log.debug("querySlice response: " + this.buffer);
        log.debug("querySlice retCode: " + Integer.toString(ret));
        if (ret != 0) {
            log.error("ProtoGENI failed to query the slice '" + sliceName +"'");
            logoff();
            status = "FAILED";
        }
        else {
            status = AggregateUtils.extractString(this.buffer, "<query_status>", "</query_status>");
            if (status == null)
                status = "UNKNOWN";
            else
                status = status.toUpperCase();
        }

        return status;
    }
}
