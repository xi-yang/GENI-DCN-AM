package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class AllocateSliceNetworkClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            AllocateSliceNetworkClient cl = new AllocateSliceNetworkClient();
            cl.AllocateSliceNetworkClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from AllocateSliceNetworkClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void AllocateSliceNetworkClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String rspecId = "";
        String userId = "";
        String rspecFile = "";
        boolean addingPlcSlice = false;
        String expires = "";
        if (args.length == 5) {
            //args[0] for repo; args[1] for service_url;
            rspecId = args[2];
            rspecFile = args[3];
            expires = args[4];
        } else if (args.length == 6) {
            //args[0] for repo; args[1] for service_url;
            rspecId = args[2];
            userId = args[3];
            rspecFile = args[4];
            expires = args[5];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                rspecId = Args.getArg(br, "Rspec ID/URN", rspecId);
                userId = Args.getArg(br, "User ID/URN", userId);
                rspecFile = Args.getArg(br, "Rspec XML file path", rspecFile);
                String yn = Args.getArg(br, "Adding PLC slice (y/n)?", "y");
                if (yn.equalsIgnoreCase("y"))
                    addingPlcSlice = true;
                expires = Args.getArg(br, "Allocation expiration", expires);
                br.close();
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }

        File file = new File(rspecFile);
        if (!file.exists()) {
            System.out.println("Cannot open Rspec file: " + rspecFile);
            return;
        }
        byte[] buffer = new byte[(int)file.length()];
        try {
            BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
            f.read(buffer);
        } catch (IOException ioe) {
            System.out.println("IO error reading from file: " + rspecFile);
            System.exit(1);
        }
        String rspecXml[] = {(new String(buffer))};

        // make the call to the server
        AllocateSliceNetworkResponseType response = this.getClient().allocateSliceNetwork(rspecId, userId, rspecXml, addingPlcSlice, expires);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(AllocateSliceNetworkResponseType response) {
        System.out.println("============ AllocateSliceNetworkResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        System.out.println("\t Message => " + response.getMessage());
    }
}
