package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class DeleteSliceNetworkClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            DeleteSliceNetworkClient cl = new DeleteSliceNetworkClient();
            cl.DeleteSliceNetworkClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from DeleteSliceNetworkClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void DeleteSliceNetworkClient(String[] args)
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
        DeleteSliceNetworkResponseType response = this.getClient().deleteSliceNetwork(rspecName);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(DeleteSliceNetworkResponseType response) {
        System.out.println("============ DeleteSliceNetworkResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        System.out.println("\t Message => " + response.getMessage());
    }
}
