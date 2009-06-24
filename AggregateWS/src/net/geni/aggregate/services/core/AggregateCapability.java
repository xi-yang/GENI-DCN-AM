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
    private String id;
    private String description;
    private String controllerURL;

    AggregateCapability(String n, String i, String d, String c) {
        name = n;
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
