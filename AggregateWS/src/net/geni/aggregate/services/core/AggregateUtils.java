/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;
import org.hibernate.*;
import net.geni.aggregate.services.api.AggregateFault;
import net.geni.aggregate.services.api.AggregateFaultMessage;
import net.geni.aggregate.services.api.AggregateFaultMessageExt;
import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.Node;
import javax.xml.namespace.QName;

/**
 *
 * @author jflidr
 */
public class AggregateUtils
{
    private static Session session;
    private static org.hibernate.Transaction tx;

    public static void executeDirectStatement(String sql) throws AggregateException {
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            SQLQuery q = session.createSQLQuery(sql);
            q.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new AggregateException(e, AggregateException.FATAL);
        } finally {
            if (session.isOpen()) session.close();
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

    public static Boolean isGeniUrn(String urn) {
        if (urn.contains("urn:publicid:IDN+"))
            return true;
        return false;
    }

    public static Boolean isDcnUrn(String urn) {
        if (urn.contains("urn:ogf:network:domain"))
            return true;
        return false;
    }

    public static String getGeniAmBase(String geniUrn) {
        String[] fields = geniUrn.split("\\+");
        if (fields.length < 2)
            return null;
        return fields[1];
    }
    
    public static String convertGeniToDcnUrn(String geniUrn) throws AggregateException {
        String aggregate = "";
        String type = "";
        String value = "";

        Pattern pattern = Pattern.compile("^urn:publicid:IDN\\+([^\\+]*)\\+(node|interface)\\+([^\\+]*)");
        Matcher matcher = pattern.matcher(geniUrn);
        if (!matcher.find()) {
            throw new AggregateException("convertGeniToDcnUrn: invalid geniUrn="+geniUrn);            
        }
        aggregate = matcher.group(1);
        type = matcher.group(2);
        value = matcher.group(3);
        
        String dcnUrn = "urn:ogf:network:domain="+aggregate;
        String[] fields = value.split(":");
        if (fields.length > 0) {
            dcnUrn += ":node=";
            dcnUrn += fields[0];
        }
        if (fields.length > 1) {
            dcnUrn += ":port=";
            dcnUrn += fields[1];
        }
        if (fields.length > 2) {
            dcnUrn += ":link=";
            dcnUrn += fields[2];
        }
        return dcnUrn;
    }

    public static String convertDcnToGeniUrn(String dcnUrn) throws AggregateException {
        if (!isDcnUrn(dcnUrn))
            throw new AggregateException("convertDcnToGeniUrn: invalid dcnUrn="+dcnUrn);
        String node = "";
        String port = "";
        String link = "";

        String[] fields = dcnUrn.split(":");
        String geniUrn = "urn:publicid:IDN+";
        for (String field: fields) {
            Pattern pattern = Pattern.compile("^(domain|node|port|link)=(.*)");
            Matcher matcher = pattern.matcher(field);
            if (matcher.find()) {
                if (matcher.group(1).equalsIgnoreCase("domain")) {
                    geniUrn += matcher.group(2);
                } else if (matcher.group(1).equalsIgnoreCase("node")) {
                    node = matcher.group(2);
                } else if (matcher.group(1).equalsIgnoreCase("port")) {
                    port = matcher.group(2);
                } else if (matcher.group(1).equalsIgnoreCase("link")) {
                    link = matcher.group(2);
                }
            }
        }
        if (node.isEmpty()) {
            throw new AggregateException("convertDcnToGeniUrn: mailformed dcnUrn "+dcnUrn);            
        }
        if (link.isEmpty() && port.isEmpty()) {
            geniUrn += "+node+";
            geniUrn += node;
        } else {
            geniUrn += "+interface+";            
            geniUrn += node;
            geniUrn += ":";
            geniUrn += port;
        }
        if (!link.isEmpty()) {
            geniUrn += ":";
            geniUrn += link;
        }
        return geniUrn;
    }

    public static String getUrnField(String urn, String field) {
        try {
            if (isGeniUrn(urn)) {
                urn = convertGeniToDcnUrn(urn);
            }
        } catch (AggregateException e) {
            return null;
        }
        int start = urn.indexOf(field+"=");
        if (start == -1)
            return null;
        start += field.length()+1;
        int end = urn.indexOf(':', start);
        if (end == -1 )
            end = urn.length();
        return urn.substring(start, end);
    }
    
    public static String getIDCQualifiedUrn(String urn) {
        // GENI Rspec v3 format
        try {
            if (isGeniUrn(urn))
                return convertGeniToDcnUrn(urn);
        } catch (AggregateException e) {
            return null;
        }
        //MAX native format
        Pattern pattern = Pattern.compile("^urn:aggregate=([^:]*):rspec=([^:]*):domain=([^:]*):node=([^:]*):.*");
        Matcher matcher = pattern.matcher(urn);
        if (matcher.find()) {
            return matcher.group(4)+"."+matcher.group(3);
        }
        pattern = Pattern.compile("^urn:ogf:network:domain=([^:]*):node=([^:]*):.*");
        matcher = pattern.matcher(urn);
        if (matcher.find()) {
            return urn;
        }
        return null;
    }

    public static String extractString(String aStr, String openStr, String closeStr) {
        int start = aStr.indexOf(openStr);
        if (start == -1)
            return null;
        start += openStr.length();
        int end = -1;
        if (closeStr != null)
            end = aStr.indexOf(closeStr, start);
        if (end == -1 )
            end = aStr.length();
        return aStr.substring(start, end);
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
        } else {
            ret = Float.valueOf(bwString)/1000000;
        }            
        return ret;
    }
    
    public static String parseVlanTag(String vlanTag, boolean getSrc) {
        String[] vtags = vlanTag.split(":");
        if (vtags == null || vtags.length == 0)
            return "";
        if (!getSrc && vtags.length == 2)
            return vtags[1];
        return vtags[0];
    }

    public static void justSleep(int secs) {
        long t0, t1;
        t0 =  System.currentTimeMillis();
        do{
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (secs*1000));
    }
    
    
    public static String getAnyName(Object anyObj) {
        return ((ElementNSImpl)anyObj).getLocalName();
    }

    public static Node getAnyNode(Object anyObj) {
        return ((ElementNSImpl)anyObj).getFirstChild();
    }
 
    public static String getAnyText(Object anyObj) {
        if (((ElementNSImpl)anyObj).getFirstChild() == null)
            return null;
        return ((ElementNSImpl)anyObj).getFirstChild().getNodeValue();
    }
    
    public static Node getAnyExtensionNode(ArrayList anyList, String name) {
        for (Object obj: anyList) {
            if (getAnyName(obj).equals(name))
                return getAnyNode(obj);
        }
        return null;
    }

    public static String getAnyExtensionText(ArrayList anyList, String name) {
        for (Object obj: anyList) {
            if (getAnyName(obj).equals(name))
                return getAnyText(obj);
        }
        return null;
    }
    
    public static String getAnyAttrString(Map<QName, String> attrs, String namespace, String localpart) {
        QName qname = new QName(namespace, localpart);
        return attrs.get(qname);
    }
    
    public static String idcSecondsToDate(long t) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date dt = new Date(t*1000);
        return df.format(dt);
    }
}
