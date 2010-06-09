package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class GetResourceTopology extends ExampleClient {
    public static void main(String[] args) {
        try {
            GetResourceTopology cl = new GetResourceTopology();
            cl.GetResourceTopology(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from GetResourceTopology");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void GetResourceTopology(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String scope = "";
        String name = "";
        try {
            // Prompt for input parameters
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            scope = Args.getArg(br, "Resource Topology Scope", "all");
            name = Args.getArg(br, "Resource Topology Name", "substrate");
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        // make the call to the server
        String rspecNames[] = {name};
        GetResourceTopologyResponseType response = this.getClient().getResourceTopology(scope, rspecNames[0].equals("substrate") ? null: rspecNames);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(GetResourceTopologyResponseType response) {
        System.out.println("============ GetResourceTopologyResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        String[] statementList = response.getResourceTopology().getStatement();
        for (String statement: statementList) {
            System.out.println("\t  Statement => " + statement);
            System.out.println();
        }
    }
}
