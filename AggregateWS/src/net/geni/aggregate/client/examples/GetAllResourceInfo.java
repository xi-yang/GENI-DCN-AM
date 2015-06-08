package net.geni.aggregate.client.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class GetAllResourceInfo extends ExampleClient {
    public static void main(String[] args) {
        try {
            GetAllResourceInfo cl = new GetAllResourceInfo();
            cl.getAllResInfo(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from ListCapabilitiesClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void getAllResInfo(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);
        String filter = "";
        if (args.length == 3) {
            //args[0] for repo; args[1] for service_url;
            filter = args[2];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                filter = Args.getArg(br, "Filter string", "all");
                br.close();
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }
        // make the call to the server
        GetAllResourceInfoResponseType response = this.getClient().getAllResourceInfo(filter);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(GetAllResourceInfoResponseType response) {
        System.out.println("============ GetAllResourceInfo Response =========== ");
        System.out.println("<AllResourceInfo>\n" + response.getInfo()+"\n</AllResourceInfo>");
    }
}
