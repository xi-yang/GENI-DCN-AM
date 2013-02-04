package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class CreateSliceClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            CreateSliceClient cl = new CreateSliceClient();
            cl.CreateSliceClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from CreateSliceClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void CreateSliceClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String sliceName = "";
        String user = "";
        String url = "";
        String nodes = "";
        String descr = "";
        if (args.length == 7) {
            //args[0] for repo; args[1] for service_url;
            sliceName = args[2];
            user = args[3];
            url = args[4];
            nodes = args[5];
            descr = args[6];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                sliceName = Args.getArg(br, "Slice ID/Name", sliceName);
                user = Args.getArg(br, "User Email", user);
                url = Args.getArg(br, "Slice URL", url);
                nodes = Args.getArg(br, "Node URNs", nodes);
                descr = Args.getArg(br, "Description", descr);
                br.close();
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }
        String[] nodeArray = nodes.split("[,\\s]");
        // make the call to the server
        CreateSliceResponseType response = this.getClient().createSlice(sliceName,
                user, url, nodeArray, descr);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(CreateSliceResponseType response) {
        System.out.println("============ CreateSliceResponse Response =========== ");
        System.out.println("\t SliceID => " + response.getSliceID());
        System.out.println("\t Status => " + response.getStatus());
    }
}
