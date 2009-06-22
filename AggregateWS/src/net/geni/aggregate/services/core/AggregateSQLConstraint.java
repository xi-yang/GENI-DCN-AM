/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.geni.aggregate.services.core;

import java.util.Vector;

/**
 *
 * @author jflidr
 */
public class AggregateSQLConstraint extends Vector<String>
{

    private String comb = " AND ";
    private int paramsCnt = 0;
    private AggregateSQLConstraints constraints = new AggregateSQLConstraints();

    public AggregateSQLConstraint(Object... o) throws IllegalArgumentException {

        Vector<Object> tmp = new Vector<Object>();
        for(int i = 0; i < o.length; i++) {
            String cn = o[i].getClass().getName();
            if((i == 0) && (cn.equals(AggregateSQLCombLogic.class.getName()))) {
                comb = AggregateSQLCombLogic.class.cast(o[i]).getComb();
            } else if(cn.equals(AggregateSQLConstraint.class.getName())) {
                constraints.add(AggregateSQLConstraint.class.cast(o[i]));
            } else {
                tmp.add(o[i]);
            }
        }
        Object[] new_o = tmp.toArray();

        if((new_o.length % 2) != 0) {
            throw new IllegalArgumentException("odd number of arguments (must be even: key => value pairs)");
        }
        paramsCnt = AggregateUtils.addKVPairs(this, new_o);
    }

    public String getConstraint() throws IllegalArgumentException {
        if(size() == 0) {
            return "";
        }
        if((size() % 2) != 0) {
            throw new IllegalArgumentException("odd number of constraints (must be even: key => value pairs)");
        }
        String ret = (constraints.size() + size() / 2) > 1?"(":"";
        for(int i = 0; i < constraints.size(); i++) {
            ret += constraints.getConstraints();
            if(i < (constraints.size() + size() / 2 - 1)) {
                ret += comb;
            }
        }
        String k;
        String v;
        String oper;
        for(int i = 0; i < size(); i += 2) {
            k = get(i);
            v = get(i + 1);
            if(v.matches("^\\s*!.+")) {
                oper = "!=";
            } else {
                oper = "=";
            }
            ret += k;
            ret += oper;
            ret += v;
            if(i < (size() - 2)) {
                ret += comb;
            }
        }

        ret += (constraints.size() + size() / 2) > 1?")":"";
        return ret;
    }

    public int getParamsCnt() {
        return paramsCnt;
    }
}
