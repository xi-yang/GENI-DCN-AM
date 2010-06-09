package net.geni.aggregate.client.examples;

import java.io.*;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class CreateSliceVlanClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            CreateSliceVlanClient cl = new CreateSliceVlanClient();
            cl.CreateSliceVlanClient(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from CreateSliceVlanClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void CreateSliceVlanClient(String[] args)
            throws AggregateFaultMessage, Exception {
        super.init(args);

        String sliceName = "";
        float bw = 0;
        String vlan = "any";
        String srcNode = "";
        String srcInterface = "eth1";
        String srcIP = "10.1.1.1/30";
        String dstNode = "";
        String dstInterface = "eth1";
        String dstIP = "10.1.1.2/30";
        try {
            // Prompt for input parameters
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            sliceName = Args.getArg(br, "Slice ID/Name", sliceName);
            vlan =  Args.getArg(br, "Vlan ID (2-4094, any)", vlan);
            String bandwidth =  Args.getArg(br, "Bandwidth (Mbps)", "100.0");
            bw = Float.valueOf(bandwidth);
            srcNode =  Args.getArg(br, "Source Node", srcNode);
            srcInterface =  Args.getArg(br, "Source Interface", srcInterface);
            srcIP =  Args.getArg(br, "Source IP/Mask", srcIP);
            dstNode =  Args.getArg(br, "Destination Node", dstNode);
            dstInterface =  Args.getArg(br, "Destination Interface", dstInterface);
            dstIP =  Args.getArg(br, "Destination IP/Mask", dstIP);
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }

        // make the call to the server
        CreateSliceVlanResponseType response = this.getClient().createSliceVlan(sliceName,
            vlan, bw, srcNode, srcInterface, srcIP, dstNode, dstInterface, dstIP);
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(CreateSliceVlanResponseType response) {
        System.out.println("============ CreateSliceVlanResponse Response =========== ");
        System.out.println("\t Status => " + response.getStatus());
        System.out.println("\t Message => " + response.getMessage());
    }
}
