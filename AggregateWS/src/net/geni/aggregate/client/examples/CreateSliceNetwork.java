package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class CreateSliceNetwork extends ExampleClient {
    public static void main(String[] args) {
        try {
            CreateSliceNetwork cl = new CreateSliceNetwork();
            cl.createSliceNetwork(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from CreateSliceNetwork");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void createSliceNetwork(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String rspecFile = "";
        try {
            // Prompt for input parameters
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            rspecFile = Args.getArg(br, "Rspec XML file path", rspecFile);
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
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
        CreateSliceNetworkResponseType response = this.getClient().createSliceNetwork(rspecXml);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(CreateSliceNetworkResponseType response) {
        System.out.println("============ CreateSliceNetworkResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        System.out.println("\t Message => " + response.getMessage());
    }
}
