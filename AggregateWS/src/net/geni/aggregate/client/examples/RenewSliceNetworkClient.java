/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.client.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import net.geni.aggregate.client.AggregateFaultMessage;
import net.geni.aggregate.client.AggregateGENIStub;

/**
 *
 * @author xyang
 */
public class RenewSliceNetworkClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            RenewSliceNetworkClient cl = new RenewSliceNetworkClient();
            cl.RenewSliceNetworkClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from RenewSliceNetworkClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void RenewSliceNetworkClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String rspecName = "";
        String expires = "";
        if (args.length == 4) {
            //args[0] for repo; args[1] for service_url;
            rspecName = args[2];
            expires = args[3];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                rspecName = Args.getArg(br, "Rspec ID/Name", rspecName);
                expires = Args.getArg(br, "New Expiration", expires);
                br.close();
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }
        // make the call to the server
        AggregateGENIStub.RenewSliceNetworkResponseType response = this.getClient().renewSliceNetwork(rspecName, expires);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(AggregateGENIStub.RenewSliceNetworkResponseType response) {
        System.out.println("============ RenewSliceNetworkResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        System.out.println("\t Message => " + response.getMessage());
    }
}
