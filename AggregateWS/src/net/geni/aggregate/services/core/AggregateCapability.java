/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.Serializable;

/**
 *
 * @author jflidr, xyang
 */
public class AggregateCapability implements java.io.Serializable {
    int id;
    private String name;
    private String description;
    private String controllerURL;
    private String urn;

    public AggregateCapability() {
        name = "";
        urn = "";
        id = 0;
        description = "";
        controllerURL = "";
    }

    public AggregateCapability(String n, String u, int i, String d, String c) {
        name = n;
        urn = u;
        id = i;
        description = d;
        controllerURL = c;
    }

    public void setControllerURL(String controllerURL) {
        this.controllerURL = controllerURL;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrn(String urn) {
        this.urn = urn;
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
