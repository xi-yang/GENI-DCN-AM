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

    public ListSlicesResponseType listSlices()
           throws AggregateFaultMessage, Exception {
        ListSlices listSlices = new ListSlices();
        ListSlicesType listSlicesType = new ListSlicesType();
        listSlices.setListSlices(listSlicesType);
        ListSlicesResponse listSlicesResponse = this.stub.ListSlices(listSlices);
        return listSlicesResponse.getListSlicesResponse();
    }

    public ListNodesResponseType listNodes(String[] capUrns)
           throws AggregateFaultMessage, Exception {
        ListNodes listNodes = new ListNodes();
        ListNodesType listNodesType = new ListNodesType();
        if (capUrns != null) {
            ListNodesTypeSequence[] listNodesTypeSeq = new ListNodesTypeSequence[capUrns.length];
            for (int i = 0; i < capUrns.length; i++) {
                listNodesTypeSeq[i] = new ListNodesTypeSequence();
                listNodesTypeSeq[i].setCapabilityURN(capUrns[i]);
            }
            listNodesType.setListNodesTypeSequence(listNodesTypeSeq);
        }
        listNodes.setListNodes(listNodesType);
        ListNodesResponse listNodesResponse = this.stub.ListNodes(listNodes);
        return listNodesResponse.getListNodesResponse();
    }


    public CreateSliceResponseType createSlice(String sliceName, String user,
            String url, String[] nodes, String descr)
           throws AggregateFaultMessage, Exception {
        CreateSlice createSlice = new CreateSlice();
        CreateSliceType createSliceType = new CreateSliceType();
        createSliceType.setSliceID(sliceName);
        createSliceType.setUser(user);
        createSliceType.setUrl(url);
        createSliceType.setNode(nodes);
        createSliceType.setDescription(descr);
        createSlice.setCreateSlice(createSliceType);
        CreateSliceResponse createSliceResponse = this.stub.CreateSlice(createSlice);
        return createSliceResponse.getCreateSliceResponse();
    }

    public UpdateSliceResponseType updateSlice(String sliceName, String[] users,
            String url, String[] nodes, int expire, String descr)
           throws AggregateFaultMessage, Exception {
        UpdateSlice updateSlice = new UpdateSlice();
        UpdateSliceType updateSliceType = new UpdateSliceType();
        updateSliceType.setSliceID(sliceName);
        updateSliceType.setUser(users);
        updateSliceType.setUrl(url);
        updateSliceType.setNode(nodes);
        updateSliceType.setExpires(expire);
        updateSliceType.setDescription(descr);
        updateSlice.setUpdateSlice(updateSliceType);
        UpdateSliceResponse updateSliceResponse = this.stub.UpdateSlice(updateSlice);
        return updateSliceResponse.getUpdateSliceResponse();
    }

    // Obsolete
    public StartSliceResponseType startSlice(String sliceName)
           throws AggregateFaultMessage, Exception {
        StartSlice StartSlice = new StartSlice();
        StartSliceType StartSliceType = new StartSliceType();
        StartSliceType.setSliceID(sliceName);
        StartSlice.setStartSlice(StartSliceType);
        StartSliceResponse StartSliceResponse = this.stub.StartSlice(StartSlice);
        return StartSliceResponse.getStartSliceResponse();
    }

    // Obsolete
    public StopSliceResponseType stopSlice(String sliceName)
           throws AggregateFaultMessage, Exception {
        StopSlice StopSlice = new StopSlice();
        StopSliceType StopSliceType = new StopSliceType();
        StopSliceType.setSliceID(sliceName);
        StopSlice.setStopSlice(StopSliceType);
        StopSliceResponse StopSliceResponse = this.stub.StopSlice(StopSlice);
        return StopSliceResponse.getStopSliceResponse();
    }

    public DeleteSliceResponseType deleteSlice(String sliceName)
           throws AggregateFaultMessage, Exception {
        DeleteSlice deleteSlice = new DeleteSlice();
        DeleteSliceType deleteSliceType = new DeleteSliceType();
        deleteSliceType.setSliceID(sliceName);
        deleteSlice.setDeleteSlice(deleteSliceType);
        DeleteSliceResponse deleteSliceResponse = this.stub.DeleteSlice(deleteSlice);
        return deleteSliceResponse.getDeleteSliceResponse();
    }

    public QuerySliceResponseType querySlice(String[] sliceNames)
           throws AggregateFaultMessage, Exception {
        QuerySlice querySlice = new QuerySlice();
        QuerySliceType querySliceType = new QuerySliceType();
        querySliceType.setSliceID(sliceNames);
        querySlice.setQuerySlice(querySliceType);
        QuerySliceResponse querySliceResponse = this.stub.QuerySlice(querySlice);
        return querySliceResponse.getQuerySliceResponse();
    }

    public CreateSliceVlanResponseType createSliceVlan(String sliceName, String vlan,
            float bw, String srcNode, String srcInterface, String srcIP,
            String dstNode, String dstInterface, String dstIP, String descr)
           throws AggregateFaultMessage, Exception {
        CreateSliceVlan createSliceVlan = new CreateSliceVlan();
        CreateSliceVlanType createSliceVlanType = new CreateSliceVlanType();
        createSliceVlanType.setSliceID(sliceName);
        VlanReservationDescriptorType vlanResvDescr = new VlanReservationDescriptorType();
        vlanResvDescr.setVlan(vlan);
        vlanResvDescr.setBandwidth(bw);
        vlanResvDescr.setSourceNode(srcNode);
        vlanResvDescr.setSrcInterface(srcInterface);
        vlanResvDescr.setSrcIpAndMask(srcIP);
        vlanResvDescr.setDestinationNode(dstNode);
        vlanResvDescr.setDstInterface(dstInterface);
        vlanResvDescr.setDstIpAndMask(dstIP);
        vlanResvDescr.setDescription(descr);
        createSliceVlanType.setVlanReservation(vlanResvDescr);
        createSliceVlan.setCreateSliceVlan(createSliceVlanType);
        CreateSliceVlanResponse createSliceVlanResponse = this.stub.CreateSliceVlan(createSliceVlan);
        return createSliceVlanResponse.getCreateSliceVlanResponse();
    }

    public DeleteSliceVlanResponseType deleteSliceVlan(String sliceName, String vlan)
           throws AggregateFaultMessage, Exception {
        DeleteSliceVlan deleteSliceVlan = new DeleteSliceVlan();
        DeleteSliceVlanType deleteSliceVlanType = new DeleteSliceVlanType();
        deleteSliceVlanType.setSliceID(sliceName);
        deleteSliceVlanType.setVlan(vlan);
        deleteSliceVlan.setDeleteSliceVlan(deleteSliceVlanType);
        DeleteSliceVlanResponse deleteSliceVlanResponse = this.stub.DeleteSliceVlan(deleteSliceVlan);
        return deleteSliceVlanResponse.getDeleteSliceVlanResponse();
    }

    public QuerySliceVlanResponseType querySliceVlan(String sliceName, String vlan)
           throws AggregateFaultMessage, Exception {
        QuerySliceVlan querySliceVlan = new QuerySliceVlan();
        QuerySliceVlanType querySliceVlanType = new QuerySliceVlanType();
        querySliceVlanType.setSliceID(sliceName);
        querySliceVlanType.setVlan(vlan);
        querySliceVlan.setQuerySliceVlan(querySliceVlanType);
        QuerySliceVlanResponse querySliceVlanResponse = this.stub.QuerySliceVlan(querySliceVlan);
        return querySliceVlanResponse.getQuerySliceVlanResponse();
    }

    public CreateSliceNetworkResponseType createSliceNetwork(String rspecId, String rsepcXml[], boolean addPlcSlice)
           throws AggregateFaultMessage, Exception {
        CreateSliceNetwork createSliceNet = new CreateSliceNetwork();
        CreateSliceNetworkType createSliceNetType = new CreateSliceNetworkType();
        RSpecTopologyType rspecTopoType = new RSpecTopologyType();
        rspecTopoType.setStatement(rsepcXml);
        createSliceNetType.setRspecNetwork(rspecTopoType);
        createSliceNetType.setAddPlcSlice(addPlcSlice);
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

    public GetResourceTopologyResponseType getResourceTopology(String scope, String rspecNames[])
           throws AggregateFaultMessage, Exception {
        GetResourceTopology getResourceTopo = new GetResourceTopology();
        GetResourceTopologyType getResourceTopoType = new GetResourceTopologyType();
        getResourceTopoType.setScope(scope);
        if (rspecNames != null) {
            getResourceTopoType.setRspec(rspecNames);
        }
        getResourceTopo.setGetResourceTopology(getResourceTopoType);
        GetResourceTopologyResponse getResourceTopoResponse = this.stub.GetResourceTopology(getResourceTopo);
        return getResourceTopoResponse.getGetResourceTopologyResponse();
    }

}
