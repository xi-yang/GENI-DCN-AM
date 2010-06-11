package net.geni.aggregate.client.examples;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class UpdateSliceClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            UpdateSliceClient cl = new UpdateSliceClient();
            cl.UpdateSliceClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from UpdateSliceClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void UpdateSliceClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String sliceName = "";
        String users = "";
        String url = "";
        String nodes = "";
        String expires = "";
        String descr = "";
        try {
            // Prompt for input parameters
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            sliceName = Args.getArg(br, "Slice ID/Name", sliceName);
            url = Args.getArg(br, "New URL", url);
            users = Args.getArg(br, "New Users", users);
            nodes = Args.getArg(br, "New Node URNs", nodes);
            expires = Args.getArg(br, "New Expiration (yyyy-mm-dd hh:mm:ss)", expires);
            descr = Args.getArg(br, "New Description", descr);
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        String[] userArray = users.split("[,\\s]");
        String[] nodeArray = nodes.split("[,\\s]");
        //SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss");
        //Date date = sdf.parse(expires);
        //Timestamp timestamp = new Timestamp(date.getTime());
        Timestamp timestamp = new Timestamp(0);
        timestamp.valueOf(expires);
        int expire = (int)(timestamp.getTime()/1000);
        // make the call to the server
        UpdateSliceResponseType response = this.getClient().updateSlice(sliceName,
                userArray, url, nodeArray, expire, descr);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(UpdateSliceResponseType response) {
        System.out.println("============ UpdateSliceResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
    }
}
