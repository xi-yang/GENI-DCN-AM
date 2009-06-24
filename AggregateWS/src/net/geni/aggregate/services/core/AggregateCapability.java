/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

/**
 *
 * @author jflidr
 */
public class AggregateCapability {
    private String name;
    private int id;
    private String description;
    private String controllerURL;
    private String urn;

    public AggregateCapability(String n, String u, int i, String d, String c) {
        name = n;
        urn = u;
        id = i;
        description = d;
        controllerURL = c;
    }

    public String getControllerURL() {
        return controllerURL;
    }

    public String getDescription() {
        return description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrn() {
        return urn;
    }

}
