/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.io.Serializable;
import org.hibernate.*;

/**
 *
 * @author root
 */
public class AggregateResource implements java.io.Serializable {
    protected int id_ = 0;
    protected String type = "";
    protected int reference = 0;
    protected int rspecId = 0;
    protected boolean inDB = false;

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

    public boolean isInDB() {
        return inDB;
    }

    public void setInDB(boolean inDB) {
        this.inDB = inDB;
    }

    public void enterResouceTable() throws AggregateException {
        String sql = "INSERT INTO resources(type, reference, rspecId) VALUES ("
            + "'" + this.type + "', "
            + Integer.toString(this.reference) + ", "
            + Integer.toString(this.rspecId) + ")";
        AggregateUtils.executeDirectStatement(sql);
        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            SQLQuery q = session.createSQLQuery("SELECT MAX(id) FROM resources");
            id_ = Integer.valueOf(q.list().get(0).toString());
        } catch (Exception e) {
            throw new AggregateException(e, AggregateException.FATAL);
        }

        inDB = true;
    }

    public void exitResouceTable() throws AggregateException {
        if (!inDB)
            return;
        AggregateUtils.executeDirectStatement( "DELETE FROM resources WHERE id="
            + Integer.toString(this.id_));
        inDB = false;
    }
}
