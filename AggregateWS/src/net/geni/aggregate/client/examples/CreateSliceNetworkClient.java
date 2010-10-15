package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class CreateSliceNetworkClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            CreateSliceNetworkClient cl = new CreateSliceNetworkClient();
            cl.CreateSliceNetworkClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from CreateSliceNetworkClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void CreateSliceNetworkClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String rspecFile = "";
        boolean addingPlcSlice = false;
        if (args.length == 3) {
            //args[0] for repo; args[1] for service_url;
            rspecFile = args[2];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                rspecFile = Args.getArg(br, "Rspec XML file path", rspecFile);
                String yn = Args.getArg(br, "Adding PLC slice (y/n)?", "y");
                if (yn.equalsIgnoreCase("y"))
                    addingPlcSlice = true;
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
        CreateSliceNetworkResponseType response = this.getClient().createSliceNetwork(rspecXml, addingPlcSlice);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(CreateSliceNetworkResponseType response) {
        System.out.println("============ CreateSliceNetworkResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        System.out.println("\t Message => " + response.getMessage());
    }
}
