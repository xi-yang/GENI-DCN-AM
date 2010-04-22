/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.Serializable;

/**
 *
 * @author root
 */
public abstract class AggregateResource implements java.io.Serializable {
    protected int id = 0;
    protected String type = "";
    protected int rspecId = 0;

    public AggregateResource() {}

    public AggregateResource(int i, String t, int r, int re) {
        this.id = i;
        this.type = t;
        this.rspecId = r;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRspecId() {
        return rspecId;
    }

    public void setRspecId(int rspecId) {
        this.rspecId = rspecId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
