package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class QuerySliceVlanClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            QuerySliceVlanClient cl = new QuerySliceVlanClient();
            cl.QuerySliceVlanClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from QuerySliceVlanClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void QuerySliceVlanClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String sliceName = "";
        String vlan = "";
        if (args.length == 4) {
            //args[0] for repo; args[1] for service_url;
            sliceName = args[2];
            vlan = args[3];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                sliceName = Args.getArg(br, "Slice ID/Name", sliceName);
                vlan = Args.getArg(br, "VLAN ID (2-4094)", vlan);
                br.close();
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }
        // make the call to the server
        QuerySliceVlanResponseType response = this.getClient().querySliceVlan(sliceName, vlan);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(QuerySliceVlanResponseType response) {
        System.out.println("============ QuerySliceVlanResponse Response =========== ");
        VlanReservationResultType vlanResult = response.getVlanResvResult();
        System.out.println("\t  GRI => " + vlanResult.getGlobalReservationId());
        System.out.println("\t  Status => " + vlanResult.getStatus());
        System.out.println("\t  Message => " + vlanResult.getMessage());
        System.out.println("\t\t VLAN_ID => " + vlanResult.getReservation().getVlan());
        System.out.println("\t\t Source => " + vlanResult.getReservation().getSourceNode());
        System.out.println("\t\t Interface => " + vlanResult.getReservation().getSrcInterface());
        System.out.println("\t\t Destination => " + vlanResult.getReservation().getDestinationNode());
        System.out.println("\t\t Interface => " + vlanResult.getReservation().getDstInterface());
        System.out.println("\t\t Bandwidth => " + vlanResult.getReservation().getBandwidth());
        System.out.println("\t\t Description => " + vlanResult.getReservation().getDescription());
    }
}
