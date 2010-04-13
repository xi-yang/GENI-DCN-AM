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
public class AggregateResource implements java.io.Serializable {
    protected int id_ = 0;
    protected String type = "";
    protected int reference = 0;
    protected int rspecId = 0;

    public AggregateResource() {}

    public AggregateResource(int i, String t, int r, int re) {
        this.id_ = i;
        this.type = t;
        this.rspecId = r;
        this.reference = re;
    }

    public int getId_() {
        return id_;
    }

    public void setId_(int id) {
        this.id_ = id;
    }

    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
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
