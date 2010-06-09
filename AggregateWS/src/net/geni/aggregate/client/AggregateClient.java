package net.geni.aggregate.client;

import java.util.*;

import org.apache.log4j.*;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;

/**
 *
 * @author Xi Yang
 */
public class AggregateClient {
    protected org.apache.log4j.Logger log;
    protected ConfigurationContext configContext;
    protected AggregateGENIStub stub;

    public void setUp(boolean useKeyStore, String url, String repo)
            throws AxisFault {
        this.setUp(useKeyStore, url, repo, null);
    }

    public void setUp(boolean useKeyStore, String url, String repo,
                      String axisConfig) throws AxisFault {
        if (useKeyStore) { KeyManagement.setKeyStore(repo); }
        this.log = org.apache.log4j.Logger.getLogger(this.getClass());
        this.configContext =
                ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repo, axisConfig);

        if(url != null){
            this.stub = new AggregateGENIStub(this.configContext, url);
            ServiceClient sc = this.stub._getServiceClient();
            Options opts = sc.getOptions();
            opts.setTimeOutInMilliSeconds(300000); // set to 5 minutes
            sc.setOptions(opts);
            this.stub._setServiceClient(sc);
        }
        this.log.debug("AggregateGENI API client setUp with repo='" + repo +
                "' url=" + url);
    }

    /**
     * Terminates the Axis2 ConfigurationContext. You only need to call
     * this if you are running the client in an environment such as
     * Tomcat to prevent memory leaks.
     */
    public void cleanUp(){
        if(this.configContext == null){
            return;
        }
        try{
            this.configContext.terminate();
        }catch(AxisFault e){
            this.log.warn("Unable to terminate Axis2 configuration context." +
                "There may be a memory leak so you should watch Tomcat's " +
                "thread count. " + e.getMessage());
        }
    }

    public ListCapabilitiesResponseType listCapabilities()
           throws AggregateFaultMessage, Exception {
        ListCapabilities listCaps = new ListCapabilities();
        ListCapabilitiesType listCapsType = new ListCapabilitiesType();
        listCapsType.setFilter("");
        listCaps.setListCapabilities(listCapsType);
        ListCapabilitiesResponse listCapsResponse = this.stub.ListCapabilities(listCaps);
        return listCapsResponse.getListCapabilitiesResponse();
    }

    public CreateSliceNetworkResponseType createSliceNetwork(String rsepcXml[])
           throws AggregateFaultMessage, Exception {
        CreateSliceNetwork createSliceNet = new CreateSliceNetwork();
        CreateSliceNetworkType createSliceNetType = new CreateSliceNetworkType();
        RSpecTopologyType rspecTopoType = new RSpecTopologyType();
        rspecTopoType.setStatement(rsepcXml);
        createSliceNetType.setRspecNetwork(rspecTopoType);
        createSliceNet.setCreateSliceNetwork(createSliceNetType);
        CreateSliceNetworkResponse createSliceNetResponse = this.stub.CreateSliceNetwork(createSliceNet);
        return createSliceNetResponse.getCreateSliceNetworkResponse();
    }

    public DeleteSliceNetworkResponseType deleteSliceNetwork(String rspecName)
           throws AggregateFaultMessage, Exception {
        DeleteSliceNetwork deleteSliceNet = new DeleteSliceNetwork();
        DeleteSliceNetworkType deleteSliceNetType = new DeleteSliceNetworkType();
        deleteSliceNetType.setRspecID(rspecName);
        deleteSliceNet.setDeleteSliceNetwork(deleteSliceNetType);
        DeleteSliceNetworkResponse deleteSliceNetResponse = this.stub.DeleteSliceNetwork(deleteSliceNet);
        return deleteSliceNetResponse.getDeleteSliceNetworkResponse();
    }

    public QuerySliceNetworkResponseType querySliceNetwork(String rspecName)
           throws AggregateFaultMessage, Exception {
        QuerySliceNetwork querySliceNet = new QuerySliceNetwork();
        QuerySliceNetworkType querySliceNetType = new QuerySliceNetworkType();
        querySliceNetType.setRspecID(rspecName);
        querySliceNet.setQuerySliceNetwork(querySliceNetType);
        QuerySliceNetworkResponse querySliceNetResponse = this.stub.QuerySliceNetwork(querySliceNet);
        return querySliceNetResponse.getQuerySliceNetworkResponse();
    }

    public GetResourceTopologyResponseType getResourceTopo(String scope, String rspecNames[])
           throws AggregateFaultMessage, Exception {
        GetResourceTopology getResourceTopo = new GetResourceTopology();
        GetResourceTopologyType getResourceTopoType = new GetResourceTopologyType();
        getResourceTopoType.setScope(scope);
        getResourceTopoType.setRspec(rspecNames);
        getResourceTopo.setGetResourceTopology(getResourceTopoType);
        GetResourceTopologyResponse getResourceTopoResponse = this.stub.GetResourceTopology(getResourceTopo);
        return getResourceTopoResponse.getGetResourceTopologyResponse();
    }

}
