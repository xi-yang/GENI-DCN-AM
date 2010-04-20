/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.*;
import java.util.regex.*;
import org.hibernate.*;
import net.geni.aggregate.services.api.AggregateFault;
import net.geni.aggregate.services.api.AggregateFaultMessage;
import net.geni.aggregate.services.api.AggregateFaultMessageExt;

/**
 *
 * @author jflidr
 */
public class AggregateUtils
{
    static private Session session = null;

    public static void executeDirectStatement(String sql) throws AggregateException {
        try {
            if (session == null || !session.isOpen()) {
                session = HibernateUtil.getSessionFactory().getCurrentSession();
            }
            org.hibernate.Transaction tx = session.beginTransaction();
            SQLQuery q = session.createSQLQuery(sql);
            q.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            throw new AggregateException(e, AggregateException.FATAL);
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

    public static String makeArrayString(String[] items, char c) {
        String retArray = "";
        for (int i = 0; i < items.length; i++) {
            retArray += items[i];
            if (i < items.length-1)
                retArray += c;
        }
        return retArray;
    }

    public static String makeArrayString(String[] items) {
        return makeArrayString(items, ',');
    }

    public static String makePyArrayString(String[] items) {
        String retArray = "[";
        for (int i = 0; i < items.length; i++) {
            retArray += "'";
            retArray += items[i];
            retArray += "'";
            if (i < items.length-1)
                retArray += ",";
        }
        retArray += "]";
        return retArray;
    }

    public static String getUrnFields(String urn, String[] fields) {
        String ret = "";
        for (int i = 0; i < fields.length; i++) {
            ret+=(fields[i]+"="+getUrnField(urn,fields[i]));
            if (i != fields.length-1)
                ret+=":";
        }
        return ret;
    }
    
    public static String getUrnField(String urn, String field) {
        int start = urn.indexOf(field+"=");
        if (start == -1)
            return null;
        start += field.length()+1;
        int end = urn.indexOf(':', start);
        if (end == -1 )
            end = urn.length();
        return urn.substring(start, end);
    }

    public static float convertBandwdithToMbps(String bwString) {
        float ret = 0;
        Pattern pattern = Pattern.compile("(\\d+)([mM]|[gG]|[kK]|[bB]).*");
        Matcher matcher = pattern.matcher(bwString);
        if (matcher.find()) {
            String bw = matcher.group(1);
            ret = Float.valueOf(bw);
            String m = matcher.group(2);
            if (m.equalsIgnoreCase("g"))
                ret *= 1000;
            else if (m.equalsIgnoreCase("k"))
                ret /= 1000;
            else if (m.equalsIgnoreCase("b"))
                ret /= 1000000;
        }
        return ret;
    }
}
