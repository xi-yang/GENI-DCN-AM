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
public class AggregateSQLValues extends Vector<String>
{

    private int paramsCnt = 0;

    public AggregateSQLValues(final int l) throws IllegalArgumentException {
        setSize(l);
        paramsCnt = l;
        for(int i = 0; i < l; i++) {
            setElementAt("?", i);
        }
    }

    public AggregateSQLValues(final Object... o) throws IllegalArgumentException {
        paramsCnt = AggregateUtils.addVals(this, o);
    }

    public int getParamsCnt() {
        return paramsCnt;
    }
}
