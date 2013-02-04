package net.geni.aggregate.client.examples;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class ListSlicesClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            ListSlicesClient cl = new ListSlicesClient();
            cl.listSlices(args);
        } catch (AggregateFaultMessage e) {
            System.out.println(
                    "AggregateFaultMessage from ListSlicesClient");
            System.out.println(e.getFaultMessage().getMsg());
        } catch (Exception e) {
            System.out.println("AggregateGENIStub threw exception");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void listSlices(String[] args)
            throws AggregateFaultMessage, Exception {

        super.init(args);
        // make the call to the server
        ListSlicesResponseType response = this.getClient().listSlices();
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(ListSlicesResponseType response) {
        System.out.println("============ ListSlices Response =========== ");
        ListSlicesResponseTypeSequence[] listSlicesTypeSeq = response.getListSlicesResponseTypeSequence();
        for (ListSlicesResponseTypeSequence listSlicesType: listSlicesTypeSeq) {
            SliceDescriptorType slice = listSlicesType.getSlice();
            System.out.println("Slice: " + slice.getName());
            System.out.println("\t URL => " + slice.getUrl());
            System.out.println("\t Creator => " + slice.getCreator());
            System.out.println("\t Nodes => " + slice.getNodes());
            System.out.println("\t Description => " + slice.getDescription());
            System.out.println("\t CreateTime => " + Long.toBinaryString(slice.getCreatedTime()));
            System.out.println("\t ExpireTime => " + Long.toBinaryString(slice.getExpiredTime()));
            System.out.println();
        }
    }
}
