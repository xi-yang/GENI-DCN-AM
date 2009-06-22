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
public class AggregateSQLConstraints extends Vector<AggregateSQLConstraint>
{

    private Vector<String> combs = new Vector<String>();

    public int getParamsCnt() {
        int ret = 0;
        for(int i = 0; i < size(); i++) {
            ret += get(i).getParamsCnt();
        }
        return ret;
    }

    @Override
    public boolean add(AggregateSQLConstraint c) {
        if(combs.size() == 0) {
            combs.add("");
        } else {
            combs.add(" AND ");
        }
        return super.add(c);
    }

    public void add(AggregateSQLConstraint c, String s) {
        if(combs.size() == 0) {
            combs.add("");
        } else if(s.equalsIgnoreCase("and")) {
            combs.add(" AND ");
        } else if(s.equalsIgnoreCase("or")) {
            combs.add(" OR ");
        } else {
            combs.add(" AND ");
            throw new IllegalArgumentException("only \"and\" and \"or\" are allowed as arguments. Defaulting to \"AND\"");
        }
        super.add(c);
    }

    public String getConstraints() {
        String ret = "";
        for(int i = 0; i < size(); i++) {
            ret += combs.get(i) + get(i).getConstraint();
        }
        return ret;
    }
}
