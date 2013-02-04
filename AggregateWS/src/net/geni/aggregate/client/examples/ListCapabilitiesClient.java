package net.geni.aggregate.client.examples;

import net.geni.aggregate.client.*;
import net.geni.aggregate.client.AggregateGENIStub.*;
/**
 *
 * @author Xi Yang
 */
public class ListCapabilitiesClient extends ExampleClient {
    public static void main(String[] args) {
        try {
            ListCapabilitiesClient cl = new ListCapabilitiesClient();
            cl.listCaps(args);
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

    public void listCaps(String[] args)
            throws AggregateFaultMessage, Exception {

        super.init(args);
        // make the call to the server
        ListCapabilitiesResponseType response = this.getClient().listCapabilities();
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(ListCapabilitiesResponseType response) {
        System.out.println("============ ListCapabilities Response =========== ");
        ListCapabilitiesResponseTypeSequence[] listCapsTypeSeq = response.getListCapabilitiesResponseTypeSequence();
        for (ListCapabilitiesResponseTypeSequence listCapsType: listCapsTypeSeq) {
            CapabilityType cap = listCapsType.getCapability();
            System.out.println("Capability: " + cap.getName());
            System.out.println("\t URN => " + cap.getUrn());
            System.out.println("\t ID => " + Integer.toBinaryString(cap.getId()));
            System.out.println("\t ControllerURN => " + cap.getControllerURL());
            System.out.println("\t Description => " + cap.getDescription());
            System.out.println();
        }
    }
}
