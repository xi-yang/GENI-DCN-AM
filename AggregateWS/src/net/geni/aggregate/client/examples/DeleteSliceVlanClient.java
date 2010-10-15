package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class DeleteSliceVlanClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            DeleteSliceVlanClient cl = new DeleteSliceVlanClient();
            cl.DeleteSliceVlanClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from DeleteSliceVlanClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void DeleteSliceVlanClient(String[] args)
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
        DeleteSliceVlanResponseType response = this.getClient().deleteSliceVlan(sliceName, vlan);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(DeleteSliceVlanResponseType response) {
        System.out.println("============ DeleteSliceVlanResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        System.out.println("\t Message => " + response.getMessage());
    }
}

