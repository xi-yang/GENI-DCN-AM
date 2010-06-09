package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class DeleteSliceNetwork extends ExampleClient {
    public static void main(String[] args) {
        try {
            DeleteSliceNetwork cl = new DeleteSliceNetwork();
            cl.DeleteSliceNetwork(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from DeleteSliceNetwork");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void DeleteSliceNetwork(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String rspecName = "";
        try {
            // Prompt for input parameters
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            rspecName = Args.getArg(br, "Rspec ID/Name", rspecName);
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
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
