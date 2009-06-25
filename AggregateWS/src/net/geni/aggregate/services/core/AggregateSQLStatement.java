/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.geni.aggregate.services.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;

/**
 *
 * @author jflidr
 */
public class AggregateSQLStatement {
 private final int SQL_STMT_NOT_SET = 0;
    private final int SQL_STMT_SELECT = 1;
    private final int SQL_STMT_INSERT = 2;
    private final int SQL_STMT_UPDATE = 3;
    private final int SQL_STMT_DELETE = 4;
    private final String[] typeNames = {"NOT SET", "SELECT", "INSERT", "UPDATE", "DELETE"};
    private Connection conn;
    private int stmtType = 0;
    private AggregateSQLColumns columns = null;
    private AggregateSQLConstraints constraints = new AggregateSQLConstraints();
    private AggregateSQLValues values = null;
    private String tab;
    private int paramsCnt = 0;
    private String query = null;
    private PreparedStatement pstmt = null;
    private Statement stmt = null;
    private ResultSet res = null;
    private boolean isDistinct = false;

    // for SELECT/INSERT
    public AggregateSQLStatement(String conn, String t, AggregateSQLColumns c) throws IllegalArgumentException {
        if(conn.matches(AggregateState.getAggregateDB())) {
            this.conn = AggregateState.getAggregateDBConnection();
        }
        tab = t;
        columns = c;
    }

    // for SELECT with constraints
    public AggregateSQLStatement(String conn, String t, AggregateSQLColumns c, Object... x) throws IllegalArgumentException {
        this(conn, t, c);
        String comb = "";
        AggregateSQLConstraint constraint = null;
        for(int i = 0; i < x.length; i++) {
            String cn = x[i].getClass().getName();
            if((i % 2) == 0) {
                if(cn.equals(AggregateSQLConstraint.class.getName())) {
                    constraint = AggregateSQLConstraint.class.cast(x[i]);
                } else {
                    throw new IllegalArgumentException("incorrect class(" + cn + ") in constraint constructor... should be AggregateSQLConstraint");
                }
            }
            if((i % 2) == 1) {
                if(cn.equals(String.class.getName())) {
                    comb = String.class.cast(x[i]);
                } else {
                    throw new IllegalArgumentException("incorrect class(" + cn + ") in constraint constructor... should be String");
                }
            }
            if(constraint != null) {
                constraints.add(constraint, comb);
                constraint = null;
            }
        }
        paramsCnt += constraints.getParamsCnt();
    }

    // for UPDATE and INSERT
    public AggregateSQLStatement(String conn, String t, AggregateSQLColumns c, AggregateSQLValues v) throws IllegalArgumentException {
        if(conn.matches(AggregateState.getAggregateDB())) {
            this.conn = AggregateState.getAggregateDBConnection();
        }
        tab = t;
        columns = c;
        values = v;
        paramsCnt += values.getParamsCnt();
    }

    // for UPDATE/INSERT with constraints
    public AggregateSQLStatement(String conn, String t, AggregateSQLColumns c, AggregateSQLValues v, AggregateSQLConstraint... x) throws IllegalArgumentException {
        this(conn, t, c, v);

        String comb = "";
        AggregateSQLConstraint constraint = null;
        for(int i = 0; i < x.length; i++) {
            String cn = x[i].getClass().getName();
            if((i % 2) == 0) {
                if(cn.equals(AggregateSQLConstraint.class.getName())) {
                    constraint = AggregateSQLConstraint.class.cast(x[i]);
                } else {
                    throw new IllegalArgumentException("incorrect class(" + cn + ") in constraint constructor... should be AggregateSQLConstraint");
                }
            }
            if((i % 2) == 1) {
                if(cn.equals(String.class.getName())) {
                    comb = String.class.cast(x[i]);
                } else {
                    throw new IllegalArgumentException("incorrect class(" + cn + ") in constraint constructor... should be String");
                }
            }
            if(constraint != null) {
                constraints.add(constraint, comb);
                constraint = null;
            }
        }
        paramsCnt += constraints.getParamsCnt();
    }

    // for DELETE
    public AggregateSQLStatement(String conn, String t) throws IllegalArgumentException {
        if(conn.matches(AggregateState.getAggregateDB())) {
            this.conn = AggregateState.getAggregateDBConnection();
        }
        tab = t;
    }
    // for DELETE with constraints

    public AggregateSQLStatement(String conn, String t, AggregateSQLConstraint... x) throws IllegalArgumentException {
        this(conn, t);

        String comb = "";
        AggregateSQLConstraint constraint = null;
        for(int i = 0; i < x.length; i++) {
            String cn = x[i].getClass().getName();
            if((i % 2) == 0) {
                if(cn.equals(AggregateSQLConstraint.class.getName())) {
                    constraint = AggregateSQLConstraint.class.cast(x[i]);
                } else {
                    throw new IllegalArgumentException("incorrect class(" + cn + ") in constraint constructor... should be AggregateSQLConstraint");
                }
            }
            if((i % 2) == 1) {
                if(cn.equals(String.class.getName())) {
                    comb = String.class.cast(x[i]);
                } else {
                    throw new IllegalArgumentException("incorrect class(" + cn + ") in constraint constructor... should be String");
                }
            }
            if(constraint != null) {
                constraints.add(constraint, comb);
                constraint = null;
            }
        }
        paramsCnt += constraints.getParamsCnt();
    }
    // ------------ mysql calls ------------
    // SELECT

    public void select() throws AggregateException {
        select(new Vector());
    }
    // SELECT

    public void select(boolean b) throws AggregateException {
        isDistinct = b;
        select(new Vector());
    }
    // SELECT

    public void select(Vector l, boolean b) throws AggregateException {
        isDistinct = b;
        select(l);
    }

    // SELECT
    public void select(Vector l) throws AggregateException {
        String msg = "";
        if((stmtType != SQL_STMT_NOT_SET) && (stmtType != SQL_STMT_SELECT)) {
            // this is not a system error - this is a result of a stupid programming error
            throw new IllegalArgumentException("cannot use SELECT on " + typeNames[stmtType] + " statement type");
        }
        if(columns == null) {
            throw new IllegalArgumentException("SELECT: nothing to select");
        }
        if(stmtType == SQL_STMT_NOT_SET) {
            stmtType = SQL_STMT_SELECT;
            // prepare the query string if it does not exist
            query = "SELECT " + (isDistinct?" DISTINCT ":"");
            for(int i = 0; i < columns.size(); i++) {
                query += columns.get(i);
                if(i < (columns.size() - 1)) {
                    query += ", ";
                } else {
                    query += " FROM " + tab;
                }
            }
            // add constraints if any
            query += addConstraints();
            try {
                initStatement(l);
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        }
        // if prepared statement - set up the parameters ...
        try {
            if(pstmt != null) {
                prepareStatement(l);
                // ... and execute
                res = pstmt.executeQuery();
            } // if simple statement ... execute
            else if(stmt != null) {
                res = stmt.executeQuery(query);
            }
        } catch(SQLException ex) {
            msg = "back store failure";
            AggregateState.logger.log(Level.SEVERE, msg, ex);
            throw new AggregateException(ex, AggregateException.FATAL);
        }
    }
    // INSERT

    public void insert() throws AggregateException {
        insert(new Vector());
    }
    // INSERT

    public void insert(Vector l) throws AggregateException {
        if((stmtType != SQL_STMT_NOT_SET) && (stmtType != SQL_STMT_INSERT)) {
            throw new IllegalArgumentException("cannot use INSERT on " + typeNames[stmtType] + " statement type");
        }
        // deal with programming errors
        if(columns == null) {
            throw new IllegalArgumentException("INSERT: no columns to insert to");
        }
        if(values == null) {
            values = new AggregateSQLValues(columns.size());
            paramsCnt = values.getParamsCnt();
        }
        if(columns.size() != values.size()) {
            throw new IllegalArgumentException("INSERT: number of columns must match number of values");
        }
        String msg = "";
        if(stmtType == SQL_STMT_NOT_SET) {
            stmtType = SQL_STMT_INSERT;
            // prepare the update string if it does not exist
            query = "INSERT INTO " + tab + " ( ";
            for(int i = 0; i < columns.size(); i++) {
                String c = columns.get(i);
                query += c;
                if(i < (columns.size() - 1)) {
                    query += ", ";
                } else {
                    query += " ) VALUES ( ";
                }
            }
            for(int i = 0; i < values.size(); i++) {
                String v = values.get(i);
                query += v;
                if(i < (values.size() - 1)) {
                    query += ", ";
                } else {
                    query += " )";
                }
            }
            try {
                initStatement(l);
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        }
        // if prepared statement - set up the parameters ...
        try {
            if(pstmt != null) {
                prepareStatement(l);
                // ... and execute
                pstmt.executeUpdate();
            } // if simple statement ... execute
            else if(stmt != null) {
                stmt.executeUpdate(query);
            }
        } catch(SQLException ex) {
            msg = "back store failure";
            AggregateState.logger.log(Level.SEVERE, msg, ex);
            throw new AggregateException(ex, AggregateException.FATAL);
        }
    }
    // UPDATE (works ony on a SELECT type) updates (all) previously selected rows

    public void update(boolean all, Object... o) throws AggregateException {
        if((stmtType != SQL_STMT_NOT_SET) && (stmtType != SQL_STMT_SELECT)) {
            throw new IllegalArgumentException("this UPDATE must be used on SELECT statement type");
        }
        if(res == null) {
            throw new IllegalArgumentException("UPDATE: no result set to work on");
        }
        if(columns == null) {
            throw new IllegalArgumentException("UPDATE: no columns to update");
        }
        if(columns.size() != o.length) {
            throw new IllegalArgumentException("UPDATE: number of columns must match number of values");
        }
        String msg = "";
        try {
            if(all) {
                res.beforeFirst();
                while(res.next()) {
                    updateRow(o);
                }
            } else {
                updateRow(o);
            }
        } catch(SQLException ex) {
            msg = "back store failure";
            AggregateState.logger.log(Level.SEVERE, msg, ex);
            throw new AggregateException(ex, AggregateException.FATAL);
        }

    }
    // UPDATE

    public void update() throws AggregateException {
        update(new Vector());
    }
    // UPDATE

    public void update(Vector l) throws AggregateException {
        if((stmtType != SQL_STMT_NOT_SET) && (stmtType != SQL_STMT_UPDATE)) {
            throw new IllegalArgumentException("cannot use UPDATE on " + typeNames[stmtType] + " statement type");
        }
        if(columns == null) {
            throw new IllegalArgumentException("UPDATE: no columns to update");
        }
        // if no values were supplied assume '?'
        if(values == null) {
            values = new AggregateSQLValues(columns.size());
            paramsCnt += values.getParamsCnt();
        }
        if(columns.size() != values.size()) {
            throw new IllegalArgumentException("UPDATE: number of columns must match number of values");
        }
        String msg = "";
        if(stmtType == SQL_STMT_NOT_SET) {
            stmtType = SQL_STMT_UPDATE;
            // prepare the update string if it does not exist
            query = "UPDATE " + tab + " SET ";
            for(int i = 0; i < columns.size(); i++) {
                String c = columns.get(i);
                String v = values.get(i);
                query += c + "=" + v;
                if(i < (values.size() - 1)) {
                    query += ", ";
                }
            }
            // add constraints if any
            query += addConstraints();
            try {
                initStatement(l);
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        }
        // if prepared statement - set up the parameters ...
        try {
            if(pstmt != null) {
                prepareStatement(l);
                // ... and execute
                pstmt.executeUpdate();
            } // if simple statement ... execute
            else if(stmt != null) {
                stmt.executeUpdate(query);
            }
        } catch(SQLException ex) {
            msg = "back store failure";
            AggregateState.logger.log(Level.SEVERE, msg, ex);
            throw new AggregateException(ex, AggregateException.FATAL);
        }
    }

    // DELETE
    public void delete() throws AggregateException {
        delete(new Vector());
    }
    // DELETE

    public void delete(Vector l) throws AggregateException {
        if((stmtType != SQL_STMT_NOT_SET) && (stmtType != SQL_STMT_DELETE)) {
            throw new IllegalArgumentException("cannot use DELETE on " + typeNames[stmtType] + " statement type");
        }
        String msg = "";
        if(stmtType == SQL_STMT_NOT_SET) {
            stmtType = SQL_STMT_DELETE;
            // prepare the query string if it does not exist
            query = "DELETE FROM " + tab;
            // add constraints if any
            query += addConstraints();
            try {
                initStatement(l);
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        }
        // if prepared statement - set up the parameters ...
        try {
            if(pstmt != null) {
                prepareStatement(l);
                // ... and execute
                pstmt.execute();
            } // if simple statement ... execute
            else if(stmt != null) {
                stmt.execute(query);
            }
        } catch(SQLException ex) {
            msg = "back store failure";
            AggregateState.logger.log(Level.SEVERE, msg, ex);
            throw new AggregateException(ex, AggregateException.FATAL);
        }
    }

    public void setComb(String s) {
        if(constraints != null) {
            //constraints.setComb()
        }
    }

    public void addConstraint(String s, AggregateSQLConstraint c) {
        constraints.add(c, s);
    }

    private String addConstraints() throws IllegalArgumentException {
        if(constraints.size() > 0) {
            return " WHERE " + constraints.getConstraints();
        } else {
            return "";
        }
    }

    private void prepareStatement(Vector l) throws SQLException {
        if((l == null) || (l.size() != paramsCnt)) {
            throw new IllegalArgumentException("The statement: \"" + query + "\" expects " + paramsCnt + " parameter(s)");
        }
        for(int i = 0; i < l.size(); i++) {
            Object o = l.get(i);
            if(o.getClass().getName().equals(Boolean.class.getName())) {
                pstmt.setObject(i + 1, o, java.sql.Types.BOOLEAN);
            } else if(o.getClass().getName().equals(Integer.class.getName())) {
                pstmt.setObject(i + 1, o, java.sql.Types.INTEGER);
            } else if(o.getClass().getName().equals(Long.class.getName())) {
                pstmt.setObject(i + 1, o, java.sql.Types.BIGINT);
            } else if(o.getClass().getName().equals(String.class.getName())) {
                pstmt.setObject(i + 1, o, java.sql.Types.VARCHAR);
            }
        }
    }

    private void initStatement(Vector l) throws SQLException {
        if(paramsCnt > 0) {
            // persistent statement
            if(l.size() != paramsCnt) {
                throw new IllegalArgumentException("The statement: \"" + query + "\" expects " + paramsCnt + " parameter(s)");
            }
            pstmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } else {
            //a fleeting glimpse
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        }
    }

    public Vector<String> getAllStrings(String... s) throws AggregateException {
        Vector<String> ret = new Vector<String>();
        String msg = "";
        if(res != null) {
            try {
                while(res.next()) {
                    for(int i = 0; i < s.length; i++) {
                        ret.add(getString(s[i]));
                    }
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        }
        return ret;
    }

    public String getNextString(String s) throws AggregateException {
        String msg = "";

        if(res != null) {
            try {
                if(!res.next()) {
                    throw new AggregateException(msg, AggregateException.FLOW);
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        }
        return getString(s);
    }

    public String getString(String s) throws AggregateException {
        String ret = null;
        String msg = "";

        if(res == null) {
            msg = "no result set for \"" + s + "\" query";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        if(columns == null) {
            msg = "no column set";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        if(columns.contains(s)) {
            try {
                // this makes a simple (noniterative) request possible:
                // get any number of columns from the first row
                // an empty set is not error (it can be used for tests)
                if(res.isBeforeFirst()) {
                    if(!res.next()) {
                        throw new AggregateException(msg, AggregateException.FLOW);
                    }
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
            try {
                ret = res.getString(s);
                if(res.wasNull()) {
                    msg = "column " + s + " is NULL)";
                    res.updateString("status", "error");
                    res.updateString("statusMsg", msg);
                    res.updateRow();
                    AggregateState.logger.log(Level.WARNING, msg);
                    throw new AggregateException(msg);
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        } else {
            msg = "\"" + s + "\" not in the column set";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new IllegalArgumentException(msg);
        }
        return ret;
    }

    public int getInt(String s) throws AggregateException {
        int ret = 0;
        String msg = "";

        if(res == null) {
            msg = "no result set for \"" + s + "\" query";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        if(columns == null) {
            msg = "no column set";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        if(columns.contains(s)) {
            try {
                // this makes a simple (noniterative) request possible:
                // get any number of columns from the first row
                if(res.isBeforeFirst()) {
                    if(!res.next()) {
                        msg = "empty result set";
                        AggregateState.logger.log(Level.WARNING, msg);
                        throw new AggregateException(msg, AggregateException.FLOW);
                    }
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
            try {
                ret = res.getInt(s);
                if(res.wasNull()) {
                    msg = "column " + s + " is NULL)";
                    res.updateString("status", "error");
                    res.updateString("statusMsg", msg);
                    res.updateRow();
                    AggregateState.logger.log(Level.WARNING, msg);
                    throw new AggregateException(msg);
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        } else {
            msg = "\"" + s + "\" not in the column set";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        return ret;
    }

    public long getLong(String s) throws AggregateException {
        long ret = 0;
        String msg = "";

        if(res == null) {
            msg = "no result set for \"" + s + "\" query";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        if(columns == null) {
            msg = "no column set";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        if(columns.contains(s)) {
            try {
                // this makes a simple (noniterative) request possible:
                // get any number of columns from the first row
                if(res.isBeforeFirst()) {
                    if(!res.next()) {
                        msg = "empty result set";
                        AggregateState.logger.log(Level.WARNING, msg);
                        throw new AggregateException(msg, AggregateException.FLOW);
                    }
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
            try {
                ret = res.getLong(s);
                if(res.wasNull()) {
                    msg = "column " + s + " is NULL)";
                    res.updateString("status", "error");
                    res.updateString("statusMsg", msg);
                    res.updateRow();
                    AggregateState.logger.log(Level.WARNING, msg);
                    throw new AggregateException(msg);
                }
            } catch(SQLException ex) {
                msg = "back store failure";
                AggregateState.logger.log(Level.SEVERE, msg, ex);
                throw new AggregateException(ex, AggregateException.FATAL);
            }
        } else {
            msg = "\"" + s + "\" not in the column set";
            AggregateState.logger.log(Level.SEVERE, msg);
            throw new AggregateException(msg, AggregateException.ERROR);
        }
        return ret;
    }

    public String getQuery() {
        return query;
    }

    private void updateRow(Object[] o) throws SQLException {
        for(int i = 0; i < o.length; i++) {
            if(o[i] == null) {
                continue;
            }
            if(o[i].getClass().getName().equals(Boolean.class.getName())) {
                res.updateBoolean(columns.get(i), Boolean.class.cast(o[i]));
            } else if(o[i].getClass().getName().equals(Integer.class.getName())) {
                res.updateInt(columns.get(i), Integer.class.cast(o[i]));
            } else if(o[i].getClass().getName().equals(Long.class.getName())) {
                res.updateLong(columns.get(i), Long.class.cast(o[i]));
            } else if(o[i].getClass().getName().equals(String.class.getName())) {
                res.updateString(columns.get(i), String.class.cast(o[i]));
            } else {
                String msg = "unknown object class: " + o[i].getClass().getName() + "at position " + i;
                AggregateState.logger.log(Level.WARNING, msg);
            }
        }
        res.updateRow();
    }
}
