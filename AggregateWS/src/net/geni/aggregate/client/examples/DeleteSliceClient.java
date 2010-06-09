package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class DeleteSliceClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            DeleteSliceClient cl = new DeleteSliceClient();
            cl.DeleteSliceClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from DeleteSliceClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void DeleteSliceClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String sliceName = "";
        try {
            // Prompt for input parameters
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            sliceName = Args.getArg(br, "Slice ID/Name", sliceName);
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        // make the call to the server
        DeleteSliceResponseType response = this.getClient().deleteSlice(sliceName);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(DeleteSliceResponseType response) {
        System.out.println("============ DeleteSliceResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
    }
}
