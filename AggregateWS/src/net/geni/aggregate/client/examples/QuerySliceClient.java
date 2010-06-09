package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class QuerySliceClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            QuerySliceClient cl = new QuerySliceClient();
            cl.QuerySliceClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from QuerySliceClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void QuerySliceClient(String[] args)
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
        String[] sliceNames = {sliceName};
        QuerySliceResponseType response = this.getClient().querySlice(sliceNames);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(QuerySliceResponseType response) {
        System.out.println("============ QuerySliceResponse Response =========== ");
        String[] resultList = response.getQueryResult();
        for (String result: resultList) {
            System.out.println("\t  Slice Details => " + result);
        }
    }
}
