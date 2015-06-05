package net.geni.aggregate.client.examples;

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
        // make the call to the server
        GetAllResourceInfoResponseType response = this.getClient().getAllResourceInfo();
        this.outputResponse(response);
        super.cleanup();
    }

    public void outputResponse(GetAllResourceInfoResponseType response) {
        System.out.println("============ GetAllResourceInfo Response =========== ");
        System.out.println("<AllResourceInfo>\n" + response.getInfo()+"\n</AllResourceInfo>");
    }
}
