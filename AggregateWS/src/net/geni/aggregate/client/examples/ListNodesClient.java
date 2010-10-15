package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class ListNodesClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            ListNodesClient cl = new ListNodesClient();
            cl.listNodes(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from ListNodesClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void listNodes(String[] args)
            throws AggregateFaultMessage, Exception {

        super.init(args);

        String capUrn = "";
        if (args.length == 3) {
            //args[0] for repo; args[1] for service_url;
            capUrn = args[2];
        } else {
            try {
                // Prompt for input parameters
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                capUrn = Args.getArg(br, "Type by Capability (URN)", capUrn);
                br.close();
            } catch (IOException ioe) {
                System.out.println("IO error reading input");
                System.exit(1);
            }
        }
        // make the call to the server
        String[] capUrns = {capUrn};
        ListNodesResponseType response = this.getClient().listNodes(capUrn.isEmpty() ? null : capUrns);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(ListNodesResponseType response) {
        System.out.println("============ ListNodes Response =========== ");
        ListNodesResponseTypeSequence[] listNodesTypeSeq = response.getListNodesResponseTypeSequence();
        for (ListNodesResponseTypeSequence listNodesType: listNodesTypeSeq) {
            NodeDescriptorType node = listNodesType.getNode();
            System.out.println("Node: " + Integer.toString(node.getId()));
            System.out.println("\t URN => " + node.getUrn());
            System.out.println("\t Description => " + node.getDescription());
            System.out.println();
        }
    }
}
