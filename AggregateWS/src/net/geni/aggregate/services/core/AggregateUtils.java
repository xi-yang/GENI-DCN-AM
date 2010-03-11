/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import java.util.logging.Level;
import net.geni.aggregate.services.api.AggregateFault;
import net.geni.aggregate.services.api.AggregateFaultMessage;
import net.geni.aggregate.services.api.AggregateFaultMessageExt;

/**
 *
 * @author jflidr
 */
public class AggregateUtils
{

    public static void executeDirectStatement(String sql) throws AggregateException {
        Statement stmt = null;
        try {
            stmt = AggregateState.getAggregateDBConnection().createStatement();
            stmt.execute(sql);
        } catch(SQLException ex) {
            String msg = "nimbis DB off line";
            //AggregateState.logger.log(Level.SEVERE, msg, ex);
            throw new AggregateException(ex, AggregateException.FATAL);
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch(SQLException ex) {
                    String msg = "nimbis DB error";
                    //AggregateState.logger.log(Level.SEVERE, msg, ex);
                    throw new AggregateException(ex, AggregateException.ERROR);
                } finally {
                    stmt = null;
                }
            }
        }
    }

    public static AggregateFaultMessage makeAggregateFault(int type) {
        return makeAggregateFault(type, "");
    }

    public static AggregateFaultMessage makeAggregateFault(int type, String s) {
        AggregateFault f = new AggregateFault();
        AggregateFaultMessage msg = new AggregateFaultMessageExt();
        if(type == AggregateFaultMessageExt.INTERNAL) {
            f.setMsg("FATAL: Broker Internal Error" + ((s.length() > 0)?": " + s:""));
        } else if(type == AggregateFaultMessageExt.SENDER) {
            f.setMsg("ERROR: Sender Error" + ((s.length() > 0)?": " + s:""));
        }
        msg.setFaultMessage(f);
        return msg;
    }

    public static int addVals(Vector<String> v, Object... o) {
        // convert to the string representation of the sql query
        String cN;
        int paramsCnt = 0;
        for(int i = 0; i < o.length; i++) {
            cN = o[i].getClass().getName();
            if(o[i] == null) {
                //AggregateState.logger.log(Level.WARNING, "attempting to pass a null object (pos " + i + ") as a value");
                continue;
            }
            if(cN.equals(Boolean.class.getName())) {
                boolean b = Boolean.class.cast(o[i]);
                v.add(b?"1":"0");
            } else if(cN.equals(Integer.class.getName())) {
                v.add(Integer.class.cast(o[i]).toString());
            } else if(cN.equals(Long.class.getName())) {
                v.add(Long.class.cast(o[i]).toString());
            } else if(cN.equals(String.class.getName())) {
                String s = String.class.cast(o[i]);
                if(s.matches("\\?")) {
                    paramsCnt++;
                    v.add(s);
                } else {
                    v.add("'" + s + "'");
                }
            } else {
                //AggregateState.logger.log(Level.WARNING, "unknown object type: " + o.toString());
            }
        }
        return paramsCnt;
    }

    public static int addKVPairs(Vector<String> v, Object... o) {
        // convert to the string representation of the sql query
        String cN;
        int paramsCnt = 0;
        for(int i = 0; i < o.length; i += 2) {
            cN = o[i + 1].getClass().getName();
            if(o[i + 1] == null) {
                //AggregateState.logger.log(Level.WARNING, "attempting to pass a null object as a constraint (key: " + o[i].toString() + ")");
                continue;
            }
            v.add(String.class.cast(o[i])); // always a String
            if(cN.equals(Boolean.class.getName())) {
                boolean b = Boolean.class.cast(o[i + 1]);
                v.add(b?"1":"0");
            } else if(cN.equals(Integer.class.getName())) {
                v.add(Integer.class.cast(o[i + 1]).toString());
            } else if(cN.equals(Long.class.getName())) {
                v.add(Long.class.cast(o[i + 1]).toString());
            } else if(cN.equals(String.class.getName())) {
                String s = String.class.cast(o[i + 1]);
                if(s.matches("\\?")) {
                    paramsCnt++;
                    v.add(s);
                } else {
                    v.add("'" + s + "'");
                }
            } else {
                //AggregateState.logger.log(Level.WARNING, "unknown object type: " + cN);
            }
        }
        return paramsCnt;
    }
}
