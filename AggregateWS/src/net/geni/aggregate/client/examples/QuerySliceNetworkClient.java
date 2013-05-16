package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class QuerySliceNetworkClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            QuerySliceNetworkClient cl = new QuerySliceNetworkClient();
            cl.QuerySliceNetworkClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from QuerySliceNetworkClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void QuerySliceNetworkClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String rspecName = "";
        if (args.length == 3) {
            //args[0] for repo; args[1] for service_url;
            rspecName = args[2];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                rspecName = Args.getArg(br, "Rspec ID/Name", rspecName);
                br.close();
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }
        // make the call to the server
        QuerySliceNetworkResponseType response = this.getClient().querySliceNetwork(rspecName);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(QuerySliceNetworkResponseType response) {
        System.out.println("============ QuerySliceNetworkResponse Response =========== ");
        System.out.println("\t Slice Status => " + response.getSliceStatus());
        System.out.println("\t Expires => " + response.getExpires());
        System.out.println("\t = P2P VLANs =");
        VlanReservationResultType[] vlanResultList = response.getVlanResvResult();
        for (VlanReservationResultType vlanResult: vlanResultList) {
            System.out.println("\t  GRI => " + vlanResult.getGlobalReservationId());
            System.out.println("\t  Status => " + vlanResult.getStatus());
            System.out.println("\t\t VLAN_ID => " + vlanResult.getReservation().getVlan());
            System.out.println("\t\t Source => " + vlanResult.getReservation().getSourceNode());
            System.out.println("\t\t Interface => " + vlanResult.getReservation().getSrcInterface());
            System.out.println("\t\t Destination => " + vlanResult.getReservation().getDestinationNode());
            System.out.println("\t\t Interface => " + vlanResult.getReservation().getDstInterface());
            System.out.println("\t\t Bandwidth => " + vlanResult.getReservation().getBandwidth());
            System.out.println("\t\t Description => " + vlanResult.getReservation().getDescription());
        }
        if (response.getExternalResourceStatus() != null && response.getExternalResourceStatus().length > 0)
            System.out.println("\t = ExternalResource Status =\n\t  " + response.getExternalResourceStatus()[0]);
    }
}
