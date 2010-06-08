package net.geni.aggregate.client.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.axis2.AxisFault;

import net.geni.aggregate.client.*;

/**
 *
 * @author Xi Yang
 */
public class ExampleClient {
    private AggregateClient client = null;

    public void init(String[] args) {
        String url = "";
        String repo = "";
        try {
            if (args.length == 0) {
                System.out.println("Missing mandatory arg[0]: repo-path");
                System.exit(1);
            }
            // Get the repository (axis2.xml ) location from the args
            repo = args[0];
            if (args.length > 1) {
                url = args[1];
            }
            // Prompt for input parameters
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            url = Args.getArg(br, "Requested service URL", url);
            System.out.println("Service URL is: " + url);
        } catch (IOException ioe) {
            System.out.println("IO error reading input");
            System.exit(1);
        }
        this.client = new AggregateClient();
        try {
            this.client.setUp(true, url, repo);
        } catch (AxisFault e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    protected AggregateClient getClient() {
        return this.client;
    }

    protected void cleanup() {
        if (this.client != null)
            this.client.cleanUp();
    }
}
